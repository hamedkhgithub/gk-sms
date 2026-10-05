package com.geniuskala.smsalert
import android.content.*
import android.provider.Telephony
import androidx.work.*
class SmsReceiver:BroadcastReceiver() {
    override fun onReceive(c:Context,i:Intent) {
        if(i.action!=Telephony.Sms.Intents.SMS_RECEIVED_ACTION || !Prefs.enabled(c)) return
        val msgs=Telephony.Sms.Intents.getMessagesFromIntent(i); if(msgs.isEmpty()) return
        val sender=msgs.first().displayOriginatingAddress ?: msgs.first().originatingAddress ?: "SMS"
        val body=msgs.joinToString("") { it.displayMessageBody ?: it.messageBody.orEmpty() }
        val contact=ContactHelper.nameFor(c,sender)
        val keyword=if(Prefs.p(c).getBoolean("keywords_enabled",true))
            Prefs.keywords(c).firstOrNull { body.contains(it,ignoreCase=true) } else null
        val reason=when {
            contact!=null && Prefs.contactAlerts(c) -> "مخاطب: $contact"
            keyword!=null -> "کلمه کلیدی: $keyword"
            else -> return
        }
        val display=contact ?: sender
        AlertHelper.show(c,display,body,reason)
        repeat(Prefs.repeats(c).coerceIn(0,5)) { n ->
            val data=workDataOf("sender" to display,"body" to body,"reason" to reason)
            val req=OneTimeWorkRequestBuilder<RepeatAlertWorker>()
                .setInitialDelay(((n+1)*Prefs.repeatMinutes(c).coerceAtLeast(1)).toLong(),
                    java.util.concurrent.TimeUnit.MINUTES).setInputData(data).build()
            WorkManager.getInstance(c).enqueue(req)
        }
    }
}
