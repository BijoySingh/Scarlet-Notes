package com.maubis.scarlet.base.note.folder.sheet

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.note.folder.FolderOptionsItem
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.visibility

class SelectedFolderChooseOptionsBottomSheet : FolderOptionItemBottomSheetBase() {

  var onActionListener: (Folder, Boolean) -> Unit = { _, _ -> }

  override fun setupViewWithDialog(dialog: Dialog) {
    val options = getOptions()
    dialog.findViewById<View>(R.id.tag_card_layout).visibility = visibility(options.isNotEmpty())
    setOptions(dialog, getOptions())
  }

  override fun onNewFolderClick() {
    val activity = context as ThemedActivity
    CreateOrEditFolderBottomSheet.openSheet(activity, FolderBuilder().emptyFolder(), { folder, _ ->
      onActionListener(folder, true)
      reset(dialog)
    })
  }

  private fun getOptions(): List<FolderOptionsItem> {
    val activity = themedContext() as SelectNotesActivity
    val options = ArrayList<FolderOptionsItem>()

    val folders = activity.getAllSelectedNotes().map { it.folder }.distinct()
    val selectedFolder = when (folders.size) {
      1 -> folders.first()
      else -> ""
    }
    for (folder in foldersDb.getAll()) {
      options.add(FolderOptionsItem(
          folder = folder,
          usages = notesDb.getNoteCountByFolder(folder.uuid),
          listener = {
            onActionListener(folder, folder.uuid != selectedFolder)
            reset(dialog)
          },
          editListener = {
            CreateOrEditFolderBottomSheet.openSheet(activity, folder, {_,_ -> reset(dialog)})
          },
          selected = folder.uuid == selectedFolder
      ))
    }
    return options
  }

  companion object {
    fun openSheet(activity: ThemedActivity, listener: (Folder, Boolean) -> Unit) {
      val sheet = SelectedFolderChooseOptionsBottomSheet()
      sheet.onActionListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}