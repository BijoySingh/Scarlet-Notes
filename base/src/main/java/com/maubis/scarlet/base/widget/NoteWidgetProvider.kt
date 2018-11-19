package com.maubis.scarlet.base.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.activity.WidgetConfigureActivity

class NoteWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
    val N = appWidgetIds.size
    for (i in 0 until N) {
      val appWidgetId = appWidgetIds[i]
      val widget = CoreConfig.instance.database().widgets().getByID(appWidgetId)
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
      val widget = CoreConfig.instance.database().widgets().getByID(appWidgetId)
      if (widget === null) {
        continue
      }
      CoreConfig.instance.database().widgets().delete(widget)
    }
  }

}