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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import ca.ilianokokoro.umihi.music.ui.components.ErrorMessage
import ca.ilianokokoro.umihi.music.ui.components.LoadingAnimation
import ca.ilianokokoro.umihi.music.ui.components.SearchBar
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

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (uiState.search.isBlank()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    FilterChip(
                        selected = uiState.activeFilter == SearchFilter.ALL,
                        onClick = { searchViewModel.onFilterChange(SearchFilter.ALL) },
                        label = { Text(stringResource(R.string.search_all)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = uiState.activeFilter == SearchFilter.SONGS,
                        onClick = { searchViewModel.onFilterChange(SearchFilter.SONGS) },
                        label = { Text(stringResource(R.string.search_songs)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = uiState.activeFilter == SearchFilter.VIDEOS,
                        onClick = { searchViewModel.onFilterChange(SearchFilter.VIDEOS) },
                        label = { Text(stringResource(R.string.search_videos)) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Videocam,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        SearchScreenContent(
            searchViewModel,
            uiState,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        )
    }

}


@Composable
fun SearchScreenContent(
    searchViewModel: SearchViewModel,
    uiState: SearchState,
    modifier: Modifier = Modifier,
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
