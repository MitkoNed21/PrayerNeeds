package com.mitkoned.prayerneeds.ui

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.ui.viewmodels.PrayerNeedViewModel

class PersonPrayerNeedsMenuProvider(
    val viewModel: PrayerNeedViewModel
): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

//        val appBarLayout = View.inflate(
//            requireContext(), R.layout.layout__top_bar_all_prayer_needs, null
//        )
//
//        activity?.actionBar?.apply {
//            setDisplayShowCustomEnabled(true)
//            setDisplayShowTitleEnabled(false)
//
//            appBarLayout.layoutParams = ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.WRAP_CONTENT
//            )
//            customView = appBarLayout
//        }
//
//        appBarLayout.apply {
//            tv_title.apply {
//                text = activity?.title
//                textSize = 26f
//                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
//            }
//            // TODO: Try .visibility = View.GONE OR View.INVISIBLE
//            tv_currentDate.text = ""
//        }
//
//        activity?.actionBar?.customView = appBarLayout

        menuInflater.inflate(R.menu.menu_appbar_person_prayerneeds, menu)
        //this.setFeatureDrawable() <- research
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.mi_toggleAnsweredPrayerNeedsVisibility -> {
//                val adapter = this.rv_prayerNeeds_fr.adapter

                if (viewModel.hideAnsweredPrayerNeeds.value) {
                    menuItem.setIcon(R.drawable.ic_hide_answered_prayer_needs)

                    viewModel.hideAnsweredPrayerNeeds.value = false
                } else {

                    menuItem.setIcon(R.drawable.ic_show_answered_prayer_needs)

                    viewModel.hideAnsweredPrayerNeeds.value = true
                }
            }
        }

        return true
    }
}