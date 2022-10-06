package com.mitkoned.prayerneeds.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"]
        )
    ],
    tableName = "prayer_needs",
    indices = [
        Index("user_id")
    ]
)
@Parcelize
data class PrayerNeed(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val description: String,
    val date: Long, // stored as milliseconds
    val answered: Boolean = false,

    val createdAt: Long = Date().toInstant().toEpochMilli(), // stored as milliseconds
    val updatedAt: Long = 0, // stored as milliseconds

    val user_id: Int
) : Parcelable {
    companion object {
        private const val DATE_FORMAT_STRING = "d MMM;yyyy"
        val dateFormat = SimpleDateFormat(DATE_FORMAT_STRING, Locale.US)
    }

    // TODO: needed?
    constructor(description: String, date: Date, answered: Boolean = false, user: User)
        : this(0, description, date.toInstant().toEpochMilli(), answered, user_id = user.id)

    constructor(description: String, date: Date, answered: Boolean = false, user_id: Int)
        : this(0, description, date.toInstant().toEpochMilli(), answered, user_id = user_id)

    val dateConverted: Date
        get() = Date.from(Instant.ofEpochMilli(date))
    val createdAtConverted: Date
        get() = Date.from(Instant.ofEpochMilli(date))
    val updatedAtConverted: Date
        get() = Date.from(Instant.ofEpochMilli(date))
    val dateDefaultFormatted: String

        get() = DateFormat.getDateTimeInstance().format(date)

    val dateCustomFormatted: String
        get() = dateFormat.format(date)

    fun getDateFormatted(format: String, locale: Locale?) =
        SimpleDateFormat(format, locale).format(date)

//    fun setDate(date: Date) {
//        this.date = date.toInstant().toEpochMilli()
//    }
}