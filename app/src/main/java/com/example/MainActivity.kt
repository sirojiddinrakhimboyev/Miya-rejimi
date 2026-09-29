package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel on launch
        NotificationHelper.createNotificationChannel(applicationContext)

        // Handle intent from notification click if specified
        intent?.getIntExtra("EXTRA_NAV_TAB", -1)?.let { tab ->
            if (tab >= 0) {
                viewModel.selectTab(tab)
            }
        }
        val proofTaskId = intent?.getLongExtra("EXTRA_PROMPT_PROOF_TASK_ID", -1L) ?: -1L
        if (proofTaskId > 0) {
            viewModel.promptProofForTaskId(proofTaskId)
        }

        setContent {
            MiyaRejimiTheme {
                val isOnboardingDone by viewModel.isOnboardingCompleted.collectAsState()
                val currentTab by viewModel.selectedTab.collectAsState()

                if (!isOnboardingDone) {
                    OnboardingScreen(
                        onComplete = { wakeH, wakeM, sleepH, sleepM ->
                            viewModel.completeOnboarding(wakeH, wakeM, sleepH, sleepM)
                        }
                    )
                } else {
                    BackHandler(enabled = currentTab != 0) {
                        viewModel.selectTab(0)
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = NeuroBackground,
                        contentWindowInsets = WindowInsets(0.dp),
                        bottomBar = {
                            NavigationBar(
                                containerColor = NeuroSurface,
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .testTag("bottom_nav_bar")
                            ) {
                                val navItems = listOf(
                                    NavigationItem("Bugun", Icons.Default.Today, 0, "nav_today"),
                                    NavigationItem("Chuqur ish", Icons.Default.HourglassTop, 1, "nav_deep_work"),
                                    NavigationItem("AI Maslahat", Icons.Default.AutoAwesome, 2, "nav_ai_advisor"),
                                    NavigationItem("Darajalar", Icons.Default.EmojiEvents, 3, "nav_levels"),
                                    NavigationItem("Sozlamalar", Icons.Default.Settings, 4, "nav_settings")
                                )

                                navItems.forEach { item ->
                                    val isSelected = currentTab == item.index
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { viewModel.selectTab(item.index) },
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NeuroCyan,
                                            selectedTextColor = NeuroCyan,
                                            indicatorColor = NeuroCyan.copy(alpha = 0.15f),
                                            unselectedIconColor = TextMuted,
                                            unselectedTextColor = TextMuted
                                        ),
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                                when (tab) {
                                    0 -> TodayScreen(viewModel = viewModel)
                                    1 -> DeepWorkScreen(viewModel = viewModel)
                                    2 -> AiAdvisorScreen(viewModel = viewModel)
                                    3 -> StatsScreen(viewModel = viewModel)
                                    4 -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getIntExtra("EXTRA_NAV_TAB", -1).let { tab ->
            if (tab >= 0) {
                viewModel.selectTab(tab)
            }
        }
        val proofTaskId = intent.getLongExtra("EXTRA_PROMPT_PROOF_TASK_ID", -1L)
        if (proofTaskId > 0) {
            viewModel.promptProofForTaskId(proofTaskId)
        }
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val index: Int,
    val testTag: String
)
