package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.components.AdminContactDialog
import com.example.ui.components.ShowTimePicker
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wakeH by viewModel.wakeHour.collectAsState()
    val wakeM by viewModel.wakeMinute.collectAsState()
    val sleepH by viewModel.sleepHour.collectAsState()
    val sleepM by viewModel.sleepMinute.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val adminAnnouncement by viewModel.adminAnnouncement.collectAsState()

    var showWakePicker by remember { mutableStateOf(false) }
    var showSleepPicker by remember { mutableStateOf(false) }
    var showAdminContactDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Admin Contact & PRO Payment Dialog
    if (showAdminContactDialog) {
        AdminContactDialog(
            onDismiss = { showAdminContactDialog = false },
            isAdmin = isAdmin,
            isPro = isPremium,
            onAdminTogglePro = { viewModel.setPremium(it) }
        )
    }

    var adminAnnouncementInput by remember(adminAnnouncement) { mutableStateOf(adminAnnouncement) }

    val canExactAlarm = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    if (showWakePicker) {
        ShowTimePicker(
            initialHour = wakeH,
            initialMinute = wakeM,
            onTimeSelected = { h, m ->
                viewModel.saveRoutineSettings(h, m, sleepH, sleepM)
                showWakePicker = false
            },
            onDismiss = { showWakePicker = false }
        )
    }

    if (showSleepPicker) {
        ShowTimePicker(
            initialHour = sleepH,
            initialMinute = sleepM,
            onTimeSelected = { h, m ->
                viewModel.saveRoutineSettings(wakeH, wakeM, h, m)
                showSleepPicker = false
            },
            onDismiss = { showSleepPicker = false }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Maxfiylik Siyosati (Privacy Policy)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = """
                        Miya Rejimi ilovasi Google Play xavfsizlik va maxfiylik qoidalariga 100% mos keladi:
                        
                        1. Ma'lumotlar saqlanishi:
                        Barcha shaxsiy vazifalar, kun tartibi, uyqu/uyg'onish vaqtlari faqat qurilmangizning ichki SQLite (Room) bazasida saqlanadi.
                        
                        2. Shaxsiy daxlsizlik:
                        Ilova hech qanday shaxsiy fotosuratlar, media yoki kontaktlarga kirish ruxsatini talab qilmaydi.
                        
                        3. Bildirishnomalar:
                        Bildirishnomalar faqat rejalashtirilgan tartib va signallarni o'z vaqtida yetkazish uchun ishlatiladi.
                        
                        4. Bog'lanish:
                        Savol va takliflar bo'yicha: handsometyon@gmail.com
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeuroCyan, contentColor = Color(0xFF090D16))
                ) {
                    Text("Tushunarli", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NeuroSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    val routineProtocols = listOf(
        Pair("SUNLIGHT", "Quyosh nuri va sergaklik (+0 daqiqa)"),
        Pair("COFFEINE_ON", "Kofein qabul qilish vaqti (+60 daqiqa)"),
        Pair("DEEP_WORK", "Chuqur ish va to'liq diqqat (+2 soat)"),
        Pair("REST", "Ekransiz dam olish va tiklanish"),
        Pair("WORKOUT", "30 daqiqalik mashq yoki yurish"),
        Pair("COFFEINE_OFF", "Kofein to'xtatish chegarasi (14:00)"),
        Pair("NAP", "20 daqiqalik quvvat uyqusi (Power nap)"),
        Pair("PLANNING", "Ertangi kun rejasini yozish"),
        Pair("PHONE_AWAY", "Telefonni qo'yish (Uxlashdan 60 daq oldin)"),
        Pair("SLEEP", "Uxlash vaqti signali")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeuroBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Sozlamalar & Boshqaruv",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Kun tartibi, obuna va boshqaruvchi funksiyalari",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // PRO / PREMIUM BANNER
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    if (isPremium) listOf(NeuroEmerald, NeuroCyan) else listOf(Color(0xFFF59E0B), Color(0xFF9333EA))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdminContactDialog = true }
                .testTag("settings_pro_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isPremium) "⚔️" else "👑", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPremium) "Solo Leveling PRO (Faol)" else "Solo Leveling PRO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (isPremium) "Solo Leveling status oynasi va cheksiz AI faol" else "1 oyga 9,999 so'm • Karta: 9860 3501 4072 3015",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPremium) NeuroEmerald else Color(0xFFF59E0B)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPremium) NeuroEmerald.copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isPremium) "FAOL ✓" else "9,999 so'm",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isPremium) NeuroEmerald else Color(0xFFF59E0B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Section 1: Core Times
        Text(
            text = "ASOSIY VAQTLAR",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NeuroCyan,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TimeRowItem(
                    title = "Uyg'onish vaqti",
                    subtitle = "Butun kunlik sikl shunga qarab tuziladi",
                    time = String.format(Locale.getDefault(), "%02d:%02d", wakeH, wakeM),
                    color = NeuroAmber,
                    onClick = { showWakePicker = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = NeuroSurfaceVariant
                )

                TimeRowItem(
                    title = "Uxlash vaqti",
                    subtitle = "Detoks va uyqu signallari vaqti",
                    time = String.format(Locale.getDefault(), "%02d:%02d", sleepH, sleepM),
                    color = NeuroIndigo,
                    onClick = { showSleepPicker = true }
                )
            }
        }

        // Section 2: Notifications Master & Exact Alarms
        Text(
            text = "BILDIRISHNOMALAR VA SIGNALLAR",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NeuroCyan,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lokal eslatmalar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ilova yopiq bo'lsa ham buyruq beradi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeuroCyan,
                            checkedTrackColor = NeuroCyanDark.copy(alpha = 0.4f)
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = NeuroSurfaceVariant
                )

                // Exact Alarm permission check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aniq vaqtli signallar (Exact Alarm)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = if (canExactAlarm) "Ruxsat berilgan (100% ishonchli)" else "Tizim sozlamalaridan ruxsat bering",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (canExactAlarm) NeuroEmerald else NeuroRose
                        )
                    }
                    if (!canExactAlarm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Yoqish", color = NeuroCyan)
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NeuroEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = NeuroSurfaceVariant
                )

                // Test Notification Button
                Button(
                    onClick = { viewModel.testNotification() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuroSurfaceVariant,
                        contentColor = NeuroCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test bildirishnoma yuborish (Sinov)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Section 3: Admin & Feedback
        Text(
            text = "BOSHQARUV VA ADMINGA BOG'LANISH",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NeuroCyan,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Admin Toggle Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bosh boshqaruvchi (Admin) rejimi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Tavsiyalarni boshqarish va e'lonlar kiritish",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isAdmin,
                        onCheckedChange = {
                            viewModel.setAdmin(it)
                            Toast.makeText(
                                context,
                                if (it) "Boshqaruvchi rejimi faollashdi 🛡️" else "Admin rejimi o'chirildi",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeuroCyan,
                            checkedTrackColor = NeuroCyanDark.copy(alpha = 0.4f)
                        )
                    )
                }

                // If Admin, show broadcast advice editor
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeuroSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📢 Ilovaga umumiy tavsiya/e'lon kiritish:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeuroCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = adminAnnouncementInput,
                                onValueChange = { adminAnnouncementInput = it },
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeuroCyan,
                                    unfocusedBorderColor = CardBorderBright
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.setAdminAnnouncement(adminAnnouncementInput.trim())
                                    Toast.makeText(context, "Tavsiya saqlandi va barchaga e'lon qilindi!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeuroCyan,
                                    contentColor = Color(0xFF090D16)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("E'lon qilish", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = NeuroSurfaceVariant
                )

                // Adminga Murojaat Paneli Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdminContactDialog = true }
                        .testTag("open_admin_contact_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "👑", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Adminga Murojaat Paneli",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Solo Leveling PRO (9,999 so'm) • Tel: +998200134311",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Karta: 9860 3501 4072 3015",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "To'lov & Aloqa ↗",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuroCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Individual Protocol Switches
        Text(
            text = "HAR BIR BUYRUQNI BOSHQARISH",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = NeuroCyan,
            letterSpacing = 1.sp
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                routineProtocols.forEachIndexed { index, (key, title) ->
                    val isEnabled = remember(key) {
                        viewModel.prefs.isRoutineEnabled(key)
                    }
                    var checked by remember(key) { mutableStateOf(isEnabled) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked) Color.White else TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                checked = it
                                viewModel.toggleRoutineItem(key, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeuroCyan,
                                checkedTrackColor = NeuroCyanDark.copy(alpha = 0.4f)
                            )
                        )
                    }
                    if (index < routineProtocols.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = NeuroSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Section 5: Play Store Info & Privacy Policy
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NeuroSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Miya Rejimi • v1.0 • Google Play Ready",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Barcha ma'lumotlar qurilmangiz xotirasida (Room DB) xavfsiz saqlanadi. Google Play talablariga to'liq javob beradi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Maxfiylik Siyosati (Privacy Policy) ↗",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeuroCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = NeuroAmber,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

@Composable
private fun TimeRowItem(
    title: String,
    subtitle: String,
    time: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NeuroBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
