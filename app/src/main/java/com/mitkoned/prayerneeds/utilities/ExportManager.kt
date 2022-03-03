package com.mitkoned.prayerneeds.utilities

import android.content.Context
import android.net.Uri
import android.os.Build
import net.javacrumbs.json2xml.JsonXmlHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler

private const val EXPORT_VERSION = 3

class ExportManager (val context: Context) {
    /**
     * Exports prayer needs to provided [uri] directory.
     * Returns a list with names that were not
     * successfully exported or null if there aren't any.
     * Throws [Exception]
     * */
    fun exportPrayerNeeds(uri: Uri) : List<String>? {
        val unexportedNames = mutableListOf<String>()
        val allNames: List<String>
        val namesJSONArray = JSONArray()

        val files = context.filesDir.listFiles { file ->
            file.extension == "json"
        }

        if (files != null) {
            allNames = files
                .map { f -> f.nameWithoutExtension }
                .sortedBy { fn -> fn }
            var lastId = 1

            for (name in allNames) {
                try {
                    val fileName = File(context.filesDir.path + "/" + name + ".json")
                    val fileContents = readFile(fileName)

                    if (fileContents != null) {
                        namesJSONArray.put(JSONObject().apply {
                            put("name", name)

                            val jsonObject = JSONObject(fileContents)
                            if (jsonObject.has("prayerNeeds")) {
                                val items = jsonObject.getJSONArray("prayerNeeds")
                                for (i in 0 until items.length()) {
                                    val item = items.getJSONObject(i)
                                    //if an item doesn't have an id, then
                                    // it the import is v1 or v2.
                                    if (!item.has("id")) {
                                        item.put("id", lastId)
                                        lastId++
                                    }
                                }
                                put("prayerNeeds", items)
                            } else {
                                throw InvalidObjectException("Object should have a \"prayerNeeds\" key!")
                            }
                        })
                    }
                } catch (e: Exception) {
                    unexportedNames.add(name)
                }
            }

            val resultJsonObject = JSONObject()
            resultJsonObject.put("exportDate", Instant.now().toString())
            resultJsonObject.put("exportVersion", EXPORT_VERSION)
            resultJsonObject.put("prayerNeedsExport", namesJSONArray)

            var exportString = resultJsonObject.toString(2)
            var exportFileExt = "json"

            val localExportsDir = File(context.filesDir.path + "/exports")

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                JsonXmlHelper.convertToDom(
                    exportString,
                    "",
                    true,
                    "root"
                ).also { rootNode ->
                    val transformer = TransformerFactory.newInstance()
                        .newTransformer()
                    val source = javax.xml.transform.dom.DOMSource(rootNode)
                    val result = javax.xml.transform.stream.StreamResult(StringWriter())
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes")

                    /**
                     * Adapted from stack overflow question:
                     * https://stackoverflow.com/questions/1384802/
                     * answer by KV Prajapati:
                     * https://stackoverflow.com/a/1384816
                     */
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

                    transformer.transform(source, result)

                    with (result.writer as StringWriter) {
                        exportString =  this.buffer.toString()
                        exportFileExt = "xml"
                    }
                }
            }

            if (!localExportsDir.exists()) {
                localExportsDir.mkdir()
            }

            val currentDateTime = Calendar.getInstance().time
            val exportDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            val dateString = exportDateFormat.format(currentDateTime)
            val exportFile = File(
                "${localExportsDir.path}/prayer_needs_export-$dateString.$exportFileExt"
            )

//            if (!exportFile.createNewFile()) {
//                exportFile = File(
//                    exportFile.path
//                        .substringBeforeLast(".$exportFileExt") + "_2.$exportFileExt"
//                )
//
//                var count = 2
//                while (!exportFile.createNewFile()) {
//                    exportFile = File(
//                        exportFile.path
//                            .substringBeforeLast("_$count.$exportFileExt") + "_${count + 1}.$exportFileExt"
//                    )
//                    count++
//                }
//            }

            with(context.contentResolver.openOutputStream(uri)) {
                this?.write(exportString.toByteArray())
            }
            writeFile(exportFile, exportString, false)

            //val downloadsDirectory = File("/storage/emulated/0/Download/")

            //if (downloadsDirectory.exists()) {
            //    exportFile.copyTo(File(downloadsDirectory.path + "/" + exportFile.name), true)
            //} else {
            //
            //}
        }

        // necessary?
        if (unexportedNames.size == 0) return null
        return unexportedNames
    }
}