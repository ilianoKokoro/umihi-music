package ca.ilianokokoro.umihi.music.core.managers

import android.content.Context
import android.widget.Toast
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
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

    fun initialize(context: Context) {
        if (versionName == null) {
            versionName = try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName
            } catch (_: Exception) {
                String()
            }
        }
    }

    fun getVersionName(): String {
        return versionName.toString()
    }

    suspend fun checkForUpdates(context: Context, showToast: Boolean = false) {
        try {
            githubRepository.getLatestVersionName().collect { result ->
                when (result) {
                    is ApiResult.Success<GithubReleaseResponse> -> {
                        val githubRelease = result.data

                        val mostUpToDateVersion = githubRelease.tagName.drop(1)
                        val outdated = mostUpToDateVersion.isVersionMoreRecent()

                        if (!outdated) {
                            printd("App is up to date")
                            if (showToast) {
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
            if (showToast) {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_check_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
            printe(message = ex.message.toString(), exception = ex)
        }
    }

    fun ignoreUpdate(version: Version) {
        // TODO
    }

    private fun String.isVersionMoreRecent(): Boolean {
        try {
            val appVersion = versionName!!.stripBetaTag()

            if (appVersion == this) {
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

    private fun String.stripBetaTag(): String {
        return if (this.isBetaVersion()) {
            this.dropLast(5)
        } else {
            this
        }
    }

    private fun String.isBetaVersion(): Boolean {
        return this.endsWith("-beta")
    }

    sealed class ScreenEvent {
        data class UpdateAvailable(val githubReleaseResponse: GithubReleaseResponse) :
            ScreenEvent()
    }
}

