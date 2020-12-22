package com.maubis.scarlet.base.widget

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.note.creation.activity.CreateListNoteActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.widget.sheet.sWidgetBackgroundColor
import com.maubis.scarlet.base.widget.sheet.sWidgetShowToolbar

class AllNotesWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]

      val views = RemoteViews(
        context.packageName,
        R.layout.widget_layout_all_notes
      )
      val intent = Intent(context, AllNotesWidgetService::class.java)
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
      views.setRemoteAdapter(R.id.list, intent)

      views.setInt(R.id.widget_background, "setBackgroundColor", sWidgetBackgroundColor)
      views.setViewVisibility(R.id.toolbar, visibility(sWidgetShowToolbar))

      val noteIntent = Intent(context, ViewAdvancedNoteActivity::class.java)
      noteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      val notePendingIntent = getPendingIntentWithStack(context, 0, noteIntent)
      views.setPendingIntentTemplate(R.id.list, notePendingIntent)

      val createNoteIntent = Intent(context, CreateNoteActivity::class.java)
      val createNotePendingIntent = getPendingIntentWithStack(context, 23214, createNoteIntent)
      views.setOnClickPendingIntent(R.id.add_note, createNotePendingIntent)

      val createListNoteIntent = Intent(context, CreateListNoteActivity::class.java)
      val createListNotePendingIntent = getPendingIntentWithStack(context, 13123, createListNoteIntent)
      views.setOnClickPendingIntent(R.id.add_list, createListNotePendingIntent)

      val mainIntent = Intent(context, MainActivity::class.java)
      val mainPendingIntent = PendingIntent.getActivity(context, 13124, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setOnClickPendingIntent(R.id.app_icon, mainPendingIntent)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }

  companion object {
    fun notifyAllChanged(context: Context) {
      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
        ComponentName(application, AllNotesWidgetProvider::class.java))
      if (ids.isEmpty()) {
        return
      }

      AppWidgetManager.getInstance(application).notifyAppWidgetViewDataChanged(ids, R.id.list)
    }
  }
}