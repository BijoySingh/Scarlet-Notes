package com.bijoysingh.quicknote.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.bijoysingh.quicknote.NoteDatabase;
import com.bijoysingh.quicknote.NoteItem;
import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.formats.NoteType;
import com.github.bijoysingh.starter.util.DateFormatter;

import java.util.ArrayList;
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

  public String getText() {
    String text = "";
    List<Format> formats = Format.getFormats(description);
    for (Format format : formats) {
      if (format.formatType == FormatType.HEADING) {
        continue;
      } else if (format.formatType == FormatType.CHECKLIST_CHECKED) {
        text += "\u2611 ";
      } else if (format.formatType == FormatType.CHECKLIST_UNCHECKED) {
        text += "\u2610 ";
      }
      text += format.text + "\n";
    }
    return text.trim();
  }

  public String getTitle() {
    List<Format> formats = Format.getFormats(description);
    for (Format format : formats) {
      if (format.formatType == FormatType.HEADING) {
        return format.text == null ? "" : format.text.trim();
      }
    }
    return "";
  }

  public void save(Context context) {
    long id = Note.db(context).insertNote(this);
    uid = isUnsaved() ? ((int) id) : uid;
  }

  public void delete(Context context) {
    if (isUnsaved()) {
      return;
    }
    Note.db(context).delete(this);
  }

  public List<Format> getFormats() {
    return Format.getFormats(description);
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

  public static void transition1To2(Context context) {
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

  public static void transition2To3(Context context) {
    List<Note> notes = Note.db(context).getAll();
    for (Note note : notes) {
      List<Format> formats = new ArrayList<>();
      if (note.title != null && !note.title.isEmpty()) {
        formats.add(new Format(FormatType.HEADING, note.title));
      }
      formats.add(new Format(FormatType.TEXT, note.description));
      note.description = Format.getNote(formats);
      note.save(context);
    }
  }

  public static void transition3To4(Context context) {
    List<Note> notes = Note.db(context).getAll();
    for (Note note : notes) {
      note.title = NoteType.RICH_NOTE.name();
      note.save(context);
    }
  }
}
