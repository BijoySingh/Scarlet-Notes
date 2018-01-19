package com.bijoysingh.quicknote.service

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.bijoysingh.quicknote.activities.WidgetConfigureActivity
import com.bijoysingh.quicknote.database.Widget


class NoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]
      val widget = Widget.db(context).getByID(appWidgetId)
      if (widget === null) {
        continue
      }
      WidgetConfigureActivity.createNoteWidget(context, widget)
    }
  }

  override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
    super.onDeleted(context, appWidgetIds)

    if (appWidgetIds === null) {
      return
    }

    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]
      val widget = Widget.db(context).getByID(appWidgetId)
      if (widget === null) {
        continue
      }
      Widget.db(context).delete(widget)
    }
  }

}