package ca.ilianokokoro.umihi.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

@Composable
fun BottomNavigationBar(
    currentTab: NavKey,
    onTabSelected: (NavKey) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab is HomeScreenKey,
            onClick = { onTabSelected(HomeScreenKey) },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Home,
                    contentDescription = null
                )
            },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = currentTab is SearchScreenKey,
            onClick = { onTabSelected(SearchScreenKey) },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Search,
                    contentDescription = null
                )
            },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = currentTab is SettingsScreenKey,
            onClick = { onTabSelected(SettingsScreenKey) },
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Default.Settings,
                    contentDescription = null
                )
            },
            label = { Text("Settings") }
        )
    }
}
