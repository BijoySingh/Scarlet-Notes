package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.CircleDrawable
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.renderMarkdown
import com.bijoysingh.quicknote.utils.shouldShowWhatsNewSheet
import com.github.bijoysingh.starter.prefs.DataStore

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
    optionsTitle.setTextColor(theme().get(activity, ThemeColorType.SECONDARY_TEXT))
    val whatsNew = "> A lot has changed in this update, here is a summary of those changes.\n" +
        "### New Features\n" +
        "- **Images on Notes :** You can now add images on your notes\n" +
        "- **Widgets :** New add note widget\n" +
        "- **Automatic Export :** You can not setup automatic export to an external file\n" +
        "- **Colors :** All new note colors, more accent sticky note colors\n" +
        "### Updates Experiences\n" +
        "- **Formatting Options :** New format blocks options\n" +
        "- **Translations :** Improved translations for French, Italian and Chinese\n" +
        "### Bugs Fix\n" +
        "- **Widget :** Fixing widget update on note change\n" +
        "- **Import :** Fixing issues with importing notes\n" +
        "- Even more little things which help you enjoy using this app everyday"

    val whatsNewView = dialog.findViewById<TextView>(R.id.whats_new_text)
    whatsNewView.setTextColor(theme().get(activity, ThemeColorType.TERTIARY_TEXT))
    whatsNewView.text = renderMarkdown(activity, whatsNew)

    val whatsNewIcon = dialog.findViewById<ImageView>(R.id.whats_new_icon)
    whatsNewIcon.background = CircleDrawable(Color.WHITE, false)

    val closeSheet = dialog.findViewById<ImageView>(R.id.close_sheet)
    closeSheet.setColorFilter(theme().get(activity, ThemeColorType.TOOLBAR_ICON))
    closeSheet.setOnClickListener { dismiss() }

    val translate = dialog.findViewById<TextView>(R.id.translate)
    translate.setTextColor(theme().get(activity, ThemeColorType.DISABLED_TEXT))
    translate.setOnClickListener {
      val url = GOOGLE_TRANSLATE_URL + "en/" + Uri.encode(whatsNew);
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
      dismiss()
    }

    val done = dialog.findViewById<TextView>(R.id.done)
    done.setTextColor(theme().get(activity, ThemeColorType.ACCENT_TEXT))
    done.setOnClickListener { dismiss() }

    return
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_whats_new

  override fun getBackgroundView(): Int = R.id.options_layout

  companion object {
    val WHATS_NEW_UID = 2
    val GOOGLE_TRANSLATE_URL = "https://translate.google.com/#auto/"

    fun openSheet(activity: ThemedActivity) {
      val sheet = WhatsNewItemsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun maybeOpenSheet(activity: MainActivity, dataStore: DataStore): Boolean {
      if (shouldShowWhatsNewSheet(activity, dataStore)) {
        openSheet(activity)
        return true
      }
      return false
    }
  }
}