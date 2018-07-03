package com.maubis.scarlet.base.core.database.room.folder;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.maubis.scarlet.base.core.database.room.note.Note;

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
