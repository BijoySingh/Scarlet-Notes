package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import com.bijoysingh.quicknote.MaterialNotes;
import com.bijoysingh.quicknote.activities.external.ExportableTag;
import com.github.bijoysingh.starter.util.RandomHelper;

@Entity(tableName = "tag", indices = {@Index("uid")})
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public String uuid;

  public static TagDao db() {
    return MaterialNotes.Companion.db().tags();
  }
}
