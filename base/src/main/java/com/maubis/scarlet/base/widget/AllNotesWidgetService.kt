package com.maubis.scarlet.base.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.widget.sheet.getWidgetNoteText
import com.maubis.scarlet.base.widget.sheet.getWidgetNotes


class AllNotesWidgetService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
    return AllNotesRemoteViewsFactory(applicationContext)
  }
}

class AllNotesRemoteViewsFactory(val context: Context) : RemoteViewsService.RemoteViewsFactory {

  var notes = emptyList<Note>()

  override fun onCreate() {
  }

  override fun getLoadingView(): RemoteViews? {
    return null
  }

  override fun getItemId(position: Int): Long {
    return if(position < notes.size) notes[position].uid.toLong() else 0
  }

  override fun onDataSetChanged() {
    notes = getWidgetNotes().take(15)
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getViewAt(position: Int): RemoteViews? {
    if (position == AdapterView.INVALID_POSITION || position >= notes.size) {
      return null
    }

    val note = notes[position]

    val views = RemoteViews(context.getPackageName(), R.layout.item_widget_note)

    views.setTextViewText(R.id.description, getWidgetNoteText(note))
    views.setInt(R.id.container_layout, "setBackgroundColor", note.color)

    val isLightShaded = ColorUtil.isLightColored(note.color)
    val colorResource = if (isLightShaded) R.color.dark_tertiary_text else R.color.light_secondary_text
    val textColor = ContextCompat.getColor(context, colorResource)
    views.setInt(R.id.description, "setTextColor", textColor)

    val extras = Bundle()
    extras.putInt(INTENT_KEY_NOTE_ID, note.uid)
    val fillInIntent = Intent()
    fillInIntent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
    fillInIntent.putExtras(extras)

    views.setOnClickFillInIntent(R.id.description, fillInIntent)
    views.setOnClickFillInIntent(R.id.container_layout, fillInIntent)

    return views
  }

  override fun getCount(): Int {
    return notes.size
  }

  override fun getViewTypeCount(): Int {
    return 1
  }

  override fun onDestroy() {

  }

}