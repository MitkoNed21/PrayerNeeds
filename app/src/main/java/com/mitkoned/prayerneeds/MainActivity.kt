package com.mitkoned.prayerneeds

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.opengl.Visibility
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.marginLeft
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileReader
import java.io.FileWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private var requestedNameIndex by Delegates.notNull<Int>()

    private lateinit var namesAddedToday: Array<String>
    private lateinit var fileName: String
    private lateinit var dateFormat: DateFormat
    private val hidingAnsweredPrayerNeeds = BoxedBoolean(false)

    private val dataPrayerNeedsList = mutableListOf<PrayerNeed>()
    private val viewPrayerNeedsList = mutableListOf<PrayerNeed>()


    override fun onDestroy() {
        dataPrayerNeedsList.sortWith(kotlin.Comparator(fun (first, second): Int {
            val firstDate = dateFormat.parse(first.date + ";" + first.year)
            val secondDate = dateFormat.parse(second.date + ";" + second.year)
            return -firstDate!!.compareTo(secondDate)
        }))
        saveFile()
        super.onDestroy()
    }

    private fun saveFile() {
        lateinit var writer: FileWriter
        try {
            writer = FileWriter(fileName, false)
            val resultString = convertPrayerNeedsListToJson()
            writer.write(resultString)
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        } finally {
            try {
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun convertPrayerNeedsListToJson(): String {
        val resultJsonObject = JSONObject()
        val prayerNeedsJSONArray = JSONArray()

        for (item in dataPrayerNeedsList) {
            prayerNeedsJSONArray.put(JSONObject().apply {
                put("description", item.description)
                put("date", item.date)
                put("year", item.year)
                put("answered", item.answered)
            })
        }

        resultJsonObject.put("prayerNeeds", prayerNeedsJSONArray)

        return resultJsonObject.toString(4)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val addButton = this.findViewById<Button>(R.id.addButton)
        val rvMain = this.findViewById<RecyclerView>(R.id.rvMain)
        val addText = this.findViewById<TextView>(R.id.addText)

        val swipeGestures = object: PrayerNeedSwipeGestures(0, ItemTouchHelper.RIGHT) {
            private lateinit var tempAddTextValue: String

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                tempAddTextValue = addText.text.toString()

                val itemPos = viewHolder.layoutPosition

                val parent = addText.parent as ConstraintLayout
                val layoutParams = addText.layoutParams

                parent.removeView(addText)
                addButton.visibility = View.INVISIBLE

                AlertDialog.Builder(this@MainActivity)
                        .setPositiveButton("Save") { _, _ ->
                            savePrayerNeed(itemPos, addText, rvMain)
                        }
                        .setTitle("Edit")
                        .setOnDismissListener {
                            (addText.parent as ViewGroup).removeView(addText)
                            parent.addView(addText, layoutParams)

                            addButton.visibility = View.VISIBLE

                            addText.text = tempAddTextValue
                        }
                        .setIcon(getDrawable(R.drawable.ic_edit_prayer_need))
                        .setView(addText.apply {
                            text = viewPrayerNeedsList[itemPos].description
                        }).create().apply {
                            setOnShowListener {

                                (addText.parent as ViewGroup).setPadding(20, 48, 20, 16)
                            }
                            show()
                        }

                rvMain.adapter?.notifyItemChanged(itemPos)
                //addText.text = viewPrayerNeedsList[itemPos].description


                //super.onSwiped(viewHolder, direction)
            }
        }


//        val swipeGestures = object: PrayerNeedSwipeGestures(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//
//        }

        val adapter = PrayerNeedAdapter(viewPrayerNeedsList, hidingAnsweredPrayerNeeds)
        rvMain.adapter = adapter
        rvMain.layoutManager = LinearLayoutManager(this)
        ItemTouchHelper(swipeGestures).attachToRecyclerView(rvMain)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.US)

        //TODO: Pass index and list, so that next and previous implemented
        requestedNameIndex = intent.getIntExtra("requestedNameIndex", -1)
        namesAddedToday = intent.getStringArrayExtra("namesArray")!! // could do later if

        if (requestedNameIndex == -1) {
            Toast.makeText(this, "Requested index was -1!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Activity invoked without requested name, create a dialog to add a name
        val requestedName = namesAddedToday[requestedNameIndex]

        fileName = filesDir.path + "/" + requestedName + ".json"

        //lateinit var reader: FileInputStream
        lateinit var reader: FileReader
        lateinit var inputJsonString: String

        try {
            reader = FileReader(fileName)
            inputJsonString = reader.readText()
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        this.title = requestedName

//        if (JsonReader(StringReader(inputJsonString)).hasNext())
        if (inputJsonString != "") {
            try {
                val jsonObject = JSONObject(inputJsonString)

                if (jsonObject.has("prayerNeeds")) {
                    val items = jsonObject.getJSONArray("prayerNeeds")
                    val itemsLength = items.length()

                    for ((index, _) in (0 until itemsLength).withIndex()) {
                        val currentObject = items.getJSONObject(index)

                        val date = currentObject.getString("date")
                        val year = currentObject.getString("year")
                        val description = currentObject.getString("description")
                        val answered = currentObject.getBoolean("answered")

                        if (description.contains("Prayer Needs")) continue

                        val loadedPrayerNeed = PrayerNeed(
                                description = description,
                                date = date,
                                year = year,
                                answered = answered
                        )

                        dataPrayerNeedsList.add(loadedPrayerNeed)
                        viewPrayerNeedsList.add(loadedPrayerNeed)

                        //Toast.makeText(this, prayerNeedsList.joinToString { "${it.description.substring(0, 9)} ${it.date}" }, Toast.LENGTH_SHORT).show()
                        adapter.notifyItemInserted(viewPrayerNeedsList.size - 1)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }

        rvMain.addItemDecoration(object : DividerItemDecoration(rvMain.context, DividerItemDecoration.VERTICAL) {
            val color = Color.parseColor("#391561")

            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                c.drawColor(color)
                super.onDraw(c, parent, state)
            }
        }).run {
            rvMain.removeItemDecorationAt(rvMain.itemDecorationCount - 1)
        }

        addButton.setOnClickListener {
            addPrayerNeed(addText, rvMain)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
//        this.findViewById<RecyclerView>(R.id.rvMain).apply {
//            removeItemDecorationAt(viewPrayerNeedsList.lastIndex)
//        }

        super.onPostCreate(savedInstanceState)
    }

    private fun savePrayerNeed(itemPosition: Int, etView: TextView, rvMain: RecyclerView) {
        val enteredText = etView.text.trim().toString()
        if (enteredText == "") return

        val desc = enteredText.replace(Regex("(?<![A-Za-z0-9А-Яа-я_])й(?![A-Za-z0-9А-Яа-я_])"), "ѝ")

        dataPrayerNeedsList[dataPrayerNeedsList.indexOf(viewPrayerNeedsList[itemPosition])].description = desc
        viewPrayerNeedsList[itemPosition].description = desc

        rvMain.adapter?.notifyItemChanged(itemPosition)
        //rvMain.scrollToPosition(0)
    }

    private fun addPrayerNeed(etView: TextView, rvMain: RecyclerView) {
        val enteredText = etView.text.trim().toString()
        if (enteredText == "") return

        val desc = enteredText.replace(Regex("(?<![A-Za-z0-9А-Яа-я_])й(?![A-Za-z0-9А-Яа-я_])"), "ѝ")
        //Toast.makeText(this, desc, Toast.LENGTH_SHORT).show()

        val currentDate = Calendar.getInstance().time
//            val year = currentDate.get(Calendar.YEAR)
//            val month = currentDate.get(Calendar.MONTH)
//            val day = currentDate.get(Calendar.DAY_OF_MONTH)
//            Toast.makeText(this, "$year $month $day", Toast.LENGTH_SHORT).show()

        val formattedCurrentDate = dateFormat.format(currentDate)
        val dateParts = formattedCurrentDate.split(';')

        val newItem = PrayerNeed(desc, date = dateParts[0], year = dateParts[1], answered = false)
        dataPrayerNeedsList.add(0, newItem)
        viewPrayerNeedsList.add(0, newItem)
        rvMain.adapter?.notifyItemInserted(0)
        rvMain.scrollToPosition(0)

        etView.text = ""
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val appBarLayout = View.inflate(this, R.layout.all_prayer_needs_top_bar_layout, null)

        supportActionBar?.apply {
            setDisplayShowCustomEnabled(true)
            setDisplayShowTitleEnabled(false)

            appBarLayout.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            customView = appBarLayout
        }

        appBarLayout.apply {
            findViewById<TextView>(R.id.tv_title).apply {
                text = this@MainActivity.title
                textSize = 26f
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            findViewById<TextView>(R.id.tv_currentDate).text = ""
        }

        this.supportActionBar?.customView = appBarLayout

        menuInflater.inflate(R.menu.person_prayer_needs_app_bar_menu, menu)
        //this.setFeatureDrawable() <- research

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toggleAnsweredPrayerNeedsVisibility_mi -> {
                val adapter = this.findViewById<RecyclerView>(R.id.rvMain).adapter!!

                if (hidingAnsweredPrayerNeeds.value) {
                    item.icon = getDrawable(R.drawable.ic_hide_answered_prayer_needs)

                    for ((index, pn) in dataPrayerNeedsList.withIndex()) {
                        if (pn.answered) {
                            viewPrayerNeedsList.add(index, pn)
                            adapter.notifyItemInserted(index)
                        }
                    }

                    hidingAnsweredPrayerNeeds.value = false
                } else {
                    item.icon = getDrawable(R.drawable.ic_show_answered_prayer_needs)

                    for (index in dataPrayerNeedsList.lastIndex downTo 0) {
                        if (dataPrayerNeedsList[index].answered) {
                            viewPrayerNeedsList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }

                    hidingAnsweredPrayerNeeds.value = true
                }


            }
        }

        return true
    }
}

/*
val result = LIS(arrayOf(0, 8, 4, 5, 9, 3, 12, 8, 2, 15))
                val result2 = LIS(arrayOf(0, -1, 0, 8, 4, 5, 9, 3, 12, 8, 2, 15))

                // result should be:
                //  0 4 5 8 15
                //  0 4 5 12 15
                //  0 4 5 9 12 15 <- 6
                //  0 8 9 12 15

                // result2 should be:
                //  -1 0 4 5 9 12 15 <- 7

                Toast.makeText(this, result.joinToString(" "), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, result2.joinToString(" "), Toast.LENGTH_SHORT).show()

fun LIS(nums: Array<Int>): List<Int> {
        val result = mutableListOf(nums[0])
        val tempResult = mutableListOf<Int>()

        for (currentIndex in 0 until nums.size) {
            if (nums.lastIndex - currentIndex < result.size) break

            tempResult.add(nums[currentIndex])
            var start = currentIndex + 1

            while (true) {
                for (index in start until nums.size) {
                    if (tempResult.last() < nums[index]) {
                        tempResult.add(nums[index])
                        if (result.size < tempResult.size) {
                            result.clear()
                            result.addAll(tempResult)
                        }
                    }
                }

                if (tempResult.size > 1) {
                    if (nums[nums.indexOf(tempResult.last())] == nums.last()) {
                        tempResult.removeAt(tempResult.lastIndex)
                    }

                    start = nums.indexOf(tempResult.last()) + 1
                    tempResult.removeAt(tempResult.lastIndex)
                } else {
                    break
                }
            }

            tempResult.clear()
        }
        return result
        }*/

/*fun knapsackProblem(capacity: Int, maxItems: Int, items: Array<Item>): Int {
        val results: Array<Item> = arrayOf()
        val itemsWeight = items.sortBy { i -> i.weight }
        val itemsPrice = items.sortBy { i -> i.price }


        for ((index, _) in (0 until maxItems).withIndex()) {

            for ((index2, _) in (0 until maxItems).withIndex()) {


            }
        }

        for ((index, _) in (0 until maxItems).withIndex()) {
            results[index] = arrayOf<Int>()
            for ((index2, _) in (0 until maxItems).withIndex()) {
                results[index][index2] = 0
            }
        }

        for ((index, _) in (0 until maxItems).withIndex()) {
            for ((index2, _) in (0 until maxItems).withIndex()) {


            }
        }
    }

    public inner class Item (val weight: Int, val price: Int)*/