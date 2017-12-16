package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.util.DateFormatter;

import java.util.Calendar;

@Entity(
    tableName = "tag",
    indices = {@Index("uid")}
)
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public void save(Context context) {
    long id = Tag.db(context).insertTag(this);
    uid = isUnsaved() ? ((int) id) : uid;
  }

  public boolean isUnsaved() {
    return uid == 0;
  }

  public static TagDao db(Context context) {
    return AppDatabase.getDatabase(context).tags();
  }

  public static Tag gen() {
    Tag tag = new Tag();
    tag.uid = 0;
    tag.title = "";
    return tag;
  }

}
