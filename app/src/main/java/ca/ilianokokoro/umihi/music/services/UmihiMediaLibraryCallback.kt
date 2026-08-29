package ca.ilianokokoro.umihi.music.services

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.PlaylistRepository
import ca.ilianokokoro.umihi.music.data.repositories.SongRepository
import ca.ilianokokoro.umihi.music.models.Playlist
import ca.ilianokokoro.umihi.music.models.PlaylistInfo
import ca.ilianokokoro.umihi.music.models.Song
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

@UnstableApi
class UmihiMediaLibraryCallback(
    private val service: PlaybackService,
    private val serviceScope: CoroutineScope,
    private val datastoreRepository: DatastoreRepository,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository
) : MediaLibrarySession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val playerCommands =
            MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS

        val sessionCommands =
            MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS

        return MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
            .setAvailablePlayerCommands(playerCommands)
            .setAvailableSessionCommands(sessionCommands)
            .build()
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId(Constants.ExoPlayer.Library.ROOT_ID)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setIsBrowsable(false)
                            .setIsPlayable(false)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build()
                    )
                    .build(),
                params
            )
        )
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        serviceScope.launch {
            try {
                val result = when {
                    parentId == Constants.ExoPlayer.Library.ROOT_ID -> {
                        listOf(
                            MediaItem.Builder()
                                .setMediaId(Constants.ExoPlayer.Library.PLAYLIST_ROOT)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(service.getString(R.string.playlists))
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                                        .build()
                                )
                                .build()
                        )
                    }

                    parentId == Constants.ExoPlayer.Library.PLAYLIST_ROOT -> {
                        val playlists = playlistRepository
                            .retrieveAll(datastoreRepository.settings.first())
                            .first { it !is ApiResult.Loading }

                        if (playlists is ApiResult.Success) {
                            val mutablePlaylists = playlists.data.toMutableList()
                            
                            val downloadedPlaylist = PlaylistInfo(
                                id = Constants.Downloads.DOWNLOADED_PLAYLIST_ID,
                                title = service.getString(R.string.downloaded),
                                coverHref = drawableUri(R.drawable.download).toString()
                            )

                            mutablePlaylists.add(0, downloadedPlaylist)
                            
                            mutablePlaylists.map { playlist ->
                                playlist.toBrowsableMediaItem()
                            }
                        } else {
                            emptyList()
                        }
                    }

                    parentId.startsWith(Constants.ExoPlayer.Library.PLAYLIST_PREFIX) -> {
                        val playlistId =
                            parentId.removePrefix(Constants.ExoPlayer.Library.PLAYLIST_PREFIX)

                        val playlist = Playlist(
                            PlaylistInfo(id = playlistId)
                        )

                        val result = playlistRepository
                            .retrieveOne(
                                playlist,
                                datastoreRepository.settings.first()
                            )
                            .first { it !is ApiResult.Loading }

                        if (result is ApiResult.Success && result.data.songs.isNotEmpty()) {
                            listOf(
                                playPlaylistMediaItem(playlistId),
                                shufflePlaylistMediaItem(playlistId)
                            ) + result.data.songs.map { song ->
                                song.mediaItem
                            }
                        } else {
                            emptyList()
                        }
                    }

                    else -> emptyList()
                }

                val rootExtras = Bundle().apply {
                    putBoolean(SEARCH_SUPPORTED_KEY, true)
                }
                val libraryParams = LibraryParams.Builder()
                    .setExtras(rootExtras)
                    .build()

                future.set(
                    LibraryResult.ofItemList(
                        result,
                        libraryParams
                    )
                )
            } catch (_: Exception) {
                future.set(
                    LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
                )
            }
        }

        return future
    }

    private var searchResults: List<Song> = emptyList()

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        if (query.isBlank()) {
            searchResults = emptyList()
            session.notifySearchResultChanged(browser, query, 0, params)
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        serviceScope.launch {
            searchResults = performSearch(query)
            session.notifySearchResultChanged(browser, query, searchResults.size, params)
        }

        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    private suspend fun performSearch(query: String): List<Song> {
        return try {
            val searchResult = withTimeout(3.seconds) {
                songRepository.search(query).first { it !is ApiResult.Loading }
            }
            if (searchResult is ApiResult.Success) {
                searchResult.data
            } else {
                AppDatabase.getInstance(service).songRepository().searchDownloaded(query)
            }
        } catch (_: Exception) {
            try {
                AppDatabase.getInstance(service).songRepository().searchDownloaded(query)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return Futures.immediateFuture(
            LibraryResult.ofItemList(
                searchResults.map { it.mediaItem },
                params
            )
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()

        serviceScope.launch {
            try {
                val resolvedItems = mediaItems.flatMap { item ->
                    resolveMediaItemForPlayback(item)
                }

                val resolvedStartIndex = when {
                    resolvedItems.isEmpty() -> C.INDEX_UNSET
                    startIndex == C.INDEX_UNSET -> 0
                    else -> startIndex.coerceIn(0, resolvedItems.lastIndex)
                }

                future.set(
                    MediaSession.MediaItemsWithStartPosition(
                        resolvedItems,
                        resolvedStartIndex,
                        startPositionMs.coerceAtLeast(0L)
                    )
                )
            } catch (e: Exception) {
                future.setException(e)
            }
        }

        return future
    }

    private suspend fun resolveMediaItemForPlayback(
        mediaItem: MediaItem
    ): List<MediaItem> {
        val mediaId = mediaItem.mediaId

        if (mediaId.startsWith(Constants.ExoPlayer.Library.PLAY_PLAYLIST_PREFIX)) {
            val playlistId = mediaId.removePrefix(Constants.ExoPlayer.Library.PLAY_PLAYLIST_PREFIX)
            return resolvePlaylistSongs(
                playlistId = playlistId,
                shuffle = false
            )
        }

        if (mediaId.startsWith(Constants.ExoPlayer.Library.SHUFFLE_PLAYLIST_PREFIX)) {
            val playlistId =
                mediaId.removePrefix(Constants.ExoPlayer.Library.SHUFFLE_PLAYLIST_PREFIX)
            return resolvePlaylistSongs(
                playlistId = playlistId,
                shuffle = true
            )
        }

        return listOf(
            resolveSongMediaItem(mediaItem)
        )
    }

    private suspend fun resolvePlaylistSongs(
        playlistId: String,
        shuffle: Boolean
    ): List<MediaItem> {
        val result = playlistRepository.retrieveOne(
            playlist = Playlist(
                PlaylistInfo(id = playlistId)
            ),
            settings = datastoreRepository.settings.first()
        ).firstOrNull { result ->
            result is ApiResult.Success
        }

        if (result !is ApiResult.Success) {
            return emptyList()
        }

        val songs = if (shuffle) {
            result.data.songs.shuffled()
        } else {
            result.data.songs
        }

        return songs.map { song ->
            song.mediaItem
        }
    }

    private suspend fun resolveSongMediaItem(
        mediaItem: MediaItem
    ): MediaItem {
        val mediaId = mediaItem.mediaId

        if (mediaItem.localConfiguration?.uri != null &&
            mediaItem.mediaMetadata.title != null
        ) {
            return mediaItem
        }

        val songResult = songRepository.getSongInfo(mediaId)
            .firstOrNull { result ->
                result is ApiResult.Success
            }

        if (songResult is ApiResult.Success) {
            return songResult.data.mediaItem
        }

        return mediaItem.buildUpon()
            .setUri("${Constants.YoutubeApi.YOUTUBE_URL_PREFIX}$mediaId")
            .build()
    }

    private fun playPlaylistMediaItem(
        playlistId: String
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId("${Constants.ExoPlayer.Library.PLAY_PLAYLIST_PREFIX}$playlistId")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(service.getString(R.string.play))
                    .setArtworkUri(drawableUri(androidx.media3.session.R.drawable.media3_icon_play))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                    .build()
            )
            .build()
    }

    private fun shufflePlaylistMediaItem(
        playlistId: String
    ): MediaItem {
        return MediaItem.Builder()
            .setMediaId("${Constants.ExoPlayer.Library.SHUFFLE_PLAYLIST_PREFIX}$playlistId")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(service.getString(R.string.shuffle))
                    .setArtworkUri(drawableUri(androidx.media3.session.R.drawable.media3_icon_shuffle_on))
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                    .build()
            )
            .build()
    }

    private fun drawableUri(
        drawableResId: Int
    ): Uri {
        return Uri.Builder()
            .scheme("android.resource")
            .authority(service.packageName)
            .appendPath(drawableResId.toString())
            .build()
    }

    companion object {
        private const val SEARCH_SUPPORTED_KEY = "android.media.browse.SEARCH_SUPPORTED"
    }
}