package com.example.resivesms

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private val PREFS = "sms_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPatterns = findViewById<EditText>(R.id.etPatterns)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val prefs: SharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE)

        etPatterns.setText(prefs.getString("patterns", ""))
        etPhone.setText(prefs.getString("phone", ""))

        btnSave.setOnClickListener {
            prefs.edit()
                .putString("patterns", etPatterns.text.toString())
                .putString("phone", etPhone.text.toString())
                .apply()

            Toast.makeText(this, "Сохранено", Toast.LENGTH_SHORT).show()
        }

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
