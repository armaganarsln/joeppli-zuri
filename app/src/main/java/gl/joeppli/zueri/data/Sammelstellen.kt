package gl.joeppli.zueri.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * A single opening window on one weekday.
 *
 * Sammelstellen commonly open twice a day (e.g. a midday slot plus an evening
 * one), so a weekday can carry several of these.
 */
data class OpeningWindow(
    val day: DayOfWeek,
    val from: LocalTime,
    val to: LocalTime
) {
    fun contains(time: LocalTime): Boolean = !time.isBefore(from) && time.isBefore(to)
}

/** A fixed municipal collection point. */
data class Sammelstelle(
    val id: String,
    val village: String,
    val address: String,
    val municipality: Municipality,
    val openingWindows: List<OpeningWindow>,
    /**
     * Approximate village-centre coordinates, used only to place the map pin
     * before [address] is geocoded. Not survey-grade — the address is the
     * authoritative field.
     */
    val approxLat: Double,
    val approxLng: Double,
    val note: String? = null
)

enum class Municipality(val label: String) {
    GLARUS("Glarus"),
    GLARUS_NORD("Glarus Nord")
}

/**
 * Real collection points published by the municipalities.
 *
 * Sources (fetched 2026-08-10):
 *  - Gemeinde Glarus, "Gemeindesammelstellen"
 *    https://www.glarus.ch/verwaltung/bau-und-versorgung/unterhaltsdienst/entsorgung-copy-1/gemeindesammelstellen.html/6406
 *  - Gemeinde Glarus Nord, "Sammelstellen"
 *    https://www.glarus-nord.ch/online-schalter/bau-und-umwelt/sammelstellen.html/7185
 *
 * Glarus Süd does not publish per-point opening hours, so it is not covered
 * here. All points close on public holidays ("Fest- und Feiertage").
 *
 * Hours change — re-check the sources above before a release.
 */
object Sammelstellen {

    private fun w(day: DayOfWeek, from: String, to: String) =
        OpeningWindow(day, LocalTime.parse(from), LocalTime.parse(to))

    private val MO = DayOfWeek.MONDAY
    private val DI = DayOfWeek.TUESDAY
    private val MI = DayOfWeek.WEDNESDAY
    private val DO = DayOfWeek.THURSDAY
    private val FR = DayOfWeek.FRIDAY
    private val SA = DayOfWeek.SATURDAY

