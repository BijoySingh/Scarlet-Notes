package com.maubis.scarlet.base.note.reminders

import androidx.annotation.Nullable
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class ReminderJobCreator : JobCreator {

  @Nullable
  override fun create(tag: String): Job? {
    when (tag) {
      ReminderJob.TAG -> return ReminderJob()
      else -> return null
    }
  }
}