package com.bijoysingh.quicknote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet;
import com.bijoysingh.quicknote.activities.sheets.HomeNavigationBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.items.EmptyRecyclerItem;
import com.bijoysingh.quicknote.items.NoteRecyclerItem;
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.async.MultiAsyncTask;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;

import java.util.List;

public class MainActivity extends ThemedActivity {

  RecyclerView recyclerView;
  NoteAppAdapter adapter;
  NoteState mode;
  DataStore store;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    adapter = new NoteAppAdapter(this);
    mode = NoteState.DEFAULT;
    store = DataStore.get(this);

    setupRecyclerView();
    setListeners();
    requestSetNightMode(store.get(ThemedActivity.Companion.getKey(), false));
  }

  public void setListeners() {
    View addNote = findViewById(R.id.menu_add_note);
    addNote.setOnClickListener(openNewNoteActivity());

    View addList = findViewById(R.id.menu_add_list);
    addList.setOnClickListener(openNewListNoteActivity());

    View homeNav = findViewById(R.id.menu_home_nav);
    homeNav.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        HomeNavigationBottomSheet.Companion.openSheet(MainActivity.this);
      }
    });

    View addRichNote = findViewById(R.id.menu_add_rich_note);
    addRichNote.setOnClickListener(openNewRichNoteActivity());

    View homeOptions = findViewById(R.id.home_option_button);
    homeOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SettingsOptionsBottomSheet.Companion.openSheet(MainActivity.this);
      }
    });

    ImageView backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });
  }

  public void setupRecyclerView() {
    recyclerView = new RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .build();
  }

  private void loadNoteByStates(final String[] states) {
    MultiAsyncTask.execute(this, new MultiAsyncTask.Task<List<Note>>() {
      @Override
      public List<Note> run() {
        return Note.db(MainActivity.this).getByNoteState(states);
      }

      @Override
      public void handle(List<Note> notes) {
        adapter.clearItems();

        if (notes.isEmpty()) {
          adapter.addItem(new EmptyRecyclerItem());
        }

        for (Note note : notes) {
          adapter.addItem(new NoteRecyclerItem(note));
        }
      }
    });
  }

  public NoteState getMode() {
    return mode == null ? NoteState.DEFAULT : mode;
  }

  public void onHomeClick() {
    mode = NoteState.DEFAULT;
    loadNoteByStates(new String[]{NoteState.DEFAULT.name(), NoteState.FAVOURITE.name()});
  }

  public void onFavouritesClick() {
    mode = NoteState.FAVOURITE;
    loadNoteByStates(new String[]{NoteState.FAVOURITE.name()});
  }

  public void onArchivedClick() {
    mode = NoteState.ARCHIVED;
    loadNoteByStates(new String[]{NoteState.ARCHIVED.name()});
  }

  public void onTrashClick() {
    mode = NoteState.TRASH;
    loadNoteByStates(new String[]{NoteState.TRASH.name()});
  }

  public View.OnClickListener openNewNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateSimpleNoteActivity.class);
        startActivity(intent);
      }
    };
  }

  public View.OnClickListener openNewRichNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateOrEditAdvancedNoteActivity.class);
        startActivity(intent);
      }
    };
  }

  public View.OnClickListener openNewListNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateAdvancedListActivity.class);
        startActivity(intent);
      }
    };
  }

  public void moveItemToTrashOrDelete(Note note) {
    if (mode == NoteState.TRASH) {
      note.delete(this);
      setupData();
      return;
    }
    markItem(note, NoteState.TRASH);
  }

  public void updateNote(Note note) {
    note.save(this);
    setupData();
  }

  public void markItem(Note note, NoteState state) {
    note.mark(this, state);
    setupData();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setupData();
  }

  private void setupData() {
    mode = getMode();
    switch (mode) {
      case FAVOURITE:
        onFavouritesClick();
        return;
      case ARCHIVED:
        onArchivedClick();
        return;
      case TRASH:
        onTrashClick();
        return;
      default:
      case DEFAULT:
        onHomeClick();
    }
  }

  @Override
  public void notifyNightModeChange() {
    store.put(ThemedActivity.Companion.getKey(), isNightMode());
    if (isNightMode()) {

    } else {

    }
  }

}
