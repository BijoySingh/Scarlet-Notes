package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.sheets.ColorPickerBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.views.ColorView;
import com.bijoysingh.quicknote.views.NoteViewHolder;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.google.android.flexbox.FlexboxLayout;

import org.jetbrains.annotations.NotNull;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CreateSimpleNoteActivity extends ThemedActivity {

  public static final String NOTE_ID = "NOTE_ID";
  public static final int HANDLER_UPDATE_TIME = 1000;

  public static boolean active = false;

  private Context context;
  protected DataStore store;

  private Note note;
  private NoteViewHolder holder;

  private ImageView colorButton;
  private ImageView backButton;
  private ImageView actionDone;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_note);
    context = this;
    store = DataStore.get(context);

    int noteId = getIntent().getIntExtra(NOTE_ID, 0);
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(NOTE_ID, 0);
    }
    note = Note.db(this).getByID(noteId);
    note = note == null ? Note.gen() : note;

    holder = new NoteViewHolder(this);
    holder.setNote(note);

    setListeners();

    requestSetNightMode(getIntent().getBooleanExtra(
        ThemedActivity.Companion.getKey(),
        store.get(ThemedActivity.Companion.getKey(), false)));
  }

  public void setListeners() {
    backButton = (ImageView) findViewById(R.id.back_button);
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
        ColorPickerBottomSheet.Companion.openSheet(
            CreateSimpleNoteActivity.this,
            new ColorPickerBottomSheet.ColorPickerController() {
              @Override
              public void onColorSelected(@NotNull Note note, int color) {
                setNoteColor(color);
              }

              @NotNull
              @Override
              public Note getNote() {
                return note;
              }
            });
      }
    });

    actionDone = (ImageView) findViewById(R.id.done_button);
    actionDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateNote();
        onBackPressed();
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

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    tryClosingTheKeyboard();
  }

  public void updateNote() {
    note = holder.getNote(note);
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

  private void setNoteColor(int color) {
    note.color = color;
    colorButton.setBackground(new CircleDrawable(note.color));
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    savedInstanceState.putInt(NOTE_ID, note == null ? 0 : note.uid);
  }

  @Override
  public void notifyNightModeChange() {
    setSystemTheme();

    View containerLayout = findViewById(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    int toolbarIconColor = getColor(R.color.material_blue_grey_700, R.color.light_secondary_text);
    backButton.setColorFilter(toolbarIconColor);

    int textColor = getColor(R.color.dark_secondary_text, R.color.light_secondary_text);
    int textHintColor = getColor(R.color.dark_hint_text, R.color.light_hint_text);
    holder.title.setTextColor(textColor);
    holder.title.setHintTextColor(textHintColor);
    holder.description.setTextColor(textColor);
    holder.description.setHintTextColor(textHintColor);

    actionDone.setColorFilter(getColor(R.color.material_blue_grey_600, R.color.light_primary_text));
  }
}
