package com.mitkoned.prayerneeds.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.activities.PrayerNeedsActivity
import java.io.File
import java.lang.Exception

class AddedNameTodayElementAdapter (private val names: MutableList<String>)
    : RecyclerView.Adapter<AddedNameTodayElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout__rv_activity_main_namecontainer, parent, false)
        return NameElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return names.size
    }
// onBindViewHolder is entered twice because calling notifyItemChanged() probably
    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            val btName = currentView.findViewById<Button>(R.id.bt_nameContainer)

            btName.setOnClickListener {
                try {
                    val intent = Intent(btName.rootView.context, PrayerNeedsActivity::class.java).apply {
                        putExtra("requestedNameIndex", position)
                        putExtra("namesArray", names.toTypedArray())
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    it.context.applicationContext.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(btName.context, e.message.toString(), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            currentView.apply {
                if (position == 0) {
                    this.findViewById<View>(R.id.divider).visibility = View.INVISIBLE
                }

                btName.text = names[position]
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}