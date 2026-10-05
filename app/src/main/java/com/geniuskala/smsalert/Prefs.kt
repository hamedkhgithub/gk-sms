package com.geniuskala.smsalert
import android.content.Context
object Prefs {
    private const val NAME="settings_v5"
    fun p(c:Context)=c.getSharedPreferences(NAME,Context.MODE_PRIVATE)
    fun enabled(c:Context)=p(c).getBoolean("enabled",true)
    fun contactAlerts(c:Context)=p(c).getBoolean("contact_alerts",true)
    fun keywords(c:Context)=p(c).getString("keywords","جنیوس کالا")!!
        .split("\n",",","،").map{it.trim()}.filter{it.isNotEmpty()}
    fun vibration(c:Context)=p(c).getBoolean("vibration",true)
    fun repeats(c:Context)=p(c).getInt("repeats",0)
    fun repeatMinutes(c:Context)=p(c).getInt("repeat_minutes",5)
    fun soundUri(c:Context)=p(c).getString("sound_uri",null)
    fun setSoundUri(c:Context,uri:String?)=p(c).edit().apply{
        if(uri==null) remove("sound_uri") else putString("sound_uri",uri)
    }.commit()
}
