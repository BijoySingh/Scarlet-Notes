package com.bijoysingh.quicknote;

import android.content.Context;

import com.github.bijoysingh.starter.recyclerview.RVAdapter;

import java.util.List;

/**
 * The RV Adapter for notes
 * Created by bijoy on 5/4/16.
 */
public class NoteRVAdapter extends RVAdapter<NoteItem, NoteRVHolder> {

    public NoteRVAdapter(Context context, List<NoteItem> notes) {
        super(context, R.layout.note_item, NoteRVHolder.class);
        contents = notes;
    }

    public void setValues(List<NoteItem> notes) {
        contents = notes;
    }

    @Override
    public List<NoteItem> getValues() {
        return contents;
    }
}
