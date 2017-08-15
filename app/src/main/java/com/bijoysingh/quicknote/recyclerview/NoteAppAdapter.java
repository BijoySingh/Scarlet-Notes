package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;

import com.bijoysingh.quicknote.items.RecyclerItem;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter;

public class NoteAppAdapter extends MultiRecyclerViewAdapter<RecyclerItem> {
  public NoteAppAdapter(Context context) {
    super(context, RecyclerItem.getList());
  }

  @Override
  public int getItemViewType(int position) {
    return getItems().get(position).getType().ordinal();
  }
}
