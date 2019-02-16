package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionsItem

class GenericOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  var title: Int = 0
  var options: List<LithoChooseOptionsItem> = emptyList()

  override fun title(): Int = title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> = options
}