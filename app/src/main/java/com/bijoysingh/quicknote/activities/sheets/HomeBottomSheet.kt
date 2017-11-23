package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.bijoysingh.quicknote.items.HomeOptionsItem
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.github.bijoysingh.uibasics.views.UIContentView

class HomeBottomSheet : SimpleBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    val layout = dialog.findViewById<LinearLayout>(R.id.options_layout);
    for (option in getOptions()) {
      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIContentView
      contentView.setTitle(option.title)
      contentView.setSubtitle(option.subtitle)
      contentView.setOnClickListener(option.listener)
      layout.addView(contentView)
    }
  }

  internal fun getOptions(): List<HomeOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<HomeOptionsItem>()
    options.add(HomeOptionsItem(
        title = R.string.home_option_export,
        subtitle = R.string.home_option_export_subtitle,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          if (manager.hasAllPermissions()) {
            ExportNotesBottomSheet.openSheet(activity)
            dismiss()
          } else {
            PermissionBottomSheet.openSheet(activity)
          }
        }
    ))
    options.add(HomeOptionsItem(
        title = R.string.home_option_import,
        subtitle = R.string.home_option_import_subtitle,
        listener = View.OnClickListener {

        }
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = HomeBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}