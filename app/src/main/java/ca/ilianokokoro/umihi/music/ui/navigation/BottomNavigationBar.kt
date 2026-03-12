package ca.ilianokokoro.umihi.music.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.PreferenceKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BottomNavigationBar(
    currentTab: NavKey?,
    onTabSelected: (NavKey) -> Unit
) {
    val clickCount = remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val datastoreRepository = remember { DatastoreRepository(context) }
    val resources = LocalResources.current

    NavigationBar {
        NavigationBarItem(
            selected = currentTab is HomeScreenKey,
            onClick = {
                onTabSelected(HomeScreenKey)
                clickCount.intValue = 0
            },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.home)) }
        )
        NavigationBarItem(
            selected = currentTab is SearchScreenKey,
            onClick = {
                onTabSelected(SearchScreenKey)
                clickCount.intValue = 0
            },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.search)) }
        )
        NavigationBarItem(
            selected = currentTab is SettingsScreenKey,
            onClick = {
                onTabSelected(SettingsScreenKey)
                clickCount.intValue++
                if (clickCount.intValue == Constants.Locale.Special.CLICK_QUANTITY) {
                    scope.launch {
                        val oldSettings = datastoreRepository.settings.first()
                        datastoreRepository.save(
                            PreferenceKeys.USE_SPECIAL_LANGUAGE,
                            !oldSettings.useSpecialLanguage
                        )

                        Toast.makeText(
                            context,
                            if (oldSettings.useSpecialLanguage) resources.getString(R.string.special_language_disabled)
                            else resources.getString(R.string.special_language_enabled),
                            Toast.LENGTH_LONG
                        ).show()
                        (context as Activity).recreate()
                    }

                }
            },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Settings,
                    contentDescription = null
                )
            },
            label = { Text(stringResource(R.string.settings)) }
        )
    }
}
