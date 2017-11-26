package com.bijoysingh.quicknote;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.bijoysingh.quicknote.database.AppDatabase;
import com.github.bijoysingh.starter.util.TextUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.bijoysingh.quicknote.database.AppDatabase.MIGRATION_2_3;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
  private static final String TEST_DB = "migration-test";

  private static final String TABLE_NOTE = "note";

  private static final String NOTE_V2 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color) "
          + "VALUES('RICH_NOTE', '{\"formats\":[]}', '6 August 2017', 32121312, 23123);";

  private static final String NOTE_V3 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color, state) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT');";

  @Rule
  public MigrationTestHelper helper;

  public MigrationTest() {
    helper = new MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase.class.getCanonicalName(),
        new FrameworkSQLiteOpenHelperFactory());
  }

  @Test
  public void migrate2To3() throws IOException {
    SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 2);
    database.execSQL(NOTE_V2);

    String title = getValue(database, select(TABLE_NOTE, 1, "title"));
    String description = getValue(database, select(TABLE_NOTE, 1, "description"));

    database.close();

    database = helper.runMigrationsAndValidate(TEST_DB, 3, false, MIGRATION_2_3);
    validate(database, select(TABLE_NOTE, 1));
    validateNotNullOrEmpty(database, select(TABLE_NOTE, 1, "state"));
    String titleChanged = getValue(database, select(TABLE_NOTE, 1, "title"));
    String descriptionChanged = getValue(database, select(TABLE_NOTE, 1, "description"));
    Assert.assertTrue(TextUtils.areEqualNullIsEmpty(title, titleChanged));
    Assert.assertTrue(TextUtils.areEqualNullIsEmpty(description, descriptionChanged));

    database.execSQL(NOTE_V3);
    validate(database, select(TABLE_NOTE, 2));
  }


  private static void validate(SupportSQLiteDatabase database, String query) {
    Cursor cursor = database.query(query);
    Assert.assertTrue(cursor.moveToNext());
  }

  private static void validateNotNullOrEmpty(SupportSQLiteDatabase database, String query) {
    Assert.assertTrue(!TextUtils.isNullOrEmpty(getValue(database, query)));
  }

  private static String getValue(SupportSQLiteDatabase database, String query) {
    Cursor cursor = database.query(query);
    cursor.moveToNext();
    return cursor.getString(0);
  }

  private static String select(String table, int uid) {
    return "SELECT * FROM " + table + " WHERE uid = " + uid;
  }

  private static String select(String table, int uid, String key) {
    return "SELECT " + key + " FROM " + table + " WHERE uid = " + uid;
  }
}
