package com.maubis.scarlet.base.main.recycler

import android.content.Context
import android.content.Intent
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.export.sheet.BackupSettingsOptionsBottomSheet
import com.maubis.scarlet.base.export.support.NoteExporter
import com.maubis.scarlet.base.main.activity.INTENT_KEY_DIRECT_NOTES_TRANSFER
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.FlavorUtils
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

const val KEY_INFO_RATE_AND_REVIEW = "KEY_RATE_AND_REVIEW_INFO"
const val KEY_INFO_INSTALL_PRO_v2 = "KEY_INFO_INSTALL_PRO_v2"
const val KEY_INFO_SIGN_IN = "KEY_INFO_SIGN_IN"
const val KEY_FORCE_SHOW_SIGN_IN = "KEY_FORCE_SHOW_SIGN_IN"
const val KEY_THEME_OPTIONS = "KEY_THEME_OPTIONS"
const val KEY_BACKUP_OPTIONS = "KEY_BACKUP_OPTIONS"
const val KEY_MIGRATE_TO_PRO_SUCCESS = "KEY_MIGRATE_TO_PRO_SUCCESS"

const val KEY_INFO_INSTALL_PRO_MAX_COUNT = 10

class InformationRecyclerItem(val icon: Int, val title: Int, val source: Int, val function: () -> Unit) : RecyclerItem() {
  override val type = RecyclerItem.Type.INFORMATION
}

fun probability(probability: Float): Boolean = Random().nextFloat() <= probability

fun shouldShowAppUpdateInformationItem(): Boolean {
  return !ApplicationBase.instance.remoteConfigFetcher().isLatestVersion()
}

fun getAppUpdateInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_info,
    R.string.information_card_title,
    R.string.information_new_app_update
  ) { IntentUtils.openAppPlayStore(context) }
}

fun shouldShowReviewInformationItem(): Boolean {
  return probability(0.01f)
    && !sAppPreferences.get(KEY_INFO_RATE_AND_REVIEW, false)
}

fun getReviewInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_rating,
    R.string.home_option_rate_and_review,
    R.string.home_option_rate_and_review_subtitle
  ) {
    sAppPreferences.put(KEY_INFO_RATE_AND_REVIEW, true)
    IntentUtils.openAppPlayStore(context)
  }
}

fun shouldShowThemeInformationItem(): Boolean {
  return probability(0.01f)
    && !sAppPreferences.get(KEY_THEME_OPTIONS, false)
}

fun getThemeInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_action_grid,
    R.string.home_option_ui_experience,
    R.string.home_option_ui_experience_subtitle
  ) {
    sAppPreferences.put(KEY_THEME_OPTIONS, true)
    UISettingsOptionsBottomSheet.openSheet(activity)
  }
}

fun shouldShowBackupInformationItem(): Boolean {
  return probability(0.01f)
    && !sAppPreferences.get(KEY_BACKUP_OPTIONS, false)
}

fun getBackupInformationItem(activity: MainActivity): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_export,
    R.string.home_option_backup_options,
    R.string.home_option_backup_options_subtitle
  ) {
    sAppPreferences.put(KEY_BACKUP_OPTIONS, true)
    openSheet(activity, BackupSettingsOptionsBottomSheet())
  }
}

fun shouldShowInstallProInformationItem(): Boolean {
  return probability(0.01f)
    && sAppPreferences.get(KEY_INFO_INSTALL_PRO_v2, 0) < KEY_INFO_INSTALL_PRO_MAX_COUNT
    && !FlavorUtils.isPro()
}

fun getInstallProInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_favorite_white_48dp,
    R.string.install_pro_app,
    R.string.information_install_pro
  ) {
    notifyProUpsellShown()
    IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
  }
}

fun shouldShowSignInformationItem(context: Context): Boolean {
  if (ApplicationBase.instance.authenticator().isLoggedIn(context) || FlavorUtils.isOpenSource()) {
    return false
  }
  if (sAppPreferences.get(KEY_FORCE_SHOW_SIGN_IN, false)) {
    sAppPreferences.put(KEY_FORCE_SHOW_SIGN_IN, false)
    return true
  }
  return probability(0.01f)
    && !sAppPreferences.get(KEY_INFO_SIGN_IN, false)
}

fun getSignInInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_sign_in_options,
    R.string.home_option_login_with_app,
    R.string.home_option_login_with_app_subtitle
  ) {
    ApplicationBase.instance.authenticator().openLoginActivity(context)?.run()
    notifyProUpsellShown()
  }
}

fun notifyProUpsellShown() {
  val proUpsellCount = sAppPreferences.get(KEY_INFO_INSTALL_PRO_v2, 0)
  sAppPreferences.put(KEY_INFO_INSTALL_PRO_v2, proUpsellCount + 1)
}

fun shouldShowMigrateToProAppInformationItem(context: Context): Boolean {
  return FlavorUtils.isLite()
    && FlavorUtils.hasProAppInstalled(context)
    && !sAppPreferences.get(KEY_MIGRATE_TO_PRO_SUCCESS, false)
}

fun getMigrateToProAppInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
    R.drawable.ic_import,
    R.string.home_option_migrate_to_pro,
    R.string.home_option_migrate_to_pro_details
  ) {
    GlobalScope.launch(Dispatchers.Main) {
      val notes = GlobalScope.async(Dispatchers.IO) { NoteExporter().getExportContent() }
      val intent = Intent(Intent.ACTION_SEND)
        .putExtra(INTENT_KEY_DIRECT_NOTES_TRANSFER, notes.await())
        .setType("text/plain")
        .setPackage(FlavorUtils.PRO_APP_PACKAGE_NAME)
      try {
        context.startActivity(intent)
        sAppPreferences.put(KEY_MIGRATE_TO_PRO_SUCCESS, true)
      } catch (exception: Exception) {
        ToastHelper.show(context, "Failed transferring notes to Scarlet Pro")
        maybeThrow(exception)
      }
    }
  }
}