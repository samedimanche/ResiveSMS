package com.example.resivesms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

        val prefs: SharedPreferences =
            context.getSharedPreferences("sms_prefs", Context.MODE_PRIVATE)

        val patterns = prefs.getString("patterns", "") ?: return
        val phone = prefs.getString("phone", "")
        val forwardTypeId = prefs.getInt("forwardType", R.id.rbSms)
        val telegramToken = prefs.getString("telegramToken", "")
        val telegramChatId = prefs.getString("telegramChatId", "")

        if (patterns.isEmpty()) return
        val patternList = patterns.split("|")

        for (pdu in pdus) {
            val sms = SmsMessage.createFromPdu(pdu as ByteArray)
            val msg = sms.messageBody

            for (p in patternList) {
                if (msg.contains(p.trim())) {
                    when (forwardTypeId) {
                        R.id.rbSms -> {
                            if (!phone.isNullOrEmpty()) {
                                SmsManager.getDefault().sendTextMessage(phone, null, msg, null, null)
                                Log.d("SmsReceiver", "Переслано через SMS на $phone: $msg")
                            }
                        }
                        R.id.rbTelegram -> {
                            if (!telegramToken.isNullOrEmpty() && !telegramChatId.isNullOrEmpty()) {
                                // Отправка в фоне через Coroutine
                                CoroutineScope(Dispatchers.IO).launch {
                                    sendToTelegramBot(msg, telegramChatId, telegramToken)
                                }
                            }
                        }
                    }
                    break
                }
            }
        }
    }

    private fun sendToTelegramBot(message: String, chatId: String, botToken: String) {
        try {
            // Кодируем сообщение чтобы Telegram принял пробелы и спецсимволы
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                Log.d("SmsReceiver", "Telegram response: code=${response.code}, body=$body")
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Ошибка отправки в Telegram: ${e.message}")
        }
    }
}
