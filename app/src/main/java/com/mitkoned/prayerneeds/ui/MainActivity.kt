package com.mitkoned.prayerneeds.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.ui.prayerneeds.PrayerNeedsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_prayer_needs.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Should not be like so?...
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        val fragments = navHostFragment.childFragmentManager.fragments.toMutableList()

        while (fragments.size > 0) {
            when (val fragment = fragments.removeAt(0)) {
                is PrayerNeedsFragment -> {
                    val prayerNeedsPersonMenuProvider = PersonPrayerNeedsMenuProvider(fragment.viewModel)

//                    this.lifecycleScope.launch {
//                        var text = ""
//                        text = fragment.viewModel.preferencesFlow.first().testPref
//                        val count = fragment.viewModel.preferencesFlow.count()

//                        text = count.toString()
//                        Toast.makeText(this@MainActivity, text, Toast.LENGTH_SHORT).show()
//                    }

                    addMenuProvider(
                        prayerNeedsPersonMenuProvider,
                        this,
                        Lifecycle.State.STARTED
                    )
                }
                else -> {
                    Toast.makeText(
                        this,
                        "Warning! unrecognized fragment ${fragment.id}, viewId: ${fragment.view?.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}