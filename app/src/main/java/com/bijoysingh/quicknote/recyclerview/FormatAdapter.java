package com.bijoysingh.quicknote.recyclerview;

import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity;
import com.bijoysingh.quicknote.formats.Format;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter;

import java.util.Collections;


public class FormatAdapter extends MultiRecyclerViewAdapter<Format> implements
    ItemTouchHelperAdapter {

  ViewAdvancedNoteActivity activity;

  public FormatAdapter(ViewAdvancedNoteActivity activity) {
    super(activity, Format.getList());
    this.activity = activity;
  }

  @Override
  public int getItemViewType(int position) {
    return getItems().get(position).formatType.ordinal();
  }

  @Override
  public void onItemDismiss(int position) {
    activity.deleteFormat(getItems().get(position));
  }

  @Override
  public boolean onItemMove(int fromPosition, int toPosition) {
    if (fromPosition < toPosition) {
      for (int i = fromPosition; i < toPosition; i++) {
        Collections.swap(getItems(), i, i + 1);
      }
    } else {
      for (int i = fromPosition; i > toPosition; i--) {
        Collections.swap(getItems(), i, i - 1);
      }
    }
    notifyItemMoved(fromPosition, toPosition);
    activity.moveFormat(fromPosition, toPosition);
    return true;
  }
}

