package com.maubis.scarlet.base.database.room.folder;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FolderDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertFolder(Folder note);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertFolders(Folder... note);

  @Delete
  void delete(Folder note);

  @Query("SELECT count(*) FROM folder")
  int getCount();

  @Query("SELECT * FROM folder ORDER BY timestamp DESC")
  List<Folder> getAll();

  @Query("SELECT * FROM folder WHERE uid = :uid LIMIT 1")
  Folder getByID(int uid);

  @Query("SELECT * FROM folder WHERE uuid = :uuid LIMIT 1")
  Folder getByUUID(String uuid);

  @Query("SELECT uuid FROM folder")
  List<String> getAllUUIDs();
}
