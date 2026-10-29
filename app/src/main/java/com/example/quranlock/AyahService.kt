package com.example.quranlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AyahService : Service() {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var task: ScheduledFuture<*>? = null
    private val channelId = "ayah"

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(channelId, "آية كل دقيقة", NotificationManager.IMPORTANCE_LOW)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val first = build("جاري تحميل الآية...", "")
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, first)
        }
        if (task == null) {
            task = executor.scheduleWithFixedDelay({ refresh() }, 0, 60, TimeUnit.SECONDS)
        }
        return START_STICKY
    }

    private fun refresh() {
        try {
            val number = Random.nextInt(1, 6237) // 6236 آية
            val conn = URL("https://api.alquran.cloud/v1/ayah/$number/quran-uthmani")
                .openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            val data = JSONObject(conn.inputStream.bufferedReader().readText()).getJSONObject("data")
            val title = "${data.getJSONObject("surah").getString("name")} • آية ${data.getInt("numberInSurah")}"
            getSystemService(NotificationManager::class.java)
                .notify(1, build(data.getString("text"), title))
        } catch (e: Exception) {
            // لو فشل الاتصال، تبقى الآية السابقة ظاهرة
        }
    }

    private fun build(text: String, title: String): Notification =
        Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    override fun onDestroy() {
        task?.cancel(true)
        executor.shutdownNow()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
