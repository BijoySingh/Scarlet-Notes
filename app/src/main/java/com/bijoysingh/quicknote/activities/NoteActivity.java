package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.FloatingNoteService;
import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.utils.CircleDrawable;
import com.bijoysingh.quicknote.views.ColorView;
import com.bijoysingh.quicknote.views.NoteViewHolder;
import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.TextUtils;
import com.google.android.flexbox.FlexboxLayout;

import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NoteActivity extends AppCompatActivity {

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
    ImageView deleteButton = (ImageView) findViewById(R.id.delete_button);
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Note.db(context).delete(note);
        finish();
      }
    });

    ImageView openBubble = (ImageView) findViewById(R.id.bubble_button);
    openBubble.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openFloatingService();
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
            .setSubject(holder.title.getText().toString())
            .setText(holder.description.getText().toString())
            .setChooserText("Share using...")
            .share();
      }
    });

    ImageView copyButton = (ImageView) findViewById(R.id.copy_button);
    copyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextUtils.copyToClipboard(context, holder.description.getText().toString());
      }
    });

    colorButton = (ImageView) findViewById(R.id.color_button);
    colorButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean isVisible = colorSelectorLayout.getVisibility() == VISIBLE;
        colorSelectorLayout.setVisibility(isVisible ? GONE : VISIBLE);
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
    note = holder.getNote(note);
    note.displayTimestamp = DateFormatter.getToday();
    note.timestamp = Calendar.getInstance().getTimeInMillis();
    setNoteColor(note.color);

    if (note.isUnsaved()
        && note.title.isEmpty()
        && note.description.isEmpty()) {
      return;
    }

    if (!note.isUnsaved()
        && note.title.isEmpty()
        && note.description.isEmpty()) {
      Note.db(context).delete(note);
    }

    long id = Note.db(context).insertNote(note);
    note.uid = note.isUnsaved() ? ((int) id) : note.uid;
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

  private void openFloatingService() {
    FloatingNoteService.openNote(this, note, true);
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
    holder.title.setTextColor(note.color);
    colorButton.setBackground(new CircleDrawable(note.color));
  }
}
