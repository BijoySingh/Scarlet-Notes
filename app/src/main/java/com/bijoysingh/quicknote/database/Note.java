package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.bijoysingh.quicknote.MaterialNotes;

/**
 * Underlying Database, difficult to migrate to Kotlin without breaking the Database.
 * Hence all the functions should be in NoteKExtensions
 */
@Entity(tableName = "note", indices = {@Index("uid")})
public class Note {
  @PrimaryKey(autoGenerate = true)
  public Integer uid;

  @Deprecated
  public String title = "";

  public String description = "";

  @Deprecated
  public String displayTimestamp = "";

  public Long timestamp;

  public Integer color;

  public String state;

  public boolean locked;

  public String tags;

  public long updateTimestamp;

  public boolean pinned;

  public String uuid;

  public String meta;

}
