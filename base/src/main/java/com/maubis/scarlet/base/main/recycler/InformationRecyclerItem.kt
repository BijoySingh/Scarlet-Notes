package com.maubis.scarlet.base.main.recycler

import android.content.Context
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.sheet.BackupSettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.utils.Flavor
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import java.util.*

const val KEY_INFO_RATE_AND_REVIEW = "KEY_RATE_AND_REVIEW_INFO"
const val KEY_INFO_INSTALL_PRO_v2 = "KEY_INFO_INSTALL_PRO_v2"
const val KEY_INFO_SIGN_IN = "KEY_INFO_SIGN_IN"
const val KEY_FORCE_SHOW_SIGN_IN = "KEY_FORCE_SHOW_SIGN_IN"
const val KEY_THEME_OPTIONS = "KEY_THEME_OPTIONS"
const val KEY_BACKUP_OPTIONS = "KEY_BACKUP_OPTIONS"

const val KEY_INFO_INSTALL_PRO_MAX_COUNT = 10

class InformationRecyclerItem(val icon: Int, val title: Int, val source: Int, val function: () -> Unit) : RecyclerItem() {
  override val type = RecyclerItem.Type.INFORMATION
}

fun probability(probability: Float): Boolean = Random().nextFloat() <= probability

fun shouldShowAppUpdateInformationItem(): Boolean {
  return !CoreConfig.instance.remoteConfigFetcher().isLatestVersion()
}

fun getAppUpdateInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_info,
      R.string.information_card_title,
      R.string.information_new_app_update,
      { IntentUtils.openAppPlayStore(context) })
}

fun shouldShowReviewInformationItem(): Boolean {
  return probability(0.01f)
      && !CoreConfig.instance.store().get(KEY_INFO_RATE_AND_REVIEW, false)
}

fun getReviewInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_rating,
      R.string.home_option_rate_and_review,
      R.string.home_option_rate_and_review_subtitle,
      {
        CoreConfig.instance.store().put(KEY_INFO_RATE_AND_REVIEW, true)
        IntentUtils.openAppPlayStore(context)
      })
}

fun shouldShowThemeInformationItem(): Boolean {
  return probability(0.01f)
      && !CoreConfig.instance.store().get(KEY_THEME_OPTIONS, false)
}

fun getThemeInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_action_grid,
      R.string.home_option_ui_experience,
      R.string.home_option_ui_experience_subtitle,
      {
        CoreConfig.instance.store().put(KEY_THEME_OPTIONS, true)
        UISettingsOptionsBottomSheet.openSheet(activity)
      })
}

fun shouldShowBackupInformationItem(): Boolean {
  return probability(0.01f)
      && !CoreConfig.instance.store().get(KEY_BACKUP_OPTIONS, false)
}

fun getBackupInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_export,
      R.string.home_option_backup_options,
      R.string.home_option_backup_options_subtitle,
      {
        CoreConfig.instance.store().put(KEY_BACKUP_OPTIONS, true)
        BackupSettingsOptionsBottomSheet.openSheet(activity)
      })
}


fun shouldShowInstallProInformationItem(): Boolean {
  return probability(0.01f)
      && CoreConfig.instance.store().get(KEY_INFO_INSTALL_PRO_v2, 0) < KEY_INFO_INSTALL_PRO_MAX_COUNT
      && CoreConfig.instance.appFlavor() != Flavor.PRO
}

fun getInstallProInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_favorite_white_48dp,
      R.string.install_pro_app,
      R.string.information_install_pro,
      {
        notifyProUpsellShown()
        IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      })
}

fun shouldShowSignInformationItem(): Boolean {
  if (CoreConfig.instance.authenticator().isLoggedIn()
      || CoreConfig.instance.appFlavor() == Flavor.NONE) {
    return false
  }
  if (CoreConfig.instance.store().get(KEY_FORCE_SHOW_SIGN_IN, false)) {
    CoreConfig.instance.store().put(KEY_FORCE_SHOW_SIGN_IN, false)
    return true
  }
  return probability(0.01f)
      && !CoreConfig.instance.store().get(KEY_INFO_SIGN_IN, false)
}

fun getSignInInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_sign_in_options,
      R.string.home_option_login_with_app,
      R.string.home_option_login_with_app_subtitle,
      {
        CoreConfig.instance.authenticator().openLoginActivity(context)?.run()
        notifyProUpsellShown()
      })
}

fun notifyProUpsellShown() {
  val proUpsellCount = CoreConfig.instance.store().get(KEY_INFO_INSTALL_PRO_v2, 0)
  CoreConfig.instance.store().put(KEY_INFO_INSTALL_PRO_v2, proUpsellCount + 1)
}
