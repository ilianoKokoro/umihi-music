package ca.ilianokokoro.umihi.music.core.managers

import android.content.Context
import android.widget.Toast
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.database.AppDatabase
import ca.ilianokokoro.umihi.music.data.datasources.local.VersionDataSource
import ca.ilianokokoro.umihi.music.data.repositories.GithubRepository
import ca.ilianokokoro.umihi.music.models.Version
import ca.ilianokokoro.umihi.music.models.dto.GithubReleaseResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow


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

    suspend fun checkForUpdates(context: Context, manualCheck: Boolean = false) {
        try {
            githubRepository.getLatestVersionName().collect { result ->
                when (result) {
                    is ApiResult.Success<GithubReleaseResponse> -> {
                        val githubRelease = result.data

                        val mostUpToDateVersion = githubRelease.versionName
                        val outdated = mostUpToDateVersion.isNewUpdate(manualCheck)

                        if (!outdated) {
                            if (manualCheck) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.up_to_date_message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            _eventsFlow.emit(ScreenEvent.UpdateAvailable(githubReleaseResponse = result.data))
                        }


                    }

                    is ApiResult.Error -> {
                        throw result.exception
                    }

                    ApiResult.Loading -> {}
                }

            }
        } catch (ex: Exception) {
            if (manualCheck) {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_check_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
            printe(message = ex.message.toString(), exception = ex)
        }
    }

    suspend fun ignoreUpdate(version: Version) {
        versionRepository.ignoreVersion(version)
    }

    private suspend fun String.isNewUpdate(manualCheck: Boolean): Boolean {
        try {
            val appVersion = versionName!!.removeSuffix(Constants.BETA_SUFFIX)

            if (appVersion == this) {
                return false
            }
            val ignoredVersions = versionRepository.getIgnoredVersions()


            if (!manualCheck && ignoredVersions.contains(Version(this))) {
                return false
            }

            val leftVersionArray = this.split(".")
            val rightVersionArray = appVersion.split(".")

            if (leftVersionArray.count() != rightVersionArray.count()) {
                return true
            }
            for (leftVersionNumberString in leftVersionArray) {
                val leftVersionNumber = leftVersionNumberString.toInt()
                val rightVersionNumber =
                    rightVersionArray[leftVersionArray.indexOf(leftVersionNumberString)].toInt()
                if (leftVersionNumber > rightVersionNumber) {
                    return true
                }
            }
            return false

        } catch (ex: Exception) {
            printe(message = ex.message.toString(), exception = ex)
            return true
        }
    }

    sealed class ScreenEvent {
        data class UpdateAvailable(val githubReleaseResponse: GithubReleaseResponse) :
            ScreenEvent()
    }
}

