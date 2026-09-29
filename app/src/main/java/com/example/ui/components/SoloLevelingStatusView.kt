package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class HunterRankData(
    val rank: String,
    val color: Color,
    val title: String,
    val job: String
)

@Composable
fun SoloLevelingStatusView(
    totalXp: Int,
    streak: Int,
    completedCount: Int,
    totalCount: Int,
    totalDeepWorkMinutes: Int,
    reactionCount: Int,
    isProActive: Boolean,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation for Solo Leveling blue neon glow
    val infiniteTransition = rememberInfiniteTransition(label = "solo_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Calculate Solo Leveling Level & Hunter Rank
    val playerLevel = remember(totalXp) {
        val calculated = (totalXp / 80) + 1
        calculated.coerceAtLeast(1)
    }

    val rankInfo = remember(playerLevel) {
        when {
            playerLevel < 8 -> HunterRankData(
                "E-Rang Ovchi",
                Color(0xFF94A3B8), // Slate
                "Eng Zaif Ovchi (Boshlovchi)",
                "Oddiy O'yinchi"
            )
            playerLevel < 18 -> HunterRankData(
                "D-Rang Ovchi",
                Color(0xFF38BDF8), // Light Blue
                "Uyg'ongan Yangi Kuch",
                "Fokus Jangchisi"
            )
            playerLevel < 35 -> HunterRankData(
                "C-Rang Ovchi",
                Color(0xFF34D399), // Emerald
                "Retsidivist Intizomchi",
                "Biohaker"
            )
            playerLevel < 60 -> HunterRankData(
                "B-Rang Ovchi",
                Color(0xFF818CF8), // Indigo
                "Elita Diqqat Ustasi",
                "Neyro-Assasin"
            )
            playerLevel < 90 -> HunterRankData(
                "A-Rang Ovchi",
                Color(0xFFF472B6), // Pink
                "Reyd Boshqaruvchisi",
                "Temir Iroda Ustasi"
            )
            playerLevel < 130 -> HunterRankData(
                "S-Rang Ovchi ⚔️",
                Color(0xFFFBBF24), // Amber Gold
                "Afsonaviy S-Daraja",
                "Miya Monarxi Nomzodi"
            )
            playerLevel < 180 -> HunterRankData(
                "Milliy Darajadagi Ovchi 👑",
                Color(0xFFF97316), // Orange
                "Davlat Quvvatiga Teng",
                "Mutlaq Hukmdor"
            )
            else -> HunterRankData(
                "SOYALAR HUKMDORI 🖤",
                Color(0xFFA855F7), // Purple Monarch
                "Bo'ysunmas Monarx (Sung Jinwoo)",
                "Soyalar Monarxi"
            )
        }
    }
    val hunterRank = rankInfo.rank
    val rankColor = rankInfo.color
    val title = rankInfo.title
    val job = rankInfo.job

    // RPG Stats based on user achievements
    val strStat = 12 + (completedCount * 3)
    val staStat = 15 + (streak * 4)
    val agiStat = 10 + (reactionCount * 3)
    val intStat = 14 + (totalDeepWorkMinutes / 12)
    val perStat = 10 + (completedCount * 2) + (streak * 2)

    val maxHp = 1000 + (staStat * 25)
    val currentHp = maxHp
    val maxMp = 400 + (intStat * 20)
    val currentMp = maxMp
    val fatigue = if (totalCount > 0) ((totalCount - completedCount) * 10).coerceIn(0, 100) else 0

    val neonBlue = Color(0xFF00E5FF)
    val neonPurple = Color(0xFF9D4EDD)
    val darkBlueBg = Color(0xFF050B14)
    val cardBg = Color(0xFF0A1324)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("solo_leveling_status_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Solo Leveling Holographic Main Window
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = cardBg,
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        neonBlue.copy(alpha = glowAlpha),
                        neonPurple.copy(alpha = glowAlpha * 0.8f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // System Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(neonBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[TIZIM: STATUS OYNASI]",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = neonBlue,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = rankColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, rankColor)
                    ) {
                        Text(
                            text = hunterRank,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rankColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Player Header info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "O'YINCHI: SIZ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Unvon: $title",
                            style = MaterialTheme.typography.bodySmall,
                            color = neonBlue
                        )
                        Text(
                            text = "Kasb: $job",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LEVEL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "$playerLevel",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = neonBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HP, MP and Fatigue Bars
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // HP Bar
                    StatusBar(
                        label = "HP (Sog'liq)",
                        current = currentHp,
                        max = maxHp,
                        color = Color(0xFFEF4444)
                    )
                    // MP Bar
                    StatusBar(
                        label = "MP (Diqqat quvvati)",
                        current = currentMp,
                        max = maxMp,
                        color = Color(0xFF00E5FF)
                    )
                    // Charchoq Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Charchoq darajasi (Fatigue):",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$fatigue%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (fatigue > 50) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(14.dp))

                // RPG Stats Grid
                Text(
                    text = "ASOSIY STATUSLAR (STATS):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = neonBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(label = "KUCH (STR)", value = strStat, icon = "⚔️", modifier = Modifier.weight(1f))
                    StatBox(label = "CHIDAM (STA)", value = staStat, icon = "🛡️", modifier = Modifier.weight(1f))
                    StatBox(label = "CHAQQON (AGI)", value = agiStat, icon = "⚡", modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(label = "AQL (INT)", value = intStat, icon = "🧠", modifier = Modifier.weight(1f))
                    StatBox(label = "IDROK (PER)", value = perStat, icon = "👁️", modifier = Modifier.weight(1f))
                    StatBox(label = "XP OCHKO", value = totalXp, icon = "💎", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Solo Leveling Skills
                Text(
                    text = "MAXSUS KO'NIKMALAR (SKILLS):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = neonPurple,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                SkillItem(name = "Qat'iy Iroda [Passiv]", level = "Lv. MAX", desc = "Charchoq va dangasalikka qarshi 100% ruhiy qarshilik.")
                SkillItem(name = "Chuqur Fokus Maydoni [Faol]", level = "Lv. 3", desc = "90 daqiqalik chalg'imaslik va kognitiv intizom aurasini yoqadi.")
                SkillItem(name = "Sirkad Qonuni [Passiv]", level = "Lv. 2", desc = "Dofamin va kortizol balansi orqali uyquni mukammallashtiradi.")
            }
        }

        // Daily Quest Card: Solo Leveling Quest Window
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📜", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[KUNLIK VAZIFA: KUCHLIROQ BO'LISH]",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B)
                        )
                    }

                    Text(
                        text = "$completedCount / $totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (completedCount == totalCount && totalCount > 0) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Vazifa maqsadi: Bugungi barcha tartib vazifalarini rasm isboti bilan to'liq bajaring.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎁 Mukofot: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Stat ochkolar tiklanishi + Rang oshishi + 100 XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠️ Bajarilmasa: ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "[Jazo Hududi faollashadi: Charchoq va dangasalik 2 barobarga ortadi!]",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF4444).copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Pro Active status or Upgrade banner
        if (!isProActive) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Solo Leveling PRO Rejimi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Narxi: 1 oyga 9,999 so'm",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Solo Leveling status oynasi, cheksiz AI maslahatchi va shaxsiy biortm to'liq Pro rejimda ishlaydi. To'lov karta raqamiga (9860 3501 4072 3015) 9,999 so'm o'tkazilgach, admin tomonidan tasdiqlanadi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B),
                            contentColor = Color(0xFF090D16)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("upgrade_solo_leveling_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRO Faollashtirish (9,999 so'm / oy)",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    current: Int,
    max: Int,
    color: Color
) {
    val progress = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$current / $max",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun StatBox(
    label: String,
    value: Int,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SkillItem(
    name: String,
    level: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF9D4EDD))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = level,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF9D4EDD)
                    )
                }
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
