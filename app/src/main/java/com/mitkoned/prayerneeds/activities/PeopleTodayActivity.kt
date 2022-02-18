package com.mitkoned.prayerneeds.activities

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.graphics.Color
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.*
import com.mitkoned.prayerneeds.utilities.*
import com.mitkoned.prayerneeds.adapters.AddedNameTodayElementAdapter
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.text.DateFormat
import java.time.format.DateTimeParseException

class PeopleTodayActivity : AppCompatActivity() {
    private lateinit var addNameBottomSheetDialog : BottomSheetDialogFragment

    private lateinit var rvNamesAddedToday: RecyclerView

    private lateinit var namesAddedTodayFile: File

    private val importManager by lazy {
        ImportManager(this)
    }

    private val exportManager by lazy {
        ExportManager(this)
    }

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

            if (uri != null) {
                val jsonObject: JSONObject
                try {
                    jsonObject = importManager.parseImportFile(uri)
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                prepareImportData(jsonObject)
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


    private val selectExportLocationActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data

            if (uri != null) {
                try {
                    exportManager.exportPrayerNeeds(uri)
                } catch (e: Throwable) {
                    /**
                     * Adapted from stack overflow question:
                     * https://stackoverflow.com/questions/63579633/
                     * answer by blackapps:
                     * https://stackoverflow.com/a/63595048
                     * */
                    DocumentFile.fromSingleUri(this, uri)?.delete()
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                    return@registerForActivityResult
                }

                try {
                    this.addContentView(
                        crdlSnackbar, ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    )

                    Snackbar.make(
                        crdlSnackbar,
                        "Export complete.",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                        behavior = BaseTransientBottomBar.Behavior().apply {
                            setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
                            setDragDismissDistance(0.7f)
                            setStartAlphaSwipeDistance(0.0f)
                        }
                        addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int
                            ) {
                                super.onDismissed(transientBottomBar, event)
                                val parent = crdlSnackbar.parent
                                if (parent != null) {
                                    (parent as ViewGroup).removeView(crdlSnackbar)
                                }
                            }
                        })
                        setAction("Show") { // on click listener
                            try {
                                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                                // TODO:
                                if (intent.resolveActivityInfo(packageManager, 0) != null) {
                                    startActivity(intent)
                                    //dismiss()
                                } else {
                                    Toast.makeText(
                                        this@PeopleTodayActivity,
                                        "Could not open the file",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@PeopleTodayActivity,
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
        }
    }

    //val items = mutableMapOf<String, MutableList<PrayerNeed>>()

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

    fun addNamesToNamesAddedToday(names: Collection<String>) {
        //TODO: Maybe add support for lowercase match
        /*//For a lowercase match:

        for (name in names) {
            if (
                name.isNotEmpty() &&
                !namesAddedToday.contains(name) &&
                !namesAddedToday.map{ it.lowercase() }.contains(name)
            ) {
                val lowercaseStoredNames = storedNames.map{ it.lowercase() }
                if (lowercaseStoredNames.contains(name)) {
                    val index = lowercaseStoredNames.indexOf(name)
                    namesAddedToday.add(storedNames[index])
                } else {
                    namesAddedToday.add(name)
                }
                rvNamesAddedToday.adapter?.notifyItemInserted(namesAddedToday.lastIndex)

                val fileName = filesDir.path + "/" + name + ".json"
                try {
                    File(fileName).createNewFile()
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    //e.printStackTrace()
                }
            }
        }*/

        for (name in names) {
            if (name.isNotEmpty() && !namesAddedToday.contains(name)) {
                namesAddedToday.add(name)
                rvNamesAddedToday.adapter?.notifyItemInserted(namesAddedToday.lastIndex)

                val fileName = filesDir.path + "/" + name + ".json"
                try {
                    File(fileName).createNewFile()
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    //e.printStackTrace()
                }
            }
        }
    }

    private fun saveNamesAddedTodayFile() {
        val result = StringBuilder()
        result.append(dateFormat.format(Calendar.getInstance()/*.apply{ this.set(Calendar.DAY_OF_MONTH, 26) }*/.time))

        if (namesAddedToday.size > 0) {
            result.append("|")
            result.append(namesAddedToday.joinToString(", "))
        }

        writeFile(namesAddedTodayFile, result.toString(), false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_today)

        //val proj = arrayOf(
        //    MediaStore.Downloads.REFERER_URI
        //)
        ////contentResolver.openOutputStream(downloadsDirUri)
        //with(contentResolver.query(
        //    downloadsDirUri,
        //    proj,
        //    null,
        //    null,
        //    null)) {
        //    if (this != null) {
        //        this.moveToFirst()
        //        Toast.makeText(this@PeopleTodayActivity, "${this.getString(0)}", Toast.LENGTH_SHORT).show()
        //    }
        //}

        // TODO: import shows exports or choose file
        val result = File(filesDir.path + "exports/").listFiles()

        toast(arrayOf<Any?>(
            //Environment.getDataDirectory(),
            //Environment.getExternalStorageDirectory(),
            //filesDir.absolutePath,
            //dateFormat.format(Instant.now()),
            //dateFormat.format(Date.from(Instant.now())),
            //dateFormat.format(Calendar.getInstance().toInstant()),
            //dateFormat.format(Calendar.getInstance().time),
            //getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            //MediaStore.getExternalVolumeNames(this).first(),
            //downloadsUri.toFile().canWrite()
            //a?.isDirectory
            //Environment.getExternalStorageDirectory().path
        ))

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

                if (inputParts.size == 2) {
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

//        val a = BottomSheetDialog(this)

        val swipeGestures = object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return if (viewHolder.adapterPosition != target.layoutPosition) {
                    val currentPosition = viewHolder.adapterPosition
                    val targetPosition = target.adapterPosition

                    val name = namesAddedToday[currentPosition]
                    namesAddedToday[currentPosition] = namesAddedToday[targetPosition]
                    namesAddedToday[targetPosition] = name

                    recyclerView.adapter?.notifyItemMoved(currentPosition, targetPosition)
                    true
                } else {
                    false
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }

        rvNamesAddedToday = findViewById(R.id.rv_namesAddedToday)
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
        ItemTouchHelper(swipeGestures).attachToRecyclerView(rvNamesAddedToday)

        //AlertDialog.Builder(this)
        //    .setTitle("Add name")

        addNameBottomSheetDialog = AddNameModalBottomSheet()
        addNameBottomSheetDialog.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.ThemeOverlay_App_BottomSheetDialog
        )

        //saveNamesAddedTodayFile()
        //val directories = filesDir.listFiles(FileFilter { pathname -> pathname.isDirectory })
        //if (ActivityCompat.checkSelfPermission(this,
        //    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        //}

        //TODO: if app is quit or switching to another activity
        // (see if switching to another activity and going back
        // saves the names in the list) save today's added names.
        // if person has no prayer needs offered, keep name
        // save also date, upon opening, if date is different
        // than saved to file (or maybe last accessed)
        // (maybe move list of names to history) clear list

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
                    //rvStoredNames_AddNameDialog.adapter?.notifyItemRangeRemoved(0, rvStoredNames_AddNameDialog.childCount)

                    for (fn in fileNames) {
                        storedNames.add(fn)

                        //rvStoredNames_AddNameDialog.adapter?.notifyItemInserted(storedNames.size - 1)
                    }
                }

                addNameBottomSheetDialog.arguments = Bundle().apply {
                    putStringArray("storedNames", storedNames.toTypedArray())
                }

                if (!addNameBottomSheetDialog.isAdded)
                {
                    addNameBottomSheetDialog.show(supportFragmentManager, AddNameModalBottomSheet.TAG)
                }
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
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        "text/xml"
                    } else {
                        "application/json"
                    }

                    //val downloadsUri = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    //    /**
                    //     * Adapted from stack overflow question:
                    //     * https://stackoverflow.com/questions/7908193/
                    //     * answer by slkorolev, edited by Derlin:
                    //     * https://stackoverflow.com/a/7908446
                    //     * */
                    //    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    //    downloadsDir.toUri()
                    //} else {
                    //    MediaStore.Downloads.getContentUri(
                    //        MediaStore.getExternalVolumeNames(this@PeopleTodayActivity).first()
                    //    )
                    //}

                    //putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)
                    addCategory(Intent.CATEGORY_OPENABLE)
                }

                selectExportLocationActivityLauncher.launch(Intent.createChooser(intent, ""))
            }
            R.id.mi_importPrayerNeeds -> {
                AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Import prayer needs")
                    .setMessage("Importing prayer needs will override the current prayer needs. " +
                            "Do you want to continue?")
                    .setPositiveButton("Yes") { _, _ ->
                        val intent = Intent(Intent.ACTION_GET_CONTENT)

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                            intent.type = "text/xml"
                        }
                        else {
                            intent.type = "*/*"
                            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/xml", "application/json"))
                        }

                        openFileActivityLauncher.launch(Intent.createChooser(intent, ""))
                    }
                    .setNegativeButton("No", null)
                    .create()
                    .show()
            }
        }

        return true
    }

    private fun prepareImportData(importJson: JSONObject) {
        try {
            val exportDate = Instant.parse(importJson.getString("exportDate"))
            val lastUpdateDate = Instant.parse(
                getSharedPreferences(
                    getString(R.string.lastUpdatedPrayerNeedsDataPrefName),
                    Context.MODE_PRIVATE
                ).getString(
                    getString(R.string.lastUpdatedPrayerNeedsDataString),
                    Instant.MIN.toString()
                )
            )

            if (exportDate < lastUpdateDate) {
                //TODO: maybe check if file contents are the same before deleting?
                AlertDialog.Builder(this).apply {
                    setMessage("The export in the file is older than current data. Continue?")
                    setCancelable(true)
                    setPositiveButton("Yes") { _, _ ->
                        importPrayerNeeds(importJson)
                    }
                    setNegativeButton("No", null)
                    create()
                    show()
                }

                // TODO: look up:
                //MaterialStyledDatePickerDialog(this)
                //MaterialStyledDatePickerDialog(this).datePicker
                //DatePickerDialog(this).datePicker
            } else {
                importPrayerNeeds(importJson)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun importPrayerNeeds(importJson: JSONObject)
    {
        val importNames = importJson.optJSONArray("prayerNeedsExport")

        if (importNames == null) {
            Toast.makeText(this, "File is not supported or is of an unsupported version!", Toast.LENGTH_SHORT).show()
            return
        }

        val importNamesCount = importNames.length()

        if (importNamesCount == 0) {
            Toast.makeText(this, "Import has no names!", Toast.LENGTH_SHORT).show()
            return
        }

        val pd = ProgressDialog(this).apply {
            setCancelable(true)
            this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            this.setCanceledOnTouchOutside(false)
            this.max = importNamesCount
            this.progress = 0
        }

        val importedNamesList = ObservableArrayList<String>()

        importedNamesList.addOnListChangedCallback(object:
            ObservableList.OnListChangedCallback<ObservableList<String>>() {

            override fun onChanged(sender: ObservableList<String>?) {}

            override fun onItemRangeChanged(
                sender: ObservableList<String>?,
                positionStart: Int,
                itemCount: Int
            ) {}

            override fun onItemRangeInserted(
                sender: ObservableList<String>?,
                positionStart: Int,
                itemCount: Int
            ) {
                Handler(Looper.getMainLooper()).post {
                    pd.incrementProgressBy(1)
                }

                if (importedNamesList.size == importNamesCount) {
                    namesAddedToday.minus(importedNamesList).also { results ->
                        if (results.isNotEmpty()) {
                            for (name in results) {
                                val index = namesAddedToday.indexOf(name)
                                namesAddedToday.removeAt(index)
                                this@PeopleTodayActivity.findViewById<RecyclerView>(R.id.rv_namesAddedToday)
                                    .adapter?.notifyItemRemoved(index)
                            }
                        }
                    }

                    pd.dismiss()
                }
            }

            override fun onItemRangeMoved(
                sender: ObservableList<String>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {}

            override fun onItemRangeRemoved(
                sender: ObservableList<String>?,
                positionStart: Int,
                itemCount: Int
            ) {}
        })

        pd.show()

        try {
            importManager.importPrayerNeeds(importJson, importedNamesList)

            updateDataDate(importJson.getString("exportDate"))

            //TODO:
            // ++ when making an import, show a dialog that import will replace currently existing data
            // ---- Don't show again checkbox
            // -- user could choose from created exports or choose a files
            // ++ when exporting, show a dialog window explaining export will be made to Downloads folder
            // ---- Don't show again checkbox?
            // -- use snackbars instead of toasts
            // -- change export structure to have (export v2):
            // prayerNeedsExport
            //   name, export
            //     ... (as json object, not as string)
            //Toast.makeText(this, "Import finished", Toast.LENGTH_SHORT).show()
        } catch (e: UnsupportedOperationException) {
            pd.dismiss()
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }catch (e: Exception) {
            pd.dismiss()
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun updateDataDate(date: Date = Calendar.getInstance().time) {
        val dateString = dateFormat.format(date)

        val prefs = getSharedPreferences(
            getString(R.string.lastUpdatedPrayerNeedsDataPrefName),
            Context.MODE_PRIVATE
        )
        with(prefs.edit()) {
            putString(
                getString(R.string.lastUpdatedPrayerNeedsDataString), dateString
            )
            apply()
        }
    }

    private fun updateDataDate(instant: Instant) {
        updateDataDate(Date.from(instant))
    }

    private fun updateDataDate(stringDate: String) {
        try {
            Instant.parse(stringDate)
        }
        catch (e: DateTimeParseException) {
            val dateFormat = DateFormat.getDateInstance().apply {
                isLenient = true
            }

            try {
                val date = dateFormat.parse(stringDate)
                if (date != null) {
                    updateDataDate(date)
                } else {
                    updateDataDate()
                }
            } catch (e: Exception) {
                updateDataDate()
            }
        }

        val prefs = getSharedPreferences(
            getString(R.string.lastUpdatedPrayerNeedsDataPrefName),
            Context.MODE_PRIVATE
        )
        with(prefs.edit()) {
            putString(
                getString(R.string.lastUpdatedPrayerNeedsDataString), stringDate
            )
            apply()
        }
    }

    fun toast(message: Any?) {
        if (message is Iterable<*>) {
            Toast.makeText(this, message.joinToString("\n\n"), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "$message", Toast.LENGTH_LONG).show()
        }
    }
}