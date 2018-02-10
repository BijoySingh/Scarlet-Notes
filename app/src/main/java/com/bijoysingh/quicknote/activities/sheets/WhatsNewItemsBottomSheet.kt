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
        "- **Markdown Toolbar :** Write markdown directly onto the editor with bold, italic and underline buttons etc.\n" +
        "- **Select Multiple Notes :** Taking actions on multiple notes is easier. Just choose 'Select' for the note, and you can act on more notes \n" +
        "- **Launcher Actions :** For Android Nougat and newer, the launcher icon can be long pressed for shortcuts\n" +
        "- **Google Keep Import :** Importing lists from Google Keep works smoothly and creates a checklist in the app\n" +
        "### New User Experience\n" +
        "- **Action Buttons :** Now it's easier to do actions. With easy to reach floating buttons to help with all the essential app, and note functions\n" +
        "- **Notification Actions :** Notifications have better and more actions to take actions quickly\n" +
        "- **Tags in Home Menu :** The tags are now easier to find in the home menu\n" +
        "- **Checked Items :** Checked items now become dull and move down in a checklist\n" +
        "- **Tablet Support :** Much better tablet support with a consistent bottom sheet experience\n" +
        "### Focusing on the little things\n" +
        "- **Tag Count :** Tags show the number of notes which have that tag, and are sorted by that\n" +
        "- **Indicator Icon :** Notes show a small indicator which help know favourite, archived notes\n" +
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
    val WHATS_NEW_UID = 1
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