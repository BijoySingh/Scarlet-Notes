package com.maubis.scarlet.base.note.actions

interface ITagActor {

  fun offlineSave()

  fun onlineSave()

  fun save()

  fun offlineDelete()

  fun delete()
}