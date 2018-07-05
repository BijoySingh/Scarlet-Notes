package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.core.folder.isUnsaved
import com.maubis.scarlet.base.note.folder.delete
import com.maubis.scarlet.base.note.folder.save
import com.maubis.scarlet.base.settings.sheet.ColorPickerBottomSheet
import com.maubis.scarlet.base.settings.view.ColorView
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.utils.getEditorActionListener


class CreateOrEditFolderBottomSheet : ThemedBottomSheetFragment() {

  var selectedFolder: Folder? = null
  var sheetOnFolderListener: (folder: Folder, deleted: Boolean) -> Unit = { _, _ -> }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val folder = selectedFolder
    if (folder == null) {
      dismiss()
      return
    }

    val title = dialog.findViewById<TextView>(R.id.options_title)
    val action = dialog.findViewById<TextView>(R.id.action_button)
    val enterFolder = dialog.findViewById<EditText>(R.id.enter_folder)
    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)
    val colorFlexbox = dialog.findViewById<FlexboxLayout>(R.id.color_flexbox)
    val colorCard = dialog.findViewById<View>(R.id.core_color_card)

    title.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    enterFolder.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    enterFolder.setHintTextColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))

    title.setText(if (folder.isUnsaved()) R.string.folder_sheet_add_note else R.string.folder_sheet_edit_note)
    action.setOnClickListener {
      val updated = onActionClick(folder, enterFolder.text.toString())
      sheetOnFolderListener(folder, !updated)
      dismiss()
    }
    removeBtn.visibility = if (folder.isUnsaved()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      folder.delete()
      sheetOnFolderListener(folder, true)
      dismiss()
    }
    enterFolder.setText(folder.title)
    enterFolder.setOnEditorActionListener(getEditorActionListener {
      val updated = onActionClick(folder, enterFolder.text.toString())
      sheetOnFolderListener(folder, !updated)
      dismiss()
      return@getEditorActionListener true
    })

    setColorsList(dialog.context, folder, colorFlexbox)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun onActionClick(folder: Folder, title: String): Boolean {
    folder.title = title
    if (folder.title.isBlank()) {
      folder.delete()
      return false
    }
    folder.updateTimestamp = System.currentTimeMillis()
    folder.save()
    return true
  }

  private fun setColorsList(context: Context, folder: Folder, colorSelectorLayout: FlexboxLayout) {
    colorSelectorLayout.removeAllViews()
    val colors = context.resources.getIntArray(R.array.bright_colors)
    for (color in colors) {
      val item = ColorView(context)
      item.setColor(color, folder.color == color)
      item.setOnClickListener {
        folder.color = color
        setColorsList(context, folder, colorSelectorLayout)
      }
      colorSelectorLayout.addView(item)
    }
  }
  override fun getLayout(): Int = R.layout.bottom_sheet_create_folder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card, R.id.core_color_card)

  companion object {
    fun openSheet(activity: ThemedActivity, folder: Folder, listener: (folder: Folder, deleted: Boolean) -> Unit) {
      val sheet = CreateOrEditFolderBottomSheet()

      sheet.selectedFolder = folder
      sheet.sheetOnFolderListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}