package com.geniuskala.smsalert
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
class RepeatAlertWorker(c:Context,p:WorkerParameters):Worker(c,p) {
    override fun doWork():Result {
        if(Prefs.enabled(applicationContext))
            AlertHelper.show(applicationContext,inputData.getString("sender")?:"SMS",
                inputData.getString("body")?:"",inputData.getString("reason")?:"هشدار",true)
        return Result.success()
    }
}
