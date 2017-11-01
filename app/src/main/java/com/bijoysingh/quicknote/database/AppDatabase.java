package com.bijoysingh.quicknote.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(
    entities = {Note.class},
    version = 2
)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase database;

  public abstract NoteDao notes();

  public static AppDatabase getDatabase(Context context) {
    if (database == null) {
      database = Room
          .databaseBuilder(context, AppDatabase.class, "note-database")
          .allowMainThreadQueries()
          .addMigrations(MIGRATION_1_2)
          .build();
    }
    return database;
  }

  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {

    }
  };
}
