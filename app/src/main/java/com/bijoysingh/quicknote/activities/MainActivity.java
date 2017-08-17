package com.bijoysingh.quicknote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.items.EmptyRecyclerItem;
import com.bijoysingh.quicknote.items.NoteRecyclerItem;
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter;
import com.bijoysingh.quicknote.utils.TransitionUtils;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity {

  RecyclerView recyclerView;
  NoteAppAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    adapter = new NoteAppAdapter(this);

    TransitionUtils.transition(this);
    setupRecyclerView();
    setListeners();
    setupData();
  }

  public void setListeners() {
    View addNote = findViewById(R.id.menu_add_note);
    addNote.setOnClickListener(openNewNoteActivity());

    View addList = findViewById(R.id.menu_add_list);
    addList.setOnClickListener(openNewListNoteActivity());

    View addRichNote = findViewById(R.id.menu_add_rich_note);
    addRichNote.setOnClickListener(openNewRichNoteActivity());

    ImageView backButton = (ImageView) findViewById(R.id.back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        onBackPressed();
      }
    });
  }

  public void setupData() {
    adapter.clearItems();
    List<Note> notes = Note.db(this).getAll();

    if (notes.isEmpty()) {
      adapter.addItem(new EmptyRecyclerItem());
    }

    for (Note note : notes) {
      adapter.addItem(new NoteRecyclerItem(note));
    }
  }

  public void setupRecyclerView() {
    recyclerView = new RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .build();
  }

  public View.OnClickListener openNewNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
        startActivity(intent);
      }
    };
  }

  public View.OnClickListener openNewRichNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AdvancedNoteActivity.class);
        startActivity(intent);
      }
    };
  }

  public View.OnClickListener openNewListNoteActivity() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AdvancedNoteActivity.class);
        startActivity(intent);
      }
    };
  }

  public void deleteItem(Note note) {
    note.delete(this);
    setupData();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setupData();
  }
}
