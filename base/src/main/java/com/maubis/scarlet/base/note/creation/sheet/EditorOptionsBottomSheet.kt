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

const val STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"
const val STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN = "editor_live_markdown"
const val STORE_KEY_EDITOR_OPTIONS_MOVE_CHECKED_ITEMS = "editor_move_checked_items"
const val STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT = "editor_markdown_default"
const val STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES = "editor_move_handles"
const val STORE_KEY_NOTE_DEFAULT_COLOR = "KEY_NOTE_DEFAULT_COLOR"

var sEditorLiveMarkdown: Boolean
  get() = sAppPreferences.get(STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN, true)
  set(value) = sAppPreferences.put(STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN, value)

var sEditorMoveChecked: Boolean
  get() = sAppPreferences.get(STORE_KEY_EDITOR_OPTIONS_MOVE_CHECKED_ITEMS, true)
  set(value) = sAppPreferences.put(STORE_KEY_EDITOR_OPTIONS_MOVE_CHECKED_ITEMS, value)

var sEditorMarkdownDefault: Boolean
  get() = sAppPreferences.get(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT, false)
  set(value) = sAppPreferences.put(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT, value)

var sEditorMoveHandles: Boolean
  get() = sAppPreferences.get(STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES, true)
  set(value) = sAppPreferences.put(STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES, value)

var sEditorMarkdownEnabled: Boolean
  get() = sAppPreferences.get(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED, true)
  set(value) = sAppPreferences.put(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED, value)

var sNoteDefaultColor: Int
  get() = sAppPreferences.get(STORE_KEY_NOTE_DEFAULT_COLOR, (0xFFD32F2F).toInt())
  set(value) = sAppPreferences.put(STORE_KEY_NOTE_DEFAULT_COLOR, value)

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