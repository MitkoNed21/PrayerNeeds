package com.mitkoned.prayerneeds.adapters

import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mitkoned.prayerneeds.R
import java.lang.Exception

class AddNameDialogElementAdapter (private val names: List<String>, private val addNameDialog: AlertDialog)
    : RecyclerView.Adapter<AddNameDialogElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout__rv_dialog_namecontainer, parent, false)
        return NameElementViewHolder(view)
        }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            //val btName = currentView.findViewById<Button>(R.id.bt_nameContainer)
            val clNameContainer = currentView.findViewById<ConstraintLayout>(R.id.cl_nameContainer)
            val tvNameText = clNameContainer.findViewById<TextView>(R.id.tv_nameText)

            clNameContainer.setOnClickListener {
                //TODO: When typing in the EditText, offer names already added

                val etName = addNameDialog.findViewById<EditText>(R.id.et_nameToAdd)
                etName?.setText(tvNameText.text)
                addNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            }

            currentView.apply {
                //if (position == 0) {
                //    this.findViewById<View>(R.id.divider).visibility = View.INVISIBLE
                //}

                tvNameText.text = names[position]
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}