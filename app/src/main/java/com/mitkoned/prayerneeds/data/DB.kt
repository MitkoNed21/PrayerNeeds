package com.mitkoned.prayerneeds.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.data.model.User
import com.mitkoned.prayerneeds.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.suspendCoroutine

@Database(
    entities = [PrayerNeed::class, User::class],
    version = 3,
    autoMigrations = [
        AutoMigration (from = 2, to = 3),
//        AutoMigration (from = 1, to = 2)
    ]
)
abstract class DB : RoomDatabase() {

    abstract fun prayerNeedDao(): PrayerNeedDao
    abstract fun userDao(): UserDao

    class Callback @Inject constructor(
        private val db: Provider<DB>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val prayerNeedDao = this.db.get().prayerNeedDao()
            val userDao = this.db.get().userDao()

            applicationScope.launch {
                userDao.insert(User(name = "Ivan Ivanov"))

                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST!",
                        date = Date().toInstant().toEpochMilli(),
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 2!",
                        date = Date().toInstant().plusSeconds(60000).toEpochMilli(),
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 3!",
                        date = Date().toInstant().plusSeconds(120000).toEpochMilli(),
                        answered = true,
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 4!",
                        date = Date().toInstant().plusSeconds(42000).toEpochMilli(),
                        user_id = 1
                    )
                )

                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST!",
                        date = Date().toInstant().toEpochMilli(),
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 2!",
                        date = Date().toInstant().plusSeconds(60000).toEpochMilli(),
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 3!",
                        date = Date().toInstant().plusSeconds(120000).toEpochMilli(),
                        answered = true,
                        user_id = 1
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 4!",
                        date = Date().toInstant().plusSeconds(42000).toEpochMilli(),
                        user_id = 1
                    )
                )

            }
        }
    }
}