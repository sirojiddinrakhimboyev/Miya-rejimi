package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.NeuroBackground
import com.example.ui.theme.NeuroCyan
import com.example.ui.theme.NeuroIndigo
import com.example.ui.theme.NeuroSurface
import com.example.ui.theme.NeuroSurfaceVariant
import java.util.Calendar
import java.util.Locale

@Composable
fun CustomTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        category: String,
        description: String,
        hour: Int,
        minute: Int,
        reminderOffsetMinutes: Int,
        repeatFrequency: String
    ) -> Unit
) {
    val now = Calendar.getInstance()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mashg'ulot / Sport") }
    var hour by remember { mutableIntStateOf((now.get(Calendar.HOUR_OF_DAY) + 1) % 24) }
    var minute by remember { mutableIntStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Reminder Offset: 0, 5, 10, 15, 30 min before
    var reminderOffsetMinutes by remember { mutableIntStateOf(0) }
    // Repeat frequency: "ONCE", "DAILY", "WEEKDAYS"
    var repeatFrequency by remember { mutableStateOf("ONCE") }

    val categories = listOf(
        "Mashg'ulot / Sport",
        "Uchrashuv",
        "O'qish / Ta'lim",
        "Salomatlik",
        "Shaxsiy reja"
    )

    val reminderOffsets = listOf(
        Pair(0, "Aynan vaqtida"),
        Pair(5, "5 daq oldin"),
        Pair(10, "10 daq oldin"),
        Pair(15, "15 daq oldin"),
        Pair(30, "30 daq oldin")
    )

    val frequencies = listOf(
        Pair("ONCE", "Faqat bir marta"),
        Pair("DAILY", "Har kuni"),
        Pair("WEEKDAYS", "Ish kunlari (Dush-Jum)")
    )

    if (showTimePicker) {
        ShowTimePicker(
            initialHour = hour,
            initialMinute = minute,
            onTimeSelected = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NeuroSurface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("custom_task_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shaxsiy vazifa yoki reja",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Mashg'ulot, uchrashuv va maxsus eslatmalar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Yopish",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nomi yoki mazmuni *") },
                        placeholder = { Text("Sport zali, Ingliz tili, Shifokor...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuroCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = NeuroCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input")
                    )

                    // Categories
                    Text(
                        text = "Kategoriya:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeuroCyan.copy(alpha = 0.2f) else NeuroSurfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan) else null,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) NeuroCyan else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeuroCyan.copy(alpha = 0.2f) else NeuroSurfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan) else null,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) NeuroCyan else Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 1. Task time selector
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeuroSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .testTag("time_picker_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = NeuroCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Vazifa vaqti",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "O'zgartirish uchun bosing",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeuroBackground
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuroCyan,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 2. Notification Reminder Timing (Aynan vaqtida / oldin)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = NeuroCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bildirishnoma vaqti (Eslatma):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reminderOffsets.take(3).forEach { (offset, label) ->
                                val isSelected = reminderOffsetMinutes == offset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) NeuroCyan.copy(alpha = 0.25f) else NeuroSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reminderOffsetMinutes = offset }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (isSelected) NeuroCyan else Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reminderOffsets.drop(3).forEach { (offset, label) ->
                                val isSelected = reminderOffsetMinutes == offset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) NeuroCyan.copy(alpha = 0.25f) else NeuroSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { reminderOffsetMinutes = offset }
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = if (isSelected) NeuroCyan else Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // 3. Repeat Frequency (Chastota)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = NeuroIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Eslatma chastotasi (Takrorlanish):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            frequencies.forEach { (freqCode, freqLabel) ->
                                val isSelected = repeatFrequency == freqCode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) NeuroIndigo.copy(alpha = 0.25f) else NeuroSurfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeuroIndigo) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { repeatFrequency = freqCode }
                                ) {
                                    Text(
                                        text = freqLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = if (isSelected) NeuroIndigo else Color.White,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Optional note
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Qo'shimcha izoh (ixtiyoriy)") },
                        placeholder = { Text("Manzil yoki qo'shimcha reja...") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuroCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onConfirm(
                                title.trim(),
                                selectedCategory,
                                description.trim(),
                                hour,
                                minute,
                                reminderOffsetMinutes,
                                repeatFrequency
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuroCyan,
                        contentColor = Color(0xFF090D16)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_custom_task_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jadvalga saqlash",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
