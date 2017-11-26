package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface NoteDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insertNote(Note note);

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  void insertNotes(Note... note);

  @Delete
  void delete(Note note);

  @Query("SELECT count(*) FROM note")
  int getCount();

  @Query("SELECT * FROM note ORDER BY timestamp DESC")
  List<Note> getAll();

  @Query("SELECT * FROM note WHERE state in (:states) ORDER BY timestamp DESC")
  List<Note> getByNoteState(String[] states);

  @Query("SELECT * FROM note ORDER BY timestamp DESC LIMIT :limit")
  List<Note> getNotes(int limit);

  @Query("SELECT * FROM note ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
  List<Note> getNotes(int offset, int limit);

  @Query("SELECT * FROM note WHERE uid = :uid LIMIT 1")
  Note getByID(int uid);

}
