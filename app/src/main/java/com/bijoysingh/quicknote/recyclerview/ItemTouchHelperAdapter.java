package com.bijoysingh.quicknote.recyclerview;

public interface ItemTouchHelperAdapter {

  boolean onItemMove(int fromPosition, int toPosition);

  void onItemDismiss(int position);
}