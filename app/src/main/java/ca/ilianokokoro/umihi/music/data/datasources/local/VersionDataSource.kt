package ca.ilianokokoro.umihi.music.data.datasources.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ca.ilianokokoro.umihi.music.models.Version

@Dao
interface VersionDataSource {
    @Query("SELECT * FROM versions")
    suspend fun getIgnoredVersions(): List<Version>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun ignoreVersion(version: Version)

    @Query("DELETE FROM versions")
    suspend fun deleteAll()
}