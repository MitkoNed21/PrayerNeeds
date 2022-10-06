package com.mitkoned.prayerneeds.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val Context.dataStore by preferencesDataStore("app_preferences")

    private object Keys {
        val TEST_PREF = stringPreferencesKey("test_pref")
        val LAST_DATA_UPDATE = longPreferencesKey("last_data_update")

    }

    val preferencesFlow = context.dataStore.data
        .catch { ex ->
            if (ex is IOException) {
                Log.e(TAG, "Error reading prefs", ex)
                emit(emptyPreferences())
            } else {
                Log.e(TAG, ex.message.toString())
            }
        }
        .map { preferences ->
            Log.i(TAG, "Successfully read preferences")

//            Log.i(TAG, "${context.dataStore.data.count()}")
            Log.i(TAG, "Has preferences: " + (preferences).toString())

//            preferences.

            preferences?.toPreferences()

            val testPref = preferences[Keys.TEST_PREF]
            val lastDataUpdatePref = preferences[Keys.LAST_DATA_UPDATE]

            val lastDataUpdate =
                if (lastDataUpdatePref != null) {
                    Date.from(
                        Instant.ofEpochMilli(lastDataUpdatePref)
                    )
                }
                else Date()

            val res = PreferencesData(testPref, lastDataUpdate)

            Log.i(TAG, res.toString())

            res
        }

    suspend fun updateTestPref(text: String) {
        context.dataStore.edit {
            it[Keys.TEST_PREF] = text
        }
    }

    suspend fun updateLastDataUpdate(lastDataUpdate: Date) {
        context.dataStore.edit {
            it.set(Keys.LAST_DATA_UPDATE, lastDataUpdate.toInstant().toEpochMilli())
//            it[Keys.LAST_DATA_UPDATE] = lastDataUpdate.toInstant().toEpochMilli()
        }
    }
}

class PreferencesData(val testPref: String?, val lastDataUpdate: Date) {
    override fun toString(): String {
        return "    testPref: $testPref\n    lastDataUpdate: $lastDataUpdate"
    }
}