package ca.ilianokokoro.umihi.music.core

import okhttp3.OkHttpClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object UmihiHttpClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15.seconds)
            .readTimeout(30.seconds)
            .writeTimeout(30.seconds)
            .callTimeout(45.seconds)
            .retryOnConnectionFailure(true)
            .build()
    }

    val downloadClient: OkHttpClient by lazy {
        client.newBuilder()
            .connectTimeout(20.seconds)
            .readTimeout(2.minutes)
            .writeTimeout(2.minutes)
            .callTimeout(0.seconds)
            .retryOnConnectionFailure(true)
            .build()
    }
}
