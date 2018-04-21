package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.sheet.BackupSettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemBottomSheetBase

class SettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<OptionsItem>> {
      override fun run(): List<OptionsItem> {
        return getOptions()
      }

      override fun handle(result: List<OptionsItem>) {
        setOptions(dialog, result)
      }
    })
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()

    val loginClick = CoreConfig.instance.authenticator().openLoginActivity(activity)
    val firebaseUser = CoreConfig.instance.authenticator().userId()
    options.add(OptionsItem(
        title = R.string.home_option_login_with_app,
        subtitle = R.string.home_option_login_with_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = View.OnClickListener {
          loginClick?.run()
          dismiss()
        },
        visible = loginClick !== null && firebaseUser === null
    ))
    options.add(OptionsItem(
        title = R.string.home_option_ui_experience,
        subtitle = R.string.home_option_ui_experience_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          UISettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_note_settings,
        subtitle = R.string.home_option_note_settings_subtitle,
        icon = R.drawable.ic_subject_white_48dp,
        listener = View.OnClickListener {
          NoteSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_backup_options,
        subtitle = R.string.home_option_backup_options_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          BackupSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_about,
        subtitle = R.string.home_option_about_subtitle,
        icon = R.drawable.ic_info,
        listener = View.OnClickListener {
          AboutSettingsOptionsBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_rate_and_review,
        subtitle = R.string.home_option_rate_and_review_subtitle,
        icon = R.drawable.ic_rating,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_logout_of_app,
        subtitle = R.string.home_option_logout_of_app_subtitle,
        icon = R.drawable.ic_sign_in_options,
        listener = View.OnClickListener {
          CoreConfig.instance.authenticator().logout()
          dismiss()
        },
        visible = firebaseUser !== null
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {

    const val KEY_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"
    const val KEY_MARKDOWN_HOME_ENABLED = "KEY_MARKDOWN_HOME_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}