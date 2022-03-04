package com.mitkoned.prayerneeds.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mitkoned.prayerneeds.entities.PrayerNeed
import com.mitkoned.prayerneeds.entities.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

    @Query("SELECT * FROM users ORDER BY name")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id == :id LIMIT 1")
    fun getById(id: Int): LiveData<User>

    @Query("SELECT * FROM users u JOIN prayer_needs pn ON u.id == pn.user_id")
    fun getUserAndPrayerNeeds(): Map<User, List<PrayerNeed>>
}