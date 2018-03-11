package com.bijoysingh.quicknote.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {Note.class, Tag.class, Widget.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase {

  public abstract NoteDao notes();

  public abstract TagDao tags();

  public abstract WidgetDao widgets();

  public static AppDatabase createDatabase(Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, "note-database")
               .allowMainThreadQueries().addMigrations(MIGRATION_1_2, MIGRATION_2_3,
                                                       MIGRATION_3_4, MIGRATION_4_5,
                                                       MIGRATION_5_6, MIGRATION_6_7,
                                                       MIGRATION_7_8, MIGRATION_8_9).build();
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
      database.execSQL("CREATE TABLE IF NOT EXISTS tag (`uid` INTEGER PRIMARY KEY AUTOINCREMENT "
                           + "NOT NULL, `title` TEXT)");
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

  public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN uuid TEXT DEFAULT ''");
      database.execSQL("UPDATE note SET uuid = hex(randomblob(16))");
    }
  };

  public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE tag ADD COLUMN uuid TEXT DEFAULT ''");
      database.execSQL("UPDATE tag SET uuid = hex(randomblob(16))");
    }
  };

  public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS widget (`widgetId` INTEGER PRIMARY KEY NOT " +
                           "NULL, `noteUUID` TEXT)");
      database.execSQL("CREATE  INDEX `index_widget_widgetId` ON `widget` (`widgetId`)");
    }
  };
}
