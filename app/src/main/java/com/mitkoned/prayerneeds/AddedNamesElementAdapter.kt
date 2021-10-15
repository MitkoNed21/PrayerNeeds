package com.mitkoned.prayerneeds

import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import java.io.FileReader
import java.lang.Exception

class AddedNamesElementAdapter (private val names: List<String>)
    : RecyclerView.Adapter<AddedNamesElementAdapter.NameElementViewHolder>() {
    inner class NameElementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameElementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.namecontainer_rv_activity_main, parent, false)
        return NameElementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return names.size
    }

    override fun onBindViewHolder(holder: NameElementViewHolder, position: Int) {
        try {
            val currentView = holder.itemView

            val btName = currentView.findViewById<Button>(R.id.bt_nameContainer)

            btName.setOnClickListener {
                try {
                    val intent = Intent(btName.rootView.context, MainActivity::class.java).apply {
                        putExtra("requestedNameIndex", position)
                        putExtra("namesArray", names.toTypedArray())
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    it.context.applicationContext.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(btName.context, e.stackTrace.joinToString("\n"), Toast.LENGTH_LONG).show()
                }
            }

            currentView.apply {
                if (position == 0) {
                    this.findViewById<View>(R.id.divider).visibility = View.INVISIBLE
                }

                btName.text = names[position]
            }
        } catch (e: Exception) {
            Toast.makeText(holder.itemView.context, e.stackTrace.joinToString("\n"), Toast.LENGTH_SHORT).show()
        }
    }
}

/*class AddNameDialogElementAdapter (private val names: List<String>, private val addNameDialog: AlertDialog)
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
            //TODO: Change activity and send button text
            // may remove the dialog window when switching;
            // if not, close dialog and switch
            // Intention: close dialog, add name to RV in MainActivity2
            // maybe could instead switch activity where could be a +
            // for adding another name to the list

            //TODO: When typing in the EditText, offer names already added,
            // if ADD is pressed, then add the name if it doesn't exist
            val etName = addNameDialog.findViewById<EditText>(R.id.et_name)
            etName?.setText(btName.text)
            addNameDialog.dismiss()
        }

        currentView.apply {
            btName.text = names[position]
        }
    }
}*/