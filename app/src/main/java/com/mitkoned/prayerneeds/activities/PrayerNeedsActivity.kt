package com.mitkoned.prayerneeds.activities

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.*
import com.mitkoned.prayerneeds.adapters.PrayerNeedAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.properties.Delegates

class PrayerNeedsActivity : AppCompatActivity() {
    private var requestedNameIndex by Delegates.notNull<Int>()

    private lateinit var namesAddedToday: Array<String>
    private lateinit var fileName: String
    private val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.dateFormat), Locale.US)
    }
    // Maybe convert back to BoxedBoolean
    private var hidingAnsweredPrayerNeeds = false

    private val dataPrayerNeedsList = mutableListOf<PrayerNeed>()
    private val viewPrayerNeedsList = mutableListOf<PrayerNeed>()

    private var sbPrayerNeedDeleted: Snackbar? = null

    override fun onPause() {
        if (sbPrayerNeedDeleted?.isShown == true)
        sbPrayerNeedDeleted?.dismiss()
        super.onPause()
    }

    override fun onDestroy() {
        //TODO: Exporting doesn't work on Android 10 AVD
        //TODO: DON'T SAVE HERE! READ onDestroy, maybe use onPause <- research

        dataPrayerNeedsList.sortWith(kotlin.Comparator(fun (first, second): Int {
            val firstDate = dateFormat.parse(first.date + ";" + first.year)
            val secondDate = dateFormat.parse(second.date + ";" + second.year)
            return -firstDate!!.compareTo(secondDate)
        }))
        saveFile()
        super.onDestroy()
    }

    private fun saveFile() {
        var errored = false

        try {
            val resultString = convertPrayerNeedsListToJson()
            writeFile(fileName, resultString, false)
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
            errored = true
        }

        if (!errored) {
            val prefs = getSharedPreferences(getString(R.string.lastUpdatedPrayerNeedsData), Context.MODE_PRIVATE)
            with (prefs.edit()) {
                putString("lastUpdatedPrayerNeedsData", Instant.now().toString())
                apply()
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
        setContentView(R.layout.activity_prayer_needs)

        val addButton = this.findViewById<Button>(R.id.btn_addPrayerNeed)
        val rvPrayerNeeds = this.findViewById<RecyclerView>(R.id.rv_prayerNeeds)
        val etPrayerNeedsText = this.findViewById<EditText>(R.id.et_prayerNeedsText)

        val swipeGestures = object: PrayerNeedSwipeGestures(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            //TODO: Add support for deleting a prayer need here...
            // otherwise would have to send dataPrayerNeedsList to adapter
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemPos = viewHolder.layoutPosition
                val adapterPosition = viewHolder.adapterPosition

                when(direction) {
                    ItemTouchHelper.LEFT -> {
                        AlertDialog.Builder(this@PrayerNeedsActivity)
                            .setTitle("Remove prayer need")
                            .setPositiveButton("Yes") { _, _ ->

                                val removedPrayerNeed = viewPrayerNeedsList.removeAt(adapterPosition)
                                rvPrayerNeeds.adapter?.notifyItemRemoved(adapterPosition)

                                sbPrayerNeedDeleted = Snackbar.make(
                                    rvPrayerNeeds, //viewHolder.itemView.parent as RecyclerView,
                                    "Item removed.", //removedPrayerNeed + " was removed.",
                                    Snackbar.LENGTH_LONG
                                ).apply {
                                    //setBackgroundTintList(Color.parseColor("#808080"))
                                    // TODO: Make a layout for the Snackbar
                                    setBackgroundTint(Color.parseColor("#808080"))
                                    setActionTextColor(Color.parseColor("#F0F0F0"))
                                    setTextColor(Color.parseColor("#F0F0F0"))

                                    setAction("Undo") { // on click listener // TODO: if names doesn't contain the name.
                                        viewPrayerNeedsList.add(adapterPosition, removedPrayerNeed)
                                        rvPrayerNeeds.adapter?.notifyItemInserted(adapterPosition)
                                    }
                                    addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                        override fun onDismissed(
                                            transientBottomBar: Snackbar?,
                                            event: Int
                                        ) {
                                            transientBottomBar?.removeCallback(this)
                                            if (event != DISMISS_EVENT_ACTION) {
                                                dataPrayerNeedsList.remove(removedPrayerNeed)
                                            }
                                            super.onDismissed(transientBottomBar, event)
                                        }
                                    })
                                }

                                sbPrayerNeedDeleted!!.show()
                            }
                            .setCancelable(true)
                            .setMessage("Do you want to remove\n\"${viewPrayerNeedsList[itemPos].description}\"?")
                            .create()
                            .show()

                        rvPrayerNeeds.adapter?.notifyItemChanged(itemPos)
                    }

                    ItemTouchHelper.RIGHT -> {
                        val tempAddTextValue = etPrayerNeedsText.text.toString()

                        val clPrayerNeedsTextParent = etPrayerNeedsText.parent as ConstraintLayout
                        val layoutParams = etPrayerNeedsText.layoutParams

                        clPrayerNeedsTextParent.removeView(etPrayerNeedsText)
                        addButton.visibility = View.INVISIBLE

                        AlertDialog.Builder(this@PrayerNeedsActivity)
                            .setPositiveButton("Save") { _, _ ->
                                saveEditedPrayerNeed(itemPos, etPrayerNeedsText, rvPrayerNeeds)
                            }
                            .setTitle("Edit")
                            .setOnDismissListener {
                                (etPrayerNeedsText.parent as ViewGroup).removeView(etPrayerNeedsText)
                                clPrayerNeedsTextParent.addView(etPrayerNeedsText, layoutParams)
                                addButton.visibility = View.VISIBLE
                                etPrayerNeedsText.setText(tempAddTextValue)
                                //TODO: Temporary, change with resources
                                etPrayerNeedsText.backgroundTintList =
                                    ColorStateList.valueOf(Color.parseColor("#242424"))
                            }
                            .setIcon(AppCompatResources.getDrawable(
                                this@PrayerNeedsActivity, R.drawable.ic_edit_prayer_need)
                            )
                            .setView(etPrayerNeedsText.apply {
                                this.setText(viewPrayerNeedsList[itemPos].description)
                            }).create().apply {
                                setOnShowListener {
                                    (etPrayerNeedsText.parent as ViewGroup).setPadding(20, 48, 20, 16)
                                    //TODO: Temporary, change with resources
                                    etPrayerNeedsText.backgroundTintList =
                                        ColorStateList.valueOf(Color.parseColor("#303030"))
                                }
                                show()
                            }

                        rvPrayerNeeds.adapter?.notifyItemChanged(itemPos)
                        //addText.text = viewPrayerNeedsList[itemPos].description


                        //super.onSwiped(viewHolder, direction)
                    }
                }
            }
        }


