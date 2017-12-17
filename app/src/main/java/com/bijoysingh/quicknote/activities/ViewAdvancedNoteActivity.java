package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.sheets.NoteGridBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.database.Tag;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.recyclerview.FormatAdapter;
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.async.MultiAsyncTask;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity.NOTE_ID;
import static com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED;

public class ViewAdvancedNoteActivity extends ThemedActivity {

  protected Context context;
  protected Note note;
  protected DataStore store;

  protected FormatAdapter adapter;
  protected List<Format> formats;

  public Format focusedFormat;

  protected View toolbar;
  protected RecyclerView formatsView;

  protected View rootView;
  protected ImageView backButton;
  protected ImageView actionCopy;
  protected ImageView actionDelete;
  protected ImageView actionShare;
  protected ImageView actionEdit;
  protected ImageView actionDone;
  protected ImageView actionOptions;
  protected FlexboxLayout colorSelectorLayout;
  protected ImageView colorButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_advanced_note);
    context = this;
    store = DataStore.get(context);

    int noteId = getIntent().getIntExtra(NOTE_ID, 0);
    if (noteId == 0 && savedInstanceState != null) {
      noteId = savedInstanceState.getInt(NOTE_ID, 0);
    }
    if (noteId != 0) {
      note = Note.db(this).getByID(noteId);
    }
    note = note == null ? Note.gen() : note;

    setNightMode(getIntent().getBooleanExtra(
        ThemedActivity.Companion.getKey(),
        store.get(ThemedActivity.Companion.getKey(), false)));

    rootView = findViewById(R.id.root_layout);
    setRecyclerView();
    setToolbars();
    setEditMode();
  }

  public static Intent getIntent(Context context, Note note, boolean nightMode) {
    Intent intent = new Intent(context, ViewAdvancedNoteActivity.class);
    intent.putExtra(CreateSimpleNoteActivity.NOTE_ID, note.uid);
    intent.putExtra(ThemedActivity.Companion.getKey(), nightMode);
    return intent;
  }

  @Override
  protected void onResume() {
    super.onResume();
    onResumeAction();
  }

  protected void onResumeAction() {
    note = Note.db(this).getByID(getIntent().getIntExtra(NOTE_ID, 0));
    if (note == null) {
      finish();
      return;
    }
    setNote();
  }

  protected void setEditMode() {
    setEditMode(getEditModeValue());
    formatsView.setBackgroundResource(isNightMode() ? R.color.material_grey_800 : R.color.white);
  }

  protected boolean getEditModeValue() {
    return false;
  }

  protected void setEditMode(boolean mode) {
    resetBundle();
    setNote();

    actionEdit.setVisibility(mode ? GONE : VISIBLE);
    actionDone.setVisibility(mode ? VISIBLE : GONE);
    toolbar.setVisibility(mode ? VISIBLE : GONE);
  }

  private void resetBundle() {
    Bundle bundle = new Bundle();
    bundle.putBoolean(FormatTextViewHolder.KEY_EDITABLE, getEditModeValue());
    bundle.putBoolean(KEY_MARKDOWN_ENABLED, store.get(KEY_MARKDOWN_ENABLED, true));
    bundle.putBoolean(ThemedActivity.Companion.getKey(), isNightMode());
    adapter.setExtra(bundle);
  }

  protected void setNote() {
    setNoteColor(note.color);
    adapter.clearItems();
    formats = note.getFormats();
    adapter.addItems(formats);

    if (!getEditModeValue()) {
      MultiAsyncTask.execute(this, new MultiAsyncTask.Task<Format>() {
        @Override
        public Format run() {
          Set<Tag> tags = note.getTags(context);
          String tagLabel = note.getTagString(tags);
          if(tagLabel.isEmpty()) {
            return null;
          }
          return new Format(FormatType.MARKDOWN, tagLabel);
        }

        @Override
        public void handle(Format result) {
          if (result == null) {
            return;
          }
          formats.add(result);
          adapter.addItem(result);
        }
      });
    }
  }

  private void setRecyclerView() {
    adapter = new FormatAdapter(this);
    formatsView = new RecyclerViewBuilder(this)
        .setAdapter(adapter)
        .setView(this, R.id.advanced_note_recycler)
        .build();
  }

  public void setFormat(Format format) {
    // do nothing
  }

  public void moveFormat(int from, int to) {
    // do nothing
  }

  public void deleteFormat(Format format) {
    // do nothing
  }

  public void createOrChangeToNextFormat(Format format) {
    // do nothing
  }

  public void setFormatChecked(Format format, boolean checked) {
    int position = getFormatIndex(format);
    if (position == -1) {
      return;
    }
    format.formatType = checked ? FormatType.CHECKLIST_CHECKED : FormatType.CHECKLIST_UNCHECKED;
    formats.set(position, format);
    adapter.updateItem(format, position);
    updateNote();
  }

  private void setToolbars() {
    toolbar = findViewById(R.id.toolbar);
    colorSelectorLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
    setButtonToolbar();

    actionDelete = (ImageView) findViewById(R.id.delete_button);
    actionDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (note.getNoteState() == NoteState.TRASH) {
          note.delete(context);
        } else {
          note.mark(context, NoteState.TRASH);
        }
        finish();
      }
    });

    actionCopy = (ImageView) findViewById(R.id.copy_button);
    actionCopy.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        note.copy(context);
      }
    });

    backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    actionOptions = findViewById(R.id.note_options_button);
    actionOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        NoteGridBottomSheet.Companion.openSheet(
            ViewAdvancedNoteActivity.this,
            note,
            getEditModeValue());
      }
    });

    actionShare = (ImageView) findViewById(R.id.share_button);
    actionShare.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        maybeUpdateNote();
        note.share(context);
      }
    });

    actionEdit = (ImageView) findViewById(R.id.edit_button);
    actionEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        openEditor();
      }
    });

    actionDone = (ImageView) findViewById(R.id.done_button);
    actionDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });

    colorButton = (ImageView) findViewById(R.id.color_button);
    setTopToolbar();
    notifyToolbarColor();
  }

  public void openEditor() {
    note.edit(context, isNightMode());
  }

  protected void notifyToolbarColor() {
    int toolbarIconColor = ContextCompat.getColor(
        context, isNightMode() ? R.color.white : R.color.material_blue_grey_700);
    backButton.setColorFilter(toolbarIconColor);
    actionCopy.setColorFilter(toolbarIconColor);
    actionDelete.setColorFilter(toolbarIconColor);
    actionShare.setColorFilter(toolbarIconColor);
    actionEdit.setColorFilter(toolbarIconColor);
    actionDone.setColorFilter(toolbarIconColor);
    actionOptions.setColorFilter(toolbarIconColor);

    int backgroundColor = isNightMode() ? R.color.material_grey_800 : R.color.white;
    rootView.setBackgroundResource(backgroundColor);
    formatsView.setBackgroundResource(backgroundColor);

    resetBundle();
    adapter.notifyDataSetChanged();
    setSystemTheme();
  }

  protected void setButtonToolbar() {
    // do nothing
    toolbar.setVisibility(GONE);
  }

  protected void setTopToolbar() {
    View colorButtonClicker = findViewById(R.id.color_button_clicker);
    colorButtonClicker.setVisibility(GONE);
  }

  protected void updateNote() {
    note.description = Format.getNote(formats);
    if (note.getFormats().isEmpty() && note.isUnsaved()) {
      return;
    }
    note.save(context);
  }

  protected void maybeUpdateNote() {
    // do nothing
  }

  protected void setNoteColor(int color) {
    colorButton.setBackground(new CircleDrawable(note.color));
  }


  public void moveItemToTrashOrDelete(Note note) {
    if (note.getNoteState() == NoteState.TRASH) {
      note.delete(this);
    } else {
      markItem(note, NoteState.TRASH);
    }
    finish();
  }


  public void markItem(Note note, NoteState state) {
    note.mark(this, state);
  }

  public void notifyNoteChange() {

  }

  protected int getFormatIndex(Format format) {
    int position = 0;
    for (Format fmt : formats) {
      if (fmt.uid == format.uid) {
        return position;
      }
      position++;
    }
    return -1;
  }

  @Override
  public void notifyNightModeChange() {
    notifyToolbarColor();
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    if (savedInstanceState == null) {
      return;
    }
    savedInstanceState.putInt(NOTE_ID,  note == null || note.uid == null ? 0 : note.uid);
  }
}
