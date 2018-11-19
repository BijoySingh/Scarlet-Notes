package com.maubis.scarlet.base.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.getLockedText
import com.maubis.scarlet.base.note.getTitle
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.ui.ColorUtil


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
    return notes[position].uid.toLong()
  }

  override fun onDataSetChanged() {
    val sorting = SortingOptionsBottomSheet.getSortingState()
    notes = sort(notesDB.getByNoteState(
        arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name, NoteState.ARCHIVED.name))
        .filter { note -> !note.locked }, sorting)
        .take(15)
  }

  override fun hasStableIds(): Boolean {
    return true
  }

  override fun getViewAt(position: Int): RemoteViews? {
    if (position == AdapterView.INVALID_POSITION) {
      return null
    }

    val note = notes[position]

    val intent = ViewAdvancedNoteActivity.getIntent(context, note)
    val pendingIntent = PendingIntent.getActivity(context, 5000 + note.uid, intent, 0)
    val views = RemoteViews(context.getPackageName(), R.layout.item_widget_note)

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

    val extras = Bundle()
    extras.putInt(INTENT_KEY_NOTE_ID, note.uid)
    val fillInIntent = Intent()
    fillInIntent.putExtra(INTENT_KEY_NOTE_ID, note.uid)
    fillInIntent.putExtras(extras)

    views.setOnClickFillInIntent(R.id.title, fillInIntent)
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