package com.mitkoned.prayerneeds

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity3 : AppCompatActivity() {
    private lateinit var namesFile: File
    private lateinit var rv_names_allPeople: RecyclerView

    private val names = mutableListOf<String>()
    private lateinit var dateFormat: DateFormat

    val items = mutableMapOf<String, MutableList<PrayerNeed>>()

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_people)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.US)
        rv_names_allPeople = findViewById(R.id.rv_names_allPeople)
        rv_names_allPeople.adapter = AddedNamesElementAdapter(names)
        rv_names_allPeople.layoutManager = LinearLayoutManager(this)

        try {
            val files = filesDir.listFiles()
            { file ->
                file.extension == "json"
            }

            if (files != null) {
                val fileNames = files
                    .map { f -> f.nameWithoutExtension }
                    .sortedBy { fn -> fn }

                for (fn in fileNames) {
                    names.add(fn)
                    rv_names_allPeople.adapter?.notifyItemInserted(names.size - 1)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.stackTrace.joinToString("\n"), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.title = "All names"
        return super.onCreateOptionsMenu(menu)
    }
}