package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import ca.ilianokokoro.umihi.music.core.Constants
import ca.ilianokokoro.umihi.music.core.helpers.LogHelper.printe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

object FileHelper {
    private val INVALID_CHARS = setOf('<', '>', ':', '"', '/', '\\', '|', '*', '?')

    fun deleteStoredFile(context: Context, stored: String?) {
        if (stored.isNullOrBlank()) {
            return
        }
        val uri = stored.toUri()
        try {
            when (uri.scheme) {
                "content" -> DocumentFile.fromSingleUri(context, uri)?.delete()
                "file" -> uri.path?.let { File(it).delete() }
                else -> File(stored).delete()
            }
        } catch (e: SecurityException) {
            printe(message = "No permission to delete $stored", exception = e)
        }
    }

    fun String.sanitizeFilename(replacement: String = "_"): String {
        val safe = this.map { character ->
            if (character.code == 0 || character < ' ' || character in INVALID_CHARS) {
                replacement
            } else {
                character
            }
        }.joinToString("")
            .trim(' ', '.', '_')          // no leading/trailing . _ or space
            .take(255)                    // ext4 / FAT cap is 255 bytes

        return safe
    }

    fun moveFile(
        context: Context,
        storedPath: String,
        newSafDir: DocumentFile?,
        mimeType: String
    ): String {
        val sourceUri = storedPath.toUri()
        val isSourceInternal = sourceUri.scheme == "file" || sourceUri.scheme == null

        return when {
            isSourceInternal && newSafDir != null -> {
                val sourceFile = File(sourceUri.path ?: storedPath)
                if (!sourceFile.exists()) return storedPath
                val fileName = sourceFile.name
                val target = newSafDir.findFile(fileName)
                    ?: newSafDir.createFile(mimeType, fileName.substringBeforeLast('.'))
                    ?: throw IOException("Failed to create file in new download folder")
                context.contentResolver.openOutputStream(target.uri, "wt")?.use { out ->
                    sourceFile.inputStream().use { it.copyTo(out) }
                } ?: throw IOException("Failed to open output stream")
                sourceFile.delete()
                target.uri.toString()
            }

            !isSourceInternal && newSafDir == null -> {
                val sourceDoc = DocumentFile.fromSingleUri(context, sourceUri)
                    ?: throw IOException("Failed to access source file")
                val audioDir = UmihiHelper.getDownloadDirectory(
                    context,
                    Constants.Downloads.AUDIO_FILES_FOLDER
                )
                val targetFile = File(audioDir, sourceDoc.name ?: return storedPath)
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    targetFile.outputStream().use { output -> input.copyTo(output) }
                } ?: throw IOException("Failed to open input stream")
                sourceDoc.delete()
                targetFile.absolutePath
            }

            isSourceInternal && newSafDir == null -> {
                val sourceFile = File(sourceUri.path ?: storedPath)
                if (!sourceFile.exists()) return storedPath
                val audioDir = UmihiHelper.getDownloadDirectory(
                    context,
                    Constants.Downloads.AUDIO_FILES_FOLDER
                )
                val targetFile = File(audioDir, sourceFile.name)
                if (sourceFile.absolutePath == targetFile.absolutePath) return storedPath
                sourceFile.copyTo(targetFile, overwrite = true)
                sourceFile.delete()
                targetFile.absolutePath
            }

            else -> {
                val sourceDoc = DocumentFile.fromSingleUri(context, sourceUri)
                    ?: throw IOException("Failed to access source file")
                val fileName = sourceDoc.name ?: return storedPath
                val target = newSafDir?.findFile(fileName)
                    ?: newSafDir?.createFile(mimeType, fileName.substringBeforeLast('.'))
                    ?: throw IOException("Failed to create file in new download folder")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    context.contentResolver.openOutputStream(target.uri, "wt")
                        ?.use { out -> input.copyTo(out) }
                } ?: throw IOException("Failed to open input stream")
                sourceDoc.delete()
                target.uri.toString()
            }
        }
    }

    fun releaseOldPermission(context: Context, oldLocation: Uri?) {
        oldLocation ?: return
        try {
            context.contentResolver.releasePersistableUriPermission(
                oldLocation,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            printe(message = "Failed to release old permission", exception = e)
        }
    }

    suspend fun getDownloadFolderSize(context: Context, downloadLocation: Uri?): Long =
        withContext(Dispatchers.IO) {
            val imageDir =
                UmihiHelper.getDownloadDirectory(context, Constants.Downloads.THUMBNAILS_FOLDER)
            val thumbSize = imageDir.folderSizeSync()

            val audioSize = if (downloadLocation != null) {
                getSAFDirSize(context, downloadLocation)
            } else {
                UmihiHelper.getDownloadDirectory(context, Constants.Downloads.AUDIO_FILES_FOLDER)
                    .folderSizeSync()
            }

            audioSize + thumbSize
        }

    private fun getSAFDirSize(context: Context, treeUri: Uri): Long {
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)

        var totalSize = 0L
        var cursor: Cursor? = null

        try {
            cursor = context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_SIZE
                ),
                null,
                null,
                null
            )

            cursor?.let {
                val idIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val sizeIndex = it.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                while (it.moveToNext()) {
                    val childId = it.getString(idIndex)
                    val size = it.getLong(sizeIndex)

                    if (size >= 0) {
                        totalSize += size
                    } else {
                        val childUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, childId)
                        val doc = DocumentFile.fromSingleUri(context, childUri)
                        if (doc?.isDirectory == true) {
                            totalSize += getSAFDirSize(context, childUri)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            printe(message = "Failed to calculate SAF folder size", exception = e)
        } finally {
            cursor?.close()
        }

        return totalSize
    }

    suspend fun clearDownloadFolder(context: Context, downloadLocation: Uri?): Unit =
        withContext(Dispatchers.IO) {
            if (downloadLocation != null) {
                val safDir = DocumentFile.fromTreeUri(context, downloadLocation)
                safDir?.listFiles()?.forEach { file ->
                    if (file.name?.endsWith(".webm") == true) {
                        file.delete()
                    }
                }
            } else {
                UmihiHelper.getDownloadDirectory(context, Constants.Downloads.AUDIO_FILES_FOLDER)
                    .deleteRecursively()
            }

            UmihiHelper.getDownloadDirectory(context, Constants.Downloads.THUMBNAILS_FOLDER)
                .deleteRecursively()
        }

    private fun File.folderSizeSync(): Long {
        return if (!exists()) {
            0L
        } else {
            walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        }
    }

}