package ca.ilianokokoro.umihi.music.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys.COOKIES
import ca.ilianokokoro.umihi.music.models.Cookies
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.Datastore.NAME)

class DatastoreRepository(private val context: Context) {
    object PreferenceKeys {
        val COOKIES = stringPreferencesKey(Constants.Datastore.COOKIES_KEY)
    }

    val cookies = context.dataStore.data.map {
        (it[COOKIES] ?: "")

    }

    suspend fun saveCookies(cookies: Cookies) {
        context.dataStore.edit {
            it[COOKIES] = cookies.toRawCookie()
        }
    }


    fun getCookies(): Cookies {
        return runBlocking {
            return@runBlocking Cookies(cookies.first())
        }
    }

}