//        val swipeGestures = object: PrayerNeedSwipeGestures(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//
//        }

        val adapter = PrayerNeedAdapter(viewPrayerNeedsList, hidingAnsweredPrayerNeeds)

        rvPrayerNeeds.adapter = adapter
        rvPrayerNeeds.layoutManager = LinearLayoutManager(this)
        ItemTouchHelper(swipeGestures).attachToRecyclerView(rvPrayerNeeds)

        //TODO: Pass index and list, so that next and previous implemented
        requestedNameIndex = intent.getIntExtra("requestedNameIndex", -1)
        namesAddedToday = intent.getStringArrayExtra("namesArray")!! // could do later if

        if (requestedNameIndex == -1) {
            Toast.makeText(this, "Requested index was -1!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Activity invoked without requested name, create a dialog to add a name
        val requestedName = namesAddedToday[requestedNameIndex]

        // TODO: make val? not required to be changed, and shouldn't be changed
        fileName = filesDir.path + "/" + requestedName + ".json"

        this.title = requestedName

        try {
            //lateinit var reader: FileInputStream
            val inputJsonString = readFile(File(fileName))

            //if (JsonReader(StringReader(inputJsonString)).hasNext())
            if (inputJsonString != null && inputJsonString != "") {
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
            }

            rvPrayerNeeds.addItemDecoration(object : DividerItemDecoration(rvPrayerNeeds.context, DividerItemDecoration.VERTICAL) {
                val color = Color.parseColor("#391561")

                override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                    c.drawColor(color)
                    super.onDraw(c, parent, state)
                }
            }).run {
                // TODO: viewPrayerNeedsList as index should work.
                rvPrayerNeeds.removeItemDecorationAt(rvPrayerNeeds.itemDecorationCount - 1)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Could not load prayer needs for $requestedName", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            finish()
        }

        addButton.setOnClickListener {
            addPrayerNeed(etPrayerNeedsText, rvPrayerNeeds)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
//        this.findViewById<RecyclerView>(R.id.rvMain).apply {
//            removeItemDecorationAt(viewPrayerNeedsList.lastIndex)
//        }

        super.onPostCreate(savedInstanceState)
    }

    private fun saveEditedPrayerNeed(itemPosition: Int, etView: TextView, rvMain: RecyclerView) {
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

        val currentDate = Calendar.getInstance().time

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
        val appBarLayout = View.inflate(this, R.layout.layout__top_bar_all_prayer_needs, null)

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
                text = this@PrayerNeedsActivity.title
                textSize = 26f
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            // TODO: Try .visibility = View.GONE
            findViewById<TextView>(R.id.tv_currentDate).text = ""
        }

        this.supportActionBar?.customView = appBarLayout

        menuInflater.inflate(R.menu.menu_appbar_person_prayerneeds, menu)
        //this.setFeatureDrawable() <- research

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_toggleAnsweredPrayerNeedsVisibility -> {
                val adapter = this.findViewById<RecyclerView>(R.id.rv_prayerNeeds).adapter!!

                if (hidingAnsweredPrayerNeeds) {
                    item.icon = AppCompatResources.getDrawable(this, R.drawable.ic_hide_answered_prayer_needs)

                    for ((index, pn) in dataPrayerNeedsList.withIndex()) {
                        if (pn.answered) {
                            viewPrayerNeedsList.add(index, pn)
                            adapter.notifyItemInserted(index)
                        }
                    }

                    hidingAnsweredPrayerNeeds = false
                } else {
                    item.icon = AppCompatResources.getDrawable(this, R.drawable.ic_show_answered_prayer_needs)

                    for (index in dataPrayerNeedsList.lastIndex downTo 0) {
                        if (dataPrayerNeedsList[index].answered) {
                            viewPrayerNeedsList.removeAt(index)
                            adapter.notifyItemRemoved(index)
                        }
                    }

                    hidingAnsweredPrayerNeeds = true
                }
            }
        }

        return true
    }
}