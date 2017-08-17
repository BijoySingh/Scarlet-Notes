package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.recyclerview.FormatAdapter;
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.views.ColorView;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;
import com.github.bijoysingh.starter.util.TextUtils;
import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.NoteActivity.HANDLER_UPDATE_TIME;
import static com.bijoysingh.quicknote.activities.NoteActivity.NOTE_ID;

public class AdvancedNoteActivity extends AppCompatActivity {
  public boolean active = false;
  public int maxUid = 0;

  private Context context;
  private Note note;
  private FormatAdapter adapter;
  private List<Format> formats;
  public Format focusedFormat;
  private boolean editMode = false;

  private ImageView actionEdit;
  private View toolbar;
  private ImageView actionDone;
  private FlexboxLayout colorSelectorLayout;
  private ImageView colorButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_advanced_note);
    context = this;

    note = Note.db(this).getByID(getIntent().getIntExtra(NOTE_ID, 0));
    editMode = note == null;
    note = note == null ? Note.gen() : note;

    setRecyclerView();
    setToolbars();
    setEditMode(editMode);
    setNote();
    startHandler();

    colorSelectorLayout = (FlexboxLayout) findViewById(R.id.flexbox_layout);
    setColorsList();
  }

  private void setEditMode(boolean mode) {
    editMode = mode;
    Bundle bundle = new Bundle();
    bundle.putBoolean(FormatTextViewHolder.KEY_EDITABLE, editMode);
    adapter.setExtra(bundle);
    setNote();

    actionEdit.setVisibility(editMode ? GONE : VISIBLE);
    actionDone.setVisibility(editMode ? VISIBLE : GONE);
    toolbar.setVisibility(editMode ? VISIBLE : GONE);
  }

  private void setNote() {
    setNoteColor(note.color);
    adapter.clearItems();
    formats = note.getFormats();
    maxUid = formats.size() + 1;
    adapter.addItems(formats);

    boolean isEmpty = formats.isEmpty();
    if (editMode && (isEmpty || formats.get(0).formatType != FormatType.HEADING)) {
      addEmptyItem(0, FormatType.HEADING);
    }
    if (editMode && isEmpty) {
      addEmptyItem(FormatType.TEXT);
    }
  }

  private void setRecyclerView() {
    adapter = new FormatAdapter(this);
    new RecyclerViewBuilder(this)
        .setAdapter(adapter)
        .setView(this, R.id.advanced_note_recycler)
        .build();
  }

  public void setFormat(Format format) {
    int position = getFormatIndex(format);
    if (position == -1) {
      return;
    }
    formats.set(position, format);
  }

  public void moveUpFormat(Format format) {
    int position = getFormatIndex(format);
    if (position <= 1) {
      return;
    }
    focusedFormat = focusedFormat == null || focusedFormat.uid == format.uid ? null : focusedFormat;
    formats.remove(position);
    adapter.removeItem(position);
    formats.add(position - 1, format);
    adapter.addItem(format, position - 1);
    updateNote();
  }

  public void moveDownFormat(Format format) {
    int position = getFormatIndex(format);
    if (position <= 0 || position == formats.size() - 1) {
      return;
    }
    focusedFormat = focusedFormat == null || focusedFormat.uid == format.uid ? null : focusedFormat;
    formats.remove(position);
    adapter.removeItem(position);
    formats.add(position + 1, format);
    adapter.addItem(format, position + 1);
    updateNote();
  }

  public void deleteFormat(Format format) {
    int position = getFormatIndex(format);
    if (position <= 0) {
      return;
    }
    focusedFormat = focusedFormat == null || focusedFormat.uid == format.uid ? null : focusedFormat;
    formats.remove(position);
    adapter.removeItem(position);
    updateNote();
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

  private int getFormatIndex(Format format) {
    int position = 0;
    for (Format fmt : formats) {
      if (fmt.uid == format.uid) {
        return position;
      }
      position++;
    }
    return -1;
  }

  private void setToolbars() {
    toolbar = findViewById(R.id.toolbar);

    ImageView text = (ImageView) findViewById(R.id.format_text);
    text.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.TEXT);
      }
    });

    ImageView heading = (ImageView) findViewById(R.id.format_heading);
    heading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.SUB_HEADING);
      }
    });

    ImageView checkList = (ImageView) findViewById(R.id.format_check_list);
    checkList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED);
      }
    });

    ImageView quote = (ImageView) findViewById(R.id.format_quote);
    quote.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.QUOTE);
      }
    });

    ImageView code = (ImageView) findViewById(R.id.format_code);
    code.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CODE);
      }
    });

    ImageView deleteButton = (ImageView) findViewById(R.id.delete_button);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        note.delete(context);
        finish();
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
        updateNote();
        new TextUtils.ShareBuilder(context)
            .setSubject(note.getTitle())
            .setText(note.getText())
            .setChooserText(context.getString(R.string.share_using))
            .share();
      }
    });

    actionEdit = (ImageView) findViewById(R.id.edit_button);
    actionEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setEditMode(true);
        actionEdit.setVisibility(GONE);
      }
    });

    actionDone = (ImageView) findViewById(R.id.done_button);
    actionDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        updateNote();
        setEditMode(false);
      }
    });

    colorButton = (ImageView) findViewById(R.id.color_button);
    View colorButtonClicker = findViewById(R.id.color_button_clicker);
    colorButtonClicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean isVisible = colorSelectorLayout.getVisibility() == VISIBLE;
        colorSelectorLayout.setVisibility(isVisible ? GONE : VISIBLE);
      }
    });
  }

  private void addEmptyItem(FormatType type) {
    addEmptyItem(formats.size(), type);
  }

  private void addEmptyItem(int position, FormatType type) {
    Format format = new Format(type);
    format.uid = maxUid + 1;
    maxUid++;

    formats.add(position, format);
    adapter.addItem(format, position);
  }

  private void addEmptyItemAtFocused(FormatType type) {
    if (focusedFormat == null) {
      addEmptyItem(type);
      return;
    }

    int position = getFormatIndex(focusedFormat);
    if (position == -1) {
      addEmptyItem(type);
      return;
    }

    addEmptyItem(position + 1, type);
  }

  private void updateNote() {
    note.description = Format.getNote(formats);
    if (note.getFormats().isEmpty() && note.isUnsaved()) {
      return;
    }
    note.save(context);
  }


  public void startHandler() {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (active && editMode) {
          updateNote();
          handler.postDelayed(this, HANDLER_UPDATE_TIME);
        }
      }
    }, HANDLER_UPDATE_TIME);
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
  }

  @Override
  public void onBackPressed() {
    updateNote();
    if (!editMode || destroyIfNeeded()) {
      finish();
    } else {
      setEditMode(false);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    destroyIfNeeded();
  }

  private boolean destroyIfNeeded() {
    if (note.isUnsaved()) {
      return true;
    }
    if (note.getFormats().isEmpty()) {
      note.delete(this);
      return true;
    }
    return false;
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
