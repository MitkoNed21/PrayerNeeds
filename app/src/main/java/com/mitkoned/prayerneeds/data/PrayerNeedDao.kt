package com.mitkoned.prayerneeds.data

import androidx.room.*
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerNeedDao {

    @Query("select * from prayer_needs")
    fun getAll() : Flow<List<PrayerNeed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayerNeed: PrayerNeed)

    @Update
    suspend fun update(prayerNeed: PrayerNeed)

    @Delete
    suspend fun delete(prayerNeed: PrayerNeed)
}