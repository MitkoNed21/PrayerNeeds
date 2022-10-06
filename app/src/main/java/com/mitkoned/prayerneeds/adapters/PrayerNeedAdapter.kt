package com.mitkoned.prayerneeds.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.BoxedBoolean
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.R

class PrayerNeedAdapter (
    private val prayerNeeds: MutableList<PrayerNeed>,
    private val hidingAnsweredPrayerNeeds: BoxedBoolean
) : RecyclerView.Adapter<PrayerNeedAdapter.PrayerNeedViewHolder>() {
    inner class PrayerNeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var foregroundView: ConstraintLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerNeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout__rv_prayer_need, parent, false)

        val viewHolder = PrayerNeedViewHolder(view)
        viewHolder.foregroundView = view.findViewById(R.id.cl_prayerNeedForeground)

        return viewHolder
    }

    override fun getItemCount(): Int {
        return prayerNeeds.size
    }

    override fun onBindViewHolder(holder: PrayerNeedViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            val tvDesc = currentView.findViewById<TextView>(R.id.tv_desc)
            val tvDate = currentView.findViewById<TextView>(R.id.tv_date)
            val tvYear = currentView.findViewById<TextView>(R.id.tv_year)
            val cbAnswered = currentView.findViewById<CheckBox>(R.id.cb_answered)

            currentView.apply {
                tvDesc.text = prayerNeeds[position].description
                val dateString = prayerNeeds[position].dateCustomFormatted
                val dateParts = dateString.split(';')
                tvDate.text = dateParts[0]
                tvYear.text = dateParts[1]
                cbAnswered.isChecked = prayerNeeds[position].answered
            }

            cbAnswered.setOnCheckedChangeListener { _, isChecked ->
                if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                    // TODO: FIX
                    //[holder.adapterPosition].answered = isChecked

                    if (hidingAnsweredPrayerNeeds.value && isChecked) {
                        prayerNeeds.removeAt(holder.adapterPosition)
                        this.notifyItemRemoved(holder.adapterPosition)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}