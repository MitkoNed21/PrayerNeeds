package com.mitkoned.prayerneeds.activities

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.*
import com.mitkoned.prayerneeds.adapters.AddNameDialogElementAdapter
import com.mitkoned.prayerneeds.adapters.AddedNameTodayElementAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
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

    private lateinit var crdlSnackbar: CoordinatorLayout

    //private lateinit var prayerNeedsExportPath: String
    private val openFileActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data

            if (uri != null) { //Try to use StartActivityForResult() and .launch(intent) {Intent.ACTION_GET_CONTENT, data: MIME_TYPE} if bringing choosing
                var fileContents = "" //TODO: change to lateinit var
                try {
                    contentResolver.openInputStream(uri)?.apply {
                        fileContents = readBytes().decodeToString()
                        close()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    return@registerForActivityResult
                }

                storePrayerNeedsFromFileContents(fileContents)
            }
        }
    }
//    private val openFileActivityLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        if (uri != null) { //Try to use StartActivityForResult() and .launch(intent) {Intent.ACTION_GET_CONTENT, data: MIME_TYPE} if bringing choosing
//            var fileContents = "" //TODO: change to lateinit var
//            try {
//                contentResolver.openInputStream(uri)?.apply {
//                    fileContents = readBytes().decodeToString()
//                    close()
//                }
//            } catch (e: Exception) {
//                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
//                e.printStackTrace()
//                return@registerForActivityResult
//            }
//
//            storePrayerNeedsFromFileContents(fileContents)
//        }
//    }


    /*private val selectExportFolderActivityLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { resultUri ->
        val resultPath = resultUri.path
        if (resultPath != null) {
            //prayerNeedsExportPath = resultPath
            val exportFile = File(prayerNeedsExportPath)

            try {
                val userFile = File(resultPath + "/" + exportFile.name)
                if (userFile.createNewFile()) {
                    exportFile.copyTo(userFile, false)
                } else {
                    Toast.makeText(this, "File $exportFile already exists in directory!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }*/

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

    override fun onDestroy() {
        openFileActivityLauncher.unregister()
        super.onDestroy()
    }

    private fun saveNamesAddedTodayFile() {
        if (namesAddedToday.size == 0) {
            return
        }

        val result = StringBuilder()
        result.append(dateFormat.format(Calendar.getInstance()/*.apply{ this.set(Calendar.DAY_OF_MONTH, 26) }*/.time))
        result.append("|")
        result.append(namesAddedToday.joinToString(", "))

        writeFile(namesAddedTodayFile, result.toString(), false)
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
        namesAddedTodayFile = File(filesDir.path + "/today.txt")
        if (!namesAddedTodayFile.exists()) {
            namesAddedTodayFile.createNewFile()

            Toast.makeText(this, "FILE NOT FOUND", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "FILE FOUND", Toast.LENGTH_SHORT).show()
        }

        try {
            val input = readFile(namesAddedTodayFile)
            if (input != null && input != "")
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
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
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

        crdlSnackbar = CoordinatorLayout(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

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
            R.id.mi_exportPrayerNeeds -> {
                val allNames: List<String>
                val namesJSONArray = JSONArray()

                val files = filesDir.listFiles { file ->
                    file.extension == "json"
                }

                if (files != null) {
                    allNames = files
                        .map { f -> f.nameWithoutExtension }
                        .sortedBy { fn -> fn }

                    for (name in allNames) {
                        try {
                            val fileName = File(filesDir.path + "/" + name + ".json")
                            val fileContents = readFile(fileName)

                            if (fileContents != null && fileContents != "") {
                                namesJSONArray.put(JSONObject().apply {
                                    put(name, fileContents)
                                })
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Could not export data for $name", Toast.LENGTH_SHORT).show()
                            //Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    }

                    val resultJsonObject = JSONObject()
                    resultJsonObject.put("exportDate", Instant.now().toString())
                    resultJsonObject.put("prayerNeedsExport", namesJSONArray)
                    val exportString = resultJsonObject.toString(2)

                    val exportsDir = File(filesDir.path + "/exports")

                    try {
                        if (!exportsDir.exists()) {
                            exportsDir.mkdir()
                        }

                        val currentDateTime = Calendar.getInstance().time
                        val exportDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                        var exportFile = File("${exportsDir.path}/prayer_needs_export-${exportDateFormat.format(currentDateTime)}.json")

                        if (!exportFile.createNewFile()) {
                            exportFile = File(
                                exportFile.path.substringBeforeLast(".json") + "_2.json"
                            )

                            var count = 2
                            while (!exportFile.createNewFile()) {
                                exportFile = File(
                                    exportFile.path.substringBeforeLast("_$count.json") + "_${count + 1}.json"
                                )
                                count++
                            }
                        }

                        writeFile(exportFile, exportString, false)

                        //val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        //    putExtra("filePath", exportFile.path)
                        //}

                        //prayerNeedsExportPath = exportFile.path

                        //Uri.fromFile(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))
                        //selectExportFolderActivityLauncher.launch(
                        //    Uri.fromFile(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))
                        //)

                        val resultDirectory = File("/storage/emulated/0/Download/")

                        if (resultDirectory != null) {
                            //prayerNeedsExportPath = resultPath
                            //val exportFile = File(prayerNeedsExportPath)
                                //if (!resultDirectory.canWrite()) {
                                //    enforceUriPermission(Uri.EMPTY,null,"",1,1,1, "TODO: message if thrown")
                                //}
                                //DownloadManager.Request(exportFile.toUri())
                            try {
                                //val userFile = File(resultPath/* + "/" + exportFile.name*/)
                                //if (resultPath.isFile && resultPath.exists() && resultPath.listFiles()?.map{ f -> f.name }?.contains(exportFile.name) == true) {
                                val savedFile = exportFile.copyTo(File(resultDirectory.path + "/" + exportFile.name), true)
                                //} else {
                                //    Toast.makeText(this, "File ${resultPath.path + exportFile.name} already exists in directory!", Toast.LENGTH_LONG).show()
                                //}

                                this.addContentView(crdlSnackbar, ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                ))

                                Snackbar.make(
                                    crdlSnackbar,
                                    //this.findViewById<View>(R.id.action_bar),
                                    "File saved to ${savedFile.path}/${savedFile.name}",
                                    Snackbar.LENGTH_INDEFINITE
                                ).apply {
                                    animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                                    behavior = BaseTransientBottomBar.Behavior().apply {
                                        setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
                                        setDragDismissDistance(0.7f)
                                        setStartAlphaSwipeDistance(0.0f)
                                    }
                                    //addCallback(object: Snackbar.Callback() { }) onShow, onDismiss
                                    addCallback(object: Snackbar.Callback() {
                                        override fun onDismissed(
                                            transientBottomBar: Snackbar?,
                                            event: Int
                                        ) {
                                            super.onDismissed(transientBottomBar, event)
                                            (crdlSnackbar.parent as ViewGroup).removeView(crdlSnackbar)
                                        }
                                    })
                                    setAction("Show") { // on click listener
                                        try {
                                            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                                            // TODO:
                                            if (intent.resolveActivityInfo(packageManager, 0) != null) {
                                                startActivity(intent);
                                                //dismiss()
                                            } else {
                                                Toast.makeText(
                                                    this@PeopleTodayActivity,
                                                    "Could not open the file",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(this@PeopleTodayActivity,
                                                e.toString(),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            e.printStackTrace()
                                        }
                                    }
                                    // TODO: Make a layout for the Snackbar
                                    //setBackgroundTintList(Color.parseColor("#808080"))
                                    setBackgroundTint(Color.parseColor("#808080"))
                                    setActionTextColor(Color.parseColor("#F0F0F0"))
                                    setTextColor(Color.parseColor("#F0F0F0"))
                                    show()
                                }

                            } catch (e: Exception) {
                                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
                }
            }
            R.id.mi_importPrayerNeeds -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.data = null
                intent.type = "application/json"
                openFileActivityLauncher.launch(intent)
            }
        }

        return true
    }

    private fun storePrayerNeedsFromFileContents(fileContents: String) {
        if (fileContents != "")
        {
            try {
                val importJson = JSONObject(fileContents)
                val exportDate = Instant.parse(importJson.getString("exportDate"))
                val lastUpdateDate = Instant.parse(
                    getSharedPreferences(getString(R.string.lastUpdatedPrayerNeedsData), Context.MODE_PRIVATE)
                        .getString("lastUpdatedPrayerNeedsData", Instant.MIN.toString())
                )

                if (exportDate < lastUpdateDate) {
                    //TODO: maybe check if file contents are the same before deleting?
                    AlertDialog.Builder(this).apply {
                        setMessage("The export in the file is older than current data. Continue?")
                        setCancelable(true)
                        setPositiveButton("Yes") { _, _ ->
                            doImport(importJson)
                        }
                        setNegativeButton("No", null)
                        create()
                        show()
                    }

                    // TODO: examine:
                    //MaterialStyledDatePickerDialog(this)
                    //MaterialStyledDatePickerDialog(this).datePicker
                    //DatePickerDialog(this).datePicker
                } else {
                    doImport(importJson)
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Input file is empty!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doImport(importJson: JSONObject)
    {
        try {
            val names = importJson.getJSONArray("prayerNeedsExport")
            val namesCount = names.length()

            filesDir.listFiles { f ->
                f.extension == "json"
            }?.forEach {
                it.delete()
            }

            val pd = ProgressDialog(this).apply {
                setCancelable(false)
                this.setCanceledOnTouchOutside(false)
                this.max = namesCount
                this.progress = 0
            }

            pd.show()

            val loadedNames = mutableListOf<String>()
            for (i in 0 until namesCount) {
                val jsonPersonPrayerNeeds = names.getJSONObject(i)
                val name = jsonPersonPrayerNeeds.names()!!.getString(0)
                val newFile = File("${filesDir.path}/$name.json")
                newFile.createNewFile()

                writeFile(newFile, jsonPersonPrayerNeeds.getString(name), false)
                loadedNames.add(name)

                pd.incrementProgressBy(1)
            }


            namesAddedToday.minus(loadedNames).also { results ->
                if (results.isNotEmpty()) {
                    for (name in results) {
                        val index = namesAddedToday.indexOf(name)
                        namesAddedToday.removeAt(index)
                        this.findViewById<RecyclerView>(R.id.rv_namesAddedToday)
                            .adapter?.notifyItemRemoved(index)
                    }
                }
            }

            val prefs = getSharedPreferences(
                getString(R.string.lastUpdatedPrayerNeedsData),
                Context.MODE_PRIVATE
            )
            with(prefs.edit()) {
                putString("lastUpdatedPrayerNeedsData", importJson.getString("exportDate"))
                apply()
            }
            pd.dismiss()
            Toast.makeText(this, "Import finished", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}


