package gl.joeppli.zueri.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.theme.Dimens
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import gl.joeppli.zueri.ui.components.PageHeader
import java.util.Locale

@Composable
fun DashboardScreen(
    onOrderClick: () -> Unit
) {
    val stats by RecyclingRepository.stats.collectAsState()
    val scrollState = rememberScrollState()
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()

    val carKm = (stats.co2Saved * 7).toInt()
    val treesPlanted = String.format(Locale.ROOT, "%.1f", stats.co2Saved * 0.05f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens.screenH)
            .padding(top = Dimens.screenTop, bottom = Dimens.screenBottom),
        verticalArrangement = Arrangement.spacedBy(Dimens.section)
    ) {
        PageHeader(
            title = "Recycling Dashboard",
            subtitle = if (lang == "en") "Your contribution to Glarus's circular economy" else "Din Biitrag zur Glarner Chreislaufwirtschaft"
        )

        // Personal summary — what this resident has handed over. No score,
        // no levels: a municipal service reports, it doesn't compete.
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = Dimens.cardHero,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.section),
                verticalArrangement = Arrangement.spacedBy(Dimens.gapSm)
            ) {
                Text(
                    text = if (lang == "en") "Recycled with Jöppli" else "Mit em Jöppli recyclet",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.ROOT, "%.1f", stats.totalKg),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 42.sp
                    )
                    Text(
                        text = " kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = if (stats.pickupCount == 1) {
                        if (lang == "en") "over 1 collection" else "über 1 Abholig"
                    } else {
                        if (lang == "en") "over ${stats.pickupCount} collections" else "über ${stats.pickupCount} Abholige"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Three-Column Quick Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.gapMd)
        ) {
            MetricCard(
                label = if (lang == "en") "TOTAL RECYCLED" else "TOTAL RECYCLET",
                value = String.format(Locale.ROOT, "%.1f kg", stats.totalKg),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = if (lang == "en") "CO2 SAVED" else "CO2 GSPART",
                value = String.format(Locale.ROOT, "-%.0f kg", stats.co2Saved),
                valueColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        // Umwelteffekt Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = Dimens.card,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg)) {
                Text(
                    text = if (lang == "en") "Environmental Impact" else "Umwelteffekt",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(Dimens.gapMd))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.gapLg)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "CAR TRIPS AVOIDED" else "AUTOFAHRTE VERMIEDE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$carKm ${if (lang == "en") "km of driving" else "km Autofahrt"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "TREES PLANTED" else "BÄUM PFLANZT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$treesPlanted ${if (lang == "en") "trees" else "Bäum"}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Categories Breakdown Progress bars
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = Dimens.card,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Dimens.gapLg)) {
                Text(
                    text = if (lang == "en") "Recycled Materials Breakdown" else "Wertstoff ufteilt",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(Dimens.gapLg))

                val cats = stats.categories
                CategoryBar(if (lang == "en") "Paper / Cardboard" else "Papier / Karton", cats.cardboard, stats.totalKg, Color(0xFF3B82F6))
                CategoryBar(if (lang == "en") "Glass" else "Altglas", cats.glass, stats.totalKg, Color(0xFF10B981))
                CategoryBar(if (lang == "en") "Aluminum / Metal" else "Alu / Metall", cats.aluminum, stats.totalKg, Color(0xFFF59E0B))
                CategoryBar(if (lang == "en") "Compost / Organic" else "Biogut / Kompost", cats.bio, stats.totalKg, Color(0xFF059669))
                CategoryBar(if (lang == "en") "PET / Plastic" else "PET / Plastik", cats.pet, stats.totalKg, Color(0xFF8B5CF6))
            }
        }

        // Order Button CTA
        Button(
            onClick = { onOrderClick() },
            shape = Dimens.ctaShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.ctaHeight)
        ) {
            Text(if (lang == "en") "Request a collection" else "Abholig aafroge", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dimens.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.gapLg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.gapXs))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.secondary else valueColor
            )
        }
    }
}

@Composable
fun CategoryBar(
    name: String,
    value: Float,
    total: Float,
    barColor: Color
) {
    Column(modifier = Modifier.padding(vertical = Dimens.gapSm)) {
        val percentage = if (total > 0f) (value / total * 100).toInt() else 0
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format(Locale.ROOT, "%.1f kg (%d%%)", value, percentage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (total > 0f) value / total else 0f },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
