# Umihi Music

Android app (Kotlin, Compose, Material 3) playing YouTube Music via InnerTube API. Single-activity MVVM, Room + DataStore, Media3 background playback, offline downloads.

## Non-obvious tech choices

- **Navigation:** experimental `androidx.navigation3` (`NavKey`, `NavDisplay`, `rememberNavBackStack`)
- **DI:** Manual — no Hilt/Dagger/Koin. Singletons via `object` declarations (e.g. `PlayerManager`, `YoutubeApiClient`, `UmihiHttpClient`). ViewModels use `viewModelFactory { initializer { ... } }` in companions, receiving `Application` from `NavEntry`. `AppDatabase` via `getInstance(context)` double-checked locking.
- **HTTP:** OkHttp + kotlinx.serialization (no Retrofit). Two client instances in `UmihiHttpClient` — `client` (regular, 15/30/30/45s timeouts) and `downloadClient` (20/2m/2m/no-call-timeout).
- **Database:** Room 2.8.4, `fallbackToDestructiveMigration(dropAllTables = true)`, `exportSchema = false`. Database version 8 (`Constants.Database.VERSION`).
- **Dual audio extract:** `resolveAndroidVrStreamUrl` (ANDROID_VR client, Quest 3) → `resolveNewPipeStreamUrl` (NewPipe Extractor) fallback with retries. In `YoutubeDataExtractor.getSongUrlFromYoutube`.

## Build

```powershell
./gradlew assembleStandaloneRelease              # beta (default)
./gradlew assembleStandaloneRelease -Pbeta=false  # stable
./gradlew assembleStoreRelease -Pbeta=false       # store (updater disabled)
# Optional: -PgitHash=<sha> → embedded in BuildConfig.COMMIT_HASH
```

`beta` defaults to `true` in `app/build.gradle.kts:10`. Output APKs: `UmihiMusic.apk` (standalone), `UmihiMusic-store.apk` (store). No linter/typechecker — Gradle compilation is the validation step.

Keys: AGP 9.2.1, Kotlin 2.3.20, JDK 17, compileSdk/targetSdk 37, minSdk 24.

Version: `versionMajor * 10000 + versionMinor * 100 + versionPatch` → e.g. 1.12.4 → 11204. Name: `$version-$betaSuffix`.

`keystore.properties` is committed — contains the real debug/release keystore path and credentials.

## Key structure

```
app/src/main/java/ca/ilianokokoro/umihi/music/
├── core/
│   ├── youtube/     # YoutubeApiClient, YoutubeAuthHelper (SAPISIDHASH), YoutubeDataExtractor (parsing), YoutubeStatsTracker
│   ├── managers/   # PlayerManager (singleton, MediaController), NotificationManager, ScreenAwakeManager, VersionManager
│   ├── workers/     # SongDownloadWorker, PlaylistDownloadWorker (WorkManager, max 8 concurrent)
│   ├── datasources/ # YoutubeDataSourceFactory (ResolvingDataSource → videoId → audio URL)
│   ├── UmihiHttpClient.kt, ExoCache.kt, Constants.kt, ApiResult.kt, YoutubeExtractor.kt (NewPipe Downloader)
├── data/
│   ├── database/    # AppDatabase (Room 2.8.4, destructive migration)
│   ├── datasources/ # SongDataSource, PlaylistDataSource + local/ subdir
│   └── repositories/ # SongRepository, PlaylistRepository, DownloadRepository, GithubRepository, DatastoreRepository
├── models/          # Song, Playlist, PlaylistInfo, UmihiSettings, Cookies, Version, DTOs
├── services/        # PlaybackService (MediaLibraryService — Android Auto), UmihiMediaLibraryCallback
├── ui/
│   ├── theme/       # Material 3
│   ├── navigation/  # NavKeys (5 screens), NavigationRoot, BottomNavBar, SharedViewModel
│   ├── components/  # MiniPlayerWrapper, song/ items, playlist/ card, dialog/, materialu/ (custom)
│   └── screens/     # home, search, playlist, player (ModalBottomSheet), auth (WebView), settings
├── error/           # ErrorActivity (customactivityoncrash)
└── extensions/      # Kotlin extensions
```

## Navigation flow

```
BottomBar: Home | Search | Settings
Home/Search → PlaylistScreen → PlayerScreen (ModalBottomSheet)
           → AuthScreen
Settings → AuthScreen
```

`ScreenUiConfig` controls bottom bar / mini player visibility per screen. `SharedViewModel` carries cross-screen state (playback, auth). Transitions: scale + fade, 200ms.

## Quirks an agent would miss

- **DI wiring:** Every screen's `NavEntry` receives `application: Application` and passes it to its ViewModel factory. `PlayerManager` is connected in `MainActivity.onStart()`. `NewPipe.init(YoutubeExtractor())` in `MainActivity.onCreate()`.
- **Umihi.kt Application:** Only calls `NotificationManager.init(this)` in `onCreate`. All other wiring is in `MainActivity`.
- **Kotlin style:** Every `if` uses braces — `if (condition) { ... }`, never single-line.
- **InnerTube API:** `youtubei/v1/{browse,player,search,playlist/create,playlist/delete,like/like,like/removelike}`. Hardcoded key `AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30`. Two client configs: `WEB_REMIX` (mobile web, default) and `ANDROID_VR` (Quest 3, used for audio extraction).
- **Auth header:** WebView OAuth → SAPISID cookie → `SAPISIDHASH <ts>_<SHA1(ts+sapisid+origin)>`. Also sent as `X-Goog-Api-Format-Version: 1`, `X-YouTube-Client-Name`, `X-YouTube-Client-Version`.
- **Flavors:** `standalone` (default, `UPDATER_ENABLED=true`, self-updater via GitHub releases). `store` (`UPDATER_ENABLED=false`, `-store` version suffix, no install permissions).
- **Offline:** WorkManager (max 8 concurrent, semaphore-limited). Songs → `.webm`, thumbs → `.jpg`. Synthesized playlist ID `_downloaded_`. Smart merge of remote + local paths.
- **Update channels:** Stable (latest GitHub release tag, semver with `-beta` suffix awareness). Beta (latest `main` commit SHA → `beta` GitHub release tag).
- **Media3:** `PlaybackService` extends `MediaLibraryService` (Android Auto). Custom `YoutubeDataSourceFactory` (ResolvingDataSource) converts video IDs → audio streams at playback time. ExoPlayer cache: `SimpleCache`, 1000 MB, LRU eviction.
- **SplashScreen:** Uses `androidx.core:core-splashscreen` (`installSplashScreen()` before `super.onCreate`).
- **Crowdin:** Translations at `app/src/main/res/values-*`. `crowdin.yml` defines source→translation mapping.
- **CI:** `main` branch → beta build + GitHub release tagged `beta`. `prod` branch → stable release (standalone + store APKs, draft release with auto-generated notes).
