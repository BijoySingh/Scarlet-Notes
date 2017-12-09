package com.bijoysingh.quicknote.views;

import android.app.Activity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
    description = (EditText) activity.findViewById(R.id.description);
    title = (EditText) activity.findViewById(R.id.title);
    title.setImeOptions(EditorInfo.IME_ACTION_DONE);
    title.setRawInputType(InputType.TYPE_CLASS_TEXT);
    title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (event == null) {
          if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
            return false;
          }
        } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
          if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return true;
          }
        } else {
          return false;
        }
        description.requestFocus();
        return true;
      }
    });
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
