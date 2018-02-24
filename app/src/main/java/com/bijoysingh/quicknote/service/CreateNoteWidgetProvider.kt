package com.bijoysingh.quicknote.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateAdvancedListActivity
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.MainActivity


class CreateNoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]

      val intent = Intent(context, CreateOrEditAdvancedNoteActivity::class.java)
      val pendingIntent = PendingIntent.getActivity(context, 23100, intent, 0)


      val views = RemoteViews(context.packageName, R.layout.add_note_widget_layout)
      views.setOnClickPendingIntent(R.id.add_note, pendingIntent)

      val intentList = Intent(context, CreateAdvancedListActivity::class.java)
      val pendingIntentList = PendingIntent.getActivity(context, 23101, intentList, 0)
      views.setOnClickPendingIntent(R.id.add_list, pendingIntentList)


      val intentApp = Intent(context, MainActivity::class.java)
      val pendingIntentApp = PendingIntent.getActivity(context, 23102, intentApp, 0)
      views.setOnClickPendingIntent(R.id.open_app, pendingIntentApp)

      appWidgetManager.updateAppWidget(appWidgetId, views);

    }
  }
}