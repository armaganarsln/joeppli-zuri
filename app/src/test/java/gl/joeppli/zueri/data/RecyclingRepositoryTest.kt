package gl.joeppli.zueri.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RecyclingRepository is a process-wide singleton, so every test sets up the
 * state it needs and asserts on relative changes — no assumptions about
 * execution order.
 */
class RecyclingRepositoryTest {

    @Test
    fun emailLogin_setsLoggedInProfile() {
        RecyclingRepository.loginWithEmail("Anna Muster", "anna@example.ch")
        val profile = RecyclingRepository.userProfile.value
        assertTrue(profile.isLoggedIn)
        assertEquals("Anna Muster", profile.name)
        assertEquals("anna@example.ch", profile.email)
        assertEquals("EMAIL", profile.authType)
    }

    @Test
    fun phoneLogin_setsPhoneAuth() {
        RecyclingRepository.loginWithPhone("+41 79 123 45 67")
        val profile = RecyclingRepository.userProfile.value
        assertTrue(profile.isLoggedIn)
        assertEquals("+41 79 123 45 67", profile.phone)
        assertEquals("PHONE", profile.authType)
    }

    @Test
    fun logout_resetsProfile() {
        RecyclingRepository.loginWithEmail("Anna", "anna@example.ch")
        RecyclingRepository.logout()
        val profile = RecyclingRepository.userProfile.value
        assertFalse(profile.isLoggedIn)
        assertEquals("", profile.name)
        assertNull(profile.authType)
    }

    @Test
    fun registerAddress_setsHomeAndInvoiceAddress() {
        RecyclingRepository.loginWithPhone("+41 79 123 45 67")
        RecyclingRepository.registerAddress("Bahnhofstrasse 1, 8001 Zürich")
        val profile = RecyclingRepository.userProfile.value
        assertEquals("Bahnhofstrasse 1, 8001 Zürich", profile.homeAddress)
        assertEquals("Bahnhofstrasse 1, 8001 Zürich", profile.invoiceAddress)
    }

    @Test
    fun setLanguage_switchesLanguage() {
        RecyclingRepository.setLanguage("en")
        assertEquals("en", RecyclingRepository.userLanguage.value)
        RecyclingRepository.setLanguage("de")
        assertEquals("de", RecyclingRepository.userLanguage.value)
    }

    @Test
    fun updateProfile_storesContactDetails() {
        RecyclingRepository.loginWithEmail("Anna", "anna@example.ch")
        RecyclingRepository.updateProfile(
            name = "Anna Muster",
            phone = "+41 79 000 00 00",
            homeAddr = "Hauptstrasse 10, 8750 Glarus"
        )
        val profile = RecyclingRepository.userProfile.value
        assertEquals("Anna Muster", profile.name)
        assertEquals("+41 79 000 00 00", profile.phone)
        assertEquals("Hauptstrasse 10, 8750 Glarus", profile.homeAddress)
    }

    @Test
    fun addPickup_recordsOrderAndGrowsStats() {
        val before = RecyclingRepository.stats.value
        RecyclingRepository.addPickup(
            address = "Hauptstrasse 1, 8750 Glarus",
            dateString = "12.06.2026",
            timeSlot = "09:00 – 11:00",
            materials = listOf("Altglas", "Papier/Karton")
        )
        val pickup = RecyclingRepository.lastPickup.value
        assertNotNull(pickup)
        assertEquals(listOf("Altglas", "Papier/Karton"), pickup!!.materials)

        val after = RecyclingRepository.stats.value
        assertTrue(after.totalKg > before.totalKg)
        assertTrue(after.co2Saved > before.co2Saved)
        assertEquals(before.pickupCount + 1, after.pickupCount)
    }

    @Test
    fun addPickup_countsEveryRequest() {
        val before = RecyclingRepository.stats.value.pickupCount
        repeat(3) {
            RecyclingRepository.addPickup(
                address = "Hauptstrasse 1, 8750 Glarus",
                dateString = "12.06.2026",
                timeSlot = "09:00 – 11:00",
                materials = listOf("Altglas", "Papier/Karton", "Alu/Metall")
            )
        }
        assertEquals(before + 3, RecyclingRepository.stats.value.pickupCount)
    }
}
