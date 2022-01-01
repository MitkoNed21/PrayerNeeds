package com.mitkoned.prayerneeds.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.PrayerNeedSwipeGestures
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.activities.PrayerNeedsActivity
import java.io.File
import java.lang.Exception

class AddedNameTodayElementAdapter (private val names: MutableList<String>)
    : RecyclerView.Adapter<AddedNameTodayElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    enum class Options(val displayName: String) {
        Delete("Delete")
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

            /*clMainContainer.setOnLongClickListener {


            }*/

            clMainContainer.setOnClickListener {
                try {
                    val intent = Intent(it.context, PrayerNeedsActivity::class.java).apply {
                        putExtra("requestedNameIndex", holder.adapterPosition)
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
            /*ibtMoreOptions.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent. -> {
                        if (!touched) {
                            touched = true
                            view.backgroundTintList =
                                ColorStateList.valueOf(Color.parseColor("#202020"))
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (touched) {
                            touched = false
                            view.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                        }
                    }
                }
                true
            }*/
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
                            Options.Delete.displayName -> {
                                AlertDialog.Builder(currentView.context)
                                    .setTitle("Remove person")
                                    .setPositiveButton("Yes") { _, _ ->
                                        val adapterPosition = holder.adapterPosition
                                        val removedName = names.removeAt(adapterPosition)
                                        this.notifyItemRemoved(adapterPosition)

                                        Snackbar.make(
                                            currentView.parent as RecyclerView,
                                            removedName + " was removed.",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("Undo") { // on click listener // TODO: if names doesn't contain the name.
                                                names.add(adapterPosition, removedName)
                                                this.notifyItemInserted(adapterPosition)
                                            }
                                            //.setBackgroundTintList(Color.parseColor("#808080"))
                                            // TODO: Make a layout for the Snackbar
                                            .setBackgroundTint(Color.parseColor("#808080"))
                                            .setActionTextColor(Color.parseColor("#F0F0F0"))
                                            .setTextColor(Color.parseColor("#F0F0F0"))
                                            .show()
                                    }
                                    .setCancelable(true)
                                    .setMessage("Do you want to remove ${names[holder.adapterPosition]} from today?")
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