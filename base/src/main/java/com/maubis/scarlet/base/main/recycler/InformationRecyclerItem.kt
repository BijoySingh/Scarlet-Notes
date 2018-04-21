package com.maubis.scarlet.base.main.recycler

import android.content.Context
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import java.util.*

const val KEY_INFO_RATE_AND_REVIEW = "KEY_RATE_AND_REVIEW_INFO"
const val KEY_INFO_INSTALL_PRO = "KEY_INFO_INSTALL_PRO"

class InformationRecyclerItem(val icon: Int, val title: Int, val source: Int, val function: () -> Unit) : RecyclerItem() {
  override val type = RecyclerItem.Type.INFORMATION
}

fun probability(probability: Float): Boolean = Random().nextFloat() <= probability

fun getAppUpdateInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_info,
      R.string.information_card_title,
      R.string.information_new_app_update,
      { IntentUtils.openAppPlayStore(context) })
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

fun getInstallProInformationItem(context: Context): InformationRecyclerItem {
  return InformationRecyclerItem(
      R.drawable.ic_favorite_white_48dp,
      R.string.install_pro_app,
      R.string.information_install_pro,
      {
        CoreConfig.instance.store().put(KEY_INFO_INSTALL_PRO, true)
        IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      })
}