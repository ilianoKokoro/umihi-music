# Umihi Music — Architecture Reference

## Overview

Android app (Kotlin, Jetpack Compose, Material 3) that plays YouTube Music through the InnerTube API. Single-activity MVVM architecture with Room + DataStore persistence, Media3 background playback, and offline downloads.

## Tech Stack

- **Language:** Kotlin 2.3.20
- **UI:** Jetpack Compose + Material 3 (Material Expressive)
- **Navigation:** Experimental `androidx.navigation3` (NavHost, NavKey, NavDisplay)
- **State:** `ViewModel` + `MutableStateFlow` + `ApiResult<T>` sealed class
- **Database:** Room (v6, destructive migration)
- **Preferences:** Jetpack DataStore
- **HTTP:** OkHttp + kotlinx.serialization
- **Playback:** Media3 `MediaLibraryService` (ExoPlayer)
- **Downloads:** WorkManager (max 8 concurrent, semaphore-limited)
- **Images:** Coil
- **DI:** Manual (no Hilt/Koin — singletons passed via Application/ViewModel)

## Architecture

```
ViewModel → Repository → DataSource → OkHttp / Room DB / DataStore
                            ↓
                     kotlinx.serialization JSON
                            ↓
                     Flow<ApiResult<T>> → StateFlow → Compose UI
```

## Key Directory Layout

```
app/src/main/java/ca/ilianokokoro/umihi/music/
├── core/              # Business logic: HTTP client, YouTube API helpers, auth, downloader, PlayerManager
├── data/              # Database, remote/local datasources, repositories
├── models/            # Entities, DTOs, settings models
├── services/          # MediaLibraryService (background playback), MediaLibraryCallback
├── ui/
│   ├── theme/         # Material 3 colors, typography, theme
│   ├── navigation/    # NavKeys, NavigationRoot, BottomNavBar, SharedViewModel
│   ├── components/    # Reusable composables (mini player, song item, playlist card, dialogs, etc.)
│   └── screens/       # Per-screen: home, search, playlist, player, auth, settings (each has Screen + ViewModel + State)
├── error/             # ErrorActivity, crash screen
└── extensions/        # Kotlin extension files
```

## Special Things About This App

### 1. Custom InnerTube API Client
Does **not** use the official YouTube Data API. Instead, calls YouTube's internal `youtubei/v1/browse`, `/player`, `/search`, `/playlist/create`, `/playlist/delete` endpoints, mimicking WEB_REMIX and ANDROID_VR clients. API key is hardcoded (`AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30`).

### 2. Dual Audio Resolution
- **Primary:** ANDROID_VR client → direct audio stream URL
- **Fallback:** NewPipe Extractor library if primary fails
- Stream URLs cached in Room per song

### 3. WebView OAuth Login
Google sign-in via WebView. Cookies captured from `CookieManager` after redirect to `music.youtube.com`. SAPISID cookie → `SAPISIDHASH` auth header for all API calls. `DataSync ID` extracted via JS injection.

### 4. Auth Header Generation
`SAPISIDHASH` = `SHA1(timestamp + sapisid + "https://music.youtube.com")`, sent as `Authorization: SAPISIDHASH <timestamp_<hash>`. Also sets `X-Goog-Api-Format-Version`, `X-YouTube-Client-Version`, `X-YouTube-Client-Name`, `X-Goog-Visitor-Id`.

### 5. Custom ExoPlayer DataSource
`YoutubeDataSourceFactory` implements `DataSource.Factory` using `ResolvingDataSource` to convert YouTube video IDs to actual audio streams at playback time.

### 6. Media3 MediaLibraryService
Background playback via `MediaLibraryService` (Android foreground service). Supports Android Auto. `UmihiMediaLibraryCallback` provides browse tree: `Root → Playlists → [Playlist → Play/Shuffle + songs]`.

### 7. Two Product Flavors
- `standalone` (default): Built-in auto-updater via GitHub releases
- `store`: No updater, no install permissions (for app stores)

### 8. Easter Egg
Clicking the Settings tab 25 times toggles Esperanto (`eo`) locale for the entire app.

### 9. Offline Downloads
WorkManager-based. Songs stored as `.webm`, thumbnails as `.jpg`. Synthesized playlist with ID `_downloaded_` shows all downloaded content. Smart merge: remote playlist data merged with local download paths.

### 10. Smart Playlist Merge
When viewing a playlist, remote songs are merged with locally downloaded song data (thumbnails, audio paths) so downloaded songs display correctly.

### 11. Update Channels
- **Stable:** Checks latest GitHub release tag
- **Beta:** Checks latest commit SHA on `main` branch
- Supports semver with `-beta` suffix comparison

### 12. No DI Framework
No Hilt/Dagger/Koin. Dependencies wired manually via `Application` class and `ViewModel` factories.

## Navigation

```
BottomNavBar: Home | Search | Settings
Home → PlaylistScreen → PlayerScreen (ModalBottomSheet)
     → AuthScreen
Search → PlaylistScreen → PlayerScreen
Settings → AuthScreen
```

Navigation screens: `HomeScreenKey`, `SearchScreenKey`, `SettingsScreenKey`, `PlaylistScreenKey(data)`, `AuthScreenKey`. `ScreenUiConfig` controls bottom bar and mini player visibility per screen. Transitions: scale + fade, 200ms.

## API Endpoints Used

| Endpoint | Purpose |
|---|---|
| `youtubei/v1/browse` | Browse playlists, playlist contents |
| `youtubei/v1/player` | Get song stream URLs |
| `youtubei/v1/search` | Search songs |
| `youtubei/v1/playlist/create` | Create playlist |
| `youtubei/v1/playlist/delete` | Delete playlist |
| GitHub releases/commits API | Version checking |

## Build Commands

```powershell
./gradlew assembleStandaloneRelease    # production build
./gradlew assembleStoreRelease          # store build
# Optional: -Pbeta=true -PgitHash=...
```

No linter/typechecker — standard Gradle build validates compilation.
