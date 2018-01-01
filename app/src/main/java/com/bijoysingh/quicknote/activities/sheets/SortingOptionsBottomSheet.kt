package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Context
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.prefs.DataStore

class SortingOptionsBottomSheet : ChooseOptionBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.sort_sheet_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val sorting = getSortingState(context)
    val options = ArrayList<OptionsItem>()

    val getIcon = fun (sortingTechnique: SortingTechnique): Int
        = if (sorting == sortingTechnique) R.drawable.ic_done_white_48dp else 0

    options.add(OptionsItem(
        title = SortingTechnique.LAST_MODIFIED.label,
        subtitle = SortingTechnique.LAST_MODIFIED.label,
        icon = getIcon(SortingTechnique.LAST_MODIFIED),
        listener = View.OnClickListener {
          setSortingState(context, SortingTechnique.LAST_MODIFIED)
          reset(dialog)
        },
        selected = sorting == SortingTechnique.LAST_MODIFIED
    ))
    options.add(OptionsItem(
        title = SortingTechnique.NEWEST_FIRST.label,
        subtitle = SortingTechnique.NEWEST_FIRST.label,
        icon = getIcon(SortingTechnique.NEWEST_FIRST),
        listener = View.OnClickListener {
          setSortingState(context, SortingTechnique.NEWEST_FIRST)
          reset(dialog)
        },
        selected = sorting == SortingTechnique.NEWEST_FIRST
    ))
    options.add(OptionsItem(
        title = SortingTechnique.OLDEST_FIRST.label,
        subtitle = SortingTechnique.OLDEST_FIRST.label,
        icon = getIcon(SortingTechnique.OLDEST_FIRST),
        listener = View.OnClickListener {
          setSortingState(context, SortingTechnique.OLDEST_FIRST)
          reset(dialog)
        },
        selected = sorting == SortingTechnique.OLDEST_FIRST
    ))
    return options
  }

  companion object {

    const val KEY_SORTING_TECHNIQUE = "KEY_SORTING_TECHNIQUE"

    fun getSortingState(context: Context): SortingTechnique {
      val dataStore = DataStore.get(context)
      return SortingTechnique.values()[dataStore.get(KEY_SORTING_TECHNIQUE, SortingTechnique.LAST_MODIFIED.ordinal)]
    }

    fun setSortingState(context: Context, sortingTechnique: SortingTechnique) {
      val dataStore = DataStore.get(context)
      dataStore.put(KEY_SORTING_TECHNIQUE, sortingTechnique.ordinal)
    }

    fun openSheet(activity: MainActivity) {
      val sheet = SortingOptionsBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}

enum class SortingTechnique(val label: Int) {
  LAST_MODIFIED(R.string.sort_sheet_last_modified),
  NEWEST_FIRST(R.string.sort_sheet_newest_first),
  OLDEST_FIRST(R.string.sort_sheet_oldest_first),
}