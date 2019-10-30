package com.maubis.scarlet.base.widget.sheet

import android.app.Application
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import com.facebook.litho.ComponentContext
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.note.getFullTextForDirectMarkdownRender
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.ColorPickerDefaultController
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.FlavorUtils
import com.maubis.scarlet.base.widget.AllNotesWidgetProvider
import com.maubis.scarlet.base.widget.NoteWidgetProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

const val STORE_KEY_WIDGET_ENABLE_FORMATTING = "widget_enable_formatting"
var sWidgetEnableFormatting: Boolean
  get() = sAppPreferences.get(STORE_KEY_WIDGET_ENABLE_FORMATTING, true)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_ENABLE_FORMATTING, value)

const val STORE_KEY_WIDGET_SHOW_LOCKED_NOTES = "widget_show_locked_notes"
var sWidgetShowLockedNotes: Boolean
  get() = sAppPreferences.get(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, false)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_SHOW_LOCKED_NOTES, value)

const val STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES = "widget_show_archived_notes"
var sWidgetShowArchivedNotes: Boolean
  get() = sAppPreferences.get(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, true)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_SHOW_ARCHIVED_NOTES, value)

const val STORE_KEY_WIDGET_SHOW_TRASH_NOTES = "widget_show_trash_notes"
var sWidgetShowDeletedNotes: Boolean
  get() = sAppPreferences.get(STORE_KEY_WIDGET_SHOW_TRASH_NOTES, false)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_SHOW_TRASH_NOTES, value)

const val STORE_KEY_WIDGET_BACKGROUND_COLOR = "widget_background_color_v1"
var sWidgetBackgroundColor: Int
  get() = sAppPreferences.get(STORE_KEY_WIDGET_BACKGROUND_COLOR, 0x65000000)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_BACKGROUND_COLOR, value)

const val STORE_KEY_WIDGET_SHOW_TOOLBAR = "widget_show_toolbar"
var sWidgetShowToolbar: Boolean
  get() = sAppPreferences.get(STORE_KEY_WIDGET_SHOW_TOOLBAR, true)
  set(value) = sAppPreferences.put(STORE_KEY_WIDGET_SHOW_TOOLBAR, value)

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
    options.add(LithoOptionsItem(
        title = R.string.widget_option_show_toolbar,
        subtitle = R.string.widget_option_show_toolbar_details,
        icon = R.drawable.ic_action_grid,
        listener = {
          sWidgetShowToolbar = !sWidgetShowToolbar
          notifyAllNotesConfigChanged(activity)
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sWidgetShowToolbar
    ))

    val isLite = FlavorUtils.isLite()
    options.add(LithoOptionsItem(
        title = R.string.widget_option_background_color,
        subtitle = R.string.widget_option_background_color_details,
        icon = R.drawable.ic_action_color,
        listener = {
          if (isLite) {
            openSheet(activity, InstallProUpsellBottomSheet())
            return@LithoOptionsItem
          }

          openSheet(activity, ColorPickerBottomSheet().apply {
            config = ColorPickerDefaultController(
                title = R.string.widget_option_background_color,
                selectedColor = sWidgetBackgroundColor,
                colors = listOf(intArrayOf(Color.TRANSPARENT, Color.WHITE, Color.LTGRAY, 0x65000000, Color.DKGRAY, Color.BLACK)),
                onColorSelected = {
                  sWidgetBackgroundColor = it
                  notifyAllNotesConfigChanged(activity)
                },
                columns = 6)
          })
        },
        actionIcon = if (!isLite) 0 else R.drawable.ic_rating
    ))
    return options
  }

  fun notifyWidgetConfigChanged(activity: MainActivity) {
    GlobalScope.launch {
      val singleNoteBroadcastIntent = GlobalScope.async {
        val application: Application = activity.applicationContext as Application
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, NoteWidgetProvider::class.java))

        val intent = Intent(application, NoteWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent
      }

      AllNotesWidgetProvider.notifyAllChanged(activity)
      activity.sendBroadcast(singleNoteBroadcastIntent.await())
    }
  }

  fun notifyAllNotesConfigChanged(activity: MainActivity) {
    GlobalScope.launch {
      val allNotesBroadcastIntent = GlobalScope.async {
        val application: Application = activity.applicationContext as Application
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, AllNotesWidgetProvider::class.java))

        val intent = Intent(application, AllNotesWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        intent
      }

      activity.sendBroadcast(allNotesBroadcastIntent.await())
    }
  }
}