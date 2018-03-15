package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.CircleDrawable
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.renderMarkdown
import com.bijoysingh.quicknote.utils.shouldShowWhatsNewSheet

class WhatsNewItemsBottomSheet : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    setContent(dialog)
  }

  private fun setContent(dialog: Dialog) {
    val activity = themedActivity()

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))
    val whatsNew = "> A lot has changed in this update, here is a summary of those changes.\n" +
        "### New Features\n" +
        "- **Search Bar:** All new search bar for notes\n" +
        "### Improvements\n" +
        "- **Import and Export :** Better, more useful and reliable import and export of notes\n" +
        "- **Scroll Performance :** Smoother scroll through notes, better handling of images\n" +
        "### New Pro Only Features\n" +
        "- **Distraction Free Mode :** New distraction free note viewing mode, so you can read your note comfortably\n" +
        "- **Reminders :** Adding ability to create reminders for notes \n" +
        "### Bugs Fix\n" +
        "- **Sync Notes :** Resolving major issue with note sync causing duplication\n" +
        "- **Duplicate Notes :** Resolving some issues with duplicating notes\n" +
        "- **Import :** Resolving invalid timestamp on notes after import\n" +
        "- Even more little things which help you enjoy using this app everyday"

    val whatsNewView = dialog.findViewById<TextView>(R.id.whats_new_text)
    whatsNewView.setTextColor(appTheme().get(ThemeColorType.TERTIARY_TEXT))
    whatsNewView.text = renderMarkdown(activity, whatsNew)

    val whatsNewIcon = dialog.findViewById<ImageView>(R.id.whats_new_icon)
    whatsNewIcon.background = CircleDrawable(Color.WHITE, false)

    val closeSheet = dialog.findViewById<ImageView>(R.id.close_sheet)
    closeSheet.setColorFilter(appTheme().get(ThemeColorType.TOOLBAR_ICON))
    closeSheet.setOnClickListener { dismiss() }

    val translate = dialog.findViewById<TextView>(R.id.translate)
    translate.setTextColor(appTheme().get(ThemeColorType.DISABLED_TEXT))
    translate.setOnClickListener {
      val url = GOOGLE_TRANSLATE_URL + "en/" + Uri.encode(whatsNew);
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
      dismiss()
    }

    val done = dialog.findViewById<TextView>(R.id.done)
    done.setTextColor(appTheme().get(ThemeColorType.ACCENT_TEXT))
    done.setOnClickListener { dismiss() }

    return
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_whats_new

  override fun getBackgroundView(): Int = R.id.options_layout

  companion object {
    val WHATS_NEW_UID = 3
    val GOOGLE_TRANSLATE_URL = "https://translate.google.com/#auto/"

    fun openSheet(activity: ThemedActivity) {
      val sheet = WhatsNewItemsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun maybeOpenSheet(activity: MainActivity): Boolean {
      if (shouldShowWhatsNewSheet()) {
        openSheet(activity)
        return true
      }
      return false
    }
  }
}