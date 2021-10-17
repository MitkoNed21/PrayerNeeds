package com.mitkoned.prayerneeds.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.*
import com.mitkoned.prayerneeds.adapters.AddNameDialogElementAdapter
import com.mitkoned.prayerneeds.adapters.AddedNameTodayElementAdapter
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PeopleTodayActivity : AppCompatActivity() {
    private lateinit var addNameDialog : AlertDialog
    private lateinit var etName_AddNameDialog : EditText
    private lateinit var rvStoredNames_AddNameDialog : RecyclerView
    private lateinit var namesAddedTodayFile: File

    private val storedNames = mutableListOf<String>()
    private val namesAddedToday = mutableListOf<String>()
    private val dateFormat: SimpleDateFormat by lazy {
        SimpleDateFormat(getString(R.string.dateFormat), Locale.US)
    }

    val items = mutableMapOf<String, MutableList<PrayerNeed>>()

    fun prayerNeedToJson(prayerNeed: PrayerNeed) : JSONObject {
        val result = JSONObject().apply {
            put("date", prayerNeed.date)
            put("year", prayerNeed.year)
            put("description", prayerNeed.description)
            put("answered", prayerNeed.answered)
        }

        return result
    }

    private fun saveNamesAddedTodayFile() {
        if (namesAddedToday.size == 0) {
            return
        }

        lateinit var writer: FileWriter
        try {
            namesAddedTodayFile.createNewFile()
            writer = FileWriter(namesAddedTodayFile.path, false)

            val result = StringBuilder()
            result.append(dateFormat.format(Calendar.getInstance()/*.apply{ this.set(Calendar.DAY_OF_MONTH, 26) }*/.time))
            result.append("|")
            result.append(namesAddedToday.joinToString(", "))

            writer.write(result.toString())
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } finally {
            try {
                writer.flush()
                writer.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun switchDataDirectories(s: String) {
        try {
            val testFolderName = "/bbbbb"
            val folderName = "/Names2"

            var switchTo = ""
            var switchFrom = ""
            if (s == "test") {
                switchTo = testFolderName
                switchFrom = folderName
            } else {
                switchTo = folderName
                switchFrom = testFolderName
            }

            val dir = File(filesDir.path + switchFrom)
            dir.mkdir()

            filesDir.listFiles { file -> file.isFile }?.forEach {
                if (!(it.name.contains("bbbb") || it.name.contains("Name"))) {
                    it.copyTo(File(dir.path + "/" + it.name), true)
                    it.delete()
                }
            }

            File(filesDir.path + switchTo).listFiles { file -> file.isFile }?.forEach {
                it.copyTo(File(filesDir.path + "/" + it.name))
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_today)

/*
        switchDataDirectories("")

        Toast.makeText(this, "/ "  +File(filesDir.path).listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "/<files> "  +File(filesDir.path).listFiles { file -> file.isFile }?.size.toString(), Toast.LENGTH_SHORT).show()

        Toast.makeText(this, "/Names2 " +File(filesDir.path + "/Names2").listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "/bbbbb " +File(filesDir.path + "/bbbbb").listFiles()?.size.toString(), Toast.LENGTH_SHORT).show()

        return
        // */


        lateinit var reader: FileReader
        try {
            namesAddedTodayFile = File(filesDir.path + "/today.txt")

            if (!namesAddedTodayFile.exists()) {
                namesAddedTodayFile.createNewFile()

                Toast.makeText(this, "FILE NOT FOUND", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "FILE FOUND", Toast.LENGTH_SHORT).show()
                reader = FileReader(namesAddedTodayFile.path)

                val input = reader.readText()
                if (input != "")
                {
                    val inputParts = input.split('|')

                    val dateString = inputParts[0]
                    val names = inputParts[1].split(", ")
                    val currentDateString = dateFormat.format(Calendar.getInstance().time)

                    if (dateString == currentDateString) {
                        namesAddedToday.addAll(names)
                    } /*else {
                        namesAddedTodayFile.delete()
                    }*/
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } finally {
            try {
                //if (reader != null && reader.ready())
                    reader.close()
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        //val parse = dateFormat.parse(testList[0].date + ";" + testList[0].year)
        //testList.sortBy() { el -> el.date }
        //testList.sortWith(kotlin.Comparator(fun (first, second): Int {
        //    val firstDate = dateFormat.parse(first.date + ";" + first.year)
        //    val secondDate = dateFormat.parse(second.date + ";" + second.year)

        //    return firstDate!!.compareTo(secondDate)
        //}))
        //val parse = testList[1].date//.toString()

        //TODO: maybe use date in PrayerNeed class
        //Toast.makeText(this, parse, Toast.LENGTH_LONG).show()
        //return


        val rvNamesAddedToday = findViewById<RecyclerView>(R.id.rv_namesAddedToday)
        rvNamesAddedToday.adapter = AddedNameTodayElementAdapter(namesAddedToday).apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    saveNamesAddedTodayFile()
                    super.onChanged()
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                    saveNamesAddedTodayFile()
                    super.onItemRangeChanged(positionStart, itemCount, payload)
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                    saveNamesAddedTodayFile()
                    super.onItemRangeChanged(positionStart, itemCount)
                }

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                    saveNamesAddedTodayFile()
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    saveNamesAddedTodayFile()
                    super.onItemRangeInserted(positionStart, itemCount)
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    saveNamesAddedTodayFile()
                    super.onItemRangeInserted(positionStart, itemCount)
                }
            })
        }
        rvNamesAddedToday.layoutManager = LinearLayoutManager(this)

        //val dialogView = View.inflate(this, R.layout.addname_dialog, null)
        //TODO: use MultiAutoCompleteTextView

        //TODO: have checkbox next to name when adding so can select
        // multiple names and add them all at once, maybe then sort

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_addname, null)
        addNameDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add name")
            .setPositiveButton("Add") { _, _ ->
               val selectedName = etName_AddNameDialog.text.toString()
                if (!namesAddedToday.contains(selectedName) && selectedName != "") {
                    namesAddedToday.add(selectedName)
                    rvNamesAddedToday.adapter?.notifyItemInserted(namesAddedToday.lastIndex)
                    // TODO: This should not work? It accesses position -1
                    rvNamesAddedToday.adapter?.notifyItemChanged(namesAddedToday.size - 2)

                    val fileName = filesDir.path + "/" + selectedName + ".json"
                    try {
                        File(fileName).createNewFile()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show() //e.printStackTrace()
                    }
                }

                //saveNamesAddedTodayFile()
                //val directories = filesDir.listFiles(FileFilter { pathname -> pathname.isDirectory })
//                if (ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//
//                }

                //TODO: if app is quit or switching to another activity
                // (see if switching to another activity and going back
                // saves the names in the list) save today's added names
                // unless list is empty,
                // if person has no prayer needs offered, keep name
                // save also date, upon opening, if date is different
                // than saved to file (or maybe last accessed)
                // (maybe move list of names to history) clear list

                //TODO: second activity could have a button to
                // show/hide prayer needs that are answered

                //TODO: add a way to see all prayer needs ever offered
                // it could be done like the main screen (list of names
                // in alphabetical order, which when clicked launch
                // another activity (could reuse MainActivity) with
                // all the prayer needs offered by the person)
            }
            .setOnDismissListener {
                //Toast.makeText(this, etName_AddNameDialog.text.toString(), Toast.LENGTH_SHORT).show()
                //TODO: Search in files for file with text in EditText
                //

                etName_AddNameDialog.setText("")
            }
            .create()

        etName_AddNameDialog = dialogView.findViewById(R.id.et_nameToAdd)

        rvStoredNames_AddNameDialog = dialogView.findViewById(R.id.rv_storedNames)
        val adapter = AddNameDialogElementAdapter(storedNames, addNameDialog)
        rvStoredNames_AddNameDialog.adapter = adapter
        rvStoredNames_AddNameDialog.layoutManager = LinearLayoutManager(addNameDialog.context)

    }

    private fun getPrayerNeedsFromFile(file: File): MutableList<PrayerNeed> {
        val result = mutableListOf<PrayerNeed>()
        lateinit var reader: FileReader
        lateinit var inputJsonString: String

        try {
            reader = FileReader(file)
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

//        if (JsonReader(StringReader(inputJsonString)).hasNext())
        if (inputJsonString != "") {
            try {
                val jsonObject = JSONObject(inputJsonString)

                if (jsonObject.has("prayerNeeds")) {
                    val items = jsonObject.getJSONArray("prayerNeeds")
                    val itemsLength = items.length()

                    for (index in (0 until itemsLength)) {
                        val currentObject = items.getJSONObject(index)

                        val date = currentObject.getString("date")
                        val year = currentObject.getString("year")
                        val description = currentObject.getString("description")
                        val answered = currentObject.getBoolean("answered")

                        result.add(
                            PrayerNeed(
                                description = description,
                                date = date,
                                year = year,
                                answered = answered
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }

        return result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar_people_today, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.mi_addName -> {
                val files = filesDir.listFiles()
                { file ->
                    file.extension == "json"
                }

                if (files != null) {
                    val fileNames = files
                            .map { f -> f.nameWithoutExtension }
                            .filter { fn -> !namesAddedToday.contains(fn) }
                            .sortedBy { fn -> fn }

                    storedNames.clear()
                    rvStoredNames_AddNameDialog.adapter?.notifyItemRangeRemoved(0, rvStoredNames_AddNameDialog.childCount)

                    for (fn in fileNames) {
                        storedNames.add(fn)

                        rvStoredNames_AddNameDialog.adapter?.notifyItemInserted(storedNames.size - 1)
                        // TODO: This should not work? It accesses position -1
                        rvStoredNames_AddNameDialog.adapter?.notifyItemChanged(storedNames.size - 2)
                    }
                }

                addNameDialog.show()
            }
            R.id.mi_showPrayerNeedsEnteredToday -> {
                try {
                    val intent = Intent(this, ViewTodaysPrayerNeedsFullscreenActivity::class.java).apply {
                        putExtra("namesAddedToday", namesAddedToday.toTypedArray())
                    }

                    this.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
            R.id.mi_viewAllNames -> {
                this.startActivity(Intent(this, AllPeopleActivity::class.java))
            }
        }

        return true
    }
}