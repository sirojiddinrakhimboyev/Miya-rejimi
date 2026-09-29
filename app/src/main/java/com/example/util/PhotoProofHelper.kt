package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PhotoProofHelper {

    /**
     * Copies an external content URI into app's private files directory
     * so that it persists reliably and can be accessed by Coil without permission expiration.
     */
    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val proofsDir = File(context.filesDir, "task_proofs")
            if (!proofsDir.exists()) proofsDir.mkdirs()

            val fileName = "proof_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val destFile = File(proofsDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }

    /**
     * Generates a verified test proof photo with timestamp and task details.
     * Useful for testing in emulators where camera/gallery photos might not be preloaded.
     */
    fun createSampleProofPhoto(context: Context, taskTitle: String): String? {
        return try {
            val proofsDir = File(context.filesDir, "task_proofs")
            if (!proofsDir.exists()) proofsDir.mkdirs()

            val width = 800
            val height = 600
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            val bgPaint = Paint().apply {
                color = android.graphics.Color.rgb(15, 23, 42) // Slate dark
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Accent card
            val cardPaint = Paint().apply {
                color = android.graphics.Color.rgb(30, 41, 59)
            }
            canvas.drawRoundRect(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat(), 24f, 24f, cardPaint)

            // Badge
            val badgePaint = Paint().apply {
                color = android.graphics.Color.rgb(6, 182, 212) // Cyan
                textSize = 36f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("📸 MIYA REJIMI • ISBOT HUJJATI", 70f, 110f, badgePaint)

            // Task title
            val titlePaint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 34f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val shortTitle = if (taskTitle.length > 30) taskTitle.take(30) + "..." else taskTitle
            canvas.drawText("Vazifa: $shortTitle", 70f, 180f, titlePaint)

            // Status Verified
            val statusPaint = Paint().apply {
                color = android.graphics.Color.rgb(16, 185, 129) // Emerald
                textSize = 30f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("Holati: Bajarildi va Rasm bilan Tasdiqlandi ✓", 70f, 250f, statusPaint)

            // Date & Time
            val timeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = timeSdf.format(Date())
            val datePaint = Paint().apply {
                color = android.graphics.Color.rgb(148, 163, 184)
                textSize = 24f
                isAntiAlias = true
            }
            canvas.drawText("Vaqti: $dateStr", 70f, 320f, datePaint)

            // Decorative stamp icon
            val stampPaint = Paint().apply {
                color = android.graphics.Color.rgb(16, 185, 129)
                style = Paint.Style.STROKE
                strokeWidth = 6f
                isAntiAlias = true
            }
            canvas.drawCircle((width - 150).toFloat(), (height - 150).toFloat(), 70f, stampPaint)
            val stampTextPaint = Paint().apply {
                color = android.graphics.Color.rgb(16, 185, 129)
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("TASDIQ", (width - 150).toFloat(), (height - 155).toFloat(), stampTextPaint)
            canvas.drawText("VERIFIED", (width - 150).toFloat(), (height - 130).toFloat(), stampTextPaint)

            val fileName = "proof_sample_${System.currentTimeMillis()}.jpg"
            val destFile = File(proofsDir, fileName)
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
