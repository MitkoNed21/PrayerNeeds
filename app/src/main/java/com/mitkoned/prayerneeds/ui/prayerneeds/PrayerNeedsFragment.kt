package com.mitkoned.prayerneeds.ui.prayerneeds

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mitkoned.prayerneeds.R
import com.mitkoned.prayerneeds.adapters.PrayerNeedAdapter
import com.mitkoned.prayerneeds.adapters.PrayerNeedsAdapter
import com.mitkoned.prayerneeds.databinding.FragmentPrayerNeedsBinding
import com.mitkoned.prayerneeds.ui.viewmodels.PrayerNeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrayerNeedsFragment : Fragment(R.layout.fragment_prayer_needs) {

    private val viewModel: PrayerNeedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPrayerNeedsBinding.bind(view)

        val prayerNeedAdapter = PrayerNeedsAdapter()
        binding.apply {
            rvPrayerNeedsFr.apply {
                adapter = prayerNeedAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

        viewModel.prayerNeeds.observe(viewLifecycleOwner) {
            prayerNeedAdapter.submitList(it)
        }
    }
}