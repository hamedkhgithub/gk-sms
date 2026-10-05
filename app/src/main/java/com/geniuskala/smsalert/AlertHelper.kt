package com.geniuskala.smsalert
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object AlertHelper {
    const val CHANNEL_ID="genius_sms_alert_v5"

    fun soundUri(c:Context):Uri =
        Prefs.soundUri(c)?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: Settings.System.DEFAULT_NOTIFICATION_URI

    fun soundTitle(c:Context):String = try {
        RingtoneManager.getRingtone(c,soundUri(c))?.getTitle(c) ?: "صدای پیش‌فرض سیستم"
    } catch(_:Exception) { "صدای پیش‌فرض سیستم" }

    fun rebuildChannel(c:Context) {
        if(Build.VERSION.SDK_INT<26) return
        val nm=c.getSystemService(NotificationManager::class.java)
        nm.deleteNotificationChannel(CHANNEL_ID)
        val attrs=AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val ch=NotificationChannel(CHANNEL_ID,"هشدار جنیوس کالا",NotificationManager.IMPORTANCE_HIGH).apply {
            description="هشدار مستقل برای مخاطبین و کلمات کلیدی"
            setSound(soundUri(c),attrs)
            enableVibration(Prefs.vibration(c))
            if(Prefs.vibration(c)) vibrationPattern=longArrayOf(0,500,200,500)
        }
        nm.createNotificationChannel(ch)
    }

    fun ensureChannel(c:Context) {
        if(Build.VERSION.SDK_INT>=26) {
            val nm=c.getSystemService(NotificationManager::class.java)
            if(nm.getNotificationChannel(CHANNEL_ID)==null) rebuildChannel(c)
        }
    }

    fun channelStatus(c:Context):String {
        if(Build.VERSION.SDK_INT<26) return "Android قدیمی: کنترل مستقیم برنامه"
        val ch=c.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL_ID)
            ?: return "کانال ساخته نشده"
        return "Sound: ${if(ch.sound!=null)"✓" else "✗"}   Vibration: ${if(ch.shouldVibrate())"✓" else "✗"}   Importance: ${ch.importance}"
    }

    fun show(c:Context,sender:String,body:String,reason:String,repeated:Boolean=false) {
        ensureChannel(c)
        val pi=PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val b=NotificationCompat.Builder(c,CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(if(repeated)"تکرار هشدار — $sender" else sender)
            .setContentText(body).setSubText(reason)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pi).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
        if(Build.VERSION.SDK_INT<26) {
            b.setSound(soundUri(c))
            if(Prefs.vibration(c)) b.setVibrate(longArrayOf(0,500,200,500))
        }
        try { NotificationManagerCompat.from(c).notify((System.nanoTime()%Int.MAX_VALUE).toInt(),b.build()) }
        catch(_:SecurityException){}
    }
}
