package com.maubis.scarlet.base.database.room.tag;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tag", indices = {@Index("uid")})
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public String uuid;
}
