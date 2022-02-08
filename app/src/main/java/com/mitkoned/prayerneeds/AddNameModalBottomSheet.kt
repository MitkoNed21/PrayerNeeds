package com.mitkoned.prayerneeds

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.properties.Delegates
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mitkoned.prayerneeds.adapters.AddNameDialogElementAdapter
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.mitkoned.prayerneeds.activities.PeopleTodayActivity

class AddNameModalBottomSheet : BottomSheetDialogFragment() {
    private var collapsedMargin by Delegates.notNull<Int>()
    private var bottomViewHeight by Delegates.notNull<Int>()
    private var expandedHeight by Delegates.notNull<Int>()

    private lateinit var rvStoredNames: RecyclerView
    private lateinit var etNameToAdd: EditText
    private lateinit var btnAddNameToToday: Button
    private lateinit var bottomView: View
    private lateinit var bottomViewLayoutParams: ConstraintLayout.LayoutParams

    private lateinit var storedNamesList: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_addname, container, false)
        storedNamesList = requireArguments().getStringArray("storedNames")!!.toList()

        bottomView = view.findViewById(R.id.cl_dialog_addName_bottomView)
        rvStoredNames = view.findViewById(R.id.rv_storedNames)
        btnAddNameToToday = view.findViewById(R.id.btn_addNameToToday)
        etNameToAdd = view.findViewById(R.id.et_nameToAdd)

        //TODO: use MultiAutoCompleteTextView

        //TODO: have checkbox next to name when adding so can select
        // multiple names and add them all at once, maybe then sort

        btnAddNameToToday.setOnClickListener {
            val selectedName = etNameToAdd.text.toString()
            (requireActivity() as PeopleTodayActivity).addNameToNamesAddedToday(selectedName)
            this.dismiss()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AddNameDialogElementAdapter(storedNamesList, requireDialog() as BottomSheetDialog)
        rvStoredNames.adapter = adapter
        rvStoredNames.layoutManager = LinearLayoutManager(requireDialog().context)
        rvStoredNames.setHasFixedSize(true)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener { dialogInterface ->
            dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            setupRatio((dialogInterface as BottomSheetDialog))
        }
        dialog.behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomViewLayoutParams.topMargin = if (slideOffset > 0) {
                    //Sliding happens from 0 (Collapsed) to 1 (Expanded) - if so, calculate margins
                    ((expandedHeight - bottomViewHeight - collapsedMargin) * slideOffset + collapsedMargin).toInt()
                } else {
                    //If not sliding above expanded, set initial margin
                    collapsedMargin
                }

                //Set layout params to button (margin from top)
                bottomView.layoutParams = bottomViewLayoutParams
            }
        })
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        etNameToAdd.setText("")
    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<CoordinatorLayout>(R.id.crdl_dialog_addName)

        if (bottomSheet == null) return

        //Retrieve bottom view parameters
        bottomViewLayoutParams = bottomView.layoutParams as ConstraintLayout.LayoutParams

        //Retrieve bottom sheet parameters
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val bottomSheetLayoutParams = bottomSheet.layoutParams
        bottomSheetLayoutParams.height = getBottomSheetDialogDefaultHeight()
        expandedHeight = bottomSheetLayoutParams.height
        //Peek height to 70% of expanded height (Change based on your view)
        val peekHeight = (expandedHeight / 1.3).toInt()

        //Setup bottom sheet
        bottomSheet.layoutParams = bottomSheetLayoutParams
        bottomSheetDialog.behavior.apply {
            this.skipCollapsed = false
            this.peekHeight = peekHeight
            this.isHideable = true
        };

        //Calculate button margin from top

        //How tall is the button + experimental distance from bottom (Change based on your view)
        bottomViewHeight = bottomView.height + 60
        //Button margin in bottom sheet collapsed state
        collapsedMargin = peekHeight - bottomViewHeight
        bottomViewLayoutParams.topMargin = collapsedMargin
        bottomView.layoutParams = bottomViewLayoutParams

        //OPTIONAL - Setting up recyclerview margins
        val recyclerLayoutParams = rvStoredNames.layoutParams as ConstraintLayout.LayoutParams
        //60 is amount that you want to be hidden behind button
        val k = (bottomViewHeight - 60) / bottomViewHeight.toFloat()
        //Recyclerview bottom margin (from button)
        recyclerLayoutParams.bottomMargin = (k * bottomViewHeight).toInt()
        rvStoredNames.layoutParams = recyclerLayoutParams


    }

    //Calculates height for 90% of fullscreen
    private fun getBottomSheetDialogDefaultHeight(): Int {
        return getWindowHeight() * 90 / 100
    }

    //Calculates window height for fullscreen use
    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (requireContext() as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    companion object {
        const val TAG = "AddNameModalBottomSheet"
    }
}