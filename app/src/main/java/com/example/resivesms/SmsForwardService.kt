package com.example.resivesms

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class SmsForwardService : Service() {

    override fun onCreate() {
        super.onCreate()

        val channelId = "sms_forward_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "ResiveSMS",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Сервис автоматической пересылки SMS"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ResiveSMS активно")
            .setContentText("Автоматическая пересылка SMS работает")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
