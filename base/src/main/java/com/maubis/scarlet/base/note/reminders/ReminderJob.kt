package com.maubis.scarlet.base.note.reminders

import com.evernote.android.job.DailyJob
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.notification.REMINDER_NOTIFICATION_CHANNEL_ID
import com.maubis.scarlet.base.support.database.notesDB
import java.util.concurrent.TimeUnit


class ReminderJob : Job() {

  override fun onRunJob(params: Params): Job.Result {
    val noteUUID = params.extras.getString(EXTRA_KEY_NOTE_UUID, "")
    val note = notesDB.getByUUID(noteUUID)
    if (note === null) {
      return Job.Result.SUCCESS
    }

    val handler = NotificationHandler(context)
    handler.openNotification(NotificationConfig(note, REMINDER_NOTIFICATION_CHANNEL_ID))
    return Job.Result.SUCCESS
  }

  companion object {
    val TAG = "reminder_job"

    val EXTRA_KEY_NOTE_UUID = "note_uuid"

    fun scheduleJob(noteUuid: String, reminder: Reminder): Int {
      val extras = PersistableBundleCompat()
      extras.putString(EXTRA_KEY_NOTE_UUID, noteUuid)

      val deltaTime = Math.abs(reminder.timestamp - System.currentTimeMillis())
      return when {
        (reminder.interval == ReminderInterval.DAILY) -> {
          val millisInsideDay = reminder.timestamp % (TimeUnit.DAYS.toMillis(1))
          val deltaMillis = TimeUnit.MINUTES.toMillis(10)
          DailyJob.schedule(JobRequest.Builder(ReminderJob.TAG), millisInsideDay - deltaMillis, millisInsideDay + deltaMillis)
        }
        (reminder.interval == ReminderInterval.ONCE) && deltaTime < 0 -> -1
        else -> {
          JobRequest.Builder(ReminderJob.TAG)
              .setExact(deltaTime)
              .setExtras(extras)
              .build()
              .schedule()
        }
      }
    }

    fun cancelJob(uid: Int) {
      JobManager.instance().cancel(uid);
    }
  }
}