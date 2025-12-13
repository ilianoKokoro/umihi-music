package ca.ilianokokoro.umihi.music.models

import ca.ilianokokoro.umihi.music.data.repositories.DatastoreRepository.UpdateChannel

data class UmihiSettings(
    val updateChannel: UpdateChannel = UpdateChannel.Stable,
    val cookies: Cookies
)