package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import com.bijoysingh.quicknote.activities.external.ExportableTag;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.RandomHelper;

import java.util.ArrayList;
import java.util.Calendar;

@Entity(
    tableName = "tag",
    indices = {@Index("uid")}
)
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public String uuid;

  public void saveIfUnique(Context context) {
    Tag existing = Tag.db(context).getByTitle(title);
    if (existing == null) {
      save(context);
      return;
    }

    this.uid = existing.uid;
    this.title = existing.title;
  }

  public boolean isUnsaved() {
    return uid == 0;
  }

  public static TagDao db(Context context) {
    return AppDatabase.getDatabase(context).tags();
  }

  /*Database Functions*/
  public void save(Context context) {
    saveWithoutSync(context);
    saveToSync();
  }

  public void saveWithoutSync(Context context) {
    long id = Tag.db(context).insertTag(this);
    uid = isUnsaved() ? ((int) id) : uid;
  }

  public void saveToSync() {
    // Notify change to online/offline sync
  }

  public void delete(Context context) {
    deleteWithoutSync(context);
    deleteToSync();
  }

  public void deleteWithoutSync(Context context) {
    if (isUnsaved()) {
      return;
    }
    Tag.db(context).delete(this);
    uid = 0;
  }

  public void deleteToSync() {
    // Notify change to online/offline sync
  }

  public static Tag gen() {
    Tag tag = new Tag();
    tag.uid = 0;
    tag.title = "";
    tag.uuid = RandomHelper.getRandomString(24);
    return tag;
  }

  public static Tag gen(ExportableTag exportableTag) {
    Tag tag = Tag.gen();
    tag.title = exportableTag.getTitle();
    return tag;
  }
}
