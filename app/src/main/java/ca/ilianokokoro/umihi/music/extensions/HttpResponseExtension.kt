package ca.ilianokokoro.umihi.music.extensions

import fuel.HttpResponse
import kotlinx.io.readByteArray
import kotlinx.io.readString


val HttpResponse.body: String
    get() = source.readString()


val HttpResponse.byteBody: ByteArray
    get() = source.readByteArray()