package com.bijoysingh.quicknote.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.INTENT_KEY_NOTE_ID
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.database.Note
import com.github.bijoysingh.starter.util.TextUtils

const val REQUEST_CODE_BASE = 3200;
const val NOTE_NOTIFICATION_CHANNEL_ID = "NOTE_NOTIFICATION_CHANNEL";

class NotificationHandler(val context: Context, val note: Note) {
  fun openNotification() {
    val pendingIntent = getNoteOpenIntent()
    var contentView = getRemoteView()
    val notificationBuilder = NotificationCompat.Builder(context, NOTE_NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_format_quote_white_48dp)
        .setContentTitle(note.getTitle())
        .setColor(note.color)
        .setCategory(NotificationCompat.CATEGORY_EVENT)
        .setContent(contentView)
        .setCustomBigContentView(contentView)
        .setContentIntent(pendingIntent)
        .setAutoCancel(false)
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(note.uid, notificationBuilder.build())
  }

  fun getRemoteView(): RemoteViews {
    val contentView = RemoteViews(context.packageName, R.layout.notification_note_layout)
    val hasTitle = !TextUtils.isNullOrEmpty(note.getTitle())
    contentView.setViewVisibility(R.id.title, if (hasTitle) VISIBLE else GONE)
    contentView.setTextViewText(R.id.title, note.getTitle())
    contentView.setTextViewText(R.id.description, note.text)
    contentView.setTextViewText(R.id.timestamp, note.displayTime)

    val theme = ThemeManager.get(context)
    val titleColor = theme.get(context, ThemeColorType.SECONDARY_TEXT)
    val descColor = theme.get(context, ThemeColorType.TERTIARY_TEXT)
    contentView.setTextColor(R.id.title, titleColor)
    contentView.setTextColor(R.id.description, titleColor)
    contentView.setTextColor(R.id.timestamp, descColor)

    val backgroundColor = theme.get(context, ThemeColorType.BACKGROUND)
    contentView.setInt(R.id.root_layout, "setBackgroundColor", backgroundColor)
    return contentView
  }

  fun getNoteOpenIntent(): PendingIntent {
    val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
    return getPendingActivityIntentWithStack(intent)
  }

  fun getPendingActivityIntentWithStack(intent: Intent): PendingIntent {
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(intent)
    return stackBuilder.getPendingIntent(
        REQUEST_CODE_BASE + note.uid,
        PendingIntent.FLAG_UPDATE_CURRENT)
  }

  fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT < 26) {
      return
    }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    if (manager === null) {
      return
    }
    val channel = NotificationChannel(
        NOTE_NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.notification_channel_label),
        NotificationManager.IMPORTANCE_MIN)
    manager.createNotificationChannel(channel)
  }
}