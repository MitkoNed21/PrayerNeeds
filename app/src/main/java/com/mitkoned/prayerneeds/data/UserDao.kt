package com.mitkoned.prayerneeds.data

import androidx.room.*
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface UserDao {

    @Query("select * from users")
    fun getAll(): Flow<List<User>>

    @Query("select * from users where id = :id limit 1")
    fun get(id: Int): Flow<User>

    @Query("select * from users where name like '%' || :name || '%'")
    fun getAll(name: String): Flow<List<User>>

    // TODO: custom filtering?
//    @Query("select * from prayer_needs where :filter")
//    fun getFiltered(filter: Map<String, Any>): Flow<List<PrayerNeed>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}