package com.mitkoned.prayerneeds.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.mitkoned.prayerneeds.data.PrayerNeedDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrayerNeedViewModel @Inject constructor(
    private val prayerNeedDao: PrayerNeedDao
) : ViewModel() {
    val prayerNeeds = prayerNeedDao.getAll().asLiveData()
}