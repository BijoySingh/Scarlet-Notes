package com.maubis.scarlet.base.database.room.tag;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "tag", indices = {@Index("uid")})
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public String uuid;
}
