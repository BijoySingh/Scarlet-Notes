package com.maubis.scarlet.base.database.remote

import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.tag.ITagContainer

interface IRemoteDatabase {

  fun init(userId: String)
  fun reset()
  fun logout()
  fun deleteEverything()

  fun insert(note: INoteContainer)
  fun insert(tag: ITagContainer)
  fun insert(folder: IFolderContainer)

  fun remove(note: INoteContainer)
  fun remove(tag: ITagContainer)
  fun remove(folder: IFolderContainer)

  fun onRemoteInsert(note: INoteContainer)
  fun onRemoteRemove(note: INoteContainer)

  fun onRemoteInsert(tag: ITagContainer)
  fun onRemoteRemove(tag: ITagContainer)

  fun onRemoteInsert(folder: IFolderContainer)
  fun onRemoteRemove(folder: IFolderContainer)
}