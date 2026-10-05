package com.geniuskala.smsalert
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
object ContactHelper {
    fun nameFor(c:Context,number:String):String? {
        if(ContextCompat.checkSelfPermission(c,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED) return null
        return try {
            val uri=android.net.Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,android.net.Uri.encode(number))
            c.contentResolver.query(uri,arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),null,null,null)?.use {
                if(it.moveToFirst()) it.getString(0) else null
            }
        } catch(_:Exception){ null }
    }
}