    val all: List<Sammelstelle> = listOf(
        // ---- Gemeinde Glarus ----
        Sammelstelle(
            id = "glarus",
            village = "Glarus",
            address = "Grubenstrasse, 8750 Glarus",
            municipality = Municipality.GLARUS,
            openingWindows = listOf(
                w(MO, "13:30", "15:30"),
                w(DI, "11:00", "13:00"),
                w(MI, "13:30", "15:30"), w(MI, "17:00", "18:00"),
                w(FR, "13:30", "15:30"),
                w(SA, "10:00", "12:00")
            ),
            approxLat = 47.040, approxLng = 9.068
        ),
        Sammelstelle(
            id = "netstal",
            village = "Netstal",
            address = "Oberes Langgetli 5, 8754 Netstal",
            municipality = Municipality.GLARUS,
            openingWindows = listOf(
                w(MO, "10:00", "12:00"),
                w(MI, "10:00", "12:00"), w(MI, "17:00", "18:00"),
                w(DO, "11:00", "13:00"),
                w(FR, "10:00", "12:00"),
                w(SA, "07:30", "09:30")
            ),
            approxLat = 47.056, approxLng = 9.055
        ),
        Sammelstelle(
            id = "riedern",
            village = "Riedern",
            address = "Fabrikstrasse, 8756 Riedern",
            municipality = Municipality.GLARUS,
            openingWindows = listOf(
                w(MO, "07:30", "09:30"),
                w(MI, "16:00", "18:00"),
                w(FR, "07:30", "09:30"),
                w(SA, "11:15", "13:00")
            ),
            approxLat = 47.047, approxLng = 9.053
        ),
        Sammelstelle(
            id = "ennenda",
            village = "Ennenda",
            address = "Tschachlistrasse, 8755 Ennenda",
            municipality = Municipality.GLARUS,
            openingWindows = listOf(
                w(MO, "16:00", "18:00"),
                w(MI, "07:30", "09:30"),
                w(FR, "16:00", "18:00"),
                w(SA, "09:00", "11:00")
            ),
            approxLat = 47.036, approxLng = 9.080
        ),

        // ---- Gemeinde Glarus Nord ----
        Sammelstelle(
            id = "bilten",
            village = "Bilten",
            address = "Werkhof, Sägestrasse 11, 8865 Bilten",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(DI, "09:30", "11:00"),
                w(DO, "09:30", "11:00"),
                w(SA, "09:30", "11:00")
            ),
            approxLat = 47.165, approxLng = 9.009
        ),
        Sammelstelle(
            id = "niederurnen",
            village = "Niederurnen",
            address = "Werkhof, Bahnhofstrasse 1, 8867 Niederurnen",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(MO, "13:00", "14:00"),
                w(DI, "13:00", "14:00"),
                w(MI, "13:00", "14:00"), w(MI, "18:00", "19:00"),
                w(DO, "13:00", "14:00"),
                w(FR, "13:00", "14:00"),
                w(SA, "09:30", "11:00")
            ),
            approxLat = 47.107, approxLng = 9.055
        ),
        Sammelstelle(
            id = "oberurnen",
            village = "Oberurnen",
            address = "Landstrasse 20a, 8868 Oberurnen",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(DI, "09:30", "11:00"),
                w(DO, "09:30", "11:00"),
                w(SA, "09:30", "11:00")
            ),
            approxLat = 47.096, approxLng = 9.059
        ),
        Sammelstelle(
            id = "naefels",
            village = "Näfels",
            address = "Werkhof, Burgstrasse 17, 8752 Näfels",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(MO, "14:00", "15:00"),
                w(DI, "14:00", "15:00"),
                w(MI, "14:00", "15:00"), w(MI, "18:00", "19:00"),
                w(DO, "14:00", "15:00"),
                w(FR, "14:00", "15:00"),
                w(SA, "09:30", "11:00")
            ),
            approxLat = 47.096, approxLng = 9.064
        ),
        Sammelstelle(
            id = "mollis",
            village = "Mollis",
            address = "Werkhof Jordan, Baumgartenstrasse 1a, 8753 Mollis",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(MO, "09:30", "11:00"),
                w(MI, "09:30", "11:00"),
                w(FR, "09:30", "11:00"),
                w(SA, "09:30", "11:00")
            ),
            approxLat = 47.091, approxLng = 9.075
        ),
        Sammelstelle(
            id = "obstalden",
            village = "Obstalden",
            address = "Kerenzerbergstrasse 39, 8873 Obstalden",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(MO, DI, MI, DO, FR, SA).flatMap {
                listOf(w(it, "09:00", "12:00"), w(it, "14:00", "18:00"))
            },
            approxLat = 47.119, approxLng = 9.187
        ),
        Sammelstelle(
            id = "muehlehorn",
            village = "Mühlehorn",
            address = "Dörflistrasse 44, 8874 Mühlehorn",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(MO, DI, MI, DO, FR, SA).flatMap {
                listOf(w(it, "09:00", "12:00"), w(it, "14:00", "18:00"))
            },
            approxLat = 47.128, approxLng = 9.172,
            note = "videoüberwacht"
        ),
        Sammelstelle(
            id = "filzbach",
            village = "Filzbach",
            address = "Kerenzerbergstrasse 69, 8872 Filzbach",
            municipality = Municipality.GLARUS_NORD,
            openingWindows = listOf(
                w(MO, "13:30", "15:00"),
                w(MI, "13:30", "15:00"),
                w(FR, "13:30", "15:00"),
                w(SA, "09:30", "11:30")
            ),
            approxLat = 47.115, approxLng = 9.155,
            note = "videoüberwacht"
        )
    )

    fun byId(id: String): Sammelstelle? = all.firstOrNull { it.id == id }
}

/** Windows for [day], earliest first. */
fun Sammelstelle.windowsOn(day: DayOfWeek): List<OpeningWindow> =
    openingWindows.filter { it.day == day }.sortedBy { it.from }

/** True when the point is open at [time] on [date]'s weekday. */
fun Sammelstelle.isOpenAt(date: LocalDate, time: LocalTime): Boolean =
    windowsOn(date.dayOfWeek).any { it.contains(time) }

/** The next window that starts after [time] today, if any. */
fun Sammelstelle.nextWindowToday(date: LocalDate, time: LocalTime): OpeningWindow? =
    windowsOn(date.dayOfWeek).firstOrNull { it.from.isAfter(time) }
