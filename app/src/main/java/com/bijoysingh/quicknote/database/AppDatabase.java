package com.bijoysingh.quicknote.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(
    entities = {Note.class, Tag.class},
    version = 6
)
public abstract class AppDatabase extends RoomDatabase {

  private static AppDatabase database;

  public abstract NoteDao notes();

  public abstract TagDao tags();

  public static AppDatabase getDatabase(Context context) {
    if (database == null) {
      database = Room
          .databaseBuilder(context, AppDatabase.class, "note-database")
          .allowMainThreadQueries()
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
          .build();
    }
    return database;
  }

  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {

    }
  };

  public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN state TEXT");
      database.execSQL("UPDATE note SET state='DEFAULT' WHERE state IS NULL OR state = ''");
    }
  };

  public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN locked INTEGER NOT NULL DEFAULT 0");
    }
  };

  public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS tag (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT)");
      database.execSQL("CREATE  INDEX `index_tag_uid` ON `tag` (`uid`)");
      database.execSQL("ALTER TABLE note ADD COLUMN tags TEXT DEFAULT ''");
    }
  };

  public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN updateTimestamp INTEGER NOT NULL DEFAULT 0");
      database.execSQL("ALTER TABLE note ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0");
      database.execSQL("UPDATE note SET updateTimestamp = timestamp");
    }
  };
}
