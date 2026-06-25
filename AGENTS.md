# Umihi Music

Android app (Kotlin, Compose, Material 3) playing YouTube Music via InnerTube API. Single-activity MVVM, Room + DataStore, Media3 background playback, offline downloads.

## Non-obvious tech choices

- **Navigation:** experimental `androidx.navigation3` (`NavKey`, `NavDisplay`, `rememberNavBackStack`)
- **DI:** Manual (no Hilt/Dagger/Koin — singletons via `Umihi` Application + ViewModel factories)
- **HTTP:** OkHttp + kotlinx.serialization (no Retrofit)
- **Database:** Room v6 with destructive migration
- **Dual audio:** ANDROID_VR client primary, NewPipe Extractor fallback

## Build

```powershell
./gradlew assembleStandaloneRelease              # beta (default)
./gradlew assembleStandaloneRelease -Pbeta=false  # stable
./gradlew assembleStoreRelease -Pbeta=false       # store (updater disabled)
# Optional: -PgitHash=<sha> → embedded in BuildConfig.COMMIT_HASH
```

`beta` defaults to `true` in `app/build.gradle.kts:10`. Output APKs: `UmihiMusic.apk` (standalone), `UmihiMusic-store.apk` (store). No linter/typechecker — Gradle compilation is the validation step.

Version: `versionMajor * 10000 + versionMinor * 100 + versionPatch` → e.g. 1.12.3 → 11203. Name: `$version-$betaSuffix`.

## Key structure

```
app/src/main/java/ca/ilianokokoro/umihi/music/
├── core/        # HTTP client, auth/inner tube helpers, downloader workers, PlayerManager, YoutubeDataSourceFactory
├── data/        # Room DAOs, remote/local datasources, repositories
├── models/      # Entities, DTOs, UmihiSettings
├── services/    # MediaLibraryService + UmihiMediaLibraryCallback (Android Auto browsing)
├── ui/
│   ├── theme/   # Material 3
│   ├── navigation/  # NavKeys (5 screens), NavigationRoot, BottomNavBar, SharedViewModel
│   ├── components/  # Mini player, song item, playlist card, dialogs, player controls
│   └── screens/     # home, search, playlist, player, auth, settings (each: Screen + ViewModel + State)
├── error/       # ErrorActivity (customactivityoncrash)
├── extensions/  # Kotlin extensions
└── MainActivity.kt + Umihi.kt (Application)
```

## Navigation flow

```
BottomBar: Home | Search | Settings
Home/Search → PlaylistScreen → PlayerScreen (ModalBottomSheet)
           → AuthScreen
Settings → AuthScreen
```

Nav keys: `HomeScreenKey`, `SearchScreenKey`, `SettingsScreenKey`, `PlaylistScreenKey(data)`, `AuthScreenKey`. `ScreenUiConfig` controls bottom bar / mini player visibility per screen. Transitions: scale + fade, 200ms.

## Quirks an agent would miss

- **Kotlin style:** Every `if` uses braces — `if (condition) { ... }`, never single-line `if (condition) ...`.
- **InnerTube API:** `youtubei/v1/{browse,player,search,playlist/create,playlist/delete}`. Hardcoded key `AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30`. Two JSON client configs: `WEB_REMIX` (mobile web) and `ANDROID_VR` (Quest 3).
- **Auth header:** WebView OAuth → SAPISID cookie → `SAPISIDHASH <ts>_<SHA1(ts+sapisid+origin)>`. Also sent as `X-Goog-Api-Format-Version: 1`, `X-YouTube-Client-Name`, `X-YouTube-Client-Version`.
- **Flavors:** `standalone` (default, `UPDATER_ENABLED=true`, self-updater via GitHub releases). `store` (`UPDATER_ENABLED=false`, `-store` version suffix, no install permissions).
- **Offline:** WorkManager (max 8 concurrent, semaphore-limited). Songs → `.webm`, thumbs → `.jpg`. Synthesized playlist ID `_downloaded_`. Smart merge of remote + local paths.
- **Update channels:** Stable (latest GitHub release tag, semver comparison with `-beta` suffix awareness). Beta (latest `main` commit SHA).
- **ExoPlayer:** Custom `YoutubeDataSourceFactory` (ResolvingDataSource) converts video IDs → audio streams at playback time.
- **Crowdin:** Translations at `app/src/main/res/values-*`. Use `crowdin.yml` to sync.
