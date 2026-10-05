package com.geniuskala.smsalert
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class KeywordsActivity:AppCompatActivity(){
 private val words=mutableListOf<String>();private lateinit var list:LinearLayout;private lateinit var enabled:Switch
 override fun onCreate(b:Bundle?){
  super.onCreate(b);setContentView(R.layout.activity_keywords);list=findViewById(R.id.list);enabled=findViewById(R.id.enabled)
  words.addAll(Prefs.keywords(this));enabled.isChecked=Prefs.p(this).getBoolean("keywords_enabled",true)
  enabled.setOnCheckedChangeListener{_,v->Prefs.p(this).edit().putBoolean("keywords_enabled",v).apply()}
  val input=findViewById<EditText>(R.id.newKeyword)
  findViewById<Button>(R.id.addButton).setOnClickListener{val w=input.text.toString().trim();if(w.isNotEmpty()&&!words.contains(w)){words.add(w);input.text.clear();save();render()}}
  render()
 }
 private fun save(){Prefs.p(this).edit().putString("keywords",words.joinToString("\n")).apply()}
 private fun render(){
  list.removeAllViews()
  words.forEachIndexed{idx,w->
   val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(8,6,8,6)}
   val del=Button(this).apply{text="🗑";setOnClickListener{words.removeAt(idx);save();render()}}
   val tv=TextView(this).apply{text=w;textSize=17f;setTextColor(getColor(R.color.text));gravity=Gravity.CENTER_VERTICAL or Gravity.RIGHT
    layoutParams=LinearLayout.LayoutParams(0,58.dp,1f)}
   row.addView(del,LinearLayout.LayoutParams(58.dp,58.dp));row.addView(tv);list.addView(row)
  }
 }
 private val Int.dp:Int get()=(this*resources.displayMetrics.density).toInt()
}
