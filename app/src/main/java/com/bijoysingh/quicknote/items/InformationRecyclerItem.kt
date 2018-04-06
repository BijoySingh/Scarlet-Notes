package com.bijoysingh.quicknote.items

import android.content.Context
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.util.IntentUtils
import java.util.*

const val KEY_INFO_RATE_AND_REVIEW = "KEY_RATE_AND_REVIEW_INFO"

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
      R.drawable.ic_info,
      R.string.home_option_rate_and_review,
      R.string.home_option_rate_and_review_subtitle,
      {
        userPreferences().put(KEY_INFO_RATE_AND_REVIEW, true)
        IntentUtils.openAppPlayStore(context)
      })
}