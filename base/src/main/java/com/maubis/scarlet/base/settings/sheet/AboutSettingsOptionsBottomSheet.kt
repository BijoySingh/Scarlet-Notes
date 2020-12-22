package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.main.sheets.WhatsNewBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.FlavorUtils
import com.maubis.scarlet.base.support.utils.maybeThrow

const val PRIVACY_POLICY_LINK = "https://www.iubenda.com/privacy-policy/8213521"

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
        openSheet(activity, AboutUsBottomSheet())
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_open_source_page,
      subtitle = R.string.home_option_open_source_page_subtitle,
      icon = R.drawable.ic_code_white_48dp,
      listener = {
        openSheet(activity, OpenSourceBottomSheet())
        dismiss()
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_faq_title,
      subtitle = R.string.home_option_faq_description,
      icon = R.drawable.icon_help,
      listener = {
        try {
          activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SettingsOptionsBottomSheet.GITHUB_FAQ_URL)))
          dismiss()
        } catch (exception: Exception) {
          maybeThrow(activity, exception)
        }
      }
    ))
    options.add(LithoOptionsItem(
      title = R.string.whats_new_title,
      subtitle = R.string.whats_new_subtitle,
      icon = R.drawable.ic_whats_new,
      listener = {
        openSheet(activity, WhatsNewBottomSheet())
        dismiss()
      }
    ))
    options.add(
      LithoOptionsItem(
        title = R.string.material_notes_privacy_policy,
        subtitle = R.string.material_notes_privacy_policy_subtitle,
        icon = R.drawable.ic_privacy_policy,
        listener = {
          activity.startActivity(
            Intent(
              Intent.ACTION_VIEW,
              Uri.parse(PRIVACY_POLICY_LINK)))
          dismiss()
        },
        visible = FlavorUtils.isPlayStore()

      ))
    options.add(LithoOptionsItem(
      title = R.string.internal_settings_title,
      subtitle = R.string.internal_settings_description,
      icon = R.drawable.icon_code_block,
      listener = {
        openSheet(activity, InternalSettingsOptionsBottomSheet())
        dismiss()
      }
    ))
    return options
  }
}