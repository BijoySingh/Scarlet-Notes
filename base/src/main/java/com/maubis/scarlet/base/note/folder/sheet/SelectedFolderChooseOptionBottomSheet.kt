package com.maubis.scarlet.base.note.folder.sheet

import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.note.selection.activity.SelectNotesActivity

class SelectedFolderChooseOptionsBottomSheet : FolderChooserBottomSheetBase() {

  var onActionListener: (Folder, Boolean) -> Unit = { _, _ -> }
  var selectedFolders: MutableList<String> = emptyList<String>().toMutableList()
  var selectedFolder: String = ""

  override fun preComponentRender(componentContext: ComponentContext) {
    val activity = requireContext() as SelectNotesActivity
    selectedFolders.clear()
    selectedFolders.addAll(activity.getAllSelectedNotes().map { it.folder }.distinct())
    selectedFolder = selectedFolders.firstOrNull() ?: ""
  }

  override fun onFolderSelected(folder: Folder) {
    onActionListener(folder, true)
    onActionListener(folder, folder.uuid != selectedFolder)
  }

  override fun isFolderSelected(folder: Folder): Boolean {
    return folder.uuid == selectedFolder
  }
}