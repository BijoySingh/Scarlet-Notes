package com.maubis.scarlet.base.database.room.folder;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Underlying Database, difficult to migrate to Kotlin without breaking the Database.
 * Hence all the functions should be in NoteKExtensions
 */
@Entity(tableName = "folder", indices = {@Index("uid")})
public class Folder {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public Long timestamp;

  public long updateTimestamp;

  public Integer color;

  public String uuid;
}
