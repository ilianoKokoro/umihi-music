package ca.ilianokokoro.umihi.music.extensions

import android.net.Uri
import android.provider.DocumentsContract


fun Uri.folderDisplayPath(): String? {
    val docId = DocumentsContract.getTreeDocumentId(this)
    val path = docId.substringAfter(':', "")
    return path.ifEmpty { null }
}