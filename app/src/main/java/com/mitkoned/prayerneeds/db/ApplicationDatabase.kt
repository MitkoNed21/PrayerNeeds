package com.mitkoned.prayerneeds.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mitkoned.prayerneeds.dao.PrayerNeedDao
import com.mitkoned.prayerneeds.dao.UserDao
import com.mitkoned.prayerneeds.entities.PrayerNeed
import com.mitkoned.prayerneeds.entities.User

@Database(entities = [PrayerNeed::class, User::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun prayerNeedDao(): PrayerNeedDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: ApplicationDatabase? = null

        fun getDatabase(context: Context): ApplicationDatabase {
//            if (instance != null) return instance!!
//
//            return synchronized(this) {
//                Room.databaseBuilder(
//                    context.applicationContext,
//                    ApplicationDatabase::class.java,
//                    "prayer_needs_app_database"
//                ).build().apply {
//                    instance = this
//                    return@synchronized this
//                }
//            }
            if (instance == null) {
                instance = synchronized(this) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        ApplicationDatabase::class.java,
                        "prayer_needs_app_database"
                    ).build()
                }
            }
            return instance!!
        }
    }
}