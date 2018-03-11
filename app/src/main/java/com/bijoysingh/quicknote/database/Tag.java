package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;

import com.bijoysingh.quicknote.activities.external.ExportableTag;
import com.github.bijoysingh.starter.util.RandomHelper;

@Entity(tableName = "tag", indices = {@Index("uid")})
public class Tag {
  @PrimaryKey(autoGenerate = true)
  public int uid;

  public String title;

  public String uuid;

  public static TagDao db(Context context) {
    return AppDatabase.getDatabase(context).tags();
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
