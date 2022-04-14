package com.mitkoned.prayerneeds.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.adapters.AddedNamesElementAdapter
import com.mitkoned.prayerneeds.R

class AllPeopleActivity : AppCompatActivity() {
    private lateinit var rvAllStoredPeople: RecyclerView

    private val names = mutableListOf<String>()

    //@ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_people)

        rvAllStoredPeople = findViewById(R.id.rv_allStoredPeople)
        rvAllStoredPeople.adapter = AddedNamesElementAdapter(names)
        rvAllStoredPeople.layoutManager = LinearLayoutManager(this)

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
                    rvAllStoredPeople.adapter?.notifyItemInserted(names.size - 1)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.stackTrace.joinToString("\n"), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.title = "All names"
        return super.onCreateOptionsMenu(menu)
    }
}