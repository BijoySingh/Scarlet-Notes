package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.uibasics.views.UIActionView
import ru.noties.markwon.Markwon

class MarkdownBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int = R.id.container_layout

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val dataStore = DataStore.get(context)
    val isMarkdownEnabled = dataStore.get(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
    val actionButton = dialog.findViewById<UIActionView>(R.id.action_button)
    actionButton.setOnClickListener {
      dataStore.put(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, !isMarkdownEnabled)
      dismiss()
    }
    actionButton.setTitleColor(getOptionsTitleColor(isMarkdownEnabled))
    actionButton.setSubtitleColor(getOptionsSubtitleColor(isMarkdownEnabled))
    actionButton.setImageTint(getOptionsTitleColor(isMarkdownEnabled))
    if (isMarkdownEnabled) {
      actionButton.setActionResource(R.drawable.ic_check_box_white_24dp);
    }

    val sourceText = dialog.findViewById<TextView>(R.id.source_text);
    val markdownText = dialog.findViewById<TextView>(R.id.markdown_text);
    sourceText.setText(R.string.markdown_sheet_examples_list)
    markdownText.setText(Markwon.markdown(context, getString(R.string.markdown_sheet_examples_list)))

    val sheetTitle = dialog.findViewById<TextView>(R.id.options_title)
    val exampleTitle = dialog.findViewById<TextView>(R.id.examples_title)
    sheetTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text))
    exampleTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text))
    sourceText.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
    markdownText.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_markdown

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = MarkdownBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}