package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.COOKIES
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.DATA_SYNC_ID
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.UPDATE_CHANNEL
import ca.ilianokokoro.umihi.music.models.Cookies
import ca.ilianokokoro.umihi.music.models.UmihiSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.Datastore.NAME)

class DatastoreRepository(private val context: Context) {
    object PreferenceKeys {
        val COOKIES = stringPreferencesKey(Constants.Datastore.COOKIES_KEY)
        val DATA_SYNC_ID = stringPreferencesKey(Constants.Datastore.DATA_SYNC_ID)
        val UPDATE_CHANNEL = stringPreferencesKey(Constants.Datastore.UPDATE_CHANNEL_KEY)

    }

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    val settings = context.dataStore.data.map {
        val updateChannel = it[UPDATE_CHANNEL]?.let { value -> UpdateChannel.valueOf(value) }
            ?: UpdateChannel.Stable
        val cookies = cookies.first()
        val dataSyncId = dataSyncId.first()

        UmihiSettings(updateChannel = updateChannel, cookies = cookies, dataSyncId = dataSyncId)
    }

    fun getSettings(): UmihiSettings {
        return runBlocking {
            settings.first()
        }
    }

    val cookies = context.dataStore.data.map {
        Cookies(it[COOKIES] ?: "")
    }

    val dataSyncId = context.dataStore.data.map {
        it[DATA_SYNC_ID] ?: ""
    }

    suspend fun saveCookies(cookies: Cookies) {
        context.dataStore.edit {
            it[COOKIES] = cookies.toRawCookie()
        }
    }

    fun getCookies(): Cookies {
        return runBlocking {
            cookies.first()
        }
    }

    suspend fun saveDataSyncId(newId: String) {
        context.dataStore.edit {
            it[DATA_SYNC_ID] = newId
        }
    }

    fun getDataSyncId(): String {
        return runBlocking {
            dataSyncId.first()
        }
    }

    enum class UpdateChannel {
        Stable,
        Beta
    }

}