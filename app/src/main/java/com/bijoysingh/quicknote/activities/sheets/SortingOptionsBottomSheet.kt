package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.items.OptionsItem
import com.maubis.scarlet.base.note.SortingTechnique

class SortingOptionsBottomSheet : ChooseOptionBottomSheetBase() {

  var listener: () -> Unit = {}

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.sort_sheet_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val sorting = getSortingState()
    val options = ArrayList<OptionsItem>()

    val getIcon = fun(sortingTechnique: SortingTechnique): Int = if (sorting == sortingTechnique) R.drawable.ic_done_white_48dp else 0

    options.add(OptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.LAST_MODIFIED),
        subtitle = getSortingTechniqueLabel(SortingTechnique.LAST_MODIFIED),
        icon = getIcon(SortingTechnique.LAST_MODIFIED),
        listener = View.OnClickListener {
          setSortingState(SortingTechnique.LAST_MODIFIED)
          listener()
          reset(dialog)
        },
        selected = sorting == SortingTechnique.LAST_MODIFIED
    ))
    options.add(OptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.NEWEST_FIRST),
        subtitle = getSortingTechniqueLabel(SortingTechnique.NEWEST_FIRST),
        icon = getIcon(SortingTechnique.NEWEST_FIRST),
        listener = View.OnClickListener {
          setSortingState(SortingTechnique.NEWEST_FIRST)
          listener()
          reset(dialog)
        },
        selected = sorting == SortingTechnique.NEWEST_FIRST
    ))
    options.add(OptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.OLDEST_FIRST),
        subtitle = getSortingTechniqueLabel(SortingTechnique.OLDEST_FIRST),
        icon = getIcon(SortingTechnique.OLDEST_FIRST),
        listener = View.OnClickListener {
          setSortingState(SortingTechnique.OLDEST_FIRST)
          listener()
          reset(dialog)
        },
        selected = sorting == SortingTechnique.OLDEST_FIRST
    ))
    return options
  }

  companion object {

    const val KEY_SORTING_TECHNIQUE = "KEY_SORTING_TECHNIQUE"

    fun getSortingState(): SortingTechnique {
      return SortingTechnique.values()[userPreferences().get(KEY_SORTING_TECHNIQUE, SortingTechnique.NEWEST_FIRST.ordinal)]
    }

    fun getSortingTechniqueLabel(technique: SortingTechnique): Int {
      return when (technique) {
        SortingTechnique.LAST_MODIFIED -> R.string.sort_sheet_last_modified
        SortingTechnique.NEWEST_FIRST -> R.string.sort_sheet_newest_first
        SortingTechnique.OLDEST_FIRST -> R.string.sort_sheet_oldest_first
      }
    }

    fun setSortingState(sortingTechnique: SortingTechnique) {
      userPreferences().put(KEY_SORTING_TECHNIQUE, sortingTechnique.ordinal)
    }

    fun openSheet(activity: MainActivity, listener: () -> Unit) {
      val sheet = SortingOptionsBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}