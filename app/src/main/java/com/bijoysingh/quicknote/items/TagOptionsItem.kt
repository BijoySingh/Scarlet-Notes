package com.bijoysingh.quicknote.items

import android.view.View
import com.bijoysingh.quicknote.R

class TagOptionsItem(
    val title: Int,
    val selected: Boolean = false,
    val editable: Boolean = false,
    val editListener: View.OnClickListener,
    val listener: View.OnClickListener) {

  fun getIcon(): Int = when (selected) {
    true -> R.drawable.ic_action_label
    false -> R.drawable.ic_action_label_unselected
  }

  fun getEditIcon(): Int = R.drawable.ic_edit_white_48dp
}