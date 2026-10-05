package com.geniuskala.smsalert
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class AlertSettingsActivity:AppCompatActivity(){
 private lateinit var soundText:TextView;private lateinit var channelStatus:TextView
 private val picker=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){r->
  if(r.resultCode==RESULT_OK){
   val uri:Uri?=if(Build.VERSION.SDK_INT>=33)r.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,Uri::class.java)
   else {@Suppress("DEPRECATION") r.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)}
   if(uri!=null&&Prefs.setSoundUri(this,uri.toString())){AlertHelper.rebuildChannel(this);refresh()}
  }
 }
 override fun onCreate(b:Bundle?){
  super.onCreate(b);setContentView(R.layout.activity_alert_settings)
  soundText=findViewById(R.id.soundText);channelStatus=findViewById(R.id.channelStatus)
  val vib=findViewById<Switch>(R.id.vibration);val rep=findViewById<Spinner>(R.id.repeatCount);val interval=findViewById<EditText>(R.id.interval)
  vib.isChecked=Prefs.vibration(this);interval.setText(Prefs.repeatMinutes(this).toString())
  rep.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,(0..5).map{"$it بار"});rep.setSelection(Prefs.repeats(this).coerceIn(0,5))
  findViewById<Button>(R.id.soundButton).setOnClickListener{picker.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply{
   putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,true)
   putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false);putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,AlertHelper.soundUri(this@AlertSettingsActivity))})}
  findViewById<Button>(R.id.defaultSoundButton).setOnClickListener{Prefs.setSoundUri(this,null);AlertHelper.rebuildChannel(this);refresh()}
  findViewById<Button>(R.id.saveButton).setOnClickListener{
   Prefs.p(this).edit().putBoolean("vibration",vib.isChecked).putInt("repeats",rep.selectedItemPosition)
    .putInt("repeat_minutes",interval.text.toString().toIntOrNull()?.coerceAtLeast(1)?:5).apply()
   AlertHelper.rebuildChannel(this);refresh();Toast.makeText(this,"تنظیمات ذخیره شد",Toast.LENGTH_SHORT).show()}
  findViewById<Button>(R.id.channelButton).setOnClickListener{if(Build.VERSION.SDK_INT>=26)startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply{
   putExtra(Settings.EXTRA_APP_PACKAGE,packageName);putExtra(Settings.EXTRA_CHANNEL_ID,AlertHelper.CHANNEL_ID)})}
  refresh()
 }
 private fun refresh(){soundText.text=AlertHelper.soundTitle(this);channelStatus.text="وضعیت کانال: "+AlertHelper.channelStatus(this)}
}
