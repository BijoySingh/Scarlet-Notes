package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.note.SortingTechnique
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoChooseOptionsItem

class SortingOptionsBottomSheet : LithoChooseOptionBottomSheet() {
  var listener: () -> Unit = {}

  override fun title(): Int =  R.string.sort_sheet_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem> {
    val sorting = getSortingState()
    val options = ArrayList<LithoChooseOptionsItem>()

    options.add(LithoChooseOptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.LAST_MODIFIED),
        listener = {
          setSortingState(SortingTechnique.LAST_MODIFIED)
          listener()
          reset(componentContext.androidContext, dialog)
        },
        selected = sorting == SortingTechnique.LAST_MODIFIED
    ))
    options.add(LithoChooseOptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.NEWEST_FIRST),
        listener = {
          setSortingState(SortingTechnique.NEWEST_FIRST)
          listener()
          reset(componentContext.androidContext, dialog)
        },
        selected = sorting == SortingTechnique.NEWEST_FIRST
    ))
    options.add(LithoChooseOptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.OLDEST_FIRST),
        listener = {
          setSortingState(SortingTechnique.OLDEST_FIRST)
          listener()
          reset(componentContext.androidContext, dialog)
        },
        selected = sorting == SortingTechnique.OLDEST_FIRST
    ))
    options.add(LithoChooseOptionsItem(
        title = getSortingTechniqueLabel(SortingTechnique.ALPHABETICAL),
        listener = {
          setSortingState(SortingTechnique.ALPHABETICAL)
          listener()
          reset(componentContext.androidContext, dialog)
        },
        selected = sorting == SortingTechnique.ALPHABETICAL
    ))
    return options
  }

  companion object {

    const val KEY_SORTING_TECHNIQUE = "KEY_SORTING_TECHNIQUE"

    fun getSortingState(): SortingTechnique {
      return SortingTechnique.values()[CoreConfig.instance.store().get(KEY_SORTING_TECHNIQUE, SortingTechnique.NEWEST_FIRST.ordinal)]
    }

    fun getSortingTechniqueLabel(technique: SortingTechnique): Int {
      return when (technique) {
        SortingTechnique.LAST_MODIFIED -> R.string.sort_sheet_last_modified
        SortingTechnique.NEWEST_FIRST -> R.string.sort_sheet_newest_first
        SortingTechnique.OLDEST_FIRST -> R.string.sort_sheet_oldest_first
        SortingTechnique.ALPHABETICAL -> R.string.sort_sheet_alphabetical
      }
    }

    fun setSortingState(sortingTechnique: SortingTechnique) {
      CoreConfig.instance.store().put(KEY_SORTING_TECHNIQUE, sortingTechnique.ordinal)
    }

    fun openSheet(activity: MainActivity, listener: () -> Unit) {
      val sheet = SortingOptionsBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}