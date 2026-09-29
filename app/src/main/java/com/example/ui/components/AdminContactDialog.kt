package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun AdminContactDialog(
    onDismiss: () -> Unit,
    isAdmin: Boolean = false,
    isPro: Boolean = false,
    onAdminTogglePro: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    val adminPhone = "+998200134311"
    val cardNumber = "9860 3501 4072 3015"
    val proPrice = "9,999 so'm / oy"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NeuroSurface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("admin_contact_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👑", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Adminga Murojaat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Solo Leveling PRO & To'lov Paneli",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Yopish",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PRO Price & Card Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PRO Obuna Narxi:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = proPrice,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "To'lov Karta Raqami (Humo / Uzcard):",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Card number box with copy button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Karta", cardNumber.replace(" ", ""))
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Karta raqami nusxalandi!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cardNumber,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Nusxa olish",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Click, Payme yoki bank orqali 9,999 so'm o'tkazib, chekni quyidagi raqamga yuboring. Admin tasdiqlagach PRO yoqiladi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Admin Direct Contact Phone Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeuroSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Bosh Boshqaruvchi Raqami:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = adminPhone,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Direct Call button
                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$adminPhone")
                                    }
                                    try {
                                        context.startActivity(dialIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Qo'ng'iroq ilovasi topilmadi", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Qo'ng'iroq", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // SMS / Chek yuborish button
                            Button(
                                onClick = {
                                    val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("sms:$adminPhone")
                                        putExtra("sms_body", "Salom, Miya Rejimi Solo Leveling PRO uchun 9999 so'm to'lov qildim. Chekni tasdiqlashingizni so'rayman.")
                                    }
                                    try {
                                        context.startActivity(smsIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "SMS ilovasi topilmadi", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chek yuborish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In-App message field to Admin
                Text(
                    text = "Yoki ilova orqali murojaat yozing:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("To'lov cheki ma'lumoti yoki taklif...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeuroCyan,
                        unfocusedBorderColor = CardBorderBright
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            Toast.makeText(context, "Murojaatingiz adminga yetkazildi! Admin tez orada tekshiradi.", Toast.LENGTH_LONG).show()
                            messageText = ""
                            onDismiss()
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeuroCyan,
                        contentColor = Color(0xFF090D16)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adminga Yuborish", fontWeight = FontWeight.Bold)
                }

                // If currently Admin, show immediate verify/toggle button
                if (isAdmin && onAdminTogglePro != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🛠️ Bosh Boshqaruvchi Boshqaruvi:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            onAdminTogglePro(!isPro)
                            Toast.makeText(
                                context,
                                if (!isPro) "Admin tasdig'i: Solo Leveling PRO faollashtirildi!" else "PRO o'chirildi",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPro) Color(0xFFEF4444) else Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.WorkspacePremium, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPro) "PRO Rejimni O'chirish" else "To'lovni Tasdiqlash va PRO Yoqish",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
