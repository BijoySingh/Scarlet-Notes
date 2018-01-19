package com.bijoysingh.quicknote.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface WidgetDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(Widget widget);

  @Delete
  void delete(Widget tag);

  @Query("SELECT * FROM widget WHERE widgetId = :uid LIMIT 1")
  Widget getByID(int uid);

  @Query("SELECT * FROM widget WHERE noteUUID = :uuid")
  List<Widget> getByNote(String uuid);
}
