package com.mitkoned.prayerneeds.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mitkoned.prayerneeds.entities.PrayerNeed
import com.mitkoned.prayerneeds.entities.User

@Dao
interface PrayerNeedDao {
    @Insert
    fun insert(prayerNeed: PrayerNeed)

    @Update
    fun update(prayerNeed: PrayerNeed)

    @Delete
    fun delete(prayerNeed: PrayerNeed)

    @Query("SELECT * FROM prayer_needs ORDER BY year DESC, date DESC")
    fun getAll(): LiveData<List<PrayerNeed>>

    @Query("SELECT * FROM prayer_needs WHERE id == :id LIMIT 1")
    fun getById(id: Int): LiveData<PrayerNeed>

    @Query("SELECT * FROM prayer_needs pn JOIN users u ON pn.user_id == u.id WHERE u.id == :userId")
    fun getPrayerNeedsOfUser(userId: Int)

    // Maybe impossible
    //@Query("SELECT * FROM prayer_needs WHERE :expression")
    //fun <TKey> get(expression: Function<PrayerNeed, Boolean>): List<PrayerNeed>
}