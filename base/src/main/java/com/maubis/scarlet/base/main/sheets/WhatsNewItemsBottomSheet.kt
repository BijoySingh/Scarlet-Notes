package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.CircleDrawable
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.support.utils.shouldShowWhatsNewSheet

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
        "- **New UI and Icon:** New Search and Top Actionbar UI and icon\n" +
        "- **Widgets:** Added a new list of notes widget. Also fixed widget not updating bug.\n" +
        "- **Reminder:** Improved reminders to be more reliable.\n" +
        "### Last Release\n" +
        "- **Notebooks:** Adding the ability to add or remove notebooks. You can add multiple notes to the notebook.\n" +
        "- **Privacy:** You can now choose to disable online-backup of some notes. Choose 'Disable Backup' on the note.\n" +
        "- **Undo Deletion:** Adding the option to Undo a deletion after a note is moved to trash.\n" +
        "- **Better Search:** Search now retains information across clicks. You can filter with folders and more.\n" +
        "- **Better Select Options:** You can now perform many more group actions on Note. Choose 'Select Notes' option on the note.\n" +
        "- Even more little things which help you enjoy using this app everyday"

    val whatsNewView = dialog.findViewById<TextView>(R.id.whats_new_text)
    whatsNewView.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
    whatsNewView.text = Markdown.render(whatsNew)

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
    val WHATS_NEW_UID = 8
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