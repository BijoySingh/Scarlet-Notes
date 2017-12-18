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
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity.NOTE_ID
import com.bijoysingh.quicknote.database.Note
import com.github.bijoysingh.starter.prefs.DataStore
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
    contentView.setTextViewText(R.id.timestamp, note.displayTimestamp)

    val isNightMode = DataStore.get(context).get(ThemedActivity.getKey(), false)
    val titleColor = if (isNightMode) R.color.light_secondary_text else R.color.dark_secondary_text
    val descColor = if (isNightMode) R.color.light_hint_text else R.color.dark_hint_text
    contentView.setTextColor(R.id.title, ContextCompat.getColor(context, titleColor))
    contentView.setTextColor(R.id.description, ContextCompat.getColor(context, titleColor))
    contentView.setTextColor(R.id.timestamp, ContextCompat.getColor(context, descColor))

    val backgroundColor = if (isNightMode) R.color.material_grey_800 else R.color.white
    contentView.setInt(
        R.id.root_layout,
        "setBackgroundColor",
        ContextCompat.getColor(context, backgroundColor))
    return contentView
  }

  fun getNoteOpenIntent(): PendingIntent {
    val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
    intent.putExtra(NOTE_ID, note.uid)
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