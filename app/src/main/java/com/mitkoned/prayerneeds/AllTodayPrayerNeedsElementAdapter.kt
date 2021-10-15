package com.mitkoned.prayerneeds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.File
import java.io.FileReader
import java.lang.IllegalStateException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AllTodayPrayerNeedsElementAdapter (private val fileNames: List<String>)
    : RecyclerView.Adapter<AllTodayPrayerNeedsElementAdapter.PersonPrayerNeedsElementViewHolder>() {
    inner class PersonPrayerNeedsElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private lateinit var dateFormat: DateFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonPrayerNeedsElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.all_today_prayer_needs_rv, parent, false)

        dateFormat = SimpleDateFormat(view.context.applicationContext.getString(R.string.dateFormat), Locale.US)

        return PersonPrayerNeedsElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fileNames.size
    }

    override fun onBindViewHolder(holder: PersonPrayerNeedsElementViewHolder, position: Int) {
        val currentView = holder.itemView

        val fileName = fileNames[position]

        lateinit var reader: FileReader
        lateinit var inputJsonString: String

        try {
            reader = FileReader(fileName)
            inputJsonString = reader.readText()
        } catch (e: Exception) {
            Toast.makeText(currentView.context, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_SHORT).show()
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                Toast.makeText(currentView.context, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_SHORT).show()
            }
        }

        val rvPrayerNeedsDescriptions = currentView.findViewById<RecyclerView>(R.id.rv_prayerNeedsDescriptions)
        val tvName = currentView.findViewById<TextView>(R.id.tv_name)

        // in a path ".../.../.../someFile.json" return only "someFile"
        tvName.text = fileName.substringAfterLast("/").substringBeforeLast(".json")
        if (position == 0) {
            tvName.setPadding(0,20,0,0)
        }

        currentView.apply {



            val prayerNeedsDescriptionsList = mutableListOf<String>()
            val prayerNeedsDescriptionsAdapter = PrayerNeedDescriptionElementAdapter(prayerNeedsDescriptionsList)
            rvPrayerNeedsDescriptions.adapter = prayerNeedsDescriptionsAdapter
            rvPrayerNeedsDescriptions.layoutManager = LinearLayoutManager(currentView.context)

            //if (JsonReader(StringReader(inputJsonString)).hasNext())
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

                            val currentDate = Calendar.getInstance().time
                            val currentDateString = dateFormat.format(currentDate)
                            val prayerNeedDateString = "$date;$year"

                            if (currentDateString == prayerNeedDateString) {
                                //TODO: could do optimizing: when reached date
                                // that is not equal, stop (applies when prayer
                                // needs are ordered by date descending)
                                prayerNeedsDescriptionsList.add(description)
                            }

                            prayerNeedsDescriptionsAdapter.notifyItemInserted(prayerNeedsDescriptionsList.size - 1)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this.context, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}