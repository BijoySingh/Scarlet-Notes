package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.view.View
import android.view.View.GONE
import android.widget.LinearLayout
import com.github.bijoysingh.uibasics.views.UIActionView
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.note.folder.FolderOptionsItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment

abstract class FolderOptionItemBottomSheetBase : ThemedBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    reset(dialog)
    setAddFolderOption(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  abstract fun onNewFolderClick()

  override fun getBackgroundView(): Int {
    return R.id.options_layout
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.tag_card_layout)

  fun setAddFolderOption(dialog: Dialog) {
    val newFolderButton = dialog.findViewById<UITextView>(R.id.new_tag_button);
    newFolderButton.setText(R.string.folder_sheet_add_note)
    newFolderButton.setOnClickListener { onNewFolderClick() }
    newFolderButton.icon.alpha = 0.6f
  }

  fun reset(dialog: Dialog) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()
    setupViewWithDialog(dialog)
  }

  fun setOptions(dialog: Dialog, options: List<FolderOptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    for (option in options) {
      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIActionView
      contentView.setTitle(option.folder.title)
      contentView.setOnClickListener { option.listener() }
      contentView.subtitle.visibility = GONE
      contentView.setImageResource(option.getIcon())

      if (option.editable) {
        contentView.setActionResource(option.getEditIcon());
        contentView.setActionTint(ApplicationBase.instance.themeController().get(ThemeColorType.HINT_TEXT));
        contentView.setActionClickListener { option.editListener() }
      }

      contentView.setTitleColor(getOptionsTitleColor(option.selected))
      contentView.setSubtitleColor(getOptionsSubtitleColor(option.selected))
      contentView.setImageTint(getOptionsTitleColor(option.selected))

      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_tag_options
}