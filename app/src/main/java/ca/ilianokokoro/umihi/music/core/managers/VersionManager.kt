package ca.ilianokokoro.umihi.music.core.managers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL


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

    suspend fun getUpdateChannel(context: Context): DatastoreRepository.UpdateChannel {
        val datastoreRepository = DatastoreRepository(context)
        return datastoreRepository.settings.first().updateChannel
    }

    suspend fun checkForUpdates(
        context: Context,
        manualCheck: Boolean = false
    ) {
        if (!BuildConfig.UPDATER_ENABLED) {
            return
        }

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
                                        latestCommit.sha
                                    ),
                                    release = GithubReleaseResponse(
                                        tagName = latestCommit.sha,
                                        body = latestCommit.commit.message,
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

    suspend fun downloadApk(
        context: Context,
        release: GithubReleaseResponse,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updateDir = File(context.cacheDir, "updates")
        updateDir.mkdirs()

        val apkFile = File(updateDir, Constants.Downloads.UPDATE_APK)

        val connection = URL(release.downloadUrl).openConnection()
        val totalBytes = connection.contentLengthLong

        connection.getInputStream().use { input ->
            apkFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloadedBytes = 0L

                while (true) {
                    val read = input.read(buffer)

                    if (read == -1) {
                        break
                    }

                    output.write(buffer, 0, read)
                    downloadedBytes += read

                    if (totalBytes > 0) {
                        val progress = downloadedBytes.toFloat() / totalBytes.toFloat()

                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }
            }
        }

        apkFile
    }

    fun installApk(
        context: Context,
        apkFile: File
    ) {
        if (hasInstallPermissions(context)) {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        }
    }


    suspend fun requestInstallPermissions(
        activity: ComponentActivity
    ): Boolean = suspendCancellableCoroutine { continuation ->

        fun resumeWithResult(value: Boolean) {
            if (!continuation.isActive) {
                return
            }

            continuation.resume(value) { _, _, _ -> }
        }


        if (hasInstallPermissions(activity)) {
            resumeWithResult(true)
            return@suspendCancellableCoroutine
        }

        var launcher: ActivityResultLauncher<Intent>? = null

        launcher = activity.activityResultRegistry.register(
            "install_permissions_${System.nanoTime()}",
            ActivityResultContracts.StartActivityForResult()
        ) {
            launcher?.unregister()
            resumeWithResult(hasInstallPermissions(activity))
        }

        continuation.invokeOnCancellation {
            launcher.unregister()
        }

        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = "package:${activity.packageName}".toUri()
        }

        launcher.launch(intent)
    }

    private fun hasInstallPermissions(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O
                || context.packageManager.canRequestPackageInstalls()
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

