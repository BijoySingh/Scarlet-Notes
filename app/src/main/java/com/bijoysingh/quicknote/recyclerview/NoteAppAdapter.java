package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;

import com.bijoysingh.quicknote.items.RecyclerItem;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter;

public class NoteAppAdapter extends MultiRecyclerViewAdapter<RecyclerItem> {
  public NoteAppAdapter(Context context) {
    this(context, false);
  }

  public NoteAppAdapter(Context context, boolean staggered) {
    super(context, RecyclerItem.getList(staggered));
  }

  @Override
  public int getItemViewType(int position) {
    return getItems().get(position).getType().ordinal();
  }
}
