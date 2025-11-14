package ca.ilianokokoro.umihi.music.models

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import ca.ilianokokoro.umihi.music.core.Constants
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Entity(tableName = Constants.Database.VERSIONS_TABLE)
data class Version(
    @PrimaryKey
    val name: String,
)