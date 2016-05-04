package com.bijoysingh.quicknote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.clans.fab.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    MainActivity activity;
    RecyclerView recyclerView;
    NoteRVAdapter adapter;
    LinearLayout noMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        noMessage = (LinearLayout) findViewById(R.id.no_notes_available);

        addToolbarListeners();
        setupData();
        setupRecyclerView();

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

    public View.OnClickListener openNewBubbleService() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utils.requiresPermission(activity)) {
                    Utils.startPermissionRequest(activity);
                } else {
                    Intent intent = new Intent(getApplicationContext(), NoteService.class);
                    startService(intent);
                }
            }
        };
    }

    public void addToolbarListeners() {
        ImageView addButton = (ImageView) findViewById(R.id.new_note_button);
        addButton.setOnClickListener(openNewNoteActivity());

        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.menu_add_post);
        addFab.setOnClickListener(openNewNoteActivity());

        ImageView openBubble = (ImageView) findViewById(R.id.bubble_button);
        openBubble.setOnClickListener(openNewBubbleService());

        FloatingActionButton bubbleFab = (FloatingActionButton) findViewById(R.id.menu_add_bubble);
        bubbleFab.setOnClickListener(openNewBubbleService());

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }


    public void deleteNote(NoteItem item) {
        NoteDatabase noteDatabase = new NoteDatabase(this);
        noteDatabase.remove(item);
        setupData();
    }

    public void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void setupData() {
        NoteDatabase noteDatabase = new NoteDatabase(this);
        List<NoteItem> notes = noteDatabase.get(NoteItem.class);
        if (adapter == null) {
            adapter = new NoteRVAdapter(this, notes);
        } else {
            adapter.setValues(notes);
            adapter.notifyDataSetChanged();
        }

        if (adapter.getValues().isEmpty()) {
            noMessage.setVisibility(View.VISIBLE);
        } else {
            noMessage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupData();
    }
}
