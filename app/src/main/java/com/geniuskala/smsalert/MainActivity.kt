package com.geniuskala.smsalert

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity:AppCompatActivity(){
    private lateinit var soundText:TextView

    private val permissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ updateStatus() }
    private val ringtone=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ r ->
        if(r.resultCode==RESULT_OK){
            val uri:Uri? = if(Build.VERSION.SDK_INT>=33)
                r.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI,Uri::class.java)
            else @Suppress("DEPRECATION") r.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if(uri!=null){
                Prefs.p(this).edit().putString("sound_uri",uri.toString()).apply()
                AlertHelper.recreateChannel(this); updateSound()
            }
        }
    }

    override fun onCreate(b:Bundle?){
        super.onCreate(b); setContentView(R.layout.activity_main)
        val p=Prefs.p(this)
        val enabled=findViewById<Switch>(R.id.enabled)
        val keys=findViewById<EditText>(R.id.keywords)
        val vib=findViewById<Switch>(R.id.vibration)
        val repeat=findViewById<Spinner>(R.id.repeatCount)
        val interval=findViewById<EditText>(R.id.interval)
        soundText=findViewById(R.id.soundText)

        enabled.isChecked=Prefs.enabled(this); keys.setText(p.getString("keywords","جنیوس کالا"))
        vib.isChecked=Prefs.vibration(this); interval.setText(Prefs.repeatMinutes(this).toString())
        repeat.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,(0..5).map{"$it بار"})
        repeat.setSelection(Prefs.repeats(this).coerceIn(0,5))

        findViewById<Button>(R.id.soundButton).setOnClickListener{
            ringtone.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply{
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,true)
            })
        }
        findViewById<Button>(R.id.saveButton).setOnClickListener{
            p.edit().putBoolean("enabled",enabled.isChecked)
                .putString("keywords",keys.text.toString())
                .putBoolean("vibration",vib.isChecked)
                .putInt("repeats",repeat.selectedItemPosition)
                .putInt("repeat_minutes",interval.text.toString().toIntOrNull()?.coerceAtLeast(1)?:5).apply()
            AlertHelper.recreateChannel(this)
            Toast.makeText(this,"تنظیمات ذخیره شد",Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.testButton).setOnClickListener{
            AlertHelper.show(this,"GeniusKala","این یک هشدار آزمایشی است","جنیوس کالا",false)
        }
        findViewById<Button>(R.id.permissionButton).setOnClickListener{requestPermissions()}
        findViewById<Button>(R.id.channelButton).setOnClickListener{
            if(Build.VERSION.SDK_INT>=26) startActivity(Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply{
                putExtra(Settings.EXTRA_APP_PACKAGE,packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID,"genius_sms_alert_v3")
            })
        }
        updateSound(); updateStatus()
    }

    private fun requestPermissions(){
        val x=mutableListOf(Manifest.permission.RECEIVE_SMS)
        if(Build.VERSION.SDK_INT>=33)x+=Manifest.permission.POST_NOTIFICATIONS
        permissions.launch(x.toTypedArray())
    }
    private fun updateSound(){
        val uri=Prefs.soundUri(this)?.let{Uri.parse(it)}?:Settings.System.DEFAULT_NOTIFICATION_URI
        soundText.text="صدای انتخاب‌شده: "+(RingtoneManager.getRingtone(this,uri)?.getTitle(this)?:"پیش‌فرض")
    }
    private fun updateStatus(){
        val sms=ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_SMS)==PackageManager.PERMISSION_GRANTED
        val n=Build.VERSION.SDK_INT<33||ContextCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
        findViewById<TextView>(R.id.status).text="دسترسی SMS: ${if(sms)"✓" else "✗"}    اعلان: ${if(n)"✓" else "✗"}"
    }
}
