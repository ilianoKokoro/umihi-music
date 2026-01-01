package ca.ilianokokoro.umihi.music.core.managers

import android.content.Context
import android.widget.Toast
import ca.ilianokokoro.umihi.music.BuildConfig
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.local.VersionDataSource
import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository
import ca.ilianokokoro.umihi.music.data.repositories.GithubRepository
import ca.ilianokokoro.umihi.music.models.Version
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext


object VersionManager {

    private val _eventsFlow =
        MutableSharedFlow<ScreenEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    private var versionName: String? = null

    private val githubRepository: GithubRepository = GithubRepository()
    private lateinit var versionRepository: VersionDataSource
    
    fun initialize(context: Context) {
        if (versionName == null) {
            versionName = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName
            } catch (_: Exception) {
                String()
            }
        }
        versionRepository = AppDatabase.getInstance(context).versionRepository()
    }

    fun getVersionName(): String {
        return versionName.toString()
    }

    fun getUpdateChannel(context: Context): DatastoreRepository.UpdateChannel {
        val datastoreRepository = DatastoreRepository(context)
        return datastoreRepository.getSettings().updateChannel
    }

    suspend fun checkForUpdates(
        context: Context,
        manualCheck: Boolean = false
    ) {
        try {
            when (getUpdateChannel(context)) {

                DatastoreRepository.UpdateChannel.Stable -> {
                    githubRepository.getLatestVersionName().collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val release = result.data
                                handleUpdateResult(
                                    context = context,
                                    manualCheck = manualCheck,
                                    outdated = release.versionName.isNewUpdate(manualCheck),
                                    release = release
                                )
                                return@collect
                            }

                            is ApiResult.Error -> throw result.exception
                            else -> Unit
                        }
                    }
                }

                DatastoreRepository.UpdateChannel.Beta -> {
                    githubRepository.getLatestCommit().collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                val latestCommit = result.data
                                handleUpdateResult(
                                    context = context,
                                    manualCheck = manualCheck,
                                    outdated = BuildConfig.COMMIT_HASH.isNewUpdate(
                                        manualCheck,
                                        latestCommit
                                    ),
                                    release = GithubReleaseResponse(
                                        tagName = latestCommit,
                                        body = "No detail",
                                        assets = emptyList()
                                    )
                                )
                                return@collect
                            }

                            is ApiResult.Error -> throw result.exception
                            else -> Unit
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            if (manualCheck) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.update_check_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            printe(message = ex.message.orEmpty(), exception = ex)
        }
    }

    private suspend fun handleUpdateResult(
        context: Context,
        manualCheck: Boolean,
        outdated: Boolean,
        release: GithubReleaseResponse
    ) {
        if (outdated) {
            _eventsFlow.emit(
                ScreenEvent.UpdateAvailable(
                    githubReleaseResponse = release
                )
            )
        } else if (manualCheck) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.up_to_date_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    suspend fun ignoreUpdate(version: Version) {
        versionRepository.ignoreVersion(version)
    }

    private suspend fun String.isNewUpdate(
        manualCheck: Boolean,
        commitHash: String? = null
    ): Boolean {
        val currentVersion = commitHash ?: versionName ?: return true
        val isBeta = currentVersion.endsWith(Constants.BETA_SUFFIX)

        if (this == currentVersion) {
            return false
        }

        if (!manualCheck) {
            val ignored = versionRepository.getIgnoredVersions()
            if (ignored.contains(Version(currentVersion))) {
                return false
            }
        }

        if (commitHash != null) {
            return true
        }

        val incomingParts = this.split(".").map { it.toInt() }
        val currentParts =
            currentVersion.removeSuffix(Constants.BETA_SUFFIX).split(".").map { it.toInt() }

        val maxLength = maxOf(incomingParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val incoming = incomingParts.getOrElse(i) { 0 }
            val current = currentParts.getOrElse(i) { 0 }

            when {
                incoming > current -> return true
                incoming < current -> return false
            }
        }

        return isBeta
    }


    sealed class ScreenEvent {
        data class UpdateAvailable(val githubReleaseResponse: GithubReleaseResponse) :
            ScreenEvent()
    }
}

