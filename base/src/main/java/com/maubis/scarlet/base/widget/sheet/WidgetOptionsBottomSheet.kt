package com.maubis.scarlet.base.widget.sheet

import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import com.facebook.litho.ComponentContext
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.getFullTextForDirectMarkdownRender
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider
import com.maubis.scarlet.base.widget.NoteWidgetProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val STORE_KEY_WIDGET_ENABLE_FORMATTING = "widget_enable_formatting"
var sWidgetEnableFormatting: Boolean
  get() = ApplicationBase.instance.store().get(STORE_KEY_WIDGET_ENABLE_FORMATTING, true)
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_WIDGET_ENABLE_FORMATTING, value)

const val STORE_KEY_WIDGET_SHOW_LOCKED_NOTES = "widget_show_locked_notes"
var sWidgetShowLockedNotes: Boolean
  get() = ApplicationBase.instance.store().get(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, false)
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, value)

const val STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES = "widget_show_archived_notes"
var sWidgetShowArchivedNotes: Boolean
  get() = ApplicationBase.instance.store().get(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, true)
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, value)

const val STORE_KEY_WIDGET_SHOW_TRASH_NOTES = "widget_show_trash_notes"
var sWidgetShowDeletedNotes: Boolean
  get() = ApplicationBase.instance.store().get(STORE_KEY_WIDGET_SHOW_TRASH_NOTES, false)
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_WIDGET_SHOW_TRASH_NOTES, value)

fun getWidgetNoteText(note: Note): CharSequence {
  if (note.locked && !sWidgetShowLockedNotes) {
    return "******************\n***********\n****************"
  }

  val text = note.getFullTextForDirectMarkdownRender()
  return when (sWidgetEnableFormatting) {
    true -> Markdown.render(text, true)
    false -> text
  }
}

fun getWidgetNotes(): List<Note> {
  val state = listOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name).toMutableList()
  if (sWidgetShowArchivedNotes) {
    state.add(NoteState.ARCHIVED.name)
  }
  if (sWidgetShowDeletedNotes) {
    state.add(NoteState.TRASH.name)
  }

  val sorting = SortingOptionsBottomSheet.getSortingState()
  return sort(CoreConfig.notesDb.getByNoteState(state.toTypedArray())
      .filter { note -> (!note.locked || sWidgetShowLockedNotes) }, sorting)
}

class WidgetOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_widget_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.widget_option_enable_formatting,
        subtitle = R.string.widget_option_enable_formatting_details,
        icon = R.drawable.ic_markdown_logo,
        listener = {
          sWidgetEnableFormatting = !sWidgetEnableFormatting
          notifyWidgetConfigChanged(activity)
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetEnableFormatting
    ))
    options.add(LithoOptionsItem(
        title = R.string.widget_option_show_locked_notes,
        subtitle = R.string.widget_option_show_locked_notes_details,
        icon = R.drawable.ic_action_lock,
        listener = {
          sWidgetShowLockedNotes = !sWidgetShowLockedNotes
          notifyWidgetConfigChanged(activity)
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowLockedNotes
    ))
    options.add(LithoOptionsItem(
        title = R.string.widget_option_show_archived_notes,
        subtitle = R.string.widget_option_show_archived_notes_details,
        icon = R.drawable.ic_archive_white_48dp,
        listener = {
          sWidgetShowArchivedNotes = !sWidgetShowArchivedNotes
          notifyWidgetConfigChanged(activity)
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowArchivedNotes
    ))
    options.add(LithoOptionsItem(
        title = R.string.widget_option_show_trash_notes,
        subtitle = R.string.widget_option_show_trash_notes_details,
        icon = R.drawable.icon_delete,
        listener = {
          sWidgetShowDeletedNotes = !sWidgetShowDeletedNotes
          notifyWidgetConfigChanged(activity)
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowDeletedNotes
    ))
    return options
  }

  fun notifyWidgetConfigChanged(activity: MainActivity) {
    GlobalScope.launch {
      val broadcastIntent = GlobalScope.async {
        val application: Application = activity.applicationContext as Application
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, NoteWidgetProvider::class.java))

        val intent = Intent(application, NoteWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent
      }

      AllNotesWidgetProvider.notifyAllChanged(activity)
      activity.sendBroadcast(broadcastIntent.await())
    }
  }
}