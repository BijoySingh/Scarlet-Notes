package com.bijoysingh.quicknote.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.sheets.HomeNavigationBottomSheet;
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet;
import com.bijoysingh.quicknote.activities.sheets.TagOpenOptionsBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.database.Tag;
import com.bijoysingh.quicknote.items.EmptyRecyclerItem;
import com.bijoysingh.quicknote.items.NoteRecyclerItem;
import com.bijoysingh.quicknote.items.RecyclerItem;
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter;
import com.bijoysingh.quicknote.utils.HomeNavigationState;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.async.MultiAsyncTask;
import com.github.bijoysingh.starter.async.SimpleThreadExecutor;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.widget.GridLayout.VERTICAL;
import static com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.KEY_LIST_VIEW;
import static com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED;

public class MainActivity extends ThemedActivity {

  private static final String MIGRATE_ZERO_NOTES = "MIGRATE_ZERO_NOTES";

  RecyclerView recyclerView;
  NoteAppAdapter adapter;
  HomeNavigationState mode;
  DataStore store;

  ImageView addList, homeNav, openTag, homeOptions, backButton, searchIcon, searchBackButton, searchCloseIcon;
  TextView addNote;
  EditText searchBox;
  View mainToolbar, searchToolbar, bottomToolbar;

  boolean isInSearchMode;
  List<Note> searchNotes;
  SimpleThreadExecutor executor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mode = HomeNavigationState.DEFAULT;
    store = DataStore.get(this);
    executor = new SimpleThreadExecutor(1);

    if(!store.get(MIGRATE_ZERO_NOTES, false)) {
      migrateZeroNotes();
    }

    setupRecyclerView();
    setListeners();
    requestSetNightMode(store.get(ThemedActivity.Companion.getKey(), false));
  }

  public void setListeners() {
    mainToolbar = findViewById(R.id.main_toolbar);
    searchToolbar = findViewById(R.id.search_toolbar);
    bottomToolbar = findViewById(R.id.bottom_toolbar_layout);

    addNote = findViewById(R.id.menu_add_note);
    addNote.setOnClickListener(openNewRichNoteActivity());

    searchIcon = findViewById(R.id.home_search_button);
    searchIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        setSearchMode(true);
        searchBox.requestFocus();
      }
    });

    searchBackButton = findViewById(R.id.search_back_button);
    searchBackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });

    searchCloseIcon = findViewById(R.id.search_close_button);
    searchCloseIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        searchBox.setText("");
      }
    });

    searchBox = findViewById(R.id.search_box);
    searchBox.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
        executor.executeNow(new Runnable() {
          @Override
          public void run() {
            final List<RecyclerItem> items = search(charSequence.toString());
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                adapter.setItems(items);
              }
            });
          }
        });
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });

    addList = findViewById(R.id.menu_add_list);
    addList.setOnClickListener(openNewListNoteActivity());

    homeNav = findViewById(R.id.menu_home_nav);
    homeNav.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        HomeNavigationBottomSheet.Companion.openSheet(MainActivity.this);
      }
    });

    openTag = findViewById(R.id.menu_open_tag);
    openTag.setOnClickListener(openTagActivity());

    homeOptions = findViewById(R.id.home_option_button);
    homeOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SettingsOptionsBottomSheet.Companion.openSheet(MainActivity.this);
      }
    });

    backButton = findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });
  }

  public void setupRecyclerView() {
    boolean staggeredView = store.get(KEY_LIST_VIEW, false);
    boolean isTablet = getResources().getBoolean(R.bool.is_tablet);

    boolean isMarkdownEnabled = store.get(KEY_MARKDOWN_ENABLED, true);
    Bundle adapterExtra = new Bundle();
    adapterExtra.putBoolean(KEY_MARKDOWN_ENABLED, isMarkdownEnabled);

    adapter = new NoteAppAdapter(this, staggeredView, isTablet);
    adapter.setExtra(adapterExtra);
    recyclerView = new RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .setLayoutManager(getLayoutManager(staggeredView, isTablet))
        .build();
  }

  private RecyclerView.LayoutManager getLayoutManager(boolean isStaggeredView, boolean isTabletView) {
    if (isTabletView) {
      return new StaggeredGridLayoutManager(2, VERTICAL);
    }
    return isStaggeredView
        ? new StaggeredGridLayoutManager(2, VERTICAL)
        : new LinearLayoutManager(this);
  }

  public void setLayoutMode(boolean staggered) {
    store.put(KEY_LIST_VIEW, staggered);
    setupRecyclerView();
    setupData();
  }

  private void loadNoteByStates(final String[] states) {
    MultiAsyncTask.execute(this, new MultiAsyncTask.Task<List<Note>>() {
      @Override
      public List<Note> run() {
        return Note.db(MainActivity.this).getByNoteState(states);
      }

      @Override
      public void handle(List<Note> notes) {
        handleNewItems(notes);
      }
    });
  }

  public HomeNavigationState getMode() {
    return mode == null ? HomeNavigationState.DEFAULT : mode;
  }

  public void onHomeClick() {
    mode = HomeNavigationState.DEFAULT;
    loadNoteByStates(new String[]{NoteState.DEFAULT.name(), NoteState.FAVOURITE.name()});
  }

  public void onFavouritesClick() {
    mode = HomeNavigationState.FAVOURITE;
    loadNoteByStates(new String[]{NoteState.FAVOURITE.name()});
  }

  public void onArchivedClick() {
    mode = HomeNavigationState.ARCHIVED;
    loadNoteByStates(new String[]{NoteState.ARCHIVED.name()});
  }

  public void onTrashClick() {
    mode = HomeNavigationState.TRASH;
    loadNoteByStates(new String[]{NoteState.TRASH.name()});
  }

  public void onLockedClick() {
    mode = HomeNavigationState.LOCKED;
    MultiAsyncTask.execute(this, new MultiAsyncTask.Task<List<Note>>() {
      @Override
      public List<Note> run() {
        return Note.db(MainActivity.this).getNoteByLocked(true);
      }

      @Override
      public void handle(List<Note> notes) {
        handleNewItems(notes);
      }
    });
  }

  private void handleNewItems(List<Note> notes) {
    adapter.clearItems();

    if (notes.isEmpty()) {
      adapter.addItem(new EmptyRecyclerItem());
    }

    for (Note note : notes) {
      adapter.addItem(new NoteRecyclerItem(note));
    }
  }

  public View.OnClickListener openTagActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TagOpenOptionsBottomSheet.Companion.openSheet(MainActivity.this);
      }
    };
  }

  public View.OnClickListener openNewRichNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateOrEditAdvancedNoteActivity.class);
        intent.putExtra(ThemedActivity.Companion.getKey(), isNightMode());
        startActivity(intent);
      }
    };
  }

  public View.OnClickListener openNewListNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateAdvancedListActivity.class);
        intent.putExtra(ThemedActivity.Companion.getKey(), isNightMode());
        startActivity(intent);
      }
    };
  }

  public void moveItemToTrashOrDelete(Note note) {
    if (mode == HomeNavigationState.TRASH) {
      note.delete(this);
      setupData();
      return;
    }
    markItem(note, NoteState.TRASH);
  }

  public void openTag(final Tag tag) {
    mode = HomeNavigationState.TAG;
    MultiAsyncTask.execute(this, new MultiAsyncTask.Task<List<Note>>() {
      @Override
      public List<Note> run() {
        List<Note> listNoteWithTag = new ArrayList<>();
        List<Note> notes = Note.db(MainActivity.this).getAll();
        for (Note note: notes) {
          if (note.getTagIDs().contains(tag.uid)) {
            listNoteWithTag.add(note);
          }
        }
        return listNoteWithTag;
      }

      @Override
      public void handle(List<Note> notes) {
        handleNewItems(notes);
      }
    });
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

  public void setupData() {
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
      case LOCKED:
        onLockedClick();
        return;
      default:
      case DEFAULT:
        onHomeClick();
    }
  }

  private void setSearchMode(boolean mode) {
    isInSearchMode = mode;
    mainToolbar.setVisibility(isInSearchMode ? View.GONE : View.VISIBLE);
    bottomToolbar.setVisibility(isInSearchMode ? View.GONE : View.VISIBLE);
    searchToolbar.setVisibility(isInSearchMode ? View.VISIBLE : View.GONE);
    searchBox.setText("");

    if (isInSearchMode) {
      tryOpeningTheKeyboard();
      searchNotes = new ArrayList<>();
      for (RecyclerItem item : adapter.getItems()) {
        if (item instanceof NoteRecyclerItem) {
          searchNotes.add(((NoteRecyclerItem) item).note);
        }
      }
    } else {
      searchNotes = null;
      setupData();
    }
  }

  private List<RecyclerItem> search(String keyword) {
    if (searchNotes == null) {
      return adapter.getItems();
    }

    List<RecyclerItem> notes = new ArrayList<>();
    for (Note note : searchNotes) {
      if (note.search(keyword)) {
        notes.add(new NoteRecyclerItem(note));
      }
    }
    return notes;
  }

  private void tryOpeningTheKeyboard() {
    try {
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    } catch (Exception exception) {
      // Do nothing
    }
  }

  @Override
  public void onBackPressed() {
    if (isInSearchMode) {
      if (searchBox.getText().toString().isEmpty()) {
        setSearchMode(false);
      } else {
        searchBox.setText("");
      }
    } else if (mode != null && mode != HomeNavigationState.DEFAULT) {
      onHomeClick();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void notifyNightModeChange() {
    store.put(ThemedActivity.Companion.getKey(), isNightMode());
    setSystemTheme();

    View containerLayout = findViewById(R.id.container_layout);
    containerLayout.setBackgroundColor(getThemeColor());

    int toolbarIconColor = getColor(R.color.material_blue_grey_700, R.color.light_secondary_text);
    addList.setColorFilter(toolbarIconColor);
    homeNav.setColorFilter(toolbarIconColor);
    openTag.setColorFilter(toolbarIconColor);
    homeOptions.setColorFilter(toolbarIconColor);
    addNote.setTextColor(toolbarIconColor);
    searchIcon.setColorFilter(toolbarIconColor);
    searchBackButton.setColorFilter(toolbarIconColor);
    searchCloseIcon.setColorFilter(toolbarIconColor);

    findViewById(R.id.separator).setVisibility(isNightMode() ? GONE : View.VISIBLE);

    TextView actionBarTitle = findViewById(R.id.action_bar_title);
    actionBarTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text));
    backButton.setColorFilter(getColor(R.color.colorAccent, R.color.colorAccentDark));

    int textColor = getColor(R.color.dark_secondary_text, R.color.light_secondary_text);
    int textHintColor = getColor(R.color.dark_hint_text, R.color.light_hint_text);
    searchBox.setTextColor(textColor);
    searchBox.setHintTextColor(textHintColor);

    bottomToolbar.setBackgroundColor(
        getColor(R.color.material_grey_50, R.color.material_grey_850));
  }

  private void migrateZeroNotes() {
    MultiAsyncTask.execute(this, new MultiAsyncTask.Task<Boolean>() {
      @Override
      public Boolean run() {
        Note note = Note.db(MainActivity.this).getByID(0);
        if (note != null) {
          Note.db(MainActivity.this).delete(note);
          note.uid = null;
          note.save(MainActivity.this);
          return true;
        }
        return false;
      }

      @Override
      public void handle(Boolean result) {
        if (result) {
          setupData();
        }
        store.put(MIGRATE_ZERO_NOTES, true);
      }
    });
  }
}
