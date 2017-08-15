package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(
    entities = {Note.class},
    version = 1
)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase database;

  public abstract NoteDao notes();

  public static AppDatabase getDatabase(Context context) {
    if (database == null) {
      database = Room
          .databaseBuilder(context, AppDatabase.class, "note-database")
          .allowMainThreadQueries()
          .build();
    }
    return database;
  }
}
