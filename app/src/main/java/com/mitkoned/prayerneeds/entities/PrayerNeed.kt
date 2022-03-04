package com.mitkoned.prayerneeds.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_needs")
data class PrayerNeed (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @NonNull
    var description: String,
    @NonNull
    val year: String,
    @NonNull
    val date: String,
    @NonNull
    var answered: Boolean,
    @ColumnInfo(name = "user_id")
    val userId: Int
)