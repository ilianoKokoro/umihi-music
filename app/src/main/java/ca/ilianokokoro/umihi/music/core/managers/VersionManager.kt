package ca.ilianokokoro.umihi.music.core.managers

import android.content.Context
import android.widget.Toast
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.ApiResult
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printd
import ca.ilianokokoro.umihi.music.core.helpers.UmihiHelper.printe
import ca.ilianokokoro.umihi.music.data.repositories.GithubRepository

object VersionManager {

    private var versionName: String? = null

    private var githubRepository: GithubRepository = GithubRepository()

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

    suspend fun checkForUpdates(context: Context? = null) {
        try {
            githubRepository.getLatestVersionName().collect { result ->
                when (result) {
                    is ApiResult.Success<String> -> {
                        val mostUpToDateVersion = result.data.drop(1)
                        val outdated = mostUpToDateVersion.isVersionMoreRecent()

                        if (!outdated) {
                            if (context != null) {
                                Toast.makeText( // TODO make the toast work
                                    context,
                                    context.getString(R.string.up_to_date_message),
                                    Toast.LENGTH_LONG
                                )
                            }
                        } else {
                            printd("outdated")
                        }

                    }

                    is ApiResult.Error -> {
                        printe(message = result.errorMessage)
                    }

                    ApiResult.Loading -> {}
                }


            }
        } catch (ex: Exception) {
            printe(message = ex.message.toString(), exception = ex)
        }
    }

    private fun String.isVersionMoreRecent(): Boolean {
        try {
            val appVersion = versionName!!

            if (appVersion.isBetaVersion()) {
                return false
            }

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

    private fun String.isBetaVersion(): Boolean {
        return this.endsWith("-beta")
    }
}