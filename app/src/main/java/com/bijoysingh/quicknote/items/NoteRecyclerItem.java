package com.bijoysingh.quicknote.items;

import com.bijoysingh.quicknote.database.Note;

public class NoteRecyclerItem extends RecyclerItem {

  public Note note;
  public NoteRecyclerItem(Note note) {
    this.note = note;
  }

  @Override
  public Type getType() {
    return Type.NOTE;
  }

}
