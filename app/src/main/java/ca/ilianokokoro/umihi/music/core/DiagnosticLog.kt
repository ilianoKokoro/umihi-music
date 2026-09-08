package ca.ilianokokoro.umihi.music.core

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import ca.ilianokokoro.umihi.music.BuildConfig
import ca.ilianokokoro.umihi.music.R
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper
import java.io.File
import java.time.Instant

object DiagnosticLog {
    const val DIAGNOSTIC_BUILD_TYPE = "diagnostic"
    private const val FILE_NAME = "diagnostic.txt"
    private const val MAX_FILE_SIZE = 5L * 1024 * 1024 // 5 MB


    private lateinit var logFile: File

    fun initialize(context: Context) {
        if (BuildConfig.BUILD_TYPE != DIAGNOSTIC_BUILD_TYPE) {
            return
        }
        logFile = File(context.filesDir, FILE_NAME)
        LogHelper.printd("Diagnostics file initialized")
    }

    @Synchronized
    fun write(level: String, message: String) {
        if (!::logFile.isInitialized) {
            return
        }

        try {
            rotateIfNeeded()

            logFile.appendText(
                "${Instant.now()} [$level] $message\n"
            )
        } catch (_: Exception) {
        }
    }

    fun getLastLines(lineCount: Int = 20): String {
        if (!::logFile.isInitialized || !logFile.exists()) {
            return ""
        }

        return try {
            logFile.useLines { lines ->
                lines.toList().takeLast(lineCount).joinToString("\n")
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun share(context: Context) {
        try {
            val exportDir = File(context.cacheDir, "updates")
            exportDir.mkdirs()
            val exportFile = File(exportDir, logFile.name)
            logFile.inputStream().use { input ->
                exportFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(logFile.name, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(
                sendIntent,
                context.getString(R.string.export_logs)
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
        } catch (ex: Exception) {
            LogHelper.printe(
                message = "Failed to share the diagnostics log",
                exception = ex
            )
        }
    }

    @Synchronized
    fun clear() {
        if (!::logFile.isInitialized) {
            return
        }

        try {
            logFile.delete()
        } catch (_: Exception) {
        }
    }

    private fun rotateIfNeeded() {
        if (logFile.exists() && logFile.length() >= MAX_FILE_SIZE) {
            val backupFile = File(
                logFile.parentFile,
                "diagnostic.old.log"
            )

            backupFile.delete()
            logFile.renameTo(backupFile)
        }
    }
}