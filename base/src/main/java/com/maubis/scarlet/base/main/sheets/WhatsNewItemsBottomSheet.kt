package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.CircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.utils.renderMarkdown
import com.maubis.scarlet.base.utils.shouldShowWhatsNewSheet

class WhatsNewItemsBottomSheet : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    setContent(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun setContent(dialog: Dialog) {
    val activity = themedActivity()

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    val whatsNew = "> A lot has changed in this update, here is a summary of those changes.\n" +
        "### New Features\n" +
        "- **History:** Adding undo / redo options for notes\n" +
        "- **Merge Notes:** Adding option to merge multiple notes into one\n" +
        "- **Search Tags:** Choose multiple tags to filter notes\n" +
        "- **Search Colors:** Choose colors to filter notes by color of the note\n" +
        "- **Sorting Mode:** Sort the notes in alphabetical order\n" +
        "- **Markdown Export:** Export in Markdown format, Optionally not export locked notes \n" +
        "- **Multiple Exports:** Export files now contain time, date information, allowing multiple backups\n" +
        "- **Note Background Color:** Set the note viewer background as the note color (Pro Only)\n" +
        "### Bugs Fix\n" +
        "- **Improved Search Privacy:** Search does not search locked notes\n" +
        "- Even more little things which help you enjoy using this app everyday"

    val whatsNewView = dialog.findViewById<TextView>(R.id.whats_new_text)
    whatsNewView.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
    whatsNewView.text = renderMarkdown(activity, whatsNew)

    val whatsNewIcon = dialog.findViewById<ImageView>(R.id.whats_new_icon)
    whatsNewIcon.background = CircleDrawable(Color.WHITE, false)

    val closeSheet = dialog.findViewById<ImageView>(R.id.close_sheet)
    closeSheet.setColorFilter(CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON))
    closeSheet.setOnClickListener { dismiss() }

    val translate = dialog.findViewById<TextView>(R.id.translate)
    translate.setOnClickListener {
      val url = GOOGLE_TRANSLATE_URL + "en/" + Uri.encode(whatsNew);
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
      dismiss()
    }

    val done = dialog.findViewById<TextView>(R.id.done)
    done.setOnClickListener { dismiss() }
    return
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_whats_new

  override fun getBackgroundView(): Int = R.id.options_layout

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.whats_new_card)

  companion object {
    val WHATS_NEW_UID = 6
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