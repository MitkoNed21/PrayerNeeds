package com.mitkoned.prayerneeds.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.activities.PrayerNeedsActivity
import java.lang.Exception

class AddedNamesElementAdapter (private val names: List<String>)
    : RecyclerView.Adapter<AddedNamesElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    enum class Options(val displayName: String) {
        //EditName("Edit name")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout__rv_activity_main_namecontainer, parent, false)
        return NameElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            val tvPersonName = currentView.findViewById<TextView>(R.id.tv_personName)
            val clMainContainer = currentView.findViewById<ConstraintLayout>(R.id.cl_mainContainer)
            val ibtMoreOptions = currentView.findViewById<ImageButton>(R.id.ibt_moreOptions)

            //val btName = currentView.findViewById<Button>(R.id.bt_nameContainer)

            //btName.setOnLongClickListener()

            clMainContainer.setOnClickListener {
                try {
                    val intent = Intent(it.context, PrayerNeedsActivity::class.java).apply {
                        putExtra("requestedNameIndex", position)
                        // if have option Delete, change "position" to "holder.adapterPosition"
                        putExtra("namesArray", names.toTypedArray())
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    it.context.applicationContext.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(it.context, e.message.toString(), Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            var touched = false
            ibtMoreOptions.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    if (!touched) {
                        touched = true
                        ibtMoreOptions.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#202020"))
                    }
                } else {
                    if (touched) {
                        touched = false
                        ibtMoreOptions.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                    }
                }
            }

            if (Options.values().isEmpty()) {
                ibtMoreOptions.visibility = View.GONE
            }

            ibtMoreOptions.setOnClickListener { button ->
                PopupMenu(button.context, button).also {
                    for (option in Options.values()) {
                        it.menu.add(option.displayName)
                    }

                    it.setOnMenuItemClickListener { item ->
                        when (item.title) {
                            ""/*Options.EditName.displayName*/ -> {

                                Toast.makeText(button.context, "NOT IMPLEMENTED YET", Toast.LENGTH_SHORT).show()
                                return@setOnMenuItemClickListener true

                                //TODO: Create layout for dialog
                                AlertDialog.Builder(currentView.context)
                                    .setTitle("Edit name")
                                    .setPositiveButton("Save") { _, _ ->
                                        val adapterPosition = holder.adapterPosition
                                        val oldName = names[adapterPosition]
                                        //TODO: Update name
                                        this.notifyItemChanged(adapterPosition)

                                        Snackbar.make(
                                            currentView.parent as RecyclerView,
                                            "Name updated.",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("Undo") { // on click listener // TODO: if names doesn't contain the name.
                                                //TODO: Reset name to "oldName"
                                                this.notifyItemChanged(adapterPosition)
                                            }
                                            //.setBackgroundTintList(Color.parseColor("#808080"))
                                            // TODO: Make a layout for the Snackbar
                                            .setBackgroundTint(Color.parseColor("#808080"))
                                            .setActionTextColor(Color.parseColor("#F0F0F0"))
                                            .setTextColor(Color.parseColor("#F0F0F0"))
                                            .show()
                                    }
                                    .setCancelable(true)
                                    .setMessage("Enter new name for ${names[holder.adapterPosition]}.")
                                    .create()
                                    .show()

                                true
                            }
                            else -> false
                        }
                    }
                    it.show()
                }
            }

            tvPersonName.text = names[position]
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}