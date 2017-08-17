package com.bijoysingh.quicknote.items;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.recyclerview.EmptyRecyclerHolder;
import com.bijoysingh.quicknote.recyclerview.NoteRecyclerHolder;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerItem {
  public enum Type {
    NOTE,
    EMPTY,
  }

  abstract public Type getType();

  public static List<MultiRecyclerViewControllerItem<RecyclerItem>> getList() {
    List<MultiRecyclerViewControllerItem<RecyclerItem>> list = new ArrayList<>();
    list.add(new MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
                 .viewType(Type.NOTE.ordinal())
                 .layoutFile(R.layout.item_note)
                 .holderClass(NoteRecyclerHolder.class)
                 .build());
    list.add(new MultiRecyclerViewControllerItem.Builder<RecyclerItem>()
                 .viewType(Type.EMPTY.ordinal())
                 .layoutFile(R.layout.item_no_notes)
                 .holderClass(EmptyRecyclerHolder.class)
                 .build());
    return list;
  }
}
