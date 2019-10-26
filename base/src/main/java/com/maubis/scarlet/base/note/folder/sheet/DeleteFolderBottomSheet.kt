package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.folder.delete
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.softDelete
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem

class DeleteFolderBottomSheet : LithoOptionBottomSheet() {

  var selectedFolder: Folder? = null
  var sheetOnFolderListener: (folder: Folder, deleted: Boolean) -> Unit = { _, _ -> }

  override fun title(): Int = R.string.folder_delete_option_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val folder = selectedFolder
    if (folder === null) {
      dismiss()
      return emptyList()
    }

    val activity = context as AppCompatActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.folder_delete_option_sheet_remove_folder,
        subtitle = R.string.folder_delete_option_sheet_remove_folder_details,
        icon = R.drawable.icon_delete,
        listener = {
          folder.delete()
          executeForFolderContent(folder) {
            it.folder = ""
            it.save(activity)
          }

          sheetOnFolderListener(folder, true)
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.folder_delete_option_sheet_remove_folder_content,
        subtitle = R.string.folder_delete_option_sheet_remove_folder_content_details,
        icon = R.drawable.icon_delete_content,
        listener = {
          executeForFolderContent(folder) {
            it.folder = ""
            it.softDelete(activity)
          }

          sheetOnFolderListener(folder, false)
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.folder_delete_option_sheet_remove_folder_and_content,
        subtitle = R.string.folder_delete_option_sheet_remove_folder_and_content_details,
        icon = R.drawable.ic_delete_permanently,
        listener = {
          folder.delete()
          executeForFolderContent(folder) {
            it.folder = ""
            it.softDelete(activity)
          }

          sheetOnFolderListener(folder, true)
          dismiss()
        }
    ))
    return options
  }

  private fun executeForFolderContent(folder: Folder, lambda: (Note) -> Unit) {
    CoreConfig.notesDb.getAll().filter { it.folder == folder.uuid }.forEach {
      lambda(it)
    }
  }
}