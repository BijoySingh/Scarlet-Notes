package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.uibasics.views.UIActionView
import ru.noties.markwon.Markwon

class MarkdownBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int = R.id.container_layout

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    setupDialogContent(dialog)
    val sourceText = dialog.findViewById<TextView>(R.id.source_text);
    val markdownText = dialog.findViewById<TextView>(R.id.markdown_text);
    sourceText.setText(R.string.markdown_sheet_examples_list)
    markdownText.setText(Markwon.markdown(themedContext(), getString(R.string.markdown_sheet_examples_list)))

    val sheetTitle = dialog.findViewById<TextView>(R.id.options_title)
    val exampleTitle = dialog.findViewById<TextView>(R.id.examples_title)
    sheetTitle.setTextColor(theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT))
    exampleTitle.setTextColor(theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT))

    sourceText.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
    markdownText.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
  }

  fun setupDialogContent(dialog: Dialog) {
    val isMarkdownEnabled = userPreferences().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
    val actionButton = dialog.findViewById<UIActionView>(R.id.action_button)
    actionButton.setOnClickListener {
      userPreferences().put(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, !isMarkdownEnabled)
      dismiss()
    }
    actionButton.setTitleColor(getOptionsTitleColor(isMarkdownEnabled))
    actionButton.setSubtitleColor(getOptionsSubtitleColor(isMarkdownEnabled))
    actionButton.setImageTint(getOptionsTitleColor(isMarkdownEnabled))
    if (isMarkdownEnabled) {
      actionButton.setActionResource(R.drawable.ic_check_box_white_24dp);
    }

    val isMarkdownHomeEnabled = userPreferences().get(SettingsOptionsBottomSheet.KEY_MARKDOWN_HOME_ENABLED, true)
    val markdownHomeButton = dialog.findViewById<UIActionView>(R.id.markdown_home_button)
    if (isMarkdownEnabled) {
      markdownHomeButton.setOnClickListener {
        userPreferences().put(SettingsOptionsBottomSheet.KEY_MARKDOWN_HOME_ENABLED, !isMarkdownHomeEnabled)
        setupDialogContent(dialog)
      }
    }
    markdownHomeButton.setTitleColor(getOptionsTitleColor(isMarkdownEnabled && isMarkdownHomeEnabled))
    markdownHomeButton.setSubtitleColor(getOptionsSubtitleColor(isMarkdownEnabled && isMarkdownHomeEnabled))
    markdownHomeButton.setImageTint(getOptionsTitleColor(isMarkdownEnabled && isMarkdownHomeEnabled))
    if (isMarkdownEnabled && isMarkdownHomeEnabled) {
      markdownHomeButton.setActionResource(R.drawable.ic_check_box_white_24dp);
    } else {
      markdownHomeButton.setActionResource(0);
    }

  }

  override fun getLayout(): Int = R.layout.bottom_sheet_markdown

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = MarkdownBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}