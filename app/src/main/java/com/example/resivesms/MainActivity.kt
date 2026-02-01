package com.example.resivesms

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private val PREFS = "sms_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(this, SmsForwardService::class.java)
            )
        } else {
            startService(
                Intent(this, SmsForwardService::class.java)
            )
        }


        // View
        val etPatterns = findViewById<EditText>(R.id.etPatterns)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val rgForwardType = findViewById<RadioGroup>(R.id.rgForwardType)
        val etTelegramToken = findViewById<EditText>(R.id.etTelegramToken)
        val etTelegramChatId = findViewById<EditText>(R.id.etTelegramChatId)

        // SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE)

        // Загрузка сохранённых значений
        etPatterns.setText(prefs.getString("patterns", ""))
        etPhone.setText(prefs.getString("phone", ""))
        etTelegramToken.setText(prefs.getString("telegramToken", ""))
        etTelegramChatId.setText(prefs.getString("telegramChatId", ""))
        val savedForwardType = prefs.getInt("forwardType", R.id.rbSms)
        rgForwardType.check(savedForwardType)

        // Сохранение всех данных одним кликом
        btnSave.setOnClickListener {
            prefs.edit()
                .putString("patterns", etPatterns.text.toString())
                .putString("phone", etPhone.text.toString())
                .putInt("forwardType", rgForwardType.checkedRadioButtonId)
                .putString("telegramToken", etTelegramToken.text.toString())
                .putString("telegramChatId", etTelegramChatId.text.toString())
                .apply()

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        }

        // Запрос разрешений
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ),
            1
        )
    }
}
