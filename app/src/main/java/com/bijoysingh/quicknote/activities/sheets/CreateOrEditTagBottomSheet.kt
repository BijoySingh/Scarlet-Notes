package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.database.Note
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.LocaleManager


class CreateOrEditTagBottomSheet : ThemedBottomSheetFragment() {

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_or_edit_tag

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = CreateOrEditTagBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}