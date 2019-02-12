package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import com.facebook.litho.ComponentContext
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.sheets.WhatsNewItemsBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.utils.Flavor

class AboutSettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_about

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.home_option_about_page,
        subtitle = R.string.home_option_about_page_subtitle,
        icon = R.drawable.ic_info,
        listener = {
          AboutUsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_open_source_page,
        subtitle = R.string.home_option_open_source_page_subtitle,
        icon = R.drawable.ic_code_white_48dp,
        listener = {
          OpenSourceBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.material_notes_privacy_policy,
        subtitle = R.string.material_notes_privacy_policy_subtitle,
        icon = R.drawable.ic_privacy_policy,
        listener = {
          activity.startActivity(Intent(
              Intent.ACTION_VIEW,
              Uri.parse(PRIVACY_POLICY_LINK)))
          dismiss()
        },
        visible = CoreConfig.instance.appFlavor() != Flavor.NONE

    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_rate_and_review,
        subtitle = R.string.home_option_rate_and_review_subtitle,
        icon = R.drawable.ic_rating,
        listener = {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.whats_new_title,
        subtitle = R.string.whats_new_subtitle,
        icon = R.drawable.ic_whats_new,
        listener = {
          WhatsNewItemsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    return options
  }

  companion object {

    const val PRIVACY_POLICY_LINK = "https://www.iubenda.com/privacy-policy/8213521"

    fun openSheet(activity: MainActivity) {
      val sheet = AboutSettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}