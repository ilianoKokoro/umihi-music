package ca.ilianokokoro.umihi.music.core

import fuel.FuelBuilder
import fuel.HttpLoader
import okhttp3.OkHttpClient

object UmihiHttpClient {
    val fuelClient: HttpLoader by lazy {
        FuelBuilder().config(client).build()
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .build()
    }
}