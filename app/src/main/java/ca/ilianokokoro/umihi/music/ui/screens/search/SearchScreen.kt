package ca.ilianokokoro.umihi.music.ui.screens.search

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarWithGapState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.addtoplaylist.AddToPlaylistBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.materialu.dropdown.MaterialUDropdownItem
import ca.ilianokokoro.umihi.music.ui.components.song.SongListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    application: Application,
    searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(application = application)
    )
) {
    val uiState = searchViewModel.uiState.collectAsStateWithLifecycle().value
    val currentSongId = PlayerManager.currentSongId.collectAsStateWithLifecycle().value
    val isLoggedIn = uiState.isLoggedIn

    var addToPlaylistSong by remember { mutableStateOf<Song?>(null) }

    val textFieldState = remember { TextFieldState(uiState.search) }
    val searchBarState = rememberSearchBarWithGapState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val scrollBehavior =
        SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    val searchBarColors =
        SearchBarDefaults.appBarWithSearchColors()


    LaunchedEffect(textFieldState.text) {
        searchViewModel.onSearchFieldChange(
            textFieldState.text.toString()
        )
    }

    val inputField: @Composable () -> Unit = {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = searchBarColors.searchBarColors.inputFieldColors,
            onSearch = {
                focusManager.clearFocus()
                searchViewModel.search()

                scope.launch {
                    searchBarState.animateToCollapsed()
                }
            },
            placeholder = {
                Text(
                    modifier = Modifier.clearAndSetSemantics {},
                    text = stringResource(R.string.search)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (textFieldState.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd("")
                            searchViewModel.onSearchFieldChange("")
                            focusManager.clearFocus()
                            scope.launch {
                                searchBarState.animateToCollapsed()
                            }
                        }, shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            AppBarWithSearch(
                scrollBehavior = scrollBehavior,
                state = searchBarState,
                colors = searchBarColors,
                inputField = inputField,
            )

            ExpandedDockedSearchBarWithGap(
                state = searchBarState,
                inputField = inputField,
            ) {
                uiState.suggestions.forEach { suggestion ->
                    MaterialUDropdownItem(
                        onClick = {
                            textFieldState.setTextAndPlaceCursorAtEnd(suggestion)
                            searchViewModel.onSearchFieldChange(suggestion)

                            focusManager.clearFocus()

                            scope.launch {
                                searchBarState.animateToCollapsed()
                            }

                            searchViewModel.search()
                        },
                        text = suggestion,
                        leadingIcon = Icons.Rounded.Search
                    )
                }
            }
        }
    ) { paddingValues ->
        SearchScreenContent(
            searchViewModel = searchViewModel,
            uiState = uiState,
            isLoggedIn = isLoggedIn,
            currentSongId = currentSongId,
            onAddToPlaylist = { addToPlaylistSong = it },
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding()
            )
        )
    }

    addToPlaylistSong?.let { song ->
        AddToPlaylistBottomSheet(
            song = song,
            application = application,
            onClose = { addToPlaylistSong = null },
        )
    }
}

@Composable
fun SearchScreenContent(
    searchViewModel: SearchViewModel,
    uiState: SearchState,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    onAddToPlaylist: (Song) -> Unit = {},
    currentSongId: String? = null,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (val screenState = uiState.screenState) {
            is ScreenState.Error -> {
                ErrorMessage(
                    ex = screenState.exception,
                    onRetry = {
                        searchViewModel.search()
                    })
            }

            ScreenState.Loading -> {
                LoadingAnimation()
            }

            is ScreenState.Success -> {
                val songs = screenState.results
                if (songs.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(bottom = Constants.Ui.SCROLLABLE_BOTTOM_PADDING),
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = songs,
                            key = { _, song ->
                                song.uid
                            }) { index, song ->
                            SongListItem(
                                song = song,
                                isCurrentSong = song.youtubeId == currentSongId,
                                onPress = {
                                    PlayerManager.playQueue(
                                        mediaItems = songs.map { it.mediaItem },
                                        startIndex = index
                                    )
                                },
                                playNext = {
                                    PlayerManager.addNext(song, context)
                                },
                                addToQueue = {
                                    PlayerManager.addToQueue(song, context)
                                },
                                download = {
                                    searchViewModel.downloadSong(song)
                                },
                                addToPlaylist = if (isLoggedIn) {
                                    { onAddToPlaylist(song) }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center

                    ) {
                        Text(stringResource(R.string.no_results))
                    }
                }
            }
        }
    }

}
