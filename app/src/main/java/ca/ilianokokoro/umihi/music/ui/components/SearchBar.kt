package ca.ilianokokoro.umihi.music.ui.components

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import ca.ilianokokoro.umihi.music.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester? = null,
    focusManager: FocusManager? = null,
) {
    val textFieldState = rememberTextFieldState(value)
    val searchBarState = rememberContainedSearchBarState()

    val appBarWithSearchColors =
        SearchBarDefaults.appBarWithSearchColors(
            searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
        )

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { newText ->
                onValueChange(newText)
            }
    }

    val searchModifier = modifier
        .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }

    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
        onSearch = {
            focusManager?.clearFocus()
            onSearch()
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search,
                contentDescription = Icons.Rounded.Search.name
            )
        },
        modifier = searchModifier,
    )
}
