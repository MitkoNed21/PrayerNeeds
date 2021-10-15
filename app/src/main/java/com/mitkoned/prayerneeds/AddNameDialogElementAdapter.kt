package com.mitkoned.prayerneeds

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.RecyclerView
import java.util.zip.Inflater
import kotlin.reflect.typeOf

class AddNameDialogElementAdapter (private val names: List<String>, private val addNameDialog: AlertDialog)
    : RecyclerView.Adapter<AddNameDialogElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.namecontainer_rv_dialog, parent, false)
        return NameElementViewHolder(view)
        }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        val currentView = holder.itemView

        val btName = currentView.findViewById<Button>(R.id.bt_nameContainer)

        btName.setOnClickListener {
            //TODO: When typing in the EditText, offer names already added

            val etName = addNameDialog.findViewById<EditText>(R.id.et_name)
            etName?.setText(btName.text)
            addNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        }

        currentView.apply {
            if (position == 0) {
                this.findViewById<View>(R.id.divider).visibility = View.INVISIBLE
            } /*else {
                this.findViewById<View>(R.id.divider).visibility = View.VISIBLE
            }
            // */
            btName.text = names[position]
        }
    }
}