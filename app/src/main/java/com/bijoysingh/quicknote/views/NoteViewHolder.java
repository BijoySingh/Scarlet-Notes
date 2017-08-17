package com.bijoysingh.quicknote.views;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.formats.NoteType;

import java.util.ArrayList;
import java.util.List;

/**
 * The note view holder
 * Created by bijoy on 5/4/16.
 */
public class NoteViewHolder {
  public EditText title;
  public EditText description;
  public TextView timestamp;

  public NoteViewHolder(Activity activity) {
    timestamp = (TextView) activity.findViewById(R.id.timestamp);
    title = (EditText) activity.findViewById(R.id.title);
    description = (EditText) activity.findViewById(R.id.description);
  }

  public void setNote(Note item) {
    timestamp.setText(item.displayTimestamp);
    title.setText(item.getTitle());
    description.setText(item.getText());
  }

  public Note getNote(Note item) {
    List<Format> formats = new ArrayList<>();
    formats.add(new Format(FormatType.HEADING, title.getText().toString()));
    formats.add(new Format(FormatType.TEXT, description.getText().toString()));
    item.title = NoteType.NOTE.name();
    item.description = Format.getNote(formats);
    return item;
  }
}
