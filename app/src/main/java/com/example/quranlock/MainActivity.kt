package com.example.quranlock

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        val svc = Intent(this, AyahService::class.java)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(48, 96, 48, 48)
        }
        layout.addView(Button(this).apply {
            text = "ابدأ عرض الآيات"
            setOnClickListener { startForegroundService(svc) }
        })
        layout.addView(Button(this).apply {
            text = "إيقاف"
            setOnClickListener { stopService(svc) }
        })
        setContentView(layout)
    }
}
