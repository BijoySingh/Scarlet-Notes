package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bijoysingh.quicknote.activities.NoteActivity;
import com.bijoysingh.quicknote.items.RecyclerItem;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;

public class EmptyRecyclerHolder extends RecyclerViewHolder<RecyclerItem> {
  /**
   * Constructor for the recycler view holder
   *
   * @param context  the application/activity context
   * @param itemView the view of the current item
   */
  public EmptyRecyclerHolder(Context context, View itemView) {
    super(context, itemView);
  }

  @Override
  public void populate(RecyclerItem data, Bundle extra) {
    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(context, NoteActivity.class);
        context.startActivity(intent);
      }
    });
  }
}
