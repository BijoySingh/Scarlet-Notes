package com.maubis.scarlet.base.note.creation.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem

const val STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"
const val STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN = "editor_live_markdown"
const val STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT = "editor_markdown_default"
const val STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES = "editor_move_handles"

var sEditorLiveMarkdown: Boolean
  get() = CoreConfig.instance.store().get(STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN, true)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_EDITOR_OPTIONS_LIVE_MARKDOWN, value)

var sEditorMarkdownDefault: Boolean
  get() = CoreConfig.instance.store().get(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT, false)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_DEFAULT, value)

var sEditorMoveHandles: Boolean
  get() = CoreConfig.instance.store().get(STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES, true)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_EDITOR_OPTIONS_MOVE_HANDLES, value)

var sEditorMarkdownEnabled: Boolean
  get() = CoreConfig.instance.store().get(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED, true)
  set(value) = CoreConfig.instance.store().put(STORE_KEY_EDITOR_OPTIONS_MARKDOWN_ENABLED, value)

class EditorOptionsBottomSheet : LithoOptionBottomSheet() {

  override fun title(): Int = R.string.home_option_editor_options_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val items = ArrayList<LithoOptionsItem>()
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