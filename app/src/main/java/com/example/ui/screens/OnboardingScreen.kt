package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ShowTimePicker
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun OnboardingScreen(
    onComplete: (wakeH: Int, wakeM: Int, sleepH: Int, sleepM: Int) -> Unit
) {
    val context = LocalContext.current
    var wakeH by remember { mutableIntStateOf(7) }
    var wakeM by remember { mutableIntStateOf(0) }
    var sleepH by remember { mutableIntStateOf(23) }
    var sleepM by remember { mutableIntStateOf(0) }

    var pickingWakeTime by remember { mutableStateOf(false) }
    var pickingSleepTime by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            onComplete(wakeH, wakeM, sleepH, sleepM)
        }
    )

    if (pickingWakeTime) {
        ShowTimePicker(
            initialHour = wakeH,
            initialMinute = wakeM,
            onTimeSelected = { h, m ->
                wakeH = h
                wakeM = m
                pickingWakeTime = false
            },
            onDismiss = { pickingWakeTime = false }
        )
    }

    if (pickingSleepTime) {
        ShowTimePicker(
            initialHour = sleepH,
            initialMinute = sleepM,
            onTimeSelected = { h, m ->
                sleepH = h
                sleepM = m
                pickingSleepTime = false
            },
            onDismiss = { pickingSleepTime = false }
        )
    }

    val sleepHours = remember(wakeH, wakeM, sleepH, sleepM) {
        val totalMinutes = if (sleepH >= wakeH) {
            (24 * 60 - (sleepH * 60 + sleepM)) + (wakeH * 60 + wakeM)
        } else {
            (wakeH * 60 + wakeM) - (sleepH * 60 + sleepM)
        }
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        if (m == 0) "$h soat" else "$h soat $m daq"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeuroBackground,
                        Color(0xFF0F172A),
                        NeuroBackground
                    )
                )
            )
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brain Icon Header
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = NeuroSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan.copy(alpha = 0.5f)),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🧠", fontSize = 42.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Miya Rejimi",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Siz boshqarmaysiz — ilova har kuni nima qilishni O'ZI buyuradi. Neyrobiologiyaga asoslangan avtomatlashgan intizom.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Step explanation
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NeuroSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = NeuroCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Ilova to'liq avtomatik ishlashi uchun faqat 2 narsani belgilang. Qolgan barcha vaqtlar o'zi hisoblanadi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wake time selector card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NeuroSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickingWakeTime = true }
                    .testTag("wake_time_picker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeuroAmber.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = NeuroAmber
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Uyg'onish vaqti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Kun boshlanishi",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeuroBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeuroAmber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", wakeH, wakeM),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeuroAmber,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sleep time selector card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NeuroSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickingSleepTime = true }
                    .testTag("sleep_time_picker")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeuroIndigo.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = null,
                                    tint = NeuroIndigo
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Uxlash vaqti",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Kutilayotgan uyqu: $sleepHours",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeuroIndigo
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeuroBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeuroIndigo.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", sleepH, sleepM),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeuroIndigo,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Start button
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onComplete(wakeH, wakeM, sleepH, sleepM)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeuroCyan,
                    contentColor = Color(0xFF090D16)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("start_protocol_button")
            ) {
                Text(
                    text = "Miya Rejimini Ishga Tushirish ⚡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Internet shart emas • Xabarlar telefonning o'zida saqlanadi",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
