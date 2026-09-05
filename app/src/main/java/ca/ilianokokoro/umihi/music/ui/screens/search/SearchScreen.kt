package ca.ilianokokoro.umihi.music.ui.screens.search

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.managers.PlayerManager
import ca.ilianokokoro.umihi.music.models.Song
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.SearchBar
import ca.ilianokokoro.umihi.music.ui.components.bottomsheet.AddToPlaylistBottomSheet
import ca.ilianokokoro.umihi.music.ui.components.song.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    application: Application,
    searchViewModel: SearchViewModel = viewModel(
        factory =
            SearchViewModel.Factory(application = application)
    )

) {
    val uiState = searchViewModel.uiState.collectAsStateWithLifecycle().value
    var addToPlaylistSong by remember { mutableStateOf<Song?>(null) }


    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (uiState.search.isBlank()) {
            focusRequester.requestFocus()
        }
    }


    Scaffold(
        topBar = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 4.dp)
            ) {
                SearchBar(
                    value = uiState.search,
                    onValueChange = { searchViewModel.onSearchFieldChange(it) },
                    onSearch = {
                        focusManager.clearFocus()
                        searchViewModel.search()
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                )
            }
        }
    ) { paddingValues ->
        SearchScreenContent(
            searchViewModel,
            uiState,
            onAddToPlaylist = { addToPlaylistSong = it },
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
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
    modifier: Modifier = Modifier,
    onAddToPlaylist: (Song) -> Unit = {},
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
                        items(
                            items = songs,
                            key = { song ->
                                song.uid
                            }) {
                            SongListItem(
                                song = it,
                                onPress = {
                                    PlayerManager.playSong(it)
                                },
                                playNext = {
                                    PlayerManager.addNext(it, context)
                                },
                                addToQueue = {
                                    PlayerManager.addToQueue(it, context)
                                },
                                addToPlaylist = {
                                    onAddToPlaylist(it)
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
