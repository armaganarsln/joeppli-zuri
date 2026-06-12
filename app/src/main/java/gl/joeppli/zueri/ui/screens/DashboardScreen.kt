package gl.joeppli.zueri.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.theme.EcoGreen
import gl.joeppli.zueri.theme.ZurichBlue
import gl.joeppli.zueri.theme.ZurichBlueDark
import gl.joeppli.zueri.ui.LocalJoeppliStrings
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

    // Entry animation for progress
    var animationPlayed by remember { mutableStateOf(false) }
    val targetProgress = stats.karma / 100f
    val progressAnimation by animateFloatAsState(
        targetValue = if (animationPlayed) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "karma_progress"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (lang == "en") "Recycling Dashboard" else "Recycling Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (lang == "en") "Your contribution to Zürich's circular economy" else "Din Biitrag zur Zürcher Chreislaufwirtschaft",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Glassmorphic Züri-Karma Hero Card with Gradient and circular progress animation
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                ZurichBlue.copy(alpha = 0.85f),
                                ZurichBlueDark.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.statKarma.uppercase(Locale.ROOT),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${stats.karma}",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 38.sp
                            )
                            Text(
                                text = "/100",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (lang == "en") "Level 4: Neighborhood Hero" else "Level 4: Quartier-Held",
                            style = MaterialTheme.typography.titleSmall,
                            color = EcoGreen
                        )
                    }

                    // Circular Progress Indicators
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Circle
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.15f),
                            strokeWidth = 6.dp,
                        )
                        // Animated fill Circle
                        CircularProgressIndicator(
                            progress = { progressAnimation },
                            modifier = Modifier.fillMaxSize(),
                            color = EcoGreen,
                            strokeWidth = 6.dp,
                        )
                        // Star Icon
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Karma badge",
                            tint = EcoGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Three-Column Quick Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            MetricCard(
                label = if (lang == "en") "STREAK (WEEKS)" else "SERIE (WUCHE)",
                value = "${stats.streakWeeks} ${if (lang == "en") "W." else "W."}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Umwelteffekt Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "en") "Environmental Impact" else "Umwelteffekt",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Breakdown Progress bars
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "en") "Recycled Materials Breakdown" else "Wertstoff ufteilt",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                val cats = stats.categories
                CategoryBar(if (lang == "en") "Paper / Cardboard" else "Papier / Karton", cats.cardboard, stats.totalKg, Color(0xFF3B82F6))
                CategoryBar(if (lang == "en") "Glass" else "Altglas", cats.glass, stats.totalKg, Color(0xFF10B981))
                CategoryBar(if (lang == "en") "Aluminum / Metal" else "Alu / Metall", cats.aluminum, stats.totalKg, Color(0xFFF59E0B))
                CategoryBar(if (lang == "en") "Compost / Organic" else "Biogut / Kompost", cats.bio, stats.totalKg, Color(0xFF059669))
                CategoryBar(if (lang == "en") "PET / Plastic" else "PET / Plastik", cats.pet, stats.totalKg, Color(0xFF8B5CF6))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Neighborhood goal progress bar
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (lang == "en") "Neighborhood Milestone" else "Quartier-Meilestei",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (lang == "en") "Collected in Kreis 4 / 5 this month" else "Gsammlet im Kreis 4 / 5 de Monet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val pct = (stats.neighborhoodTotalKg / 5000f * 100).toInt()
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { stats.neighborhoodTotalKg / 5000f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stats.neighborhoodTotalKg.toInt()} kg ${if (lang == "en") "collected" else "gsammlet"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (lang == "en") "Goal: 5,000 kg" else "Ziel: 5'000 kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Order Button CTA
        Button(
            onClick = { onOrderClick() },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(if (lang == "en") "Order Jöppli" else "Jöppli bestelle", style = MaterialTheme.typography.labelLarge)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
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
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
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
