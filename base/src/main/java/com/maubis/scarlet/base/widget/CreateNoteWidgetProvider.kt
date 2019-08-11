package com.maubis.scarlet.base.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.support.v4.app.TaskStackBuilder
import android.widget.RemoteViews
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.note.creation.activity.CreateListNoteActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity


class CreateNoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]

      val views = RemoteViews(context.packageName, R.layout.add_note_widget_layout)
      views.setOnClickPendingIntent(R.id.add_note, getPendingIntent(context, CreateNoteActivity::class.java, 23100))

      val pendingIntentList = getPendingIntent(context, CreateListNoteActivity::class.java, 23101)
      views.setOnClickPendingIntent(R.id.add_list, pendingIntentList)

      val intentApp = Intent(context, MainActivity::class.java)
      val pendingIntentApp = PendingIntent.getActivity(context, 23102, intentApp, 0)
      views.setOnClickPendingIntent(R.id.open_app, pendingIntentApp)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  private fun <T> getPendingIntent(context: Context, activityClass: Class<T>, requestCode: Int): PendingIntent {
    return getPendingIntentWithStack(context, requestCode, Intent(context, activityClass))
  }
}

fun getPendingIntentWithStack(context: Context,  requestCode: Int, resultIntent: Intent, flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
  return TaskStackBuilder.create(context)
      .addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
      .addNextIntent(resultIntent)
      .getPendingIntent(requestCode, flags)
      ?: PendingIntent.getActivity(context, requestCode, resultIntent, 0)
}