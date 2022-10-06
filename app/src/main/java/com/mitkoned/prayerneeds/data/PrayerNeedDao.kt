package com.mitkoned.prayerneeds.data

import androidx.room.*
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerNeedDao {

    @Query("select * from prayer_needs")
    fun getAll(): Flow<List<PrayerNeed>>

    @Query("select * from prayer_needs where id = :id limit 1")
    fun get(id: Int): Flow<PrayerNeed>

    @Query("select * from prayer_needs order by date desc")
    fun getAllSortedByDate(): Flow<List<PrayerNeed>>

    @Query(
        "select * from prayer_needs where user_id = :user_id order by date desc"
    )
    fun getAllForUser(user_id: Int): Flow<List<PrayerNeed>>

    @Query(
        "select * from prayer_needs where user_id = :user_id AND answered order by date desc"
    )
    fun getAllAnsweredForUser(user_id: Int): Flow<List<PrayerNeed>>

    @Query(
        "select * from prayer_needs where user_id = :user_id AND not answered order by date desc"
    )
    fun getAllUnansweredForUser(user_id: Int): Flow<List<PrayerNeed>>

    @Query( //TODO: fix, not working datetime(,,...) returns null
        "select * from prayer_needs where user_id = :user_id AND date >= datetime('now', 'start of day', 'unixepoch') * 1000 order by date"
    )
    fun getAllTodaysForUser(user_id: Int): Flow<List<PrayerNeed>>

    // TODO: custom filtering?
//    @Query("select * from prayer_needs where :filter")
//    fun getFiltered(filter: Map<String, Any>): Flow<List<PrayerNeed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prayerNeed: PrayerNeed)

    @Update
    suspend fun update(prayerNeed: PrayerNeed)

    @Delete
    suspend fun delete(prayerNeed: PrayerNeed)
}