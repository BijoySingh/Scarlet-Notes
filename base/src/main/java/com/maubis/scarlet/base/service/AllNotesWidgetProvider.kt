package com.maubis.scarlet.base.service

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import android.app.PendingIntent
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.note.creation.activity.CreateListNoteActivity
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity


const val STORE_KEY_ALL_NOTE_WIDGET = "all_note_widget"

class AllNotesWidgetProvider : AppWidgetProvider() {

  override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
    super.onDeleted(context, appWidgetIds)
    if (appWidgetIds === null) {
      return
    }
    appWidgetIds.forEach { removeWidget(it) }
  }

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]
      addWidget(appWidgetId)

      val views = RemoteViews(
          context.packageName,
          R.layout.widget_layout_all_notes
      )
      val intent = Intent(context, AllNotesWidgetService::class.java)
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
      views.setRemoteAdapter(R.id.list, intent)

      val noteIntent = Intent(context, ViewAdvancedNoteActivity::class.java)
      noteIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
      val notePendingIntent = PendingIntent.getActivity(context, 0, noteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setPendingIntentTemplate(R.id.list, notePendingIntent)

      val createNoteIntent = Intent(context, CreateNoteActivity::class.java)
      val createNotePendingIntent = PendingIntent.getActivity(context, 23214, createNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
      views.setOnClickPendingIntent(R.id.add_note, createNotePendingIntent)

      val createListNoteIntent = Intent(context, CreateListNoteActivity::class.java)
      val createListNotePendingIntent = PendingIntent.getActivity(context, 13123, createListNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
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

      val widgetIds = allNoteWidgets()
      val intentIds = IntArray(widgetIds.size, { index -> widgetIds[index] })

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intentIds);

      application.sendBroadcast(intent)
    }

    @Synchronized
    fun allNoteWidgets(): List<Int> {
      val widgets = CoreConfig.instance.store().get(STORE_KEY_ALL_NOTE_WIDGET, "").split(",")
      return widgets.filter { it.isNotBlank() }.map { it.toInt() }
    }

    @Synchronized
    fun storeAllNoteWidgets(list: List<Int>) {
      CoreConfig.instance.store().put(STORE_KEY_ALL_NOTE_WIDGET, list.joinToString(","))
    }

    @Synchronized
    fun addWidget(widgetId: Int) {
      val widgets = allNoteWidgets()
      if (widgets.contains(widgetId)) {
        return
      }
      widgets.toMutableList().add(widgetId)
      storeAllNoteWidgets(widgets)
    }

    @Synchronized
    fun removeWidget(widgetId: Int) {
      val widgets = allNoteWidgets()
      if (!widgets.contains(widgetId)) {
        return
      }
      widgets.toMutableList().remove(widgetId)
      storeAllNoteWidgets(widgets)
    }
  }
}