package com.example.zuerijoeppli.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zuerijoeppli.data.RecyclingRepository
import com.example.zuerijoeppli.theme.EcoGreen
import com.example.zuerijoeppli.theme.ZurichBlue

@Composable
fun DashboardScreen(
    onOrderClick: () -> Unit
) {
    val stats by RecyclingRepository.stats.collectAsState()
    val scrollState = verticalScrollState()
    val carKm = (stats.co2Saved * 7).toInt()
    val treesPlanted = String.format("%.1f", stats.co2Saved * 0.05f)

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
            text = "Recycling Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Dein Beitrag zur Zürcher Kreislaufwirtschaft",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Züri-Karma Hero Card with Gradient
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
                            colors = listOf(ZurichBlue, Color(0xFF1A256B))
                        )
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
                                text = "ZÜRI-KARMA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Level 4: Quartier-Held",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Karma badge",
                            tint = EcoGreen,
                            modifier = Modifier.size(44.dp)
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
                label = "TOTAL RECYCELT",
                value = String.format("%.1f kg", stats.totalKg),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "CO2 ENTSPART",
                value = String.format("-%.0f kg", stats.co2Saved),
                valueColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "SERIE (WOCHEN)",
                value = "${stats.streakWeeks} W.",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Umwelteffekt Card (Subsidized Ecological values)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBEF)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Umwelteffekt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZurichBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AUTOFAHRTEN VERMIEDEN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "$carKm km Autofahrt",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZurichBlue
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BÄUME GEPFLANZT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "$treesPlanted Bäume",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZurichBlue
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
                    text = "Wertstoffe Aufgeteilt",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZurichBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val cats = stats.categories
                CategoryBar("Papier / Karton", cats.cardboard, stats.totalKg, Color(0xFF3B82F6))
                CategoryBar("Altglas", cats.glass, stats.totalKg, Color(0xFF10B981))
                CategoryBar("Alu / Metall", cats.aluminum, stats.totalKg, Color(0xFFF59E0B))
                CategoryBar("Biogut / Kompost", cats.bio, stats.totalKg, Color(0xFF059669))
                CategoryBar("PET / Plastik", cats.pet, stats.totalKg, Color(0xFF8B5CF6))
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
                    Column {
                        Text(
                            text = "Quartier-Meilenstein",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZurichBlue
                        )
                        Text(
                            text = "Gesammelt im Kreis 4 / 5 diesen Monat",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    val pct = (stats.neighborhoodTotalKg / 5000f * 100).toInt()
                    Text(
                        text = "$pct%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoGreen
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { stats.neighborhoodTotalKg / 5000f },
                    color = EcoGreen,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
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
                        text = "${stats.neighborhoodTotalKg.toInt()} kg gesammelt",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ziel: 5'000 kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Order Button CTA
        Button(
            onClick = { onOrderClick() },
            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Jöppli bestellen", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    valueColor: Color = ZurichBlue,
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format("%.1f kg (%d%%)", value, percentage),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (total > 0f) value / total else 0f },
            color = barColor,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun verticalScrollState() = rememberScrollState()
