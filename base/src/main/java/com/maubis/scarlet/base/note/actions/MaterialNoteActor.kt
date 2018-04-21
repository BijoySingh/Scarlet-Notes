package com.maubis.scarlet.base.note.actions

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getNoteState
import com.maubis.scarlet.base.core.note.isUnsaved
import com.maubis.scarlet.base.main.activity.WidgetConfigureActivity
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.getText
import com.maubis.scarlet.base.note.getTitle
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.service.FloatingNoteService
import java.util.*

open class MaterialNoteActor(val note: Note) : INoteActor {
  override fun copy(context: Context) {
    TextUtils.copyToClipboard(context, note.getFullText())
  }

  override fun share(context: Context) {
    IntentUtils.ShareBuilder(context)
        .setSubject(note.getTitle())
        .setText(note.getText())
        .setChooserText(context.getString(R.string.share_using))
        .share()
  }

  override fun popup(activity: AppCompatActivity) {
    FloatingNoteService.openNote(activity, note, true)
  }

  override fun offlineSave(context: Context) {
    val id = CoreConfig.instance.notesDatabase().database().insertNote(note)
    note.uid = if (note.isUnsaved()) id.toInt() else note.uid
    CoreConfig.instance.notesDatabase().notifyInsertNote(note)
  }

  override fun onlineSave(context: Context) {

  }

  override fun save(context: Context) {
    offlineSave(context)
    onlineSave(context)
  }

  override fun softDelete(context: Context) {
    if (note.getNoteState() === NoteState.TRASH) {
      delete(context)
      return
    }
    note.mark(context, NoteState.TRASH)
  }

  override fun offlineDelete(context: Context) {
    NoteImage(context).deleteAllFiles(note)
    if (note.isUnsaved()) {
      return
    }
    CoreConfig.instance.notesDatabase().database().delete(note)
    CoreConfig.instance.notesDatabase().notifyDelete(note)
    note.description = FormatBuilder().getDescription(ArrayList())
    note.uid = 0
    onNoteDestroyed(context)
  }

  override fun onlineDelete(context: Context) {

  }

  override fun delete(context: Context) {
    offlineDelete(context)
    onlineDelete(context)
  }

  protected fun onNoteDestroyed(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    notificationManager?.cancel(note.uid)
  }

  protected fun onNoteUpdated(context: Context) {
    WidgetConfigureActivity.notifyNoteChange(context, note)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    if (Build.VERSION.SDK_INT >= 23 && notificationManager != null) {
      for (notification in notificationManager.activeNotifications) {
        if (notification.id == note.uid) {
          val handler = NotificationHandler(context)
          handler.openNotification(NotificationConfig(note = note))
        }
      }
    }
  }

}
