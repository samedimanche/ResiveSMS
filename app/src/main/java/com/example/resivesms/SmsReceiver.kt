package com.example.resivesms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsManager
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return

        val prefs: SharedPreferences =
            context.getSharedPreferences("sms_prefs", Context.MODE_PRIVATE)

        val patterns = prefs.getString("patterns", "") ?: return
        val phone = prefs.getString("phone", "") ?: return

        if (patterns.isEmpty() || phone.isEmpty()) return

        val patternList = patterns.split("|")

        for (pdu in pdus) {
            val msg = SmsMessage.createFromPdu(pdu as ByteArray).messageBody

            for (p in patternList) {
                if (msg.contains(p.trim())) {
                    SmsManager.getDefault()
                        .sendTextMessage(phone, null, msg, null, null)
                    break
                }
            }
        }
    }
}
