package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.ilianokokoro.umihi.music.BuildConfig
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.isNullOrInvalidId
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.AUTO_UPDATE
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.COOKIES
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.DATA_SYNC_ID
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.KEEP_SCREEN_ON
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.SEND_PLAYBACK_DATA
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.SHOW_PODCAST_PLAYLIST
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.UPDATE_CHANNEL
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.USE_AUDIO_OFFLOAD
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.USE_SPECIAL_LANGUAGE
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.Datastore.NAME)

class DatastoreRepository(private val context: Context) {
    object PreferenceKeys {
        val COOKIES = stringPreferencesKey(Constants.Datastore.COOKIES_KEY)
        val DATA_SYNC_ID = stringPreferencesKey(Constants.Datastore.DATA_SYNC_ID)
        val UPDATE_CHANNEL = stringPreferencesKey(Constants.Datastore.UPDATE_CHANNEL_KEY)
        val SHOW_PODCAST_PLAYLIST = booleanPreferencesKey(Constants.Datastore.SHOW_PODCAST_PLAYLIST)
        val USE_SPECIAL_LANGUAGE = booleanPreferencesKey(Constants.Datastore.USE_SPECIAL_LANGUAGE)
        val USE_AUDIO_OFFLOAD = booleanPreferencesKey(Constants.Datastore.USE_AUDIO_OFFLOAD)
        val KEEP_SCREEN_ON = booleanPreferencesKey(Constants.Datastore.KEEP_SCREEN_ON)
        val AUTO_UPDATE = booleanPreferencesKey(Constants.Datastore.AUTO_UPDATE)
        val SEND_PLAYBACK_DATA = booleanPreferencesKey(Constants.Datastore.SEND_PLAYBACK_DATA)
    }

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    val settings = context.dataStore.data.map {
        val updateChannel = it[UPDATE_CHANNEL]?.let { value -> UpdateChannel.valueOf(value) }
            ?: if (BuildConfig.IS_BETA) {
                UpdateChannel.Beta
            } else {
                UpdateChannel.Stable
            }
        val showPodcastPlaylist = it[SHOW_PODCAST_PLAYLIST] ?: true
        val useSpecialLanguage = it[USE_SPECIAL_LANGUAGE] ?: false
        val useAudioOffload = it[USE_AUDIO_OFFLOAD] ?: false
        val keepScreenOn = it[KEEP_SCREEN_ON] ?: false
        val updateChecking = it[AUTO_UPDATE] ?: true
        val sendPlaybackData = it[SEND_PLAYBACK_DATA] ?: false
        val cookies = cookies.first()
        val dataSyncId = dataSyncId.first()


        UmihiSettings(
            updateChannel = updateChannel,
            showPodcastPlaylist = showPodcastPlaylist,
            cookies = cookies,
            dataSyncId = dataSyncId,
            useSpecialLanguage = useSpecialLanguage,
            useAudioOffload = useAudioOffload,
            keepScreenOn = keepScreenOn,
            sendPlaybackData = sendPlaybackData,
            updateChecking = updateChecking
        )
    }

    suspend fun getSettings(): UmihiSettings {
        return settings.first()
    }

    val cookies = context.dataStore.data.map {
        Cookies(it[COOKIES] ?: String())
    }

    val dataSyncId: Flow<String?> = flow {
        context.dataStore.data.collect { prefs ->
            val id = prefs[DATA_SYNC_ID]
            if (id.isNullOrInvalidId()) {
                if (id != null) {
                    context.dataStore.edit { it.remove(DATA_SYNC_ID) }
                }
                emit(null)
            } else {
                emit(id)
            }
        }
    }

    suspend fun saveCookies(cookies: Cookies) {
        context.dataStore.edit {
            it[COOKIES] = cookies.toRawCookie()
        }
    }

    suspend fun logOut() {
        saveCookies(Cookies())
        saveDataSyncId("")
    }

    suspend fun saveDataSyncId(newId: String) {
        if (newId.isNullOrInvalidId()) {
            context.dataStore.edit { it.remove(DATA_SYNC_ID) }
            return
        }
        context.dataStore.edit {
            it[DATA_SYNC_ID] = newId
        }
    }

    suspend fun debugPrintAllPreferences() {
        val prefs = context.dataStore.data.first()
        LogHelper.printd("=== All preferences ===")
        prefs.asMap().forEach { (key, value) ->
            LogHelper.printd("  $key = $value")
        }
        LogHelper.printd("========================")
    }


    enum class UpdateChannel {
        Stable,
        Beta
    }

}