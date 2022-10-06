package com.mitkoned.prayerneeds.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "users")
@Parcelize
data class User (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val connected: Boolean = false,

    val createdAt: Long = Date().toInstant().toEpochMilli(), // stored as milliseconds
    val updatedAt: Long = 0, // stored as milliseconds
) : Parcelable {

}