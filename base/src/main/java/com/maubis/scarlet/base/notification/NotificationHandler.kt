package com.maubis.scarlet.base.notification

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.getDisplayTime
import com.maubis.scarlet.base.note.getText
import com.maubis.scarlet.base.note.getTitle
import com.maubis.scarlet.base.support.INTENT_KEY_ACTION
import com.maubis.scarlet.base.support.ui.ThemeColorType

const val REQUEST_CODE_BASE = 3200;
const val REQUEST_CODE_MULTIPLIER = 250;
const val NOTE_NOTIFICATION_CHANNEL_ID = "NOTE_NOTIFICATION_CHANNEL";
const val REMINDER_NOTIFICATION_CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL";

class NotificationConfig(
    val note: Note,
    val channel: String = NOTE_NOTIFICATION_CHANNEL_ID
)

class NotificationHandler(val context: Context) {

  val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

  init {
    createNotificationChannel()
  }

  fun openNotification(config: NotificationConfig) {
    val pendingIntent = getPendingActivityIntent(config, getNoteOpenIntent(config), 1)
    var contentView = getRemoteView(config)
    val notificationBuilder = NotificationCompat.Builder(context, config.channel)
        .setSmallIcon(R.drawable.ic_format_quote_white_48dp)
        .setContentTitle(config.note.getTitle())
        .setColor(config.note.color)
        .setCategory(NotificationCompat.CATEGORY_EVENT)
        .setContent(contentView)
        .setCustomBigContentView(contentView)
        .setContentIntent(pendingIntent)
        .setAutoCancel(false)

    if (config.channel === REMINDER_NOTIFICATION_CHANNEL_ID) {
      notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
      notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
      notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
      notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
    }

    notificationManager.notify(config.note.uid, notificationBuilder.build())
  }

  private fun createNotificationChannel() {
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

    val channelForReminder = NotificationChannel(
        REMINDER_NOTIFICATION_CHANNEL_ID,
        context.getString(R.string.notification_reminder_channel_label),
        NotificationManager.IMPORTANCE_HIGH)
    manager.createNotificationChannel(channelForReminder)
  }

  fun getRemoteView(config: NotificationConfig): RemoteViews {
    val contentView = RemoteViews(context.packageName, R.layout.notification_note_layout)
    val hasTitle = !TextUtils.isNullOrEmpty(config.note.getTitle())
    contentView.setViewVisibility(R.id.title, if (hasTitle) VISIBLE else GONE)
    contentView.setTextViewText(R.id.title, config.note.getTitle())
    contentView.setTextViewText(R.id.description, config.note.getText())
    contentView.setTextViewText(R.id.timestamp, config.note.getDisplayTime())

    val theme = CoreConfig.instance.themeController()
    val titleColor = theme.get(ThemeColorType.SECONDARY_TEXT)
    val descColor = theme.get(ThemeColorType.TERTIARY_TEXT)
    contentView.setTextColor(R.id.title, titleColor)
    contentView.setTextColor(R.id.description, titleColor)
    contentView.setTextColor(R.id.timestamp, descColor)

    val backgroundColor = theme.get(ThemeColorType.BACKGROUND)
    contentView.setInt(R.id.root_layout, "setBackgroundColor", backgroundColor)

    val iconColor = theme.get(ThemeColorType.TOOLBAR_ICON)
    contentView.setInt(R.id.options_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.copy_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.share_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.delete_button, "setColorFilter", iconColor)
    contentView.setInt(R.id.edit_button, "setColorFilter", iconColor)

    contentView.setOnClickPendingIntent(
        R.id.options_button,
        getPendingActivityIntent(config, getNoteOpenIntent(config), 2))
    contentView.setOnClickPendingIntent(
        R.id.edit_button,
        getPendingActivityIntent(config, getNoteEditIntent(config), 3))
    contentView.setOnClickPendingIntent(
        R.id.copy_button,
        getPendingServiceIntent(config, getNoteActionIntent(config, NotificationIntentService.NoteAction.COPY), 4))
    contentView.setOnClickPendingIntent(
        R.id.share_button,
        getPendingServiceIntent(config, getNoteActionIntent(config, NotificationIntentService.NoteAction.SHARE), 5))
    contentView.setOnClickPendingIntent(
        R.id.delete_button,
        getPendingServiceIntent(config, getNoteActionIntent(config, NotificationIntentService.NoteAction.DELETE), 6))

    return contentView
  }

  private fun getNoteOpenIntent(config: NotificationConfig): Intent {
    val intent = Intent(context, ViewAdvancedNoteActivity::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    return intent
  }

  private fun getNoteEditIntent(config: NotificationConfig): Intent {
    val intent = Intent(context, CreateNoteActivity::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    return intent
  }

  private fun getPendingActivityIntent(
      config: NotificationConfig,
      intent: Intent,
      requestCode: Int): PendingIntent {
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(intent)
    return stackBuilder.getPendingIntent(
        REQUEST_CODE_BASE + config.note.uid + requestCode * REQUEST_CODE_MULTIPLIER,
        PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun getNoteActionIntent(
      config: NotificationConfig,
      action: NotificationIntentService.NoteAction): Intent {
    val intent = Intent(context, NotificationIntentService::class.java)
    intent.putExtra(INTENT_KEY_NOTE_ID, config.note.uid)
    intent.putExtra(INTENT_KEY_ACTION, action.name)
    return intent
  }

  private fun getPendingServiceIntent(
      config: NotificationConfig,
      intent: Intent,
      requestCode: Int): PendingIntent {
    return PendingIntent.getService(
        context,
        REQUEST_CODE_BASE + config.note.uid + requestCode * REQUEST_CODE_MULTIPLIER,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT)
  }
}