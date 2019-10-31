package com.bijoysingh.quicknote.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

var remoteDatabase: RemoteUploadDataDao? = null
fun genRemoteDatabase(context: Context): RemoteUploadDataDao? {
  if (remoteDatabase === null) {
    remoteDatabase = Room.databaseBuilder(context, RemoteUploadDatabase::class.java, "google_drive_db")
      .fallbackToDestructiveMigration()
      .build().remote()
  }
  return remoteDatabase
}

@Database(entities = [RemoteUploadData::class], version = 4)
abstract class RemoteUploadDatabase : RoomDatabase() {
  abstract fun remote(): RemoteUploadDataDao
}