package com.bijoysingh.quicknote.views;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;

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
    title.setText(item.title);
    description.setText(item.description);
  }

  public Note getNote(Note item) {
    item.title = title.getText().toString();
    item.description = description.getText().toString();
    return item;
  }
}
