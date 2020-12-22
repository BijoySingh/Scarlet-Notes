package com.maubis.scarlet.base.database.room.widget;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "widget", indices = {@Index("widgetId")})
public class Widget {

  @PrimaryKey
  public int widgetId;

  public String noteUUID;

  public Widget() {
  }

  public Widget(int widgetId, String noteId) {
    this.widgetId = widgetId;
    this.noteUUID = noteId;
  }
}
