package gl.joeppli.zueri.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class JoeppliServiceTest {

    private val tuesday = LocalDate.of(2026, 8, 11)
    private val wednesday = LocalDate.of(2026, 8, 12)
    private val thursday = LocalDate.of(2026, 8, 13)

    @Test
    fun runsOnlyOnOperatingDays() {
        assertTrue(JoeppliService.operatesOn(tuesday))
        assertFalse(JoeppliService.operatesOn(wednesday))
        assertTrue(JoeppliService.operatesOn(thursday))
    }

    @Test
    fun windowsAvoidCommuterPeaks() {
        // Nothing may run during the 07:00–09:00 or 17:00–19:00 rush.
        val morningRush = LocalTime.of(8, 0)
        val eveningRush = LocalTime.of(18, 0)
        JoeppliService.windows.forEach {
            assertFalse("${it.id} overlaps the morning rush", it.contains(morningRush))
            assertFalse("${it.id} overlaps the evening rush", it.contains(eveningRush))
        }
    }

    private fun JoeppliService.ServiceWindow.contains(t: LocalTime) =
        !t.isBefore(from) && t.isBefore(to)

    @Test
    fun upcomingDatesOnlyReturnsOperatingDays() {
        val dates = JoeppliService.upcomingDates(wednesday, 3)
        assertEquals(3, dates.size)
        dates.forEach { assertTrue(it.dayOfWeek in JoeppliService.operatingDays) }
        // Wednesday is not a service day, so the first offer is Thursday.
        assertEquals(DayOfWeek.THURSDAY, dates.first().dayOfWeek)
    }

    @Test
    fun nextSlotSkipsWindowsThatAlreadyClosed() {
        // Tuesday lunchtime: the morning window is over, evening is next.
        val (date, window) = JoeppliService.nextSlot(tuesday, LocalTime.of(12, 0))!!
        assertEquals(tuesday, date)
        assertEquals("evening", window.id)
    }

    @Test
    fun nextSlotRollsToTheFollowingServiceDay() {
        // Tuesday late night: nothing left today, so Thursday morning.
        val (date, window) = JoeppliService.nextSlot(tuesday, LocalTime.of(23, 0))!!
        assertEquals(thursday, date)
        assertEquals("morning", window.id)
    }

    @Test
    fun isCollectingNowTracksTheWindows() {
        assertTrue(JoeppliService.isCollectingNow(tuesday, LocalTime.of(10, 0)))
        assertFalse(JoeppliService.isCollectingNow(tuesday, LocalTime.of(15, 0)))
        // Right time, wrong day.
        assertFalse(JoeppliService.isCollectingNow(wednesday, LocalTime.of(10, 0)))
    }
}
