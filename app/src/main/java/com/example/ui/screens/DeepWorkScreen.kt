package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DeepWorkScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Session modes
    var selectedDurationMinutes by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }
    var isRestMode by remember { mutableStateOf(false) }
    var totalSeconds by remember { mutableIntStateOf(60 * 60) }
    var secondsRemaining by remember { mutableIntStateOf(60 * 60) }

    // Keep screen on when running
    DisposableEffect(isRunning) {
        val window = (context as? Activity)?.window
        if (isRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Timer loop
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining -= 1
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            triggerVibration(context)
            if (!isRestMode) {
                // Completed Deep work session
                viewModel.recordDeepWorkSession(selectedDurationMinutes)
                // Offer rest mode
                isRestMode = true
                totalSeconds = 15 * 60
                secondsRemaining = 15 * 60
            } else {
                // Rest mode ended
                isRestMode = false
                totalSeconds = selectedDurationMinutes * 60
                secondsRemaining = totalSeconds
            }
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val progress = if (totalSeconds > 0) 1f - (secondsRemaining.toFloat() / totalSeconds.toFloat()) else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeuroBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isRestMode) "Miyani dam oldirish" else "Chuqur Ish (Deep Work)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = if (isRestMode) "15 daqiqa ekransiz tanaffus" else "100% diqqat, chalg'imaslik holati",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRestMode) NeuroEmerald else NeuroCyan
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isRunning) NeuroCyan.copy(alpha = 0.2f) else NeuroSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessHigh,
                        contentDescription = null,
                        tint = if (isRunning) NeuroCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRunning) "Ekran o'chmaydi" else "Ekran tayyor",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRunning) NeuroCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Duration selector (only changeable when not running and not rest mode)
        if (!isRestMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(60, 90).forEach { mins ->
                    val isSelected = selectedDurationMinutes == mins
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) NeuroCyan.copy(alpha = 0.2f) else NeuroSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeuroCyan else CardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = !isRunning) {
                                selectedDurationMinutes = mins
                                totalSeconds = mins * 60
                                secondsRemaining = mins * 60
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$mins daqiqa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeuroCyan else Color.White
                            )
                            Text(
                                text = if (mins == 60) "Optimal sikl" else "Maksimal chuqurlik",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        } else {
            // Rest mode banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NeuroEmerald.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeuroEmerald.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🌿", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Dam olish fazasi: Suv iching, xona bo'ylab yuring. Ekranga qaramang!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Large Circular Timer Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(260.dp)
                .testTag("deep_work_timer_circle")
        ) {
            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = NeuroSurfaceVariant,
                strokeWidth = 14.dp
            )

            // Dynamic progress
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = if (isRestMode) NeuroEmerald else NeuroCyan,
                strokeWidth = 14.dp,
                strokeCap = StrokeCap.Round
            )

            // Inner content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isRestMode) "TIKLANISH" else if (isRunning) "DIQQAT SAQLANMOQDA" else "TAYYOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRestMode) NeuroEmerald else if (isRunning) NeuroCyan else TextMuted,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Controls: Start, Pause, Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            OutlinedButton(
                onClick = {
                    isRunning = false
                    isRestMode = false
                    totalSeconds = selectedDurationMinutes * 60
                    secondsRemaining = totalSeconds
                },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                modifier = Modifier
                    .weight(0.8f)
                    .height(56.dp)
                    .testTag("reset_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Qayta o'rnatish",
                    tint = TextSecondary
                )
            }

            // Main Play/Pause Button
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) NeuroRose else (if (isRestMode) NeuroEmerald else NeuroCyan),
                    contentColor = if (isRunning) Color.White else Color(0xFF090D16)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1.8f)
                    .height(56.dp)
                    .testTag("toggle_timer_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "To'xtatish" else (if (secondsRemaining < totalSeconds) "Davom etish" else "Boshlash"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Protocol Rules Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = NeuroAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chuqur ishning 3 oltin qoidasi:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                RuleItem(number = "1", text = "Telefonni boshqa xonaga yoki ko'rinmas joyga qo'ying")
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(number = "2", text = "Barcha brauzer tablarini yopib, faqat 1 ta vazifaga kirishing")
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(number = "3", text = "Seans tugagach, darhol 10-15 daqiqa ekransiz dam oling")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun RuleItem(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = NeuroSurfaceVariant,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeuroCyan
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    } catch (_: Exception) {}
}
