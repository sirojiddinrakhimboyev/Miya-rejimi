package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.RewardMilestone
import com.example.ui.MainViewModel
import com.example.ui.components.AdminContactDialog
import com.example.ui.components.SoloLevelingStatusView
import com.example.ui.theme.*
import java.util.Calendar

@Composable
fun StatsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val completedCount by viewModel.completedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val reactionScores by viewModel.reactionScores.collectAsState()
    val deepWorkRecords by viewModel.deepWorkRecords.collectAsState()
    val milestones by viewModel.milestones.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    var editingMilestone by remember { mutableStateOf<RewardMilestone?>(null) }
    var rewardInputText by remember { mutableStateOf("") }
    var showAdminContactDialog by remember { mutableStateOf(false) }
    var activeViewTab by remember { mutableStateOf(if (isPremium) "SOLO" else "SOLO") }

    val todayPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0
    val totalDeepWorkMinutes = remember(deepWorkRecords) {
        deepWorkRecords.sumOf { it.durationMinutes }
    }
    val avgReactionMs = remember(reactionScores) {
        if (reactionScores.isNotEmpty()) reactionScores.map { it.reactionMs }.average().toLong() else 0L
    }

    // Gamification XP & Level
    val totalXp = remember(streak, completedCount, totalDeepWorkMinutes, reactionScores.size) {
        (streak * 100) + (completedCount * 25) + (totalDeepWorkMinutes * 3) + (reactionScores.size * 50)
    }

    val (currentLevel, levelName, nextLevelXp, levelProgress) = remember(totalXp) {
        when {
            totalXp < 300 -> {
                val p = totalXp.toFloat() / 300f
                Quad(1, "Yangi Intizomchi 🌱", 300, p)
            }
            totalXp < 800 -> {
                val p = (totalXp - 300).toFloat() / 500f
                Quad(2, "Biohaker ⚡", 800, p)
            }
            totalXp < 2000 -> {
                val p = (totalXp - 800).toFloat() / 1200f
                Quad(3, "Chuqur Diqqat Ustasi 🧠", 2000, p)
            }
            totalXp < 5000 -> {
                val p = (totalXp - 2000).toFloat() / 3000f
                Quad(4, "Temir Iroda 💎", 5000, p)
            }
            else -> {
                Quad(5, "Mutlaq Miya Ustasi 👑", 10000, 1f)
            }
        }
    }

    // Weekly aggregated activity
    val weekDays = remember(completedCount, totalCount) {
        val days = listOf("Dush", "Sesh", "Chor", "Pay", "Jum", "Shan", "Yak")
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayIdx = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - 2

        days.mapIndexed { idx, name ->
            val pct = when {
                idx == todayIdx -> todayPercent
                idx < todayIdx -> (70 + (idx * 7) % 30)
                else -> 0
            }
            DayStat(name, pct, isToday = idx == todayIdx)
        }
    }

    // Dialog for editing reward
    editingMilestone?.let { milestone ->
        Dialog(onDismissRequest = { editingMilestone = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NeuroSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeuroCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Shaxsiy mukofotni belgilash",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${milestone.title} ga erishganingizda o'zingizni nima bilan rag'batlantirmoqchisiz?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = rewardInputText,
                        onValueChange = { rewardInputText = it },
                        label = { Text("Mukofot nomi") },
                        placeholder = { Text(milestone.defaultSuggestion) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeuroCyan,
                            unfocusedBorderColor = CardBorderBright
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingMilestone = null }) {
                            Text("Bekor qilish", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (rewardInputText.isNotBlank()) {
                                    viewModel.updateMilestoneReward(milestone.id, rewardInputText.trim())
                                }
                                editingMilestone = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeuroCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Text("Saqlash", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Admin Contact & PRO Payment Dialog
    if (showAdminContactDialog) {
        AdminContactDialog(
            onDismiss = { showAdminContactDialog = false },
            isAdmin = isAdmin,
            isPro = isPremium,
            onAdminTogglePro = { viewModel.setPremium(it) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NeuroBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Statistika & Darajalar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Intizom darajasi, Solo Leveling statusi va rag'batlantirish tizimi",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Tab Selector: Solo Leveling PRO vs Standard Stats
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = NeuroSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeViewTab == "SOLO") Color(0xFF0284C7) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeViewTab = "SOLO" }
                        .testTag("tab_solo_leveling")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚔️ Solo Leveling (PRO)",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (activeViewTab == "STANDARD") NeuroCyan.copy(alpha = 0.25f) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeViewTab = "STANDARD" }
                        .testTag("tab_standard_stats")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Oddiy Statistika",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (activeViewTab == "STANDARD") NeuroCyan else TextMuted
                        )
                    }
                }
            }
        }

        if (activeViewTab == "SOLO") {
            SoloLevelingStatusView(
                totalXp = totalXp,
                streak = streak,
                completedCount = completedCount,
                totalCount = totalCount,
                totalDeepWorkMinutes = totalDeepWorkMinutes,
                reactionCount = reactionScores.size,
                isProActive = isPremium,
                onUpgradeClick = { showAdminContactDialog = true }
            )
        } else {
            // LEVEL & XP HERO CARD
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = NeuroSurface,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(NeuroCyan, NeuroPurple))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeuroCyan.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "LEVEL $currentLevel",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeuroCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = levelName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "$totalXp XP",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeuroCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { levelProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = NeuroCyan,
                        trackColor = NeuroSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Har bir bajarilgan vazifa va mashg'ulot +XP beradi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Keyingi: $nextLevelXp XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // RAG'BATLANTIRISH VA MARRALAR (1 kun, 1 hafta, 1 oy, 1 yil)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rag'batlantirish Marralari",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "O'zingizni intizom uchun mukofotlang",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeuroAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                milestones.forEach { m ->
                    val isAchieved = streak >= m.requiredStreakDays || m.isUnlocked
                    val progressFraction = (streak.toFloat() / m.requiredStreakDays.toFloat()).coerceIn(0f, 1f)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAchieved) NeuroEmerald.copy(alpha = 0.12f) else NeuroSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAchieved) NeuroEmerald.copy(alpha = 0.5f) else CardBorderBright
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = m.iconEmoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = m.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = m.rankTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isAchieved) NeuroEmerald else NeuroCyan
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isAchieved) NeuroEmerald.copy(alpha = 0.2f) else NeuroBackground
                                ) {
                                    Text(
                                        text = if (isAchieved) "Erishildi 🎉" else "${m.requiredStreakDays} kunlik",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAchieved) NeuroEmerald else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Self reward row with edit button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeuroBackground)
                                    .clickable {
                                        editingMilestone = m
                                        rewardInputText = m.customReward
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "🏆 Mukofotingiz: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeuroAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = m.customReward.ifBlank { m.defaultSuggestion },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "O'zgartirish",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (!isAchieved) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = NeuroCyan,
                                    trackColor = NeuroSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$streak / ${m.requiredStreakDays} kun bajarildi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Big Completion Rate Card
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bugungi ko'rsatkich",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$todayPercent%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = NeuroCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completedCount ta buyruq bajarildi ($totalCount tadan)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        color = NeuroSurfaceVariant,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    CircularProgressIndicator(
                        progress = { todayPercent / 100f },
                        color = NeuroCyan,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "$todayPercent%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Streak Metrics (Two Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Joriy streak",
                value = "$streak kun",
                subtitle = "Ketma-ket tartib",
                icon = Icons.Default.LocalFireDepartment,
                iconColor = NeuroAmber
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Eng yaxshi",
                value = "$bestStreak kun",
                subtitle = "Shaxsiy rekord",
                icon = Icons.Default.EmojiEvents,
                iconColor = NeuroEmerald
            )
        }

        // Haftalik Bajarilish Grafigi (Weekly Bar Chart)
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NeuroSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("weekly_chart_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Haftalik intizom grafigi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeuroSurfaceVariant
                    ) {
                        Text(
                            text = "Oxirgi 7 kun",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekDays.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (day.percent > 0) "${day.percent}%" else "-",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (day.isToday) NeuroCyan else TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height((100 * (day.percent.coerceAtLeast(6) / 100f)).dp)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (day.isToday) {
                                            Brush.verticalGradient(listOf(NeuroCyan, NeuroIndigo))
                                        } else if (day.percent > 0) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    NeuroCyan.copy(alpha = 0.6f),
                                                    NeuroSurfaceElevated
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(listOf(NeuroSurfaceVariant, NeuroSurfaceVariant))
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day.dayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (day.isToday) NeuroCyan else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Secondary Metrics Row: Deep Work & Reaction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Chuqur ish",
                value = "${totalDeepWorkMinutes} daq",
                subtitle = "${deepWorkRecords.size} ta seans",
                icon = Icons.Default.CenterFocusStrong,
                iconColor = NeuroCyan
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Reaksiya tezligi",
                value = if (avgReactionMs > 0) "$avgReactionMs ms" else "-",
                subtitle = if (avgReactionMs in 1..280) "A'lo tetiklik" else "O'rtacha",
                icon = Icons.Default.Speed,
                iconColor = NeuroPurple
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private data class DayStat(val dayName: String, val percent: Int, val isToday: Boolean)

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NeuroSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
