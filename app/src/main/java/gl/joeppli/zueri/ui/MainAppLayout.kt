package gl.joeppli.zueri.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import gl.joeppli.zueri.data.RecyclingRepository
import gl.joeppli.zueri.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout() {
    val lang by RecyclingRepository.userLanguage.collectAsState()
    val strings = if (lang == "en") EnStrings else DeStrings
    val profile by RecyclingRepository.userProfile.collectAsState()

    CompositionLocalProvider(LocalJoeppliStrings provides strings) {
        if (!profile.isLoggedIn) {
            AuthScreen()
        } else {
            MainAppContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    var activeTab by rememberSaveable { mutableStateOf("HOME") }
    var prefillQuickOrder by rememberSaveable { mutableStateOf(false) }

    // From any tab other than Home, Back returns to Home rather than exiting
    // the app. On Home it stays disabled so the system handles back (exit).
    // The Order tab registers its own BackHandler for in-wizard steps, which
    // takes precedence while it's shown.
    BackHandler(enabled = activeTab != "HOME") {
        prefillQuickOrder = false
        activeTab = "HOME"
    }

    Scaffold(
        bottomBar = {
            CustomBottomBar(
                activeTab = activeTab,
                onTabSelect = { tab ->
                    prefillQuickOrder = false
                    activeTab = tab
                },
                onQuickPickupClick = {
                    prefillQuickOrder = true
                    activeTab = "ORDER"
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                "HOME" -> HomeScreen(
                    onNavigateToTab = { tab -> activeTab = tab },
                    onQuickPickupClick = {
                        prefillQuickOrder = true
                        activeTab = "ORDER"
                    }
                )
                "STATS" -> DashboardScreen(
                    onOrderClick = { activeTab = "ORDER" }
                )
                "ORDER" -> OrderScreen(
                    onNavigateHome = { activeTab = "HOME" },
                    prefillQuick = prefillQuickOrder
                )
                "GUIDE" -> GuideScreen()
                "PROFILE" -> ProfileScreen()
            }
        }
    }
}

@Composable
fun CustomBottomBar(
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onQuickPickupClick: () -> Unit
) {
    val strings = LocalJoeppliStrings.current
    // 26dp of transparent headroom keeps the floating FAB inside the
    // composable's bounds (no clipping, full touch target).
    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 26.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(76.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BottomTabItem(
                    label = strings.bottomStart,
                    icon = Icons.Outlined.Home,
                    selectedIcon = Icons.Filled.Home,
                    selected = activeTab == "HOME",
                    onClick = { onTabSelect("HOME") }
                )
                BottomTabItem(
                    label = strings.bottomRecycling,
                    icon = Icons.Outlined.BarChart,
                    selectedIcon = Icons.Outlined.BarChart,
                    selected = activeTab == "STATS",
                    onClick = { onTabSelect("STATS") }
                )
                // Space reserved for the centered FAB
                Spacer(modifier = Modifier.weight(1f))
                BottomTabItem(
                    label = strings.bottomScanner,
                    icon = Icons.Outlined.CenterFocusWeak,
                    selectedIcon = Icons.Outlined.CenterFocusWeak,
                    selected = activeTab == "GUIDE",
                    onClick = { onTabSelect("GUIDE") }
                )
                BottomTabItem(
                    label = strings.bottomProfile,
                    icon = Icons.Outlined.Person,
                    selectedIcon = Icons.Filled.Person,
                    selected = activeTab == "PROFILE",
                    onClick = { onTabSelect("PROFILE") }
                )
            }
        }
        FloatingActionButton(
            onClick = onQuickPickupClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Recycling,
                contentDescription = strings.quickPickup,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun RowScope.BottomTabItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
