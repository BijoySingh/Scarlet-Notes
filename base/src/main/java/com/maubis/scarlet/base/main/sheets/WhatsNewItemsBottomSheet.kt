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
        "- **New UI:** Introducing a new rounder and sweeter UI\n" +
        "- **Privacy:** Adding clear policy message, forget me option for logged in users on Scarlet \n" +
        "### New Features in Last Release\n" +
        "- **Search Bar:** All new search bar for notes. Search through tags, filter by tags, and improved searching experience\n" +
        "- **Reminders:** Adding ability to create reminders for notes \n" +
        "- **Import and Export :** Better, more useful and reliable import and export of notes\n" +
        "- **Scroll Performance :** Smoother scroll through notes, better handling of images\n" +
        "- **Distraction Free Mode :** New distraction free note viewing mode, so you can read your note comfortably\n" +
        "### Bugs Fix\n" +
        "- **Markdown in Feed :** Markdown in home cannot be disabled\n" +
        "- **Note Widget :** Fixed widget not getting added \n" +
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
    val WHATS_NEW_UID = 4
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