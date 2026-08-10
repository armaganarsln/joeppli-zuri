package gl.joeppli.zueri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import gl.joeppli.zueri.data.Municipality
import gl.joeppli.zueri.data.OpeningWindow
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.data.Sammelstelle
import gl.joeppli.zueri.data.Sammelstellen
import gl.joeppli.zueri.data.isOpenAt
import gl.joeppli.zueri.data.nextWindowToday
import gl.joeppli.zueri.data.windowsOn
import gl.joeppli.zueri.theme.Dimens
import gl.joeppli.zueri.ui.components.PageHeader
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * The fixed municipal collection points: where they are, when they're open,
 * and whether you can go right now.
 *
 * Jöppli is the mobile counterpart to these, so residents need both in one
 * place — this screen is the "already built in" recycling map.
 */
@Composable
fun SammelstellenScreen() {
    val lang by RecyclingRepository.userLanguage.collectAsState()
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    // Re-evaluate open/closed on a slow tick so the badges don't go stale
    // while the screen is open.
    var now by remember { mutableStateOf(LocalTime.now()) }
    val today = remember { LocalDate.now() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = LocalTime.now()
        }
    }

    val points = remember { Sammelstellen.all }
    val openCount = points.count { it.isOpenAt(today, now) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = Dimens.screenH,
            end = Dimens.screenH,
            top = Dimens.screenTop,
            bottom = Dimens.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.gapMd)
    ) {
        item(key = "header") {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.section),
                modifier = Modifier.padding(bottom = Dimens.gapMd)
            ) {
                PageHeader(
                    title = if (lang == "en") "Collection points" else "Sammelstelle",
                    subtitle = if (lang == "en") "$openCount of ${points.size} open right now"
                    else "$openCount vo ${points.size} jetzt offe"
                )
                SammelstellenMap(points)
            }
        }

        items(points, key = { it.id }) { point ->
            SammelstelleCard(
                point = point,
                lang = lang,
                today = today,
                now = now,
                expanded = expandedId == point.id,
                onToggle = { expandedId = if (expandedId == point.id) null else point.id }
            )
        }

        item(key = "source") {
            Text(
                text = if (lang == "en")
                    "Hours published by Gemeinde Glarus and Gemeinde Glarus Nord. Closed on public holidays."
                else
                    "Öffnigszite vo de Gmeinde Glarus und Glarus Nord. A Fest- und Feiertag gschlosse.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.gapSm)
            )
        }
    }
}

@Composable
private fun SammelstellenMap(points: List<Sammelstelle>) {
    val cameraPositionState = rememberCameraPositionState {
        // Centred on the valley so both Glarus and Glarus Nord are in frame.
        position = CameraPosition.fromLatLngZoom(LatLng(47.080, 9.080), 10.5f)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(Dimens.cardHero)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            points.forEach { point ->
                Marker(
                    state = MarkerState(position = LatLng(point.approxLat, point.approxLng)),
                    title = point.village,
                    snippet = point.address
                )
            }
        }
    }
}

@Composable
private fun SammelstelleCard(
    point: Sammelstelle,
    lang: String,
    today: LocalDate,
    now: LocalTime,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val isOpen = point.isOpenAt(today, now)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dimens.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(Dimens.gapLg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.gapXs)
                ) {
                    Text(
                        text = point.village,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = point.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                ) {
                    OpenBadge(isOpen = isOpen, lang = lang)
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.gapSm))
            TodayLine(point = point, lang = lang, today = today, now = now)

            if (expanded) {
                Spacer(modifier = Modifier.height(Dimens.gapMd))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(Dimens.gapMd))
                WeekHours(point = point, lang = lang, today = today)
                point.note?.let {
                    Spacer(modifier = Modifier.height(Dimens.gapSm))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.gapSm))
                Text(
                    text = point.municipality.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OpenBadge(isOpen: Boolean, lang: String) {
    val container = if (isOpen) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHighest
    val content = if (isOpen) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .background(container, Dimens.chipSmall)
            .padding(horizontal = Dimens.gapSm, vertical = Dimens.gapXs)
    ) {
        Text(
            text = if (isOpen) (if (lang == "en") "OPEN" else "OFFE")
            else (if (lang == "en") "CLOSED" else "ZUE"),
            style = MaterialTheme.typography.labelSmall,
            color = content
        )
    }
}

/** "Open until 15:30" / "Opens at 17:00" / "Closed today". */
@Composable
private fun TodayLine(point: Sammelstelle, lang: String, today: LocalDate, now: LocalTime) {
    val current = point.windowsOn(today.dayOfWeek).firstOrNull { it.contains(now) }
    val next = point.nextWindowToday(today, now)
    val text = when {
        current != null ->
            if (lang == "en") "Open until ${current.to.hhmm()}" else "Offe bis ${current.to.hhmm()}"
        next != null ->
            if (lang == "en") "Opens at ${next.from.hhmm()}" else "Macht um ${next.from.hhmm()} uf"
        point.windowsOn(today.dayOfWeek).isEmpty() ->
            if (lang == "en") "Closed today" else "Hüt gschlosse"
        else ->
            if (lang == "en") "Closed for today" else "Hüt scho zue"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.gapSm)
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun WeekHours(point: Sammelstelle, lang: String, today: LocalDate) {
    val locale = if (lang == "en") Locale.ENGLISH else Locale.GERMAN
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapXs)) {
        DayOfWeek.entries.forEach { day ->
            val windows = point.windowsOn(day)
            val isToday = day == today.dayOfWeek
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (windows.isEmpty()) "–"
                    else windows.joinToString(", ") { it.range() },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun LocalTime.hhmm(): String = "%02d:%02d".format(hour, minute)
private fun OpeningWindow.range(): String = "${from.hhmm()}–${to.hhmm()}"
