package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.recyclerview.FormatAdapter;
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity.NOTE_ID;

public class ViewAdvancedNoteActivity extends AppCompatActivity {

  protected Context context;
  protected Note note;

  protected FormatAdapter adapter;
  protected List<Format> formats;

  public Format focusedFormat;

  protected View toolbar;
  protected RecyclerView formatsView;
  protected ImageView actionPopUp;
  protected ImageView actionCopy;
  protected ImageView actionDelete;
  protected ImageView actionShare;
  protected ImageView actionEdit;
  protected ImageView actionDone;
  protected FlexboxLayout colorSelectorLayout;
  protected ImageView colorButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_advanced_note);
    context = this;

    note = Note.db(this).getByID(getIntent().getIntExtra(NOTE_ID, 0));
    note = note == null ? Note.gen() : note;

    setRecyclerView();
    setToolbars();
    setEditMode();
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
    setEditMode(false);
    formatsView.setBackgroundColor(Color.WHITE);
  }

  protected void setEditMode(boolean mode) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(FormatTextViewHolder.KEY_EDITABLE, mode);
    adapter.setExtra(bundle);
    setNote();

    actionEdit.setVisibility(mode ? GONE : VISIBLE);
    actionDone.setVisibility(mode ? VISIBLE : GONE);
    toolbar.setVisibility(mode ? VISIBLE : GONE);
  }

  protected void setNote() {
    setNoteColor(note.color);
    adapter.clearItems();
    formats = note.getFormats();
    adapter.addItems(formats);
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
        note.delete(context);
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

    actionPopUp = (ImageView) findViewById(R.id.popup_button);
    actionPopUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        note.popup(ViewAdvancedNoteActivity.this);
      }
    });

    ImageView backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
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
        note.edit(context);
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
}
