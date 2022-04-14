package com.mitkoned.prayerneeds

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "prayer_needs")
@Parcelize
data class PrayerNeed(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val description: String,
    val date: Long, // stored as milliseconds
    val answered: Boolean = false
) : Parcelable {
    companion object {
        private const val DATE_FORMAT_STRING = "d MMM;yyyy"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_STRING, Locale.US)
    }

    val dateDefaultFormatted: String
        get() = DateFormat.getDateTimeInstance().format(date)

    val dateCustomFormatted: String
        get() = dateFormat.format(date)

    fun getDateFormatted(format: String, locale: Locale?) =
        SimpleDateFormat(format, locale).format(date)
}