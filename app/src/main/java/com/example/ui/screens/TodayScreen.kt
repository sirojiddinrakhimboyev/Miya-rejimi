package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyTask
import com.example.ui.MainViewModel
import com.example.ui.components.CustomTaskDialog
import com.example.ui.components.PhotoProofDialog
import com.example.ui.components.ViewProofDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val nextTask by viewModel.nextActiveTask.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val adminAnnouncement by viewModel.adminAnnouncement.collectAsState()
    val taskForProofDialog by viewModel.taskForProofDialog.collectAsState()
    val taskForViewProof by viewModel.taskForViewProof.collectAsState()
    val proofNotice by viewModel.proofNotice.collectAsState()

    var showCustomTaskDialog by remember { mutableStateOf(false) }
    var selectedTaskForDetails by remember { mutableStateOf<DailyTask?>(null) }

    // Live ticker for countdown timer
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d-MMMM", Locale("uz", "UZ"))
        val dateStr = sdf.format(Date())
        dateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    if (showCustomTaskDialog) {
        CustomTaskDialog(
            onDismiss = { showCustomTaskDialog = false },
            onConfirm = { title, category, description, hour, minute, reminderOffsetMinutes, repeatFrequency ->
                viewModel.addCustomTask(title, category, description, hour, minute, reminderOffsetMinutes, repeatFrequency)
                showCustomTaskDialog = false
            }
        )
    }

    // Photo Proof Verification Dialog
    taskForProofDialog?.let { task ->
        PhotoProofDialog(
            task = task,
            onDismiss = { viewModel.closeProofDialog() },
            onConfirmProof = { photoUri ->
                viewModel.completeTaskWithProof(task, photoUri)
            },
            onRejectWithoutProof = {
                viewModel.rejectTaskWithoutProof(task)
            }
        )
    }

    // View Already Attached Proof Dialog
    taskForViewProof?.let { task ->
        ViewProofDialog(
            task = task,
            onDismiss = { viewModel.closeViewProof() },
            onRemoveProof = {
                viewModel.uncompleteTask(task)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeuroBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App & Date Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧠",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Miya Rejimi",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeuroAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeuroAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔥", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streak kunlik",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeuroAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Progress Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeuroSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kunlik intizom",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "$completedCount / $totalCount bajarildi (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NeuroCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeuroCyan,
                            trackColor = NeuroSurfaceVariant,
                        )
                    }
                }

                if (adminAnnouncement.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = NeuroIndigo.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeuroIndigo.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📢", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Boshqaruvchi Tavsiyasi",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeuroIndigo
                                )
                                Text(
                                    text = adminAnnouncement,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (proofNotice != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (proofNotice!!.contains("✅") || proofNotice!!.contains("📸")) NeuroEmerald.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (proofNotice!!.contains("✅") || proofNotice!!.contains("📸")) NeuroEmerald.copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (proofNotice!!.contains("✅") || proofNotice!!.contains("📸")) "📸" else "⚠️",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = proofNotice!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearProofNotice() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Yopish",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // NEXT ACTIVE TASK CARD (Prominent Hero Card with Countdown)
        item {
            if (nextTask != null) {
                ActiveTaskHeroCard(
                    task = nextTask!!,
                    currentTimeMillis = currentTimeMillis,
                    onComplete = { viewModel.markTaskCompleted(nextTask!!, true) },
                    onSnooze = { viewModel.snoozeTask(nextTask!!, 15) }
                )
            } else if (totalCount > 0 && completedCount == totalCount) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NeuroEmerald.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeuroEmerald.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎉", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Barcha buyruqlar bajarildi!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Bugungi miya rejimi a'lo darajada o'tdi. Yaxshi dam oling!",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeuroEmerald
                            )
                        }
                    }
                }
            }
        }

        // Section: Add Personal Task Button & Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugungi tartib jadvali",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = { showCustomTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuroSurfaceVariant,
                        contentColor = NeuroCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_custom_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vazifa qo'shish",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Timeline items
        items(tasks, key = { it.id }) { task ->
            TaskRowCard(
                task = task,
                isNext = task.id == nextTask?.id,
                onToggleDone = { viewModel.markTaskCompleted(task, !task.isCompleted) },
                onSnooze = { viewModel.snoozeTask(task, 15) },
                onViewProof = { viewModel.openViewProof(task) },
                onDelete = if (task.isCustom) { { viewModel.deleteTask(task.id) } } else null,
                onClickDetails = { selectedTaskForDetails = task }
            )
        }
    }

    // Detail Dialog if clicked
    selectedTaskForDetails?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTaskForDetails = null },
            title = {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeuroCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = task.getFormattedTime(),
                                color = NeuroCyan,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bo'lim: ${task.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    if (task.isSnoozed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏱ 15 daqiqaga qoldirilgan",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeuroAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (task.hasProof()) NeuroEmerald.copy(alpha = 0.15f) else NeuroAmber.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (task.hasProof()) NeuroEmerald.copy(alpha = 0.4f) else NeuroAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (task.hasProof()) "📸" else "⚠️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (task.hasProof()) "Rasm orqali isbotlangan ✓" else "Isbot rasmi biriktirilmagan (Bajarilmagan)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (task.hasProof()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.openViewProof(task)
                                selectedTaskForDetails = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeuroEmerald),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeuroEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Biriktirilgan isbot rasmini ko'rish")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markTaskCompleted(task, !task.isCompleted)
                        selectedTaskForDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) NeuroSurfaceVariant else NeuroEmerald,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (task.isCompleted) "Bekor qilish" else "📸 Rasm bilan isbotlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTaskForDetails = null }) {
                    Text("Yopish", color = TextSecondary)
                }
            },
            containerColor = NeuroSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ActiveTaskHeroCard(
    task: DailyTask,
    currentTimeMillis: Long,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onViewProof: (() -> Unit)? = null
) {
    val countdownText = remember(task, currentTimeMillis) {
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, task.targetHour)
            set(Calendar.MINUTE, task.targetMinute)
            set(Calendar.SECOND, 0)
        }
        val targetMillis = if (task.isSnoozed && task.snoozedUntilTime != null) {
            task.snoozedUntilTime
        } else {
            targetCal.timeInMillis
        }

        val diffSec = (targetMillis - currentTimeMillis) / 1000
        if (diffSec <= 0) {
            "Hozir vaqti bo'ldi! ⚡"
        } else {
            val hours = diffSec / 3600
            val minutes = (diffSec % 3600) / 60
            val seconds = diffSec % 60
            if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d qoldi", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d qoldi", minutes, seconds)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = NeuroSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                listOf(NeuroCyan, NeuroIndigo)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_task_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeuroCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "KEYINGI BUYRUQ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = NeuroCyan,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeuroBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = task.getFormattedTime(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mandatory photo proof badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NeuroCyan.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = NeuroCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vazifani bajarish uchun rasm orqali isbot talab qilinadi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Countdown Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeuroSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = NeuroAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bajarish vaqti:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = countdownText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (countdownText.contains("Hozir")) NeuroEmerald else NeuroCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two primary action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuroEmerald,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("hero_complete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rasm bilan isbotlash",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedButton(
                    onClick = onSnooze,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderBright),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_snooze_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeuroAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Keyinroq",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRowCard(
    task: DailyTask,
    isNext: Boolean,
    onToggleDone: () -> Unit,
    onSnooze: () -> Unit,
    onViewProof: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onClickDetails: () -> Unit
) {
    val cardBackground by animateColorAsState(
        targetValue = when {
            task.isCompleted -> NeuroSurface.copy(alpha = 0.5f)
            isNext -> NeuroSurfaceElevated
            else -> NeuroSurface
        },
        label = "bg"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isNext) NeuroCyan.copy(alpha = 0.6f) else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetails() }
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox icon
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier.size(36.dp)
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Bajarildi",
                        tint = NeuroEmerald,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Bajarilmagan",
                        tint = if (isNext) NeuroCyan else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (task.isCompleted) NeuroSurfaceVariant else NeuroCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = task.getFormattedTime(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) TextMuted else NeuroCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = task.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isCustom) NeuroIndigo else TextSecondary,
                        fontWeight = if (task.isCustom) FontWeight.Bold else FontWeight.Normal
                    )

                    if (task.isCompleted && task.hasProof()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeuroEmerald.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeuroEmerald.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable { onViewProof?.invoke() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = NeuroEmerald,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Isbot ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuroEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (!task.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeuroCyan.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { onToggleDone() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = NeuroCyan,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Rasm isboti",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuroCyan
                                )
                            }
                        }
                    }

                    if (task.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeuroIndigo.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Shaxsiy",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeuroIndigo,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (task.reminderOffsetMinutes > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeuroCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🔔 " + task.getReminderLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuroCyan,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        if (task.repeatFrequency != "ONCE") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeuroAmber.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🔁 " + task.getFrequencyLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuroAmber,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) TextMuted else Color.White,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.isSnoozed && !task.isCompleted) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⏱ 15 daqiqaga qoldirilgan",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeuroAmber
                    )
                }
            }

            // Quick actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!task.isCompleted) {
                    IconButton(
                        onClick = onSnooze,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Keyinroq",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "O'chirish",
                            tint = NeuroRose.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
