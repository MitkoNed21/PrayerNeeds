package com.mitkoned.prayerneeds.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.mitkoned.prayerneeds.data.PrayerNeedDao
import com.mitkoned.prayerneeds.data.PreferencesManager
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val BULGARIAN_ACCENTED_I_REPLACEMENT_REGEX = "(?<![A-Za-z0-9А-Яа-я_])й(?![A-Za-z0-9А-Яа-я_])"

@HiltViewModel
class PrayerNeedViewModel @Inject constructor(
    private val prayerNeedDao: PrayerNeedDao,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    // TODO: Test maybe revert to BoxedBoolean
    val hideAnsweredPrayerNeeds = MutableStateFlow(false)
    val preferencesFlow = preferencesManager.preferencesFlow

    private val prayerNeedsEventsChannel = Channel<PrayerNeedsEvent>()
    val prayerNeedsEvent = prayerNeedsEventsChannel.receiveAsFlow()

    @ExperimentalCoroutinesApi
    private val prayerNeedsFlow = combine(
        hideAnsweredPrayerNeeds,
        preferencesFlow
    ) { hideAnswered, preferencesFlow ->
        Pair(hideAnswered, preferencesFlow)
    }
        .flatMapLatest { (hideAnswered, _) ->
            if (hideAnswered)
                prayerNeedDao.getAllUnansweredForUser(1)
            else
                prayerNeedDao.getAllForUser(1)
        }

//    @ExperimentalCoroutinesApi
//    val prayerNeeds = hideAnsweredPrayerNeeds.flatMapLatest {
//        if (it)
//            prayerNeedDao.getAllUnansweredForUser(1)
//        else
//            prayerNeedDao.getAllForUser(1)
//    }.asLiveData()

//    @ExperimentalCoroutinesApi
//    val prayerNeeds = combine(
//            hideAnsweredPrayerNeeds,
//            hideAnsweredPrayerNeeds
//        ) { hideAnswered, _ ->
//            hideAnswered
//        }.flatMapLatest { hideAnswered ->
//            if (hideAnswered)
//                prayerNeedDao.getAllUnansweredForUser(1)
//            else
//                prayerNeedDao.getAllForUser(1)
//        }.asLiveData()

    @ExperimentalCoroutinesApi
    val prayerNeeds = prayerNeedsFlow.asLiveData()


    fun onLastDataUpdateChanged(lastDataUpdate: Date) = viewModelScope.launch {
        preferencesManager.updateLastDataUpdate(lastDataUpdate)
    }

    fun onTestPrefChanged(text: String) = viewModelScope.launch {
        preferencesManager.updateTestPref(text)
    }

    fun onPrayerNeedClicked(prayerNeed: PrayerNeed) {
        //
    }

    fun onPrayerNeedAnsweredChanged(prayerNeed: PrayerNeed, isAnswered: Boolean) = viewModelScope.launch {
        prayerNeedDao.update(prayerNeed.copy(answered = isAnswered))
    }

    /*
    *   TODO: Callback when undo hit. : Do something?
    *     also: When not hit then delete
    * */

    fun onPrayerNeedSwiped(prayerNeed: PrayerNeed, direction: Int) = viewModelScope.launch {
        when (direction) {
            ItemTouchHelper.LEFT -> {
                prayerNeedsEventsChannel.send(PrayerNeedsEvent.ShowConfirmDeletePrayerNeedMessage(prayerNeed))
            }
            ItemTouchHelper.RIGHT -> {
                prayerNeedsEventsChannel.send(PrayerNeedsEvent.ShowEditPrayerNeedView(prayerNeed))
            }
        }
    }

    fun onAddClick(enteredText: String) = viewModelScope.launch {
        val description = enteredText.trim()
        if (description == "") cancel()

        // TODO: Shared preference option on whether to do so
        val finalDescription = description.replace(Regex(BULGARIAN_ACCENTED_I_REPLACEMENT_REGEX), "ѝ")

        prayerNeedDao.insert(PrayerNeed(
            description = finalDescription,
            date = Date().toInstant().toEpochMilli(),
            user_id = 1 // TODO: get from fragmentData?
        ))
    }

    fun onSaveClick(prayerNeed: PrayerNeed, newDescription: String) = viewModelScope.launch {
        val description = newDescription.trim()
        if (description == "") cancel()

        // TODO: Shared preference option on whether to do so
        val finalDescription = description.replace(Regex(BULGARIAN_ACCENTED_I_REPLACEMENT_REGEX), "ѝ")

        prayerNeedDao.update(prayerNeed.copy(
            description = finalDescription,
            updatedAt = Date().toInstant().toEpochMilli()
        ))
    }

    fun onConfirmDeleteClick(prayerNeed: PrayerNeed) = viewModelScope.launch {
        prayerNeedDao.delete(prayerNeed)
        prayerNeedsEventsChannel.send(PrayerNeedsEvent.ShowUndoDeletePrayerNeedMessage(prayerNeed))
    }

    fun onUndoDeleteClick(prayerNeed: PrayerNeed) = viewModelScope.launch {
        prayerNeedDao.insert(prayerNeed)
    }

    sealed class PrayerNeedsEvent {
        data class ShowConfirmDeletePrayerNeedMessage(val prayerNeed: PrayerNeed) : PrayerNeedsEvent()
        data class ShowUndoDeletePrayerNeedMessage(val prayerNeed: PrayerNeed) : PrayerNeedsEvent()
        data class ShowEditPrayerNeedView(val prayerNeed: PrayerNeed) : PrayerNeedsEvent()
    }
}