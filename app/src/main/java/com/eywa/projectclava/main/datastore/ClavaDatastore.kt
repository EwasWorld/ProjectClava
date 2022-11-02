package com.eywa.projectclava.main.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.eywa.projectclava.main.common.asCalendar
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "clava_user_preferences"

// TODO_HACKY Use dependency injection?
val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

class ClavaDatastore(private val dataStore: DataStore<Preferences>) {
    fun getPreferences() = dataStore.data.map { preferences ->
        var state = DatastoreState()
        preferences[DatastoreKeys.OVERRUN_INDICATOR_THRESHOLD]?.let {
            state = state.copy(overrunIndicatorThreshold = it)
        }
        preferences[DatastoreKeys.DEFAULT_MATCH_TIME]?.let {
            state = state.copy(defaultMatchTime = it)
        }
        preferences[DatastoreKeys.DEFAULT_TIME_TO_ADD]?.let {
            state = state.copy(defaultTimeToAdd = it)
        }
        preferences[DatastoreKeys.CLUB_NIGHT_START_TIME]?.let {
            state = state.copy(clubNightStartTime = it.asCalendar()!!, isDefaultClubNightStartTime = false)
        }
        preferences[DatastoreKeys.PREPEND_COURT]?.let {
            state = state.copy(prependCourt = it)
        }
        state
    }

    suspend fun handle(action: DataStoreIntent, currentState: DatastoreState) {
        when (action) {
            is DataStoreIntent.UpdateClubNightStartTime -> {
                handle(
                        DataStoreIntent.UpdateClubNightStartTimeCalendar(
                                action.value.updateCalendar(currentState.clubNightStartTime)
                        ),
                        currentState,
                )
            }
            else -> dataStore.edit {
                when (action) {
                    is DataStoreIntent.UpdateClubNightStartTimeCalendar ->
                        it[DatastoreKeys.CLUB_NIGHT_START_TIME] = action.value.timeInMillis
                    is DataStoreIntent.UpdateDefaultMatchTime ->
                        it[DatastoreKeys.DEFAULT_MATCH_TIME] = action.value
                    is DataStoreIntent.UpdateDefaultTimeToAdd ->
                        it[DatastoreKeys.DEFAULT_TIME_TO_ADD] = action.value
                    is DataStoreIntent.UpdateOverrunIndicatorThreshold ->
                        it[DatastoreKeys.OVERRUN_INDICATOR_THRESHOLD] = action.value
                    is DataStoreIntent.TogglePrependCourt ->
                        it[DatastoreKeys.PREPEND_COURT] = !currentState.prependCourt
                    DataStoreIntent.ClearDatastore -> it.clear()
                    else -> throw NotImplementedError()
                }
            }
        }
    }

    private object DatastoreKeys {
        val OVERRUN_INDICATOR_THRESHOLD = intPreferencesKey("overrun_indicator_threshold")
        val DEFAULT_MATCH_TIME = intPreferencesKey("default_match_time")
        val DEFAULT_TIME_TO_ADD = intPreferencesKey("default_time_to_add")
        val CLUB_NIGHT_START_TIME = longPreferencesKey("club_night_start_time")
        val PREPEND_COURT = booleanPreferencesKey("prepend_court")
    }
}