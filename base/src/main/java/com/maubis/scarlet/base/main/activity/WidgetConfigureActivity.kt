package com.maubis.scarlet.base.main.activity

import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.widget.Widget
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.selection.activity.INoteSelectorActivity
import com.maubis.scarlet.base.note.selection.activity.SelectableNotesActivityBase
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.widget.NoteWidgetProvider
import com.maubis.scarlet.base.widget.sheet.getWidgetNoteText
import com.maubis.scarlet.base.widget.sheet.getWidgetNotes
import com.maubis.scarlet.base.widget.sheet.sWidgetShowLockedNotes

class WidgetConfigureActivity : SelectableNotesActivityBase(), INoteSelectorActivity {

  var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_select_note)

    val intent = intent
    val extras = intent.extras
    if (extras != null) {
      appWidgetId = extras.getInt(
          AppWidgetManager.EXTRA_APPWIDGET_ID,
          AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    initUI()
  }

  override fun getNotes(): List<Note> {
    return getWidgetNotes()
  }

  override fun onNoteClicked(note: Note) {
    val widget = Widget(appWidgetId, note.uuid)
    ApplicationBase.instance.database().widgets().insert(widget)
    createWidget(widget)
  }

  override fun isNoteSelected(note: Note): Boolean {
    return true
  }

  fun createWidget(widget: Widget) {
    createNoteWidget(this, widget)

    val resultValue = Intent()
    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.widgetId)
    setResult(Activity.RESULT_OK, resultValue)
    finish()
  }

  companion object {
    fun createNoteWidget(context: Context, widget: Widget) {
      val note = notesDb.getByUUID(widget.noteUUID)
      val appWidgetManager = AppWidgetManager.getInstance(context)
      if (note === null || (note.locked && !sWidgetShowLockedNotes)) {
        val views = RemoteViews(context.getPackageName(), R.layout.widget_invalid_note)
        appWidgetManager.updateAppWidget(widget.widgetId, views)
        return
      }

      val pendingIntent = ViewAdvancedNoteActivity.getIntentWithStack(context, note)
      val views = RemoteViews(context.getPackageName(), R.layout.widget_layout)

      views.setTextViewText(R.id.description, getWidgetNoteText(note))
      views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

      val isLightShaded = ColorUtil.isLightColored(note.color)
      val colorResource = if (isLightShaded) R.color.dark_tertiary_text else R.color.light_secondary_text
      val textColor = ContextCompat.getColor(context, colorResource)
      views.setInt(R.id.description, "setTextColor", textColor)

      views.setOnClickPendingIntent(R.id.description, pendingIntent)
      views.setOnClickPendingIntent(R.id.container_layout, pendingIntent)

      appWidgetManager.updateAppWidget(widget.widgetId, views)
    }

    private fun notifyNoteChangeBroadcast(context: Context, note: Note): Intent? {
      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
          ComponentName(application, NoteWidgetProvider::class.java))
      val widgets = ApplicationBase.instance.database().widgets().getByNote(note.uuid)

      val widgetIds = ArrayList<Int>()
      for (widget in widgets) {
        if (ids.contains(widget.widgetId)) {
          widgetIds.add(widget.widgetId)
        }
      }

      if (widgetIds.isEmpty()) {
        return null
      }

      val intentIds = IntArray(widgetIds.size, { index -> widgetIds[index] })

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intentIds)
      return intent
    }

    fun notifyNoteChange(context: Context?, note: Note?) {
      if (context === null || note === null) {
        return
      }

      val intent = notifyNoteChangeBroadcast(context, note)
      if (intent === null) {
        return
      }
      context.sendBroadcast(intent);
    }
  }
}
