package com.bijoysingh.quicknote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    public static final String EXISTING_NOTE = "EXISTING_NOTE";
    public static final String OPENED_FROM_SERVICE = "OPENED_FROM_SERVICE";

    public static boolean active = false;
    public static NoteActivity instance;

    public NoteViewHolder noteViewHolder;
    public NoteItem note;

    public Boolean openedFromService;

    public NoteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        instance = this;

        if (getIntent().hasExtra(EXISTING_NOTE)) {
            note = (NoteItem) getIntent().getSerializableExtra(EXISTING_NOTE);
        } else {
            note = new NoteItem(getTimestamp());
        }

        openedFromService = getIntent().getBooleanExtra(OPENED_FROM_SERVICE, false);

        db = new NoteDatabase(this);

        noteViewHolder = new NoteViewHolder(this);
        noteViewHolder.setNote(note);

        addToolbarListeners();
    }

    public String getTimestamp() {
        Calendar calendar = Calendar.getInstance();
        DateFormat sdf = new SimpleDateFormat("dd MMM yyyy, h mm a", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public void addToolbarListeners() {
        ImageView openBubble = (ImageView) findViewById(R.id.bubble_button);
        openBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.requiresPermission(instance)) {
                    Utils.startPermissionRequest(instance);
                } else {
                    Intent intent = new Intent(getApplicationContext(), NoteService.class);
                    intent.putExtra(NoteActivity.EXISTING_NOTE, note);
                    startService(intent);
                    finish();
                }
            }
        });

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(openedFromService) {
            openBubble.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.INVISIBLE);
            View fillerSpace = findViewById(R.id.filler_space);
            fillerSpace.setVisibility(View.VISIBLE);
        }

    }

    public NoteItem getSavableNote() {
        updateNote();
        if (note.id == null && note.title.isEmpty() && note.description.isEmpty()) {
            return null;
        }
        return note;
    }

    public void updateNote() {
        note = noteViewHolder.getNote(note);
        note.timestamp = getTimestamp();

        if (note.id == null && note.title.isEmpty() && note.description.isEmpty()) {
            return;
        }

        note = db.addOrUpdate(note);
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        updateNote();
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
        updateNote();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
        updateNote();
    }

}
