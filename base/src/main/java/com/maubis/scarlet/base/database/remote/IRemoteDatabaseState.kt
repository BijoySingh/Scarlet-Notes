package com.maubis.scarlet.base.database.remote

interface IRemoteDatabaseState {
  fun notifyInsert(data: Any, onExecution: () -> Unit)
  fun notifyRemove(data: Any, onExecution: () -> Unit)
}