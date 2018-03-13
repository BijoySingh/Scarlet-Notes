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

  @Query("SELECT * FROM note ORDER BY pinned DESC, timestamp DESC")
  List<Note> getAll();

  @Query("SELECT * FROM note WHERE state in (:states) ORDER BY pinned DESC, timestamp DESC")
  List<Note> getByNoteState(String[] states);

  @Query("SELECT * FROM note WHERE state = 'TRASH' AND updateTimestamp < :timestamp")
  List<Note> getOldTrashedNotes(long timestamp);

  @Query("SELECT * FROM note WHERE locked = :locked ORDER BY pinned DESC, timestamp DESC")
  List<Note> getNoteByLocked(boolean locked);

  @Query("SELECT * FROM note WHERE tags LIKE :uuidRegex ORDER BY pinned DESC, timestamp DESC")
  List<Note> getNoteByTag(String uuidRegex);

  @Query("SELECT COUNT(*) FROM note WHERE tags LIKE :uuidRegex ORDER BY pinned DESC, timestamp DESC")
  int getNoteCountByTag(String uuidRegex);

  @Query("SELECT * FROM note WHERE uid = :uid LIMIT 1")
  Note getByID(int uid);

  @Query("SELECT * FROM note WHERE uuid = :uuid LIMIT 1")
  Note getByUUID(String uuid);

  @Query("SELECT uuid FROM note")
  List<String> getAllUUIDs();

  @Query("SELECT updateTimestamp FROM note ORDER BY updateTimestamp DESC LIMIT 1")
  long getLastTimestamp();

  @Query("UPDATE note SET locked=0 WHERE locked <> 0")
  void unlockAll();
}
