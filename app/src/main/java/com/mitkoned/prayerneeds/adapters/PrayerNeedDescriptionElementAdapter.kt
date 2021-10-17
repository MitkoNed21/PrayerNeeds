package com.mitkoned.prayerneeds.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.R

class PrayerNeedDescriptionElementAdapter (private val prayerNeedsDescriptions: List<String>)
    : RecyclerView.Adapter<PrayerNeedDescriptionElementAdapter.PrayerNeedDescriptionElementViewHolder>() {
    inner class PrayerNeedDescriptionElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerNeedDescriptionElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout__rv_all_prayer_needs_prayer_need, parent, false)
        return PrayerNeedDescriptionElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return prayerNeedsDescriptions.size
    }

    override fun onBindViewHolder(holder: PrayerNeedDescriptionElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            val tvDescription = currentView.findViewById<TextView>(R.id.tv_prayerNeedDescription)
            tvDescription.text = prayerNeedsDescriptions[position]

        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.message.toString(), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}