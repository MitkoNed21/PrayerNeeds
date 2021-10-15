package com.mitkoned.prayerneeds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class PrayerNeedDescriptionElementAdapter (private val prayerNeedsDescriptions: List<String>)
    : RecyclerView.Adapter<PrayerNeedDescriptionElementAdapter.PrayerNeedDescriptionElementViewHolder>() {
    inner class PrayerNeedDescriptionElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerNeedDescriptionElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.prayer_need_rv_all_prayer_needs_layout, parent, false)
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
            Toast.makeText(holder.itemView.context, e.stackTrace.joinToString { "\n" }, Toast.LENGTH_SHORT).show()
        }
    }
}