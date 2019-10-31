package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.ViewGroup
import com.maubis.scarlet.base.R

class BottomSheetTabletDialog(context: Context, theme: Int) : BottomSheetDialog(context, theme) {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState);
    val width = context.resources.getDimensionPixelSize(R.dimen.bottom_sheet_width_for_tablets)
    window?.setLayout(
      if (width > 0) width else ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT);
  }
}