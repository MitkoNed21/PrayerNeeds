package com.mitkoned.prayerneeds.ui.prayerneeds

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mitkoned.prayerneeds.PrayerNeedSwipeGestures
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.adapters.PrayerNeedsAdapter
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.databinding.FragmentPrayerNeedsBinding
import com.mitkoned.prayerneeds.ui.viewmodels.PrayerNeedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_prayer_needs.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PrayerNeedsFragment :
    Fragment(R.layout.fragment_prayer_needs),
    PrayerNeedsAdapter.OnItemClickListener,
    PrayerNeedsAdapter.OnItemRangeInsertedListener {

    val viewModel: PrayerNeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPrayerNeedsBinding.bind(view)

        val prayerNeedAdapter = PrayerNeedsAdapter(this, this)
        binding.apply {
            rvPrayerNeedsFr.apply {
                adapter = prayerNeedAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        val swipeGestures = object: PrayerNeedSwipeGestures(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                super.onSwiped(viewHolder, direction)

                val adapterPosition = viewHolder.adapterPosition
                val currentPrayerNeed = prayerNeedAdapter.currentList[adapterPosition]

                viewModel.onPrayerNeedSwiped(currentPrayerNeed, direction)
                prayerNeedAdapter.notifyItemChanged(adapterPosition)
            }
        }

        btn_addPrayerNeed_fr.setOnClickListener {
            viewModel.onAddClick(et_addPrayerNeed_prayerNeedText_fr.text.toString())
            et_addPrayerNeed_prayerNeedText_fr.setText("")

        }

        ItemTouchHelper(swipeGestures).attachToRecyclerView(rv_prayerNeeds_fr)

        viewModel.prayerNeeds.observe(viewLifecycleOwner) {
            prayerNeedAdapter.submitList(it)
        }

//        val pref = viewModel.preferencesFlow.asLiveData().value
//        if (pref?.lastDataUpdate?.after(Date()) == null ? true : pref?.lastDataUpdate?.after(Date())!!) {
//
//        }

        viewLifecycleOwner.lifecycleScope.launch { // TODO: maybe try with ...RESUMED
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prayerNeedsEvent.collect { event ->
                    when (event) {
                        is PrayerNeedViewModel.PrayerNeedsEvent.ShowConfirmDeletePrayerNeedMessage -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Remove prayer need")
                                .setPositiveButton("Yes") { _, _ ->
                                    viewModel.onConfirmDeleteClick(event.prayerNeed)
                                }
                                .setCancelable(true)
                                .setNegativeButton("No", null)
                                .setMessage("Do you want to remove\n\"${event.prayerNeed.description}\"?")
                                .create()
                                .show()
                        }
                        is PrayerNeedViewModel.PrayerNeedsEvent.ShowUndoDeletePrayerNeedMessage -> {
                            Snackbar.make(
//                                activity?.findViewById(android.R.id.content)!!,
//                                requireActivity().findViewById(R.id.rv_prayerNeeds_fr),
                                rv_prayerNeeds_fr,
                                //viewHolder.itemView.parent as RecyclerView,
                                "Item removed.", //removedPrayerNeed + " was removed.",
                                5000
                            ).apply {
                                //setBackgroundTintList(Color.parseColor("#808080"))
                                // TODO: Make a layout for the Snackbar
                                setBackgroundTint(Color.parseColor("#202020"))
                                setActionTextColor(Color.parseColor("#F0F0F0"))
                                setTextColor(Color.parseColor("#F0F0F0"))

                                setAction("Undo") { // TODO: if names added today don't contain the name.
                                    viewModel.onUndoDeleteClick(event.prayerNeed)
                                }
                            }.show()
                        }
                        is PrayerNeedViewModel.PrayerNeedsEvent.ShowEditPrayerNeedView -> {
                            val etPrayerNeedsText = et_addPrayerNeed_prayerNeedText_fr

                            val tempAddTextValue = etPrayerNeedsText.text.toString()

                            val clPrayerNeedsTextParent = etPrayerNeedsText.parent as ConstraintLayout
                            val layoutParams = etPrayerNeedsText.layoutParams

                            clPrayerNeedsTextParent.removeView(etPrayerNeedsText)
                            btn_addPrayerNeed_fr.visibility = View.INVISIBLE

                            AlertDialog.Builder(requireContext())
                                .setPositiveButton("Save") { _, _ ->
                                    viewModel.onSaveClick(event.prayerNeed, etPrayerNeedsText.text.toString())
                                }
                                .setNegativeButton("Cancel", null)
                                .setTitle("Edit")
                                .setOnDismissListener {
                                    (etPrayerNeedsText.parent as ViewGroup).removeView(etPrayerNeedsText)
                                    clPrayerNeedsTextParent.addView(etPrayerNeedsText, layoutParams)
                                    btn_addPrayerNeed_fr.visibility = View.VISIBLE
                                    etPrayerNeedsText.setText(tempAddTextValue)
                                    //TODO: Temporary, change with resources
                                    //etPrayerNeedsText.backgroundTintList =
                                    //    ColorStateList.valueOf(Color.parseColor("#242424"))
                                }
                                .setIcon(AppCompatResources.getDrawable(
                                    requireContext(), R.drawable.ic_edit_prayer_need)
                                )
                                .setView(etPrayerNeedsText.apply {
                                    this.setText(event.prayerNeed.description)
                                }).create().apply {
                                    setOnShowListener {
                                        (etPrayerNeedsText.parent as ViewGroup).setPadding(20, 48, 20, 16)
                                        //TODO: Temporary, change with resources
                                        //etPrayerNeedsText.backgroundTintList =
                                        //    ColorStateList.valueOf(Color.parseColor("#303030"))
                                    }
                                    show()
                                }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    override fun onItemClick(prayerNeed: PrayerNeed) {
        viewModel.onPrayerNeedClicked(prayerNeed)
    }

    override fun onCheckBoxClick(prayerNeed: PrayerNeed, isChecked: Boolean) {
        viewModel.onPrayerNeedAnsweredChanged(prayerNeed, isChecked)
    }

    override fun onItemRangeInserted() {
        rv_prayerNeeds_fr.scrollToPosition(0)
    }
}