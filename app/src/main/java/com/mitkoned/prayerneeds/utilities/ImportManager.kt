package com.mitkoned.prayerneeds.utilities

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.databinding.ObservableArrayList
import com.mitkoned.prayerneeds.R
import net.javacrumbs.json2xml.JsonXmlHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory


/**
 * [context] is the context of the running application.
 */
class ImportManager(val context: Context) {

    private val prayerNeedFullDateFormat by lazy {
        SimpleDateFormat(context.getString(R.string.dateFormat), Locale.US)
    }

    private val unsupportedFileException =
        UnsupportedOperationException("File is not supported or is of an unsupported version!")

    fun parseImportFile(uri: Uri): JSONObject {
        lateinit var fileContents: String
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(uri)?.apply {
            fileContents = readBytes().decodeToString()
            close()
        }

        if (fileContents.isBlank()) {
            TODO("Choose appropriate exception for blank file")
        } else {
            if (contentResolver.getType(uri) == "text/xml") {
                val xmlDocument = with(contentResolver.openInputStream(uri)) {
                    DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(this)
                }
                fileContents = JsonXmlHelper.convertToJson(xmlDocument)
            }
        }

        return JSONObject(fileContents)
    }

    fun parseImportFile(path: String): JSONObject {
        return parseImportFile(File(path).toUri())
    }

    fun importPrayerNeeds(json: JSONObject, importedNamesList: ObservableArrayList<String>) {
        val exportVersion = json.optInt("exportVersion", -1)

        if (exportVersion == -1) {
            throw unsupportedFileException
        }

        context.filesDir.listFiles { f ->
            f.extension == "json"
        }?.forEach {
            it.delete()
        }

        when (exportVersion) {
            1 -> {
                val names = json.getJSONArray("prayerNeedsExport")
                val namesCount = names.length()

                // TODO: maybe this should not be this way, but using it
                //  makes the process "async"
                //Handler(Looper.getMainLooper()).post {
                    for (i in 0 until namesCount) {
                        val jsonPersonPrayerNeeds = names.getJSONObject(i)
                        val name = jsonPersonPrayerNeeds
                            .names()!!
                            .getString(0)

                        val nameToImport = name.replace("_", " ")
                        val newFile = File("${context.filesDir.path}/${nameToImport}.json")
                        newFile.createNewFile()

                        writeFile(newFile, jsonPersonPrayerNeeds.getString(name), false)
                        importedNamesList.add(nameToImport)
                //    }
//            namesAddedToday.clear()
//            this.findViewById<RecyclerView>(R.id.rv_namesAddedToday)
//                .adapter?.also {
//                    it.notifyItemRangeRemoved(0, it.itemCount)
//                }
                }
            }
            2 -> {
                val names = json.getJSONArray("prayerNeedsExport")
                val namesCount = names.length()

                // TODO: maybe this should not be this way, but using it
                //  makes the process "async"
                //Handler(Looper.getMainLooper()).post {
                for (i in 0 until namesCount) {
                    val name = names.getJSONObject(i).getString("name")
                    val jsonPersonPrayerNeeds = names.getJSONObject(i).getJSONArray("prayerNeeds")
                    val prayerNeedsCount = jsonPersonPrayerNeeds.length()

                    val prayerNeedsMap = mutableMapOf<Date, JSONObject>()
                    for (j in 0 until prayerNeedsCount) {
                        val dateString = jsonPersonPrayerNeeds.getJSONObject(j).let {
                            it.getString("date") + ";" + it.getString("year")
                        }
                        val date = prayerNeedFullDateFormat.parse(dateString)
                        prayerNeedsMap[date!!] = jsonPersonPrayerNeeds.getJSONObject(j)
                    }

                    // sort descending
                    val sortedPrayerNeedsMap = prayerNeedsMap.toSortedMap { first, second ->
                        -first!!.compareTo(second)
                    }

                    val jsonObject = JSONObject().apply {
                        put("prayerNeeds", JSONArray(sortedPrayerNeedsMap.map { it.value }))
                    }


                    val newFile = File("${context.filesDir.path}/${name}.json")
                    newFile.createNewFile()

                    writeFile(newFile, jsonObject.toString(2), false)
                    importedNamesList.add(name)
                    //    }
//            namesAddedToday.clear()
//            this.findViewById<RecyclerView>(R.id.rv_namesAddedToday)
//                .adapter?.also {
//                    it.notifyItemRangeRemoved(0, it.itemCount)
//                }
                }
            }
        }
    }
}