package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NightStandClockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SleepAnalyticsScreen
import com.example.ui.screens.SmartNapScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SunriseGold
import com.example.ui.viewmodel.AlarmViewModel

enum class MainTab {
    ALARMS,
    POWER_NAP,
    ANALYTICS,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()

            // Request Notification permission on Android 13+
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { /* granted */ }

            LaunchedEffect(Unit) {
                val perms = mutableListOf<String>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                perms.add(Manifest.permission.CAMERA)
                if (perms.isNotEmpty()) {
                    permissionLauncher.launch(perms.toTypedArray())
                }
            }

            MyApplicationTheme(themeMode = themeMode) {
                // Apply customizable font scale accessibility
                val currentDensity = LocalDensity.current
                val customDensity = Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScale
                )

                CompositionLocalProvider(LocalDensity provides customDensity) {
                    AuraRiseApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AuraRiseApp(viewModel: AlarmViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.ALARMS) }
    var isNightstandOpen by remember { mutableStateOf(false) }

    val alarms by viewModel.alarms.collectAsState()

    if (isNightstandOpen) {
        BackHandler { isNightstandOpen = false }
        NightStandClockScreen(
            viewModel = viewModel,
            onBack = { isNightstandOpen = false }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("aurora_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xF20F111E),
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == MainTab.ALARMS,
                    onClick = { currentTab = MainTab.ALARMS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Alarms",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Alarms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D1600),
                        selectedTextColor = SunriseGold,
                        indicatorColor = SunriseGold,
                        unselectedIconColor = Color(0xFF8E93B0),
                        unselectedTextColor = Color(0xFF8E93B0)
                    ),
                    modifier = Modifier.testTag("nav_item_alarms")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.POWER_NAP,
                    onClick = { currentTab = MainTab.POWER_NAP },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Nap",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Smart Nap", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D1600),
                        selectedTextColor = SunriseGold,
                        indicatorColor = SunriseGold,
                        unselectedIconColor = Color(0xFF8E93B0),
                        unselectedTextColor = Color(0xFF8E93B0)
                    ),
                    modifier = Modifier.testTag("nav_item_power_nap")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.ANALYTICS,
                    onClick = { currentTab = MainTab.ANALYTICS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Analytics",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Recovery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D1600),
                        selectedTextColor = SunriseGold,
                        indicatorColor = SunriseGold,
                        unselectedIconColor = Color(0xFF8E93B0),
                        unselectedTextColor = Color(0xFF8E93B0)
                    ),
                    modifier = Modifier.testTag("nav_item_analytics")
                )

                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { currentTab = MainTab.SETTINGS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Comfort", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D1600),
                        selectedTextColor = SunriseGold,
                        indicatorColor = SunriseGold,
                        unselectedIconColor = Color(0xFF8E93B0),
                        unselectedTextColor = Color(0xFF8E93B0)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.ALARMS -> {
                    HomeScreen(
                        viewModel = viewModel,
                        alarms = alarms,
                        onNavigateToNap = { currentTab = MainTab.POWER_NAP },
                        onNavigateToAnalytics = { currentTab = MainTab.ANALYTICS },
                        onNavigateToNightstand = { isNightstandOpen = true }
                    )
                }
                MainTab.POWER_NAP -> {
                    SmartNapScreen(viewModel = viewModel)
                }
                MainTab.ANALYTICS -> {
                    SleepAnalyticsScreen(viewModel = viewModel)
                }
                MainTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
