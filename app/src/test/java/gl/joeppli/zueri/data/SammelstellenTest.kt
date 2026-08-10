package gl.joeppli.zueri.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class SammelstellenTest {

    /** A Wednesday, so the two-window weekday cases are exercised. */
    private val wednesday = LocalDate.of(2026, 8, 12)

    @Test
    fun allEntriesHaveAddressAndHours() {
        assertTrue(Sammelstellen.all.isNotEmpty())
        Sammelstellen.all.forEach {
            assertTrue("${it.id} has no address", it.address.isNotBlank())
            assertTrue("${it.id} has no opening windows", it.openingWindows.isNotEmpty())
            assertTrue("${it.id} window ends before it starts", it.openingWindows.all { w -> w.from < w.to })
        }
    }

    @Test
    fun idsAreUnique() {
        val ids = Sammelstellen.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun glarusIsOpenWednesdayEveningButNotLateAfternoon() {
        val glarus = Sammelstellen.byId("glarus")
        assertNotNull(glarus)
        // Published Wednesday hours: 13:30–15:30 and 17:00–18:00
        assertTrue(glarus!!.isOpenAt(wednesday, LocalTime.of(14, 0)))
        assertFalse(glarus.isOpenAt(wednesday, LocalTime.of(16, 0)))
        assertTrue(glarus.isOpenAt(wednesday, LocalTime.of(17, 30)))
        assertFalse(glarus.isOpenAt(wednesday, LocalTime.of(18, 0))) // end is exclusive
    }

    @Test
    fun windowsOnReturnsBothWednesdaySlotsInOrder() {
        val glarus = Sammelstellen.byId("glarus")!!
        val windows = glarus.windowsOn(DayOfWeek.WEDNESDAY)
        assertEquals(2, windows.size)
        assertEquals(LocalTime.of(13, 30), windows[0].from)
        assertEquals(LocalTime.of(17, 0), windows[1].from)
    }

    @Test
    fun nextWindowTodayFindsTheEveningSlot() {
        val glarus = Sammelstellen.byId("glarus")!!
        val next = glarus.nextWindowToday(wednesday, LocalTime.of(16, 0))
        assertEquals(LocalTime.of(17, 0), next?.from)
        assertNull(glarus.nextWindowToday(wednesday, LocalTime.of(19, 0)))
    }

    @Test
    fun sundayIsClosedEverywhere() {
        val sunday = LocalDate.of(2026, 8, 16)
        Sammelstellen.all.forEach {
            assertTrue("${it.id} claims Sunday hours", it.windowsOn(sunday.dayOfWeek).isEmpty())
        }
    }
}
