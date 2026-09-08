package ca.ilianokokoro.umihi.music

import ca.ilianokokoro.umihi.music.core.youtube.YoutubeDataExtractor.extractTrackCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


class SongCountExtractorUnitTest {

    @Test
    fun `extracts track count from english text`() {
        assertEquals(170, extractTrackCount("Rus • 170 tracks"))
    }

    @Test
    fun `extracts track count from french text`() {
        assertEquals(170, extractTrackCount("Rus • 170 pistes"))
    }

    @Test
    fun `extracts track count from russian text`() {
        assertEquals(170, extractTrackCount("Rus • 170 треков"))
    }

    @Test
    fun `extracts track count from text without track label`() {
        assertEquals(170, extractTrackCount("Rus • 170"))
    }

    @Test
    fun `extracts large track count with comma separator`() {
        assertEquals(4020, extractTrackCount("Redlist - Top Songs • 4,020 tracks"))
    }

    @Test
    fun `extracts large track count with dot separator`() {
        assertEquals(4020, extractTrackCount("Redlist - Top Songs • 4.020 tracks"))
    }

    @Test
    fun `extracts track count with space separator`() {
        assertEquals(4020, extractTrackCount("Redlist - Top Songs • 4 020 tracks"))
    }

    @Test
    fun `extracts single digit count`() {
        assertEquals(5, extractTrackCount("apelleru • 5 tracks"))
    }

    @Test
    fun `extracts zero tracks`() {
        assertEquals(0, extractTrackCount("Empty playlist • 0 tracks"))
    }

    @Test
    fun `returns null when no number exists`() {
        assertNull(extractTrackCount("Your queued episodes"))
    }

    @Test
    fun `returns null for empty text`() {
        assertNull(extractTrackCount(""))
    }

    @Test
    fun `extracts track count when username contains a number`() {
        assertEquals(170, extractTrackCount("User 2026 • 170 tracks"))
    }

    @Test
    fun `extracts track count when username contains only numbers`() {
        assertEquals(1270, extractTrackCount("1,899 1000 28 • 1,270 tracks"))
    }


    @Test
    fun `extracts number regardless of localized text`() {
        assertEquals(98, extractTrackCount("ilianoKokoro • 98 morceaux"))
        assertEquals(14, extractTrackCount("ilianoKokoro • 14 canciones"))
        assertEquals(50, extractTrackCount("ilianoKokoro • 50 Titel"))
    }
}
