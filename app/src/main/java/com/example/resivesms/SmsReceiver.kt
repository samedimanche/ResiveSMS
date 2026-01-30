package com.example.resivesms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return

        val prefs = context.getSharedPreferences("sms_prefs", Context.MODE_PRIVATE)

        val patterns = prefs.getString("patterns", "") ?: return
        val phone = prefs.getString("phone", "")
        val forwardTypeId = prefs.getInt("forwardType", R.id.rbSms)
        val telegramToken = prefs.getString("telegramToken", "")
        val telegramChatId = prefs.getString("telegramChatId", "")

        if (patterns.isEmpty()) return
        val patternList = patterns.split("|")

        // Склеиваем ВСЕ части входящего SMS
        val fullMessage = StringBuilder()
        for (pdu in pdus) {
            val sms = SmsMessage.createFromPdu(pdu as ByteArray)
            fullMessage.append(sms.messageBody)
        }

        val msg = fullMessage.toString()
        Log.d("SmsReceiver", "FULL SMS: $msg")

        for (p in patternList) {
            if (msg.contains(p.trim(), ignoreCase = true)) {

                when (forwardTypeId) {

                    // SMS
                    R.id.rbSms -> {
                        if (!phone.isNullOrEmpty()) {
                            val smsManager = SmsManager.getDefault()

                            val parts = smsManager.divideMessage(msg)
                            smsManager.sendMultipartTextMessage(
                                phone,
                                null,
                                parts,
                                null,
                                null
                            )

                            Log.d("SmsReceiver", "SMS переслан (multipart) на $phone")
                        }
                    }

                    // TELEGRAM
                    R.id.rbTelegram -> {
                        if (!telegramToken.isNullOrEmpty() && !telegramChatId.isNullOrEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                sendToTelegram(msg, telegramChatId, telegramToken)
                            }
                        }
                    }
                }
                break
            }
        }
    }

    private fun sendToTelegram(message: String, chatId: String, botToken: String) {
        try {
            val encoded = URLEncoder.encode(message, "UTF-8")
            val url =
                "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                Log.d("SmsReceiver", "Telegram response: ${response.code}")
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Telegram error", e)
        }
    }
}
