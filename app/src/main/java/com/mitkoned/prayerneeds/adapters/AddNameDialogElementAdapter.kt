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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mitkoned.prayerneeds.R
import org.w3c.dom.Text
import java.lang.Exception

class AddNameDialogElementAdapter (private val names: List<String>, private val addNameDialog: BottomSheetDialog)
    : RecyclerView.Adapter<AddNameDialogElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private lateinit var clNameContainer: ConstraintLayout
    private lateinit var tvNameText: TextView

    private lateinit var etName: EditText
    private lateinit var btnAddNameToToday: Button
    private val namesArray = names.toTypedArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout__rv_dialog_namecontainer, parent, false)

        // this sets fields to last element's views.
        // look up how to use?
        clNameContainer = view.findViewById(R.id.cl_nameContainer)
        tvNameText = clNameContainer.findViewById(R.id.tv_nameText)

        etName = addNameDialog.findViewById(R.id.et_nameToAdd)!!
        btnAddNameToToday = addNameDialog.findViewById(R.id.btn_addNameToToday)!!

        return NameElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun getItemId(position: Int): Long {
        return namesArray.indexOf(names[position]).toLong()
    }

    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView
            val clNameContainer = currentView.findViewById<ConstraintLayout>(R.id.cl_nameContainer)
            val tvNameText = clNameContainer.findViewById<TextView>(R.id.tv_nameText)

            val etName = addNameDialog.findViewById<EditText>(R.id.et_nameToAdd)!!
            val btnAddNameToToday = addNameDialog.findViewById<Button>(R.id.btn_addNameToToday)!!

            clNameContainer.setOnClickListener {
                etName.text.append(tvNameText.text, ", ")
            }

            currentView.apply {
                tvNameText.text = names[position]
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.message.toString(), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}