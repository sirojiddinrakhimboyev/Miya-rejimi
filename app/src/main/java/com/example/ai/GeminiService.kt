package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val systemInstruction = """
        Siz "Miya Rejimi" ilovasining shaxsiy sun'iy intellekt maslahatchisisiz (Neuro Coach).
        Sizning vazifangiz foydalanuvchiga sirkad ritmlar, uyqu gigiyenasi, kofein qoidalari, ertalabki quyosh nuri, chuqur diqqat (Deep Work), miyani charchoqdan tiklash va kunlik intizom bo'yicha ilmiy asoslangan (Endryu Xuberman, Mettyu Uolker, Kel Nyport qoidalari) amaliy maslahatlar berish.
        Qoidalar:
        1. Javoblaringizni o'zbek tilida, do'stona, qat'iy va motivatsion tarzda yozing.
        2. Maslahatlaringiz aniq va amaliy bo'lsin (masalan: daqiqalar, vaqtlar, harakatlar).
        3. Javobni chiroyli formatda (punktlar, emojilar bilan) bering.
    """.trimIndent()

    suspend fun askAdvisor(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getExpertOfflineAdvice(prompt)
        }

        try {
            // Direct REST API call as specified in gemini-api skill
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                // System instruction
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                })
                // Generation config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 800)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text").trim()
                    }
                }
            }
            Log.w("GeminiService", "API call unsuccesful or empty, code: ${response.code}, fallbacking")
            getExpertOfflineAdvice(prompt)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API: ${e.message}", e)
            getExpertOfflineAdvice(prompt)
        }
    }

    private fun getExpertOfflineAdvice(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("kofein") || lower.contains("qahva") || lower.contains("choy") -> {
                """
                ☕ **Kofein va Miya Ishlash Qoidasi:**
                
                1. **Uyg'ongandan keyin 60-90 daqiqa kuting:**
                   Ertalab uyg'onganingizda tanada kortizol tabiiy ravishda ko'tariladi. Darhol qahva ichsangiz, organizm adenozinni tozalashga ulgurmaydi va tushdan keyin (14:00 da) kuchli charchoq paydo bo'ladi.
                2. **Soat 14:00 da to'xtating:**
                   Kofein tanada 6-8 soat davomida saqlanadi. Kechasi uxlab qolsangiz ham, chuqur (Delta) uyqu fazasi buziladi.
                3. **Suv balansi:**
                   Har bir chashka qahva uchun 1 stakan toza suv ichishni unutmang.
                """.trimIndent()
            }
            lower.contains("chuqur ish") || lower.contains("diqqat") || lower.contains("focus") || lower.contains("chalg'i") -> {
                """
                🧠 **Chuqur Ish (Deep Work) bo'yicha Maslahatlar:**
                
                1. **90 daqiqalik bloklar (Ultradian ritmi):**
                   Miyaning maksimal konsentratsiya chegarasi 90 daqiqani tashkil etadi. Shundan so'ng neyronlar charchaydi.
                2. **Telefonni boshqa xonaga qo'ying:**
                   Tadqiqotlar shuni ko'rsatadiki, hatto ekrani o'chirilgan telefon stolda tursa ham, miya quvvatining 15-20% qismini uni tekshirmaslikni nazorat qilishga sarflaydi.
                3. **Oq yoki jigarrang shovqin:**
                   Agar atrofda shovqin bo'lsa, past ovozda binafsharang/jigarrang shovqin eshiting.
                4. **Tanaffusda ekranga qaramang:**
                   60-90 daqiqadan so'ng 10 daqiqa derazadan uzoqqa qarang yoki yurib keling.
                """.trimIndent()
            }
            lower.contains("quyosh") || lower.contains("nur") || lower.contains("ertalab") -> {
                """
                ☀️ **Ertalabki Quyosh Nuri — Eng Kuchli Bio-Xak:**
                
                1. **Melatoninni to'xtatadi:**
                   Ko'z to'r pardasidagi maxsus hujayralar (ipRGC) quyosh fotonlarini qabul qilib, miyadagi epifiz beziga melatonin (uyqu gormoni) ishlab chiqarishni to'xtatish haqida signal yuboradi.
                2. **Davomiyligi:**
                   Quyoshli kunda 5-10 daqiqa, bulutli kunda 15-20 daqiqa ochiq havoda (deraza oynasisiz) bo'lish yetarli.
                3. **Kechki uyquni sozlaydi:**
                   Bugun ertalab olgan quyosh nuringiz 14-16 soatdan keyin tanada melatonin ajralishini boshlab beradi.
                """.trimIndent()
            }
            lower.contains("uyqu") || lower.contains("uxlash") || lower.contains("yotish") -> {
                """
                🌙 **Sifatli Uyqu va Miya Regeneratsiyasi:**
                
                1. **60 daqiqa oldin raqamli detoks:**
                   Telefon va noutbuklarning ko'k nuri (blue light) miyaga hali kun davom etyapti degan noto'g'ri signal beradi.
                2. **Xona harorati:**
                   Ideal uxlash harorati 18-20°C. Salqin xonada miya chuqur uyquga tezroq ketadi.
                3. **Ertangi 3 ta vazifani yozing:**
                   Uxlamasdan oldin ertangi ishlarni qog'ozga tushirish miyadagi "ochiq sikllar"ni yopadi va xavotirni kamaytiradi.
                """.trimIndent()
            }
            lower.contains("reaksiya") || lower.contains("test") || lower.contains("sekin") -> {
                """
                ⚡ **Reaksiya Tezligini Oshirish:**
                
                1. **Gidratatsiya:**
                   1-2% suvsizlanish ham reaksiya tezligini 100 millisekundgacha susaytiradi. Bir stakan toza suv iching.
                2. **20 daqiqalik quvvat uyqusi (Power nap):**
                   Agar reaksiyangiz 400 ms dan oshgan bo'lsa, asab tizimingiz ortiqcha yuklangan. 20 daqiqa ko'zingizni yuming.
                3. **Yengil jismoniy mashq:**
                   10 ta o'tirib-turish yoki 5 daqiqa tez yurish miyaga qon oqimini 25% ga oshiradi.
                """.trimIndent()
            }
            else -> {
                """
                💡 **Miya Rejimi bo'yicha Shaxsiy Tavsiya:**
                
                Sizning kognitiv samaradorligingiz to'g'ridan-to'g'ri kundalik odatlaringizning izchilligiga bog'liq.
                
                • **Bosh qoida:** Bir vaqtda uxlab, bir vaqtda uyg'onish.
                • **Eng muhim ish:** Uyg'ongandan 2 soat keyin 60 daqiqalik chuqur ish seansini hech qanday chalg'ishlarsiz bajaring.
                • **Quvvat manbai:** Ertalabki 10 daqiqa quyosh nuri va soat 14:00 dan keyin kofeinsiz rejim.
                
                Ilovadagi **Bugun** bo'limida belgilangan buyruqlarni qat'iy bajarish orqali intizom streak'ingizni oshirib boring!
                """.trimIndent()
            }
        }
    }
}
