package ca.ilianokokoro.umihi.music.core.helpers

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import java.io.File

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
            LogHelper.printe(message = "No permission to delete $stored", exception = e)
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

}