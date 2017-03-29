package com.bijoysingh.quicknote;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.TextUtils;

public class NoteActivity extends AppCompatActivity {

  public static final String EXISTING_NOTE = "EXISTING_NOTE";

  public static boolean active = false;
  public NoteActivity activity;

  public NoteViewHolder noteViewHolder;
  public NoteItem note;

  public NoteDatabase db;
  public Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_note);
    activity = this;
    context = this;

    if (getIntent().hasExtra(EXISTING_NOTE)) {
      note = (NoteItem) getIntent().getSerializableExtra(EXISTING_NOTE);
    } else {
      note = new NoteItem(getTimestamp());
    }

    db = new NoteDatabase(this);

    noteViewHolder = new NoteViewHolder(this);
    noteViewHolder.setNote(note);

    addToolbarListeners();
  }

  public String getTimestamp() {
    return DateFormatter.getToday();
  }

  public void addToolbarListeners() {
    ImageView openBubble = (ImageView) findViewById(R.id.bubble_button);
    openBubble.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FloatingNoteService.openNote(activity, note, true);
      }
    });

    ImageView backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    ImageView shareButton = (ImageView) findViewById(R.id.share_button);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new TextUtils.ShareBuilder(context)
            .setSubject(noteViewHolder.title.getText().toString())
            .setText(noteViewHolder.description.getText().toString())
            .setChooserText("Share using...")
            .share();
      }
    });

    ImageView copyButton = (ImageView) findViewById(R.id.copy_button);
    copyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextUtils.copyToClipboard(context, noteViewHolder.description.getText().toString());
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    active = false;
    updateNote();
  }

  public void updateNote() {
    note = noteViewHolder.getNote(note);
    note.timestamp = getTimestamp();

    if (note.id == null
        && note.title.isEmpty()
        && note.description.isEmpty()) {
      return;
    }

    if (note.id != null
        && note.title.isEmpty()
        && note.description.isEmpty()) {
      db.remove(note);
    }

    note = db.addOrUpdate(note);
  }

  @Override
  protected void onPause() {
    super.onPause();
    active = false;
    updateNote();
  }

  @Override
  protected void onResume() {
    super.onResume();
    active = true;
    updateNote();
    startHandler();
  }

  public void startHandler() {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (active) {
          updateNote();
          handler.postDelayed(this, 1000);
        }
      }
    }, 1000);
  }

}
