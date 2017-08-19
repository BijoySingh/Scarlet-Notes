package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.views.ColorView;
import com.bijoysingh.quicknote.views.NoteViewHolder;
import com.google.android.flexbox.FlexboxLayout;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CreateSimpleNoteActivity extends AppCompatActivity {

  public static final String NOTE_ID = "NOTE_ID";
  public static final int HANDLER_UPDATE_TIME = 1000;

  public static boolean active = false;

  private Context context;

  private Note note;
  private NoteViewHolder holder;
  private FlexboxLayout colorSelectorLayout;
  private ImageView colorButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_note);
    context = this;

    note = Note.db(this).getByID(getIntent().getIntExtra(NOTE_ID, 0));
    note = note == null ? Note.gen() : note;

    holder = new NoteViewHolder(this);
    holder.setNote(note);

    colorSelectorLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
    setColorsList();
    setListeners();
  }

  public void setListeners() {
    ImageView backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    colorButton = (ImageView) findViewById(R.id.color_button);
    View colorPicker = findViewById(R.id.color_button_clicker);
    colorPicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean isVisible = colorSelectorLayout.getVisibility() == VISIBLE;
        colorSelectorLayout.setVisibility(isVisible ? GONE : VISIBLE);
      }
    });

    ImageView actionDone = (ImageView) findViewById(R.id.done_button);
    actionDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateNote();
        finish();
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    active = false;
    updateNote();
    destroyIfNeeded();
  }

  public void updateNote() {
    note = holder.getNote(note);
    // note.displayTimestamp = DateFormatter.getToday();
    // note.timestamp = Calendar.getInstance().getTimeInMillis();
    setNoteColor(note.color);

    if (note.isUnsaved() && note.getFormats().isEmpty()) {
      return;
    }
    note.save(context);
  }


  private void destroyIfNeeded() {
    if (note.isUnsaved()) {
      return;
    }
    if (note.getFormats().isEmpty()) {
      note.delete(this);
    }
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
          handler.postDelayed(this, HANDLER_UPDATE_TIME);
        }
      }
    }, HANDLER_UPDATE_TIME);
  }

  private void setColorsList() {
    colorSelectorLayout.removeAllViews();
    int[] colors = getResources().getIntArray(R.array.bright_colors);
    for (final int color : colors) {
      ColorView item = new ColorView(this);
      item.setColor(color, note.color == color);
      item.root.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          setNoteColor(color);
          setColorsList();
          colorSelectorLayout.setVisibility(GONE);
        }
      });
      colorSelectorLayout.addView(item);
    }
  }

  private void setNoteColor(int color) {
    note.color = color;
    colorButton.setBackground(new CircleDrawable(note.color));
  }
}
