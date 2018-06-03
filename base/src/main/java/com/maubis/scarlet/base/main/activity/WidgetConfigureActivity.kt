package com.maubis.scarlet.base.main.activity

import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RemoteViews
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.widget.Widget
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.getLockedText
import com.maubis.scarlet.base.note.getTitle
import com.maubis.scarlet.base.note.selection.activity.INoteSelectorActivity
import com.maubis.scarlet.base.note.selection.activity.SelectableNotesActivityBase
import com.maubis.scarlet.base.service.NoteWidgetProvider
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.ui.ColorUtil

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
    return notesDB.getByNoteState(
        arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name, NoteState.ARCHIVED.name))
        .filter { note -> !note.locked }
  }

  override fun onNoteClicked(note: Note) {
    val widget = Widget(appWidgetId, note.uuid)
    CoreConfig.instance.database().widgets().insert(widget)
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
      val note = notesDB.getByUUID(widget.noteUUID)
      val appWidgetManager = AppWidgetManager.getInstance(context)
      if (note === null || note.locked) {
        val views = RemoteViews(context.getPackageName(), R.layout.widget_invalid_note)
        appWidgetManager.updateAppWidget(widget.widgetId, views)
        return
      }

      val intent = ViewAdvancedNoteActivity.getIntent(context, note)
      val pendingIntent = PendingIntent.getActivity(context, 5000 + note.uid, intent, 0)

      val views = RemoteViews(context.getPackageName(), R.layout.widget_layout)

      val noteTitle = note.getTitle()
      views.setViewVisibility(R.id.title, if (TextUtils.isNullOrEmpty(noteTitle)) GONE else VISIBLE)
      views.setTextViewText(R.id.title, noteTitle)
      views.setTextViewText(
          R.id.description,
          note.getLockedText(context, false))
      views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

      val isLightShaded = ColorUtil.isLightColored(note.color)
      val colorResource = if (isLightShaded) R.color.dark_tertiary_text else R.color.light_secondary_text
      val textColor = ContextCompat.getColor(context, colorResource)
      views.setInt(R.id.title, "setTextColor", textColor)
      views.setInt(R.id.description, "setTextColor", textColor)

      views.setOnClickPendingIntent(R.id.title, pendingIntent)
      views.setOnClickPendingIntent(R.id.description, pendingIntent)
      views.setOnClickPendingIntent(R.id.container_layout, pendingIntent)

      appWidgetManager.updateAppWidget(widget.widgetId, views)
    }

    fun notifyNoteChange(context: Context?, note: Note?) {
      if (context === null || note === null) {
        return
      }

      val application: Application = context.applicationContext as Application
      val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
          ComponentName(application, NoteWidgetProvider::class.java))
      val widgets = CoreConfig.instance.database().widgets().getByNote(note.uuid)

      val widgetIds = ArrayList<Int>()
      for (widget in widgets) {
        if (ids.contains(widget.widgetId)) {
          widgetIds.add(widget.widgetId)
        }
      }

      if (widgetIds.isEmpty()) {
        return
      }

      val intentIds = IntArray(widgetIds.size, { index -> widgetIds[index] })

      val intent = Intent(application, NoteWidgetProvider::class.java)
      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intentIds);
      context.sendBroadcast(intent);
    }
  }
}
