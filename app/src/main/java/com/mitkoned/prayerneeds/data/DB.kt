package com.mitkoned.prayerneeds.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mitkoned.prayerneeds.data.model.PrayerNeed
import com.mitkoned.prayerneeds.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.suspendCoroutine

@Database(entities = [PrayerNeed::class], version = 1)
abstract class DB : RoomDatabase() {

    abstract fun prayerNeedDao(): PrayerNeedDao

    class Callback @Inject constructor(
        private val db: Provider<DB>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val prayerNeedDao = this.db.get().prayerNeedDao()

            applicationScope.launch {
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST!",
                        date = Date().toInstant().toEpochMilli())
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 2!",
                        date = Date().toInstant().plusSeconds(60000).toEpochMilli()
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 3!",
                        date = Date().toInstant().plusSeconds(120000).toEpochMilli(),
                        answered = true
                    )
                )
                prayerNeedDao.insert(
                    PrayerNeed(
                        description = "TEST 4!",
                        date = Date().toInstant().plusSeconds(42000).toEpochMilli()
                    )
                )
            }
        }
    }
}