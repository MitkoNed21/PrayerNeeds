package com.mitkoned.prayerneeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mitkoned.prayerneeds.adapters.AddNameDialogElementAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.doOnTextChanged
import com.mitkoned.prayerneeds.activities.PeopleTodayActivity

class AddNameModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var rvStoredNames: RecyclerView
    private lateinit var etNameToAdd: EditText
    private lateinit var btnAddNameToToday: Button

    private lateinit var storedNamesList: List<String>
    private lateinit var shownNamesList: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_addname, container, false)
        storedNamesList = requireArguments().getStringArray("storedNames")!!.toList()
        shownNamesList = storedNamesList.toMutableList()

        rvStoredNames = view.findViewById(R.id.rv_storedNames)
        btnAddNameToToday = view.findViewById(R.id.btn_addNameToToday)
        etNameToAdd = view.findViewById(R.id.et_nameToAdd)

        etNameToAdd.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                shownNamesList.clear()
                shownNamesList.addAll(storedNamesList)
                rvStoredNames.adapter?.notifyDataSetChanged()
                return@doOnTextChanged
            }

            val currentlyTypedName: String
            val names = text
                .split(",")
                .toMutableList().apply {
                    currentlyTypedName = this.last().trim()
                    removeLast()
                    removeIf { it.isBlank() }
                }
                .map { it.trim() }

            val notAddedNamesList = storedNamesList.minus(names.toSet())
            if (currentlyTypedName.isEmpty()) {
                shownNamesList.clear()
                shownNamesList.addAll(notAddedNamesList)
                rvStoredNames.adapter?.notifyDataSetChanged()
                return@doOnTextChanged
            }

            val matchingNames = notAddedNamesList.filter {
                it.startsWith(currentlyTypedName, true)
            }

            shownNamesList.clear()
            shownNamesList.addAll(matchingNames)
            rvStoredNames.adapter?.notifyDataSetChanged()
        }

        /*etNameToAdd.doOnTextChanged { text, _, before, _ ->
            if (text.isNullOrEmpty()) {
                val missingNames = storedNamesList.minus(shownNamesList.toSet())
                for (name in missingNames) {
                    val index = storedNamesList.indexOf(name)
                    namesToShow[index] = name
                    shownNamesList = namesToShow.filterNotNull()
                    rvStoredNames.adapter?.notifyItemInserted(index)
                }
                return@doOnTextChanged
            }

            val currentlyTypedName: String
            val names = text
                .split(",")
                .toMutableList().apply {
                    currentlyTypedName = this.last().trim()
                    removeIf { it.isBlank() }
                    removeLast()
                }
                .map { it.trim() }

            val notAddedNamesList = storedNamesList.minus(names.toSet())
            if (currentlyTypedName.isEmpty()) {
                val missingNames = notAddedNamesList.minus(shownNamesList.toSet())
                for (name in missingNames) {
                    val index = notAddedNamesList.indexOf(name)
                    namesToShow[index] = name
                    shownNamesList = namesToShow.filterNotNull()
                    rvStoredNames.adapter?.notifyItemInserted(index)
                }
                return@doOnTextChanged
            }

            val matchingNames = notAddedNamesList.filter {
                it.startsWith(currentlyTypedName, true)
            }

            //if (matchingNames.isEmpty()) {
            //    shownNamesList.clear()
            //    rvStoredNames.adapter?.notifyItemRangeRemoved(0, rvStoredNames.adapter!!.itemCount)
            //}

            val hasDeleted = text.length < before

            if (hasDeleted) {
                for (name in matchingNames) {
                    if (!shownNamesList.contains(name)) {
                        val index = notAddedNamesList.indexOf(name)
                        namesToShow[index] = name
                        shownNamesList = namesToShow.filterNotNull()
                        rvStoredNames.adapter?.notifyItemInserted(index)
                    }
                }
            } else {
                val unmatchedNames = notAddedNamesList.minus(matchingNames.toSet())

                for (name in unmatchedNames) {
                    if (shownNamesList.contains(name)) {
                        val index = namesToShow.indexOf(name)
                        namesToShow[index] = null
                        shownNamesList = namesToShow.filterNotNull()
                        rvStoredNames.adapter?.notifyItemRemoved(index)
                    }
                }
            }


        }*/

        //TODO: Maybe have checkbox next to name when adding so can select
        // multiple names and add them all at once, maybe then sort

        btnAddNameToToday.setOnClickListener {
            val selectedNames = etNameToAdd.text.split(",")
                .map { it.trim() }

            (requireActivity() as PeopleTodayActivity).addNamesToNamesAddedToday(selectedNames)
            this.dismiss()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvStoredNames.layoutManager = object : LinearLayoutManager(requireDialog().context) {
            override fun supportsPredictiveItemAnimations(): Boolean { return true }
        }

        val adapter = AddNameDialogElementAdapter(shownNamesList, requireDialog() as BottomSheetDialog)
        adapter.setHasStableIds(true)
        rvStoredNames.adapter = adapter
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener { dialogInterface ->
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            etNameToAdd.requestFocus()
            //ViewCompat.getWindowInsetsController(requireView())?.show(WindowInsetsCompat.Type.ime())
            //val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            //imm.showSoftInput(etNameToAdd, InputMethodManager.SHOW_FORCED)
            //Toast.makeText(requireContext(), etNameToAdd.isInEditMode.toString(), Toast.LENGTH_SHORT).show()
            //dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        etNameToAdd.setText("")
    }

    companion object {
        const val TAG = "AddNameModalBottomSheet"
    }
}