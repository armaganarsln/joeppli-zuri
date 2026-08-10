package gl.joeppli.zueri.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * When and where the mobile Sammelstelle runs.
 *
 * Jöppli is a municipal service, not an on-demand one: it drives on set days,
 * and only outside commuter traffic — a window in the late morning and one in
 * the evening. It complements the fixed [Sammelstellen] rather than replacing
 * them, so residents who cannot make a collection point's opening hours still
 * have a way to hand over their recycling.
 *
 * These windows are the service's operating policy, not published third-party
 * data — adjust them here if the municipality sets a different schedule.
 */
object JoeppliService {

    /** Depot the vehicle departs from. */
    const val DEPOT_NAME = "Werkhof Glarus"
    const val DEPOT_ADDRESS = "Grubenstrasse, 8750 Glarus"
    const val DEPOT_LAT = 47.040
    const val DEPOT_LNG = 9.068

    /** Days the service operates. Deliberately not every day. */
    val operatingDays: Set<DayOfWeek> = setOf(
        DayOfWeek.TUESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.SATURDAY
    )

    /**
     * A named collection window. Both sit outside the morning and evening
     * commuter peaks so the vehicle never adds to rush-hour traffic.
     */
    data class ServiceWindow(
        val id: String,
        val from: LocalTime,
        val to: LocalTime,
        val labelDe: String,
        val labelEn: String
    ) {
        fun label(lang: String) = if (lang == "en") labelEn else labelDe
        fun timeRange() = "%02d:%02d – %02d:%02d".format(from.hour, from.minute, to.hour, to.minute)
    }

    val windows: List<ServiceWindow> = listOf(
        ServiceWindow(
            id = "morning",
            from = LocalTime.of(9, 0),
            to = LocalTime.of(11, 0),
            labelDe = "Vormittag",
            labelEn = "Late morning"
        ),
        ServiceWindow(
            id = "evening",
            from = LocalTime.of(19, 0),
            to = LocalTime.of(21, 0),
            labelDe = "Obig",
            labelEn = "Evening"
        )
    )

    fun windowById(id: String): ServiceWindow? = windows.firstOrNull { it.id == id }

    fun operatesOn(date: LocalDate): Boolean = date.dayOfWeek in operatingDays

    /**
     * The next [count] service dates from [from] inclusive, so the request flow
     * can offer only days the vehicle actually runs.
     */
    fun upcomingDates(from: LocalDate, count: Int = 3): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var cursor = from
        // 21 days is comfortably more than enough to find 3 of 3 weekly days.
        var guard = 0
        while (dates.size < count && guard < 21) {
            if (operatesOn(cursor)) dates += cursor
            cursor = cursor.plus(1, ChronoUnit.DAYS)
            guard++
        }
        return dates
    }

    /**
     * The next window the vehicle is collecting in, as a (date, window) pair.
     * Skips windows that have already closed today.
     */
    fun nextSlot(date: LocalDate, time: LocalTime): Pair<LocalDate, ServiceWindow>? {
        if (operatesOn(date)) {
            windows.firstOrNull { time.isBefore(it.to) }?.let { return date to it }
        }
        val nextDay = upcomingDates(date.plus(1, ChronoUnit.DAYS), 1).firstOrNull() ?: return null
        return nextDay to windows.first()
    }

    /** True while the vehicle is actively collecting. */
    fun isCollectingNow(date: LocalDate, time: LocalTime): Boolean =
        operatesOn(date) && windows.any { !time.isBefore(it.from) && time.isBefore(it.to) }
}
