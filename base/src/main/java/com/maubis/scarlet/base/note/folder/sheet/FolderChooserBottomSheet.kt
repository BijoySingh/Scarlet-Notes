package com.maubis.scarlet.base.note.folder.sheet

import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.save

class FolderChooserBottomSheet : FolderChooserBottomSheetBase() {

  var note: Note? = null

  override fun preComponentRender(componentContext: ComponentContext) {

  }

  override fun onFolderSelected(folder: Folder) {
    note!!.folder = when {
      note!!.folder == folder.uuid -> ""
      else -> folder.uuid
    }
    note!!.save(requireContext())
  }

  override fun isFolderSelected(folder: Folder): Boolean {
    return note!!.folder == folder.uuid
  }
}