package com.maubis.scarlet.base.database.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.maubis.scarlet.base.database.room.folder.Folder;
import com.maubis.scarlet.base.database.room.folder.FolderDao;
import com.maubis.scarlet.base.database.room.note.Note;
import com.maubis.scarlet.base.database.room.note.NoteDao;
import com.maubis.scarlet.base.database.room.tag.Tag;
import com.maubis.scarlet.base.database.room.tag.TagDao;
import com.maubis.scarlet.base.database.room.widget.Widget;
import com.maubis.scarlet.base.database.room.widget.WidgetDao;

@Database(entities = {Note.class, Tag.class, Widget.class, Folder.class}, version = 14)
public abstract class AppDatabase extends RoomDatabase {

  public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
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
      database.execSQL("CREATE TABLE IF NOT EXISTS tag (`uid` INTEGER PRIMARY KEY AUTOINCREMENT " + "NOT NULL, `title` TEXT)");
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
      database.execSQL("CREATE TABLE IF NOT EXISTS widget (`widgetId` INTEGER PRIMARY KEY NOT " + "NULL, `noteUUID` TEXT)");
      database.execSQL("CREATE  INDEX `index_widget_widgetId` ON `widget` (`widgetId`)");
    }
  };
  public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN meta TEXT DEFAULT ''");
    }
  };
  public static final Migration MIGRATION_10_11 = new Migration(10, 11) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("ALTER TABLE note ADD COLUMN disableBackup INTEGER NOT NULL DEFAULT 0");
    }
  };
  public static final Migration MIGRATION_11_12 = new Migration(11, 12) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS folder (`uid` INTEGER PRIMARY KEY " +
                           "AUTOINCREMENT, `title` TEXT, `timestamp` INTEGER, " +
                           "`updateTimestamp` INTEGER NOT NULL, `color` INTEGER, `uuid` TEXT)");
      database.execSQL("CREATE  INDEX `index_folder_uid` ON `folder` (`uid`)");
    }
  };
  public static final Migration MIGRATION_12_13 = new Migration(12, 13) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.beginTransaction();
      database.execSQL("CREATE TEMPORARY TABLE folder_backup(`title` TEXT, `timestamp` INTEGER, " +
                           "`updateTimestamp` INTEGER NOT NULL, `color` INTEGER, `uuid` TEXT)");
      database.execSQL("INSERT INTO folder_backup SELECT title, timestamp, updateTimestamp, color, uuid FROM folder");
      database.execSQL("DROP TABLE folder");
      database.execSQL("CREATE TABLE folder(`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                           " `title` TEXT, `timestamp` INTEGER, " +
                           "`updateTimestamp` INTEGER NOT NULL, `color` INTEGER, `uuid` TEXT)");
      database.execSQL("CREATE INDEX IF NOT EXISTS `index_folder_uid` ON `folder` (`uid`)");
      database.execSQL("INSERT INTO folder SELECT NULL, title, timestamp, updateTimestamp, color, uuid FROM folder_backup");
      database.execSQL("DROP TABLE folder_backup");
      database.execSQL("ALTER TABLE note ADD COLUMN folder TEXT DEFAULT ''");
      database.setTransactionSuccessful();
      database.endTransaction();
    }
  };
  public static final Migration MIGRATION_13_14 = new Migration(13, 14) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
    }
  };

  public static AppDatabase createDatabase(Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, "note-database")
               .allowMainThreadQueries()
               .addMigrations(
                   MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6,
                   MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11,
                   MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14
               ).build();
  }

  public abstract NoteDao notes();

  public abstract TagDao tags();

  public abstract FolderDao folders();

  public abstract WidgetDao widgets();
}
