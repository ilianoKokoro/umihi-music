package ca.ilianokokoro.umihi.music.core.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DatabaseEvents {
    private val _destructiveMigrationOccurred = MutableStateFlow(false)
    val destructiveMigrationOccurred = _destructiveMigrationOccurred.asStateFlow()

    fun notifyDestructiveMigration() {
        _destructiveMigrationOccurred.value = true
    }

    fun consume() {
        _destructiveMigrationOccurred.value = false
    }
}