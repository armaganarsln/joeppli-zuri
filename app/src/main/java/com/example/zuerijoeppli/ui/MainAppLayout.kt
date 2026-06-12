package com.example.zuerijoeppli.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import com.example.zuerijoeppli.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout() {
    var activeTab by remember { mutableStateOf("HOME") }
    var prefillQuickOrder by remember { mutableStateOf(false) }

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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Tab 1: HOME
            BottomTabItem(
                label = "Start",
                icon = Icons.Default.Home,
                selected = activeTab == "HOME",
                onClick = { onTabSelect("HOME") }
            )

            // Tab 2: STATS
            BottomTabItem(
                label = "Recycling",
                icon = Icons.Default.Refresh,
                selected = activeTab == "STATS",
                onClick = { onTabSelect("STATS") }
            )

            // Center FAB (Quick summons order)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(y = (-14).dp)
                    .clip(CircleShape)
                    .background(EcoGreen)
                    .clickable { onQuickPickupClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share, // Replaces electric truck / summon symbol
                    contentDescription = "Order Jöppli",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tab 4: GUIDE
            BottomTabItem(
                label = "Scannen",
                icon = Icons.Default.Menu,
                selected = activeTab == "GUIDE",
                onClick = { onTabSelect("GUIDE") }
            )

            // Tab 5: PROFILE
            BottomTabItem(
                label = "Profil",
                icon = Icons.Default.Person,
                selected = activeTab == "PROFILE",
                onClick = { onTabSelect("PROFILE") }
            )
        }
    }
}

@Composable
fun RowScope.BottomTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) EcoGreen else Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) EcoGreen else Color.Gray.copy(alpha = 0.6f)
        )
    }
}
