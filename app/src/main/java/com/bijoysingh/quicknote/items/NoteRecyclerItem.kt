package com.bijoysingh.quicknote.items

import com.bijoysingh.quicknote.database.Note

class NoteRecyclerItem(var note: Note) : RecyclerItem() {

  override val type = RecyclerItem.Type.NOTE
}
