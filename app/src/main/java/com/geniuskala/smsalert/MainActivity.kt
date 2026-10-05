package com.geniuskala.smsalert
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity:AppCompatActivity(){
 private lateinit var status:TextView
 private val permissions=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){updateStatus()}
 override fun onCreate(b:Bundle?){
  super.onCreate(b);setContentView(R.layout.activity_main)
  val contacts=findViewById<Switch>(R.id.contacts);val keys=findViewById<Switch>(R.id.keywordsSwitch)
  status=findViewById(R.id.status)
  contacts.isChecked=Prefs.contactAlerts(this)
  keys.isChecked=Prefs.p(this).getBoolean("keywords_enabled",true)
  contacts.setOnCheckedChangeListener{_,v->Prefs.p(this).edit().putBoolean("contact_alerts",v).apply()}
  keys.setOnCheckedChangeListener{_,v->Prefs.p(this).edit().putBoolean("keywords_enabled",v).apply()}
  findViewById<Button>(R.id.manageKeywordsButton).setOnClickListener{
   startActivity(Intent(this,KeywordsActivity::class.java))
  }
  findViewById<ImageButton>(R.id.settingsButton).setOnClickListener{startActivity(Intent(this,AlertSettingsActivity::class.java))}
  findViewById<Button>(R.id.permissionButton).setOnClickListener{requestPermissions()}
  findViewById<Button>(R.id.testButton).setOnClickListener{AlertHelper.show(this,"تست SMS Alert","این اعلان باید با صدای انتخابی و ویبره اجرا شود.","TEST")}
  AlertHelper.ensureChannel(this);updateStatus()
 }
 override fun onResume(){super.onResume();if(::status.isInitialized)updateStatus()}
 private fun granted(x:String)=ContextCompat.checkSelfPermission(this,x)==PackageManager.PERMISSION_GRANTED
 private fun requestPermissions(){
  val p=mutableListOf(Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_CONTACTS)
  if(Build.VERSION.SDK_INT>=33)p+=Manifest.permission.POST_NOTIFICATIONS
  permissions.launch(p.toTypedArray())
 }
 private fun updateStatus(){
  status.text="✓  دسترسی به پیامک‌ها: ${if(granted(Manifest.permission.RECEIVE_SMS))"فعال" else "نیاز به دسترسی"}\n"+
   "✓  دسترسی به مخاطبین: ${if(granted(Manifest.permission.READ_CONTACTS))"فعال" else "نیاز به دسترسی"}\n"+
   "✓  اعلان‌ها: ${if(Build.VERSION.SDK_INT<33||granted(Manifest.permission.POST_NOTIFICATIONS))"فعال" else "نیاز به دسترسی"}"
 }
}
