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
import static com.bijoysingh.quicknote.database.AppDatabase.MIGRATION_3_4;
import static com.bijoysingh.quicknote.database.AppDatabase.MIGRATION_4_5;
import static com.bijoysingh.quicknote.database.AppDatabase.MIGRATION_5_6;
import static com.bijoysingh.quicknote.database.AppDatabase.MIGRATION_6_7;

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
  private static final String TEST_DB = "migration-test";

  private static final String TABLE_NOTE = "note";
  private static final String TABLE_TAG = "tag";

  private static final String NOTE_V2 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color) "
          + "VALUES('RICH_NOTE', '{\"formats\":[]}', '6 August 2017', 32121312, 23123);";

  private static final String NOTE_V3 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color, state) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT');";

  private static final String NOTE_V4 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color, state, locked) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT', 1);";

  private static final String NOTE_V5 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color, state, locked, tags) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT', 1, '1,2');";

  private static final String NOTE_V6 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color," +
          " state, locked, tags, pinned, updateTimestamp) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT', 1, '1,2', 1, 213213);";

  private static final String NOTE_V7 =
      "INSERT INTO note (title, description, displayTimestamp, timestamp, color," +
          " state, locked, tags, pinned, updateTimestamp, uuid) "
          + "VALUES('Title', 'Description', '6 August 2017', 32121312, 23123, 'DEFAULT', 1, '1,2', 1, 213213, 'test');";

  private static final String TAG_V5 =
      "INSERT INTO tag (title) VALUES('Title');";



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


  @Test
  public void migrate3To4() throws IOException {
    SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 3);
    database.execSQL(NOTE_V3);
    database.close();

    database = helper.runMigrationsAndValidate(TEST_DB, 4, false, MIGRATION_3_4);
    validate(database, select(TABLE_NOTE, 1));
    Assert.assertTrue(getIntValue(database, select(TABLE_NOTE, 1, "locked")) == 0);

    database.execSQL(NOTE_V4);
    validate(database, select(TABLE_NOTE, 2));
    Assert.assertTrue(getIntValue(database, select(TABLE_NOTE, 2, "locked")) == 1);
  }


  @Test
  public void migrate4To5() throws IOException {
    SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 4);
    database.execSQL(NOTE_V4);
    database.close();

    database = helper.runMigrationsAndValidate(TEST_DB, 5, false, MIGRATION_4_5);
    validate(database, select(TABLE_NOTE, 1));
    Assert.assertTrue(getValue(database, select(TABLE_NOTE, 1, "tags")).isEmpty());

    database.execSQL(NOTE_V5);
    database.execSQL(TAG_V5);
    validate(database, select(TABLE_NOTE, 2));
    validate(database, select(TABLE_TAG, 1));
  }

  @Test
  public void migrate5To6() throws IOException {
    SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 5);
    database.execSQL(NOTE_V5);
    database.close();

    database = helper.runMigrationsAndValidate(TEST_DB, 6, false, MIGRATION_5_6);
    validate(database, select(TABLE_NOTE, 1));
    Assert.assertTrue(getIntValue(database, select(TABLE_NOTE, 1, "pinned")) == 0);
    Assert.assertTrue(getIntValue(database, select(TABLE_NOTE, 1, "updateTimestamp")) == 32121312);

    database.execSQL(NOTE_V6);
    validate(database, select(TABLE_NOTE, 2));
  }


  @Test
  public void migrate6To7() throws IOException {
    SupportSQLiteDatabase database = helper.createDatabase(TEST_DB, 6);
    database.execSQL(NOTE_V6);
    database.close();

    database = helper.runMigrationsAndValidate(TEST_DB, 7, false, MIGRATION_6_7);
    validate(database, select(TABLE_NOTE, 1));

    String uuid = getValue(database, select(TABLE_NOTE, 1, "uuid"));
    Assert.assertTrue(!uuid.isEmpty());

    database.execSQL(NOTE_V7);
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

  private static Integer getIntValue(SupportSQLiteDatabase database, String query) {
    Cursor cursor = database.query(query);
    cursor.moveToNext();
    return cursor.getInt(0);
  }

  private static String select(String table, int uid) {
    return "SELECT * FROM " + table + " WHERE uid = " + uid;
  }

  private static String select(String table, int uid, String key) {
    return "SELECT " + key + " FROM " + table + " WHERE uid = " + uid;
  }
}
