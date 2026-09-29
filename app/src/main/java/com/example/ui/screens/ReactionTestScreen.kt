package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ReactionScore
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private enum class ReactionState {
    IDLE,
    WAITING,
    READY,
    TOO_EARLY,
    RESULT
}

@Composable
fun ReactionTestScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scores by viewModel.reactionScores.collectAsState()

    var state by remember { mutableStateOf(ReactionState.IDLE) }
    var startTime by remember { mutableLongStateOf(0L) }
    var lastReactionMs by remember { mutableLongStateOf(0L) }
    var lastRating by remember { mutableStateOf("") }

    // Coroutine for waiting state
    LaunchedEffect(state) {
        if (state == ReactionState.WAITING) {
            val randomDelay = Random.nextLong(2000, 5000)
            delay(randomDelay)
            if (state == ReactionState.WAITING) {
                startTime = System.currentTimeMillis()
                state = ReactionState.READY
            }
        }
    }

    val testAreaColor by animateColorAsState(
        targetValue = when (state) {
            ReactionState.IDLE -> NeuroSurface
            ReactionState.WAITING -> Color(0xFFDC2626) // Red - Wait!
            ReactionState.READY -> Color(0xFF16A34A) // Green - Tap NOW!
            ReactionState.TOO_EARLY -> Color(0xFFB91C1C)
            ReactionState.RESULT -> NeuroSurfaceElevated
        },
        label = "testColor"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NeuroBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Miya Reaksiya Testi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Haftada 1 marta asab tizimi tetikligini tekshirish",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Test Interactive Board
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = testAreaColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (state == ReactionState.READY) Color.White else CardBorderBright
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        when (state) {
                            ReactionState.IDLE -> {
                                state = ReactionState.WAITING
                            }
                            ReactionState.WAITING -> {
                                state = ReactionState.TOO_EARLY
                            }
                            ReactionState.READY -> {
                                val reactionMs = System.currentTimeMillis() - startTime
                                lastReactionMs = reactionMs
                                val rating = when {
                                    reactionMs < 250 -> "Super tetik 🚀"
                                    reactionMs < 320 -> "Yaxshi holatda ⚡"
                                    reactionMs < 400 -> "O'rtacha diqqat 💡"
                                    else -> "Charchagan / Dam kerak 😴"
                                }
                                lastRating = rating
                                viewModel.recordReactionScore(reactionMs)
                                state = ReactionState.RESULT
                            }
                            ReactionState.TOO_EARLY, ReactionState.RESULT -> {
                                state = ReactionState.WAITING
                            }
                        }
                    }
                    .testTag("reaction_test_board")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    when (state) {
                        ReactionState.IDLE -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "⚡", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Boshlash uchun bosing",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Qizil chiqqanda kuting, Yashil bo'lishi bilan imkon qadar tez bosing!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        ReactionState.WAITING -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🛑", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "KUTING...",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Yashil rang chiqishini kuting",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        ReactionState.READY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🟢", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "TEZ BOSING!",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                        ReactionState.TOO_EARLY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "⚠️", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Juda erta!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Yashil chiqishidan oldin bosdingiz. Qayta boshlash uchun bosing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        ReactionState.RESULT -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$lastReactionMs ms",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Black,
                                    color = NeuroCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = lastRating,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Qayta sinash uchun bosing 🔄",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Benchmark Reference Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = NeuroSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ilmiy me'yorlar:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BenchmarkRow("< 250 ms", "Super tetik (sportchi darajasi)", NeuroEmerald)
                    BenchmarkRow("250 - 320 ms", "Yaxshi holat (optimal kognitiv quvvat)", NeuroCyan)
                    BenchmarkRow("320 - 400 ms", "O'rtacha (kunlik me'yor)", NeuroAmber)
                    BenchmarkRow("> 400 ms", "Charchagan (uyqu yoki dam zarur)", NeuroRose)
                }
            }
        }

        // History Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Natijalar tarixi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${scores.size} ta sinov",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (scores.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeuroSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hozircha test o'tkazilmadi. Yuqoridagi taxtachaga bosib birinchi natijangizni o'lchang!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(scores, key = { it.id }) { score ->
                ReactionScoreRow(score = score)
            }
        }
    }
}

@Composable
private fun BenchmarkRow(range: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = range,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun ReactionScoreRow(score: ReactionScore) {
    val dateFormatted = remember(score.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(score.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NeuroSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = score.rating,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeuroSurfaceVariant
            ) {
                Text(
                    text = "${score.reactionMs} ms",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeuroCyan,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
