package com.geniuskala.smsalert

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object AlertHelper {
    private const val CHANNEL="genius_sms_alert_v3"

    private fun sound(c:Context):Uri =
        Prefs.soundUri(c)?.let{runCatching{Uri.parse(it)}.getOrNull()}
            ?: Settings.System.DEFAULT_NOTIFICATION_URI

    fun recreateChannel(c:Context){
        if(Build.VERSION.SDK_INT<26)return
        val nm=c.getSystemService(NotificationManager::class.java)
        nm.deleteNotificationChannel(CHANNEL)
        val ch=NotificationChannel(CHANNEL,"هشدار اختصاصی جنیوس کالا",NotificationManager.IMPORTANCE_HIGH)
        ch.description="هشدار مستقل برای SMSهای مطابق کلمات کلیدی"
        val attrs=AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        ch.setSound(sound(c),attrs)
        ch.enableVibration(Prefs.vibration(c))
        if(Prefs.vibration(c)) ch.vibrationPattern=longArrayOf(0,350,180,350)
        nm.createNotificationChannel(ch)
    }

    fun show(c:Context,sender:String,body:String,keyword:String,repeated:Boolean){
        if(Build.VERSION.SDK_INT>=26){
            val nm=c.getSystemService(NotificationManager::class.java)
            if(nm.getNotificationChannel(CHANNEL)==null) recreateChannel(c)
        }
        val pi=PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val title=if(repeated)"تکرار هشدار: $keyword" else "پیامک مهم: $keyword"
        val b=NotificationCompat.Builder(c,CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText("$sender — $body")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$sender\n$body"))
            .setContentIntent(pi).setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(sound(c))
        if(Prefs.vibration(c)) b.setVibrate(longArrayOf(0,350,180,350))
        else b.setVibrate(longArrayOf(0))
        try{NotificationManagerCompat.from(c).notify((System.nanoTime()%Int.MAX_VALUE).toInt(),b.build())}
        catch(_:SecurityException){}
    }
}
