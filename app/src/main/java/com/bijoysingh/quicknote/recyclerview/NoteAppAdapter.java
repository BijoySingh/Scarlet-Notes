package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;

import com.bijoysingh.quicknote.items.RecyclerItem;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem;

import java.util.List;

public class NoteAppAdapter extends MultiRecyclerViewAdapter<RecyclerItem> {
  public NoteAppAdapter(Context context) {
    this(context, false, false);
  }

  public NoteAppAdapter(Context context, boolean staggered, boolean isTablet) {
    super(context, RecyclerItem.getList(staggered, isTablet));
  }

  public NoteAppAdapter(Context context, List<MultiRecyclerViewControllerItem<RecyclerItem>> list) {
    super(context, list);
  }

  @Override
  public int getItemViewType(int position) {
    return getItems().get(position).getType().ordinal();
  }
}
