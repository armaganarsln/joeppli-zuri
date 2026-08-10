package gl.joeppli.zueri.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Compost
import androidx.compose.material.icons.outlined.ElectricalServices
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Liquor
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.theme.Dimens
import gl.joeppli.zueri.ui.LocalJoeppliStrings
import gl.joeppli.zueri.ui.components.PageHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MaterialItem(
    val id: String,
    val nameEn: String,
    val nameDe: String,
    val categoryEn: String,
    val categoryDe: String,
    val accepted: Boolean,
    val tipsEn: String,
    val tipsDe: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    val strings = LocalJoeppliStrings.current
    val lang by RecyclingRepository.userLanguage.collectAsState()
    val context = LocalContext.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showScanSheet by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val materials = remember {
        listOf(
            MaterialItem("1", "Newspapers & Magazines", "Zeitung & Magazine", "Paper", "Papier", true, "Bundle it, do not put in paper bag.", "Bündle, nöd im Papiersack.", Icons.Outlined.Newspaper),
            MaterialItem("2", "Cardboard Boxes", "Karton-Schachtle", "Paper", "Papier", true, "Flatten and bundle.", "Flach mache und bündle.", Icons.Outlined.Inventory2),
            MaterialItem("3", "PET Beverage Bottles", "PET Getränkefläsche", "Plastic", "Plastik", true, "Compress to release air, keep cap on.", "Luft usepressen, Deckel druf.", Icons.Outlined.Liquor),
            MaterialItem("4", "Milk Bottles (PE)", "Milchfläsche (PE)", "Plastic", "Plastik", false, "Please return to supermarket.", "Bitte im Supermarkt abgäh.", Icons.Outlined.Block),
            MaterialItem("5", "Wine Bottles & Glass jars", "Wii-Fläsche & Glas", "Glass", "Glas", true, "Rinse clean. Separate colors if possible.", "Bitte usspüele. Farbe trenne wenn möglich.", Icons.Outlined.WineBar),
            MaterialItem("6", "Aluminum Cans", "Aludose", "Metal", "Metall", true, "Crush flat.", "Flachdrucke.", Icons.Outlined.LocalDrink),
            MaterialItem("7", "Organic Waste & Compost", "Biogut & Kompost", "Organic", "Biogut", true, "No plastic bags, compostable bags only.", "Kei Plastiktüte, nur kompostierbare Säcke.", Icons.Outlined.Compost),
            MaterialItem("8", "Electronic Waste", "Elektroschrott", "Special Waste", "Sonderabfall", true, "Cables, phones, computers – Jöppli recycles them safely. (Bulky fee applies)", "Kabels, Handys, Computer – Jöppli recycelt es fachgerecht. (Sperrgut-Gebühr)", Icons.Outlined.ElectricalServices),
            MaterialItem("9", "Bulky Waste (Furniture, Wood)", "Sperrgut (Möbel, Holz)", "Bulky Waste", "Sperrgut", true, "Furniture, wood, or large plastic items up to 30kg. (Bulky fee applies)", "Möbel, Holz oder grosse Plastikteile bis 30kg. (Sperrgut-Gebühr)", Icons.Outlined.Chair)
        )
    }

    val filteredMaterials = materials.filter {
        val name = if (lang == "en") it.nameEn else it.nameDe
        val cat = if (lang == "en") it.categoryEn else it.categoryDe
        name.contains(searchQuery, ignoreCase = true) || cat.contains(searchQuery, ignoreCase = true)
    }

    // Keep track of expanded card IDs
    val expandedItemIds = remember { mutableStateListOf<String>() }

    Box(modifier = Modifier.fillMaxSize()) {
        // One scrolling list for the whole screen: the header and search used to
        // be pinned above a LazyColumn, which squeezed the material list into
        // whatever height was left over.
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
                        title = strings.guideTitle,
                        subtitle = strings.guideSubtitle
                    )

                    // AI Camera Scan Trigger Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = Dimens.cardHero,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showScanSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimens.gapLg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.gapLg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.18f),
                                        Dimens.card
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CenterFocusWeak,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.gapXs)) {
                                Text(
                                    text = strings.guideScannerTitle,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = strings.guideScannerSubtitle,
                                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Search input field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(if (lang == "en") "What are you disposing?" else "Was entsorgsch du?") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = if (lang == "en") "Clear search" else "Suech lösche",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = Dimens.card
                    )
                }
            }

            // AI Result details block
            scanResult?.let { result ->
                item(key = "scan-result") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = Dimens.card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.padding(Dimens.gapLg)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.guideScanResultTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(onClick = { scanResult = null }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = if (lang == "en") "Close" else "Schliesse",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.gapSm))
                            Text(
                                text = result,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // No search hits — say so instead of leaving the list area blank
            if (filteredMaterials.isEmpty()) {
                item(key = "empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Dimens.section),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.gapXs)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(Dimens.gapSm))
                        Text(
                            text = if (lang == "en") "Nothing found for \"$searchQuery\""
                            else "Nüt gfunde für \"$searchQuery\"",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (lang == "en") "Try a different term, or scan the item instead."
                            else "Probier en andere Begriff, oder scanne s'Objekt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filteredMaterials, key = { it.id }) { item ->
                val isExpanded = expandedItemIds.contains(item.id)
                val displayName = if (lang == "en") item.nameEn else item.nameDe

                // Material 3 SwipeToDismissBox
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            if (item.accepted) {
                                Toast.makeText(context, strings.guideSwipeAddedToast.format(displayName), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, if (lang == "en") "Cannot collect: return to retail store." else "Entsorgig nöd möglich: Bitte im Fachhandel zruggäh.", Toast.LENGTH_SHORT).show()
                            }
                            true
                        } else {
                            false
                        }
                    }
                )

                // Snap back to neutral state after swipe completes
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val addColor = MaterialTheme.colorScheme.primary
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            addColor.copy(alpha = 0.8f)
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(Dimens.card)
                                .background(color)
                                .padding(horizontal = Dimens.section),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    MaterialRow(
                        item = item,
                        lang = lang,
                        isExpanded = isExpanded,
                        onExpandToggle = {
                            if (isExpanded) expandedItemIds.remove(item.id) else expandedItemIds.add(item.id)
                        }
                    )
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
                        .padding(Dimens.section),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.guideScannerTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = strings.guideScannerPrompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Dimens.gapXs, bottom = Dimens.section)
                    )

                    if (isScanning) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (lang == "en") "Analyzing item..." else "Analysiere Gegestand…",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Trash Selection grid to simulate camera vision scanner
                        val itemsToScan = if (lang == "en") listOf(
                            Pair("Coffee Capsule (Alu)", "The object is a Nespresso coffee capsule made of aluminum. It is 100% recyclable in Switzerland. Please hand it to the Jöppli under 'Metal'. Tip: capsules don't need to be emptied — ERZ separates the coffee grounds automatically for biogas!"),
                            Pair("Pizza Box", "The object is a pizza box. Boxes with grease stains do NOT belong in the paper/cardboard recycling, as they contaminate the cellulose fibers. Please dispose of it with household waste. Only clean cardboard is recyclable."),
                            Pair("PET Beverage Bottle", "The object is a PET beverage bottle. It is 100% recyclable. Please flatten it, screw the cap back on and drop it into the Jöppli. Do not mix with milk bottles (PE)!"),
                            Pair("Yogurt Cup (Plastic)", "The object is a plastic yogurt cup. Cup and aluminum lid must be separated. The alu lid goes into metal recycling. The plastic cup belongs in the paid plastic collection or household waste (in Zürich, Jöppli subsidizes this as special waste).")
                        ) else listOf(
                            Pair("Kaffeekapsel (Alu)", "Das Objekt ist eine Nespresso Kaffeekapsel aus Aluminium. Sie ist zu 100% recycelbar in der Schweiz. Bitte im Jöppli unter 'Metall' abgeben. Tipp: Die Kapseln müssen nicht entleert werden, ERZ trennt den Kaffeesatz automatisch für Biogas!"),
                            Pair("Pizzakarton", "Das Objekt ist eine Pizzaschachtel. Pizzakartons mit Ölflecken gehören NICHT ins Altpapier (Karton), da sie die Zellstofffasern verunreinigen. Bitte über den Hausmüll entsorgen. Nur saubere Kartons sind recycelbar."),
                            Pair("PET Getränkeflasche", "Das Objekt ist eine PET Getränkeflasche. Sie ist zu 100% recycelbar. Bitte flachdrücken, den Deckel aufschrauben und ins Jöppli werfen. Nicht mit Milchflaschen (PE) mischen!"),
                            Pair("Joghurtbecher (Plastik)", "Das Objekt ist ein Plastik-Joghurtbecher. Becher und Aluminiumdeckel müssen getrennt werden. Der Aludeckel kommt ins Metall-Recycling. Der Plastikbecher gehört in die kostenpflichtige Plastiksammlung oder den Hausmüll (in Zürich subventioniert Jöppli dies als Spezialabfall).")
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(Dimens.gapMd),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsToScan.forEach { (name, explanation) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                                    shape = Dimens.chip,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isScanning = true
                                            scope.launch {
                                                delay(1500)
                                                isScanning = false
                                                scanResult = explanation
                                                showScanSheet = false
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(Dimens.gapLg),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.gapMd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CenterFocusWeak,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
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
fun MaterialRow(
    item: MaterialItem,
    lang: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit
) {
    val name = if (lang == "en") item.nameEn else item.nameDe
    val categoryClean = if (lang == "en") item.categoryEn else item.categoryDe
    val tips = if (lang == "en") item.tipsEn else item.tipsDe

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = Dimens.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() }
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.gapLg)
                .animateContentSize() // Smooth expand animation
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (item.accepted) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (item.accepted) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = categoryClean,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.accepted) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (lang == "en") "FREE" else "GRATIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (lang == "en") "RETAIL ONLY" else "MUSS RETOUR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tips,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (lang == "en") "💡 Swipe left to add directly to pickup order" else "💡 Nach links wische zum direkt Hinzufüge",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
