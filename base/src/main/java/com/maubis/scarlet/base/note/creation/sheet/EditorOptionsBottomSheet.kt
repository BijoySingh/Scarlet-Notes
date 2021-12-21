package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.sheet.ColorPickerDefaultController
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet

var sEditorLiveMarkdown: Boolean
  get() = sAppPreferences.get("editor_live_markdown", true)
  set(value) = sAppPreferences.put("editor_live_markdown", value)

var sEditorMoveChecked: Boolean
  get() = sAppPreferences.get("editor_move_checked_items", true)
  set(value) = sAppPreferences.put("editor_move_checked_items", value)

var sEditorMarkdownDefault: Boolean
  get() = sAppPreferences.get("editor_markdown_default", false)
  set(value) = sAppPreferences.put("editor_markdown_default", value)

var sEditorSkipNoteViewer: Boolean
  get() = sAppPreferences.get("skip_note_viewer", false)
  set(value) = sAppPreferences.put("skip_note_viewer", value)

var sEditorMoveHandles: Boolean
  get() = sAppPreferences.get("editor_move_handles", true)
  set(value) = sAppPreferences.put("editor_move_handles", value)

var sEditorMarkdownEnabled: Boolean
  get() = sAppPreferences.get("KEY_MARKDOWN_ENABLED", true)
  set(value) = sAppPreferences.put("KEY_MARKDOWN_ENABLED", value)

var sNoteDefaultColor: Int
  get() = sAppPreferences.get("KEY_NOTE_DEFAULT_COLOR", (0xFFD32F2F).toInt())
  set(value) = sAppPreferences.put("KEY_NOTE_DEFAULT_COLOR", value)

class EditorOptionsBottomSheet : LithoOptionBottomSheet() {

  override fun title(): Int = R.string.home_option_editor_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val items = ArrayList<LithoOptionsItem>()
    val activity = context as MainActivity
    items.add(LithoOptionsItem(
      title = R.string.note_option_default_color,
      subtitle = R.string.note_option_default_color_subtitle,
      icon = R.drawable.ic_action_color,
      listener = {
        val config = ColorPickerDefaultController(
          title = R.string.note_option_default_color,
          colors = listOf(
            activity.resources.getIntArray(R.array.bright_colors),
            activity.resources.getIntArray(R.array.bright_colors_accent)),
          selectedColor = sNoteDefaultColor,
          onColorSelected = { sNoteDefaultColor = it }
        )
        openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
        dismiss()
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.markdown_sheet_markdown_support,
      subtitle = R.string.markdown_sheet_markdown_support_subtitle,
      icon = R.drawable.ic_markdown_logo,
      selected = sEditorMarkdownEnabled,
      isSelectable = true,
      listener = {
        sEditorMarkdownEnabled = !sEditorMarkdownEnabled
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_live_markdown,
      subtitle = R.string.editor_option_enable_live_markdown_description,
      icon = R.drawable.icon_realtime_markdown,
      selected = sEditorLiveMarkdown,
      isSelectable = true,
      listener = {
        sEditorLiveMarkdown = !sEditorLiveMarkdown
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_markdown_mode_default,
      subtitle = R.string.editor_option_enable_markdown_mode_default_details,
      icon = R.drawable.ic_formats_logo,
      selected = sEditorMarkdownDefault,
      isSelectable = true,
      listener = {
        sEditorMarkdownDefault = !sEditorMarkdownDefault
        reset(componentContext.androidContext, dialog)
      }
    ))

    items.add(LithoOptionsItem(
      title = R.string.editor_option_skip_view_note,
      subtitle = R.string.editor_option_skip_view_note_details,
      icon = R.drawable.ic_redo_history,
      selected = sEditorSkipNoteViewer,
      isSelectable = true,
      listener = {
        sEditorSkipNoteViewer = !sEditorSkipNoteViewer
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_move_checked_items,
      subtitle = R.string.editor_option_move_checked_items_description,
      icon = R.drawable.ic_check_box_white_24dp,
      selected = sEditorMoveChecked,
      isSelectable = true,
      listener = {
        sEditorMoveChecked = !sEditorMoveChecked
        reset(componentContext.androidContext, dialog)
      }
    ))
    items.add(LithoOptionsItem(
      title = R.string.editor_option_enable_move_handle,
      subtitle = R.string.editor_option_enable_move_handle_description,
      icon = R.drawable.icon_drag_indicator,
      selected = sEditorMoveHandles,
      isSelectable = true,
      listener = {
        sEditorMoveHandles = !sEditorMoveHandles
        reset(componentContext.androidContext, dialog)
      }
    ))
    return items
  }
}