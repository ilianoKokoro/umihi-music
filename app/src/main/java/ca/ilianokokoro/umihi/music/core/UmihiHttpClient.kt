package ca.ilianokokoro.umihi.music.core

import okhttp3.OkHttpClient

object UmihiHttpClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }
}