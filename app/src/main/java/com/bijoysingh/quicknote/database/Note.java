package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bijoysingh.quicknote.NoteDatabase;
import com.bijoysingh.quicknote.NoteItem;
import com.bijoysingh.quicknote.R;
import com.github.bijoysingh.starter.util.DateFormatter;

import java.util.Calendar;
import java.util.List;

@Entity(
    tableName = "note",
    indices = {@Index("uid")}
)
public class Note {
  @PrimaryKey(autoGenerate = true)
  public Integer uid;

  public String title;

  public String description;

  public String displayTimestamp;

  public long timestamp;

  public int color;

  public boolean isUnsaved() {
    return uid == null || uid == 0;
  }

  public static NoteDao db(Context context) {
    return AppDatabase.getDatabase(context).notes();
  }

  public static Note gen() {
    Note note = new Note();
    note.timestamp = Calendar.getInstance().getTimeInMillis();
    note.displayTimestamp = DateFormatter.getDate(Calendar.getInstance());
    note.color = 0xFF00796B;
    return note;
  }

  public static void transition(Context context) {
    NoteDatabase noteDatabase = new NoteDatabase(context);
    List<NoteItem> notes = noteDatabase.get(NoteItem.class);
    for (NoteItem note : notes) {
      Note roomNote = Note.gen();
      roomNote.title = note.title;
      roomNote.description = note.description;
      roomNote.displayTimestamp = note.timestamp;
      roomNote.color = ContextCompat.getColor(context, R.color.material_teal_700);
      Note.db(context).insertNote(roomNote);
      noteDatabase.remove(note);
    }
  }
}
