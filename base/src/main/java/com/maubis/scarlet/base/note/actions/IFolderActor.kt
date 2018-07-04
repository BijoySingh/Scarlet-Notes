package com.maubis.scarlet.base.note.actions

interface IFolderActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}