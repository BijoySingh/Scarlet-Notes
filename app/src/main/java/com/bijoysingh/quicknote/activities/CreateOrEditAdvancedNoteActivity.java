package com.bijoysingh.quicknote.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.sheets.ColorPickerBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder;
import com.bijoysingh.quicknote.recyclerview.SimpleItemTouchHelper;
import com.bijoysingh.quicknote.utils.CircleDrawable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Collections;

import static android.view.View.GONE;
import static com.bijoysingh.quicknote.utils.NoteBuilderKt.copyNote;

public class CreateOrEditAdvancedNoteActivity extends ViewAdvancedNoteActivity {

  private boolean active = false;
  private int maxUid = 0;

  private ImageView text;
  private ImageView heading;
  private ImageView subHeading;
  private ImageView checkList;
  private ImageView quote;
  private ImageView code;

  private Note lastNoteInstance = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTouchListener();
    startHandler();

    if (getIntent().getBooleanExtra(ThemedActivity.Companion.getKey(), false)) {
      setNightMode(true);
    }
    lastNoteInstance = copyNote(note);
  }

  @Override
  protected void setEditMode() {
    setEditMode(getEditModeValue());
  }

  @Override
  protected boolean getEditModeValue() {
    return true;
  }

  private void setTouchListener() {
    ItemTouchHelper.Callback callback = new SimpleItemTouchHelper(adapter);
    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
    touchHelper.attachToRecyclerView(formatsView);
  }

  @Override
  protected void setNote() {
    super.setNote();
    maxUid = formats.size() + 1;
    boolean isEmpty = formats.isEmpty();
    if (isEmpty || formats.get(0).formatType != FormatType.HEADING) {
      addEmptyItem(0, FormatType.HEADING);
    }
    if (isEmpty) {
      addDefaultItem();
    }
  }

  protected void addDefaultItem() {
    addEmptyItem(FormatType.TEXT);
  }

  @Override
  protected void setButtonToolbar() {
    text = (ImageView) findViewById(R.id.format_text);
    text.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.TEXT);
      }
    });

    heading = (ImageView) findViewById(R.id.format_heading);
    heading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.HEADING);
      }
    });

    subHeading = (ImageView) findViewById(R.id.format_sub_heading);
    subHeading.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.SUB_HEADING);
      }
    });

    checkList = (ImageView) findViewById(R.id.format_check_list);
    checkList.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED);
      }
    });

    quote = (ImageView) findViewById(R.id.format_quote);
    quote.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.QUOTE);
      }
    });

    code = (ImageView) findViewById(R.id.format_code);
    code.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        addEmptyItemAtFocused(FormatType.CODE);
      }
    });
  }

  @Override
  protected void setTopToolbar() {
    actionDelete.setVisibility(GONE);
    actionShare.setVisibility(GONE);
    actionCopy.setVisibility(GONE);

    View colorButtonClicker = findViewById(R.id.color_button_clicker);
    colorButtonClicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ColorPickerBottomSheet.Companion.openSheet(
            CreateOrEditAdvancedNoteActivity.this,
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
  }

  @Override
  protected void notifyToolbarColor() {
    super.notifyToolbarColor();

    int toolbarIconColor = ContextCompat.getColor(
        context, isNightMode() ? R.color.white : R.color.material_blue_grey_700);
    text.setColorFilter(toolbarIconColor);
    heading.setColorFilter(toolbarIconColor);
    subHeading.setColorFilter(toolbarIconColor);
    checkList.setColorFilter(toolbarIconColor);
    quote.setColorFilter(toolbarIconColor);
    code.setColorFilter(toolbarIconColor);

    toolbar.setBackgroundColor(getColor(R.color.material_grey_50, R.color.material_grey_850));
  }

  @Override
  protected void onPause() {
    super.onPause();
    active = false;
    maybeUpdateNoteWithSync();
    destroyIfNeeded();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    tryClosingTheKeyboard();
  }

  @Override
  protected void onResume() {
    super.onResume();
    active = true;
  }

  @Override
  protected void onResumeAction() {
    // do nothing
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

  protected void maybeUpdateNoteWithoutSync() {
    maybeUpdateNote(false);
  }

  protected void maybeUpdateNoteWithSync() {
    maybeUpdateNote(true);
  }

  protected void maybeUpdateNote(boolean sync) {
    note.updateTimestamp = Calendar.getInstance().getTimeInMillis();
    note.description = Format.getNote(formats);

    // Ignore update if nothing changed. It allows for one undo per few seconds
    if (note.isEqual(lastNoteInstance)) {
      return;
    }

    maybeSaveNote(sync);
    lastNoteInstance.copyNote(note);
  }

  private void startHandler() {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (active) {
          maybeUpdateNoteWithoutSync();
          handler.postDelayed(this, HANDLER_UPDATE_TIME);
        }
      }
    }, HANDLER_UPDATE_TIME);
  }


  protected void addEmptyItem(FormatType type) {
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

    int newPosition = position + 1;
    addEmptyItem(newPosition, type);
    formatsView.getLayoutManager().scrollToPosition(newPosition);
    focus(newPosition);
  }

  public void focus(final int position) {
    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        FormatTextViewHolder holder = findViewHolderAtPositionAggressively(position);
        if (holder == null) {
          return;
        }

        holder.requestEditTextFocus();
      }
    }, 100);
  }

  @Nullable
  private FormatTextViewHolder findViewHolderAtPositionAggressively(int position) {
    RecyclerView.ViewHolder holder = formatsView.findViewHolderForAdapterPosition(position);
    if (holder == null) {
      holder = formatsView.findViewHolderForLayoutPosition(position);
      if (holder == null) {
        return null;
      }
    }

    if (!(holder instanceof FormatTextViewHolder)) {
      return null;
    }

    return (FormatTextViewHolder) holder;
  }

  @Override
  protected void setNoteColor(int color) {
    note.color = color;
    colorButton.setBackground(new CircleDrawable(note.color));
  }

  @Override
  public void setFormat(Format format) {
    int position = getFormatIndex(format);
    if (position == -1) {
      return;
    }
    formats.set(position, format);
  }

  @Override
  public void moveFormat(int fromPosition, int toPosition) {
    if (fromPosition < toPosition) {
      for (int i = fromPosition; i < toPosition; i++) {
        Collections.swap(formats, i, i + 1);
      }
    } else {
      for (int i = fromPosition; i > toPosition; i--) {
        Collections.swap(formats, i, i - 1);
      }
    }
    maybeUpdateNoteWithoutSync();
  }

  @Override
  public void deleteFormat(Format format) {
    int position = getFormatIndex(format);
    if (position <= 0) {
      return;
    }
    focusedFormat = focusedFormat == null || focusedFormat.uid == format.uid ? null : focusedFormat;
    formats.remove(position);
    adapter.removeItem(position);
    maybeUpdateNoteWithoutSync();
  }

  @Override
  public void setFormatChecked(Format format, boolean checked) {
    // do nothing
  }

  @Override
  public void createOrChangeToNextFormat(Format format) {
    int position = getFormatIndex(format);
    if (position == -1) {
      return;
    }
    int newPosition = position + 1;
    if (newPosition < formats.size()) {
      focus(position + 1);
    } else {
      addEmptyItemAtFocused(Format.getNextFormatType(format.formatType));
    }
  }
}
