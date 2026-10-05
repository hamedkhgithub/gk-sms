package com.geniuskala.smsalert

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.*

class SmsReceiver: BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        if(intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION || !Prefs.enabled(context)) return
        val msgs=Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if(msgs.isEmpty()) return
        val sender=msgs.first().displayOriginatingAddress ?: msgs.first().originatingAddress ?: "SMS"
        val body=msgs.joinToString(""){ it.displayMessageBody ?: it.messageBody.orEmpty() }
        val matched=Prefs.keywords(context).firstOrNull{ body.contains(it,ignoreCase=true) } ?: return

        AlertHelper.show(context,sender,body,matched,false)

        val count=Prefs.repeats(context).coerceIn(0,10)
        val mins=Prefs.repeatMinutes(context).coerceAtLeast(1)
        repeat(count){ i ->
            val data=workDataOf("sender" to sender,"body" to body,"keyword" to matched)
            val req=OneTimeWorkRequestBuilder<RepeatAlertWorker>()
                .setInitialDelay(((i+1)*mins).toLong(),java.util.concurrent.TimeUnit.MINUTES)
                .setInputData(data).build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
