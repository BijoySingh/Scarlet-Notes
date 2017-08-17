package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;

import com.bijoysingh.quicknote.formats.Format;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewAdapter;


public class FormatAdapter extends MultiRecyclerViewAdapter<Format> {

  public FormatAdapter(Context context) {
    super(context, Format.getList());
  }

  @Override
  public int getItemViewType(int position) {
    return getItems().get(position).formatType.ordinal();
  }
}

