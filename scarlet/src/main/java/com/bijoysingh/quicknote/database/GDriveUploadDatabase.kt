package com.bijoysingh.quicknote.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

var gDriveDatabase: GDriveUploadDataDao? = null
fun genGDriveUploadDatabase(context: Context): GDriveUploadDataDao? {
  if (gDriveDatabase === null) {
    gDriveDatabase = Room.databaseBuilder(context, GDriveUploadDatabase::class.java, "google_drive_db")
        .fallbackToDestructiveMigration()
        .build().drive()
  }
  return gDriveDatabase
}

@Database(entities = [GDriveUploadData::class], version = 3)
abstract class GDriveUploadDatabase : RoomDatabase() {
  abstract fun drive(): GDriveUploadDataDao
}