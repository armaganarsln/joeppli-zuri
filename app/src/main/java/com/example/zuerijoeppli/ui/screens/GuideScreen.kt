package com.example.zuerijoeppli.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zuerijoeppli.theme.EcoGreen
import com.example.zuerijoeppli.theme.ZurichBlue

data class MaterialItem(
    val id: String,
    val name: String,
    val category: String,
    val accepted: Boolean,
    val tips: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var showScanSheet by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }

    val materials = remember {
        listOf(
            MaterialItem("1", "Zeitung & Magazine", "Papier", true, "Bündle, nöd im Papiersack.", Icons.Default.Menu),
            MaterialItem("2", "Karton-Schachtle", "Papier", true, "Flach mache und bündle.", Icons.Default.List),
            MaterialItem("3", "PET Getränkefläsche", "Plastik", true, "Luft usepressen, Deckel druf.", Icons.Default.PlayArrow),
            MaterialItem("4", "Milchfläsche (PE)", "Plastik", false, "Bitte im Supermarkt abgäh.", Icons.Default.Warning),
            MaterialItem("5", "Wii-Fläsche & Glas", "Glas", true, "Bitte usspüele. Farbe trenne wenn möglich.", Icons.Default.ShoppingCart),
            MaterialItem("6", "Aludose", "Metall", true, "Flachdrucke.", Icons.Default.Check),
            MaterialItem("7", "Biogut & Kompost", "Biogut", true, "Kei Plastiktüte, nur kompostierbare Säcke.", Icons.Default.Build),
            MaterialItem("8", "Elektroschrott", "Sonderabfall", true, "Kabels, Handys, Computer – Jöppli recycelt es fachgerecht. (Sperrgut-Gebühr)", Icons.Default.Call),
            MaterialItem("9", "Sperrgut (Möbel, Holz)", "Sperrgut", true, "Möbel, Holz oder grosse Plastikteile bis 30kg. (Sperrgut-Gebühr)", Icons.Default.Home)
        )
    }

    val filteredMaterials = materials.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Entsorgungs-Wegweiser",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Was kann entsorgt werden? Frag unseren AI Assistenten.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // AI Camera Scan Trigger Card
            Card(
                colors = CardDefaults.cardColors(containerColor = ZurichBlue),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScanSheet = true }
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "AI Scanner",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Wertstoff scannen (AI)",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Foto machen und Recycling-Kategorie erkennen",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Was entsorgst du?") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AI Result details block
            AnimatedVisibility(visible = scanResult != null) {
                scanResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FBEF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Entsorgungs-Tipp",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoGreen
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { scanResult = null }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ZurichBlue,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // List of materials
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredMaterials) { item ->
                    MaterialRow(item)
                }
            }
        }

        // Camera Scan Simulator Sheet
        if (showScanSheet) {
            ModalBottomSheet(
                onDismissRequest = { showScanSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Jöppli AI Vision Scanner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZurichBlue
                    )
                    Text(
                        text = "Wähle einen Gegenstand zum Scannen aus",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    if (isScanning) {
                        CircularProgressIndicator(color = EcoGreen)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Analysiere Gegenstand...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZurichBlue
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Trash Selection grid to simulate camera vision scanner
                        val itemsToScan = listOf(
                            Pair("Kaffeekapsel (Alu)", "Das Objekt ist eine Nespresso Kaffeekapsel aus Aluminium. Sie ist zu 100% recycelbar in der Schweiz. Bitte im Jöppli unter 'Metall' abgeben. Tipp: Die Kapseln müssen nicht entleert werden, ERZ trennt den Kaffeesatz automatisch für Biogas!"),
                            Pair("Pizzakarton", "Das Objekt ist eine Pizzaschachtel. Pizzakartons mit Ölflecken gehören NICHT ins Altpapier (Karton), da sie die Zellstofffasern verunreinigen. Bitte über den Hausmüll entsorgen. Nur saubere Kartons sind recycelbar."),
                            Pair("PET Getränkeflasche", "Das Objekt ist eine PET Getränkeflasche. Sie ist zu 100% recycelbar. Bitte flachdrücken, den Deckel aufschrauben und ins Jöppli werfen. Nicht mit Milchflaschen (PE) mischen!"),
                            Pair("Joghurtbecher (Plastik)", "Das Objekt ist ein Plastik-Joghurtbecher. Becher und Aluminiumdeckel müssen getrennt werden. Der Aludeckel kommt ins Metall-Recycling. Der Plastikbecher gehört in die kostenpflichtige Plastiksammlung oder den Hausmüll (in Zürich subventioniert Jöppli dies als Spezialabfall).")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsToScan.forEach { (name, explanation) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isScanning = true
                                            // Simulate delay
                                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                isScanning = false
                                                scanResult = explanation
                                                showScanSheet = false
                                            }, 1500)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = EcoGreen
                                        )
                                        Text(
                                            text = name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialRow(item: MaterialItem) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(EcoGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = EcoGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = item.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZurichBlue
                        )
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (item.accepted) {
                    Box(
                        modifier = Modifier
                            .background(EcoGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "GRATIS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MUSS RETOUR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.tips,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
        }
    }
}
