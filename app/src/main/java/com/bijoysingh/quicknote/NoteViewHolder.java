package com.bijoysingh.quicknote;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * The note view holder
 * Created by bijoy on 5/4/16.
 */
public class NoteViewHolder {
    TextView timestamp;
    EditText title;
    EditText description;

    public NoteViewHolder(Activity activity) {
        timestamp = (TextView) activity.findViewById(R.id.timestamp);
        title = (EditText) activity.findViewById(R.id.title);
        description = (EditText) activity.findViewById(R.id.description);
    }

    public void setNote(NoteItem item) {
        timestamp.setText(item.timestamp);
        title.setText(item.title);
        description.setText(item.description);
    }

    public NoteItem getNote(NoteItem item) {
        item.title = title.getText().toString();
        item.description = description.getText().toString();
        return item;
    }
}
