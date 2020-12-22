package com.maubis.scarlet.base.support.database

import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest
import java.util.concurrent.TimeUnit

class HouseKeeperJob : DailyJob() {
  companion object {
    const val TAG = "HouseKeeper"

    fun schedule() {
      val builder = JobRequest.Builder(TAG).setRequiresDeviceIdle(true)
      schedule(builder, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(16))
    }
  }

  override fun onRunDailyJob(params: Params): DailyJobResult {
    HouseKeeper(context).execute()
    return DailyJobResult.SUCCESS
  }
}