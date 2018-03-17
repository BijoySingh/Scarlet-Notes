package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.NotesDB
import com.bijoysingh.quicknote.database.utils.TagsDB
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.items.TagOptionsItem
import com.bijoysingh.quicknote.utils.HomeNavigationState
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.genEmptyTag
import com.bijoysingh.quicknote.views.HomeTagView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.LocaleManager
import com.github.bijoysingh.uibasics.views.UIActionView
import com.github.bijoysingh.uibasics.views.UITextView

class HomeNavigationBottomSheet : GridBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    resetOptions(dialog)
    resetTags(dialog)
    setAddTagOption(dialog)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.nav_home,
        subtitle = R.string.nav_home_details,
        icon = R.drawable.ic_home_white_48dp,
        selected = activity.mode == HomeNavigationState.DEFAULT,
        listener = View.OnClickListener {
          activity.onHomeClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_favourites,
        subtitle = R.string.nav_favourites_details,
        icon = R.drawable.ic_favorite_white_48dp,
        selected = activity.mode == HomeNavigationState.FAVOURITE,
        listener = View.OnClickListener {
          activity.onFavouritesClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_archived,
        subtitle = R.string.nav_archived_details,
        icon = R.drawable.ic_archive_white_48dp,
        selected = activity.mode == HomeNavigationState.ARCHIVED,
        listener = View.OnClickListener {
          activity.onArchivedClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_locked,
        subtitle = R.string.nav_locked_details,
        icon = R.drawable.ic_action_lock,
        selected = activity.mode == HomeNavigationState.LOCKED,
        listener = View.OnClickListener {
          activity.onLockedClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_trash,
        subtitle = R.string.nav_trash_details,
        icon = R.drawable.ic_delete_white_48dp,
        selected = activity.mode == HomeNavigationState.TRASH,
        listener = View.OnClickListener {
          activity.onTrashClick();
          dismiss();
        }
    ))
    options.add(OptionsItem(
        title = R.string.nav_settings,
        subtitle = R.string.nav_settings,
        icon = R.drawable.ic_action_settings,
        listener = View.OnClickListener {
          SettingsOptionsBottomSheet.openSheet(activity)
          dismiss();
        }
    ))
    return options
  }

  fun resetOptions(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<OptionsItem>> {
      override fun run(): List<OptionsItem> = getOptions()
      override fun handle(result: List<OptionsItem>) {
        val titleView = dialog.findViewById<TextView>(R.id.options_title)
        titleView.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))

        val separator = dialog.findViewById<View>(R.id.separator)
        separator.setBackgroundColor(appTheme().get(ThemeColorType.HINT_TEXT))

        setOptions(dialog, result)
      }
    })
  }

  fun resetTags(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<TagOptionsItem>> {
      override fun run(): List<TagOptionsItem> = getTagOptions()
      override fun handle(result: List<TagOptionsItem>) {
        val titleView = dialog.findViewById<TextView>(R.id.tag_options_title)
        titleView.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))

        val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
        layout.removeAllViews()
        setTagOptions(dialog, result)
      }
    })
  }

  fun setTagOptions(dialog: Dialog, options: List<TagOptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options.sorted()) {
      val contentView = HomeTagView(View.inflate(context, R.layout.layout_home_tag_item, null))
      contentView.title.setText(option.tag.title)
      contentView.rootView.setOnClickListener(option.listener)
      contentView.subtitle.visibility = View.GONE
      contentView.icon.setImageResource(option.getIcon())

      contentView.action.setImageResource(option.getEditIcon());
      contentView.action.setColorFilter(appTheme().get(ThemeColorType.HINT_TEXT));
      contentView.action.setOnClickListener(option.editListener)

      if (option.usages > 0) {
        contentView.subtitle.setText(LocaleManager.toString(option.usages))
        contentView.subtitle.visibility = View.VISIBLE
      }

      contentView.title.setTextColor(getOptionsTitleColor(option.selected))
      contentView.subtitle.setTextColor(getOptionsSubtitleColor(option.selected))
      contentView.icon.setColorFilter(getOptionsTitleColor(option.selected))

      layout.addView(contentView.rootView)
    }
  }

  fun getTagOptions(): List<TagOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<TagOptionsItem>()
    for (tag in TagsDB.db.getAll()) {
      options.add(TagOptionsItem(
          tag = tag,
          usages = NotesDB.db.getNoteCountByTag(tag.uuid),
          listener = View.OnClickListener {
            activity.openTag(tag)
            dismiss()
          },
          editable = true,
          editListener = View.OnClickListener {
            CreateOrEditTagBottomSheet.openSheet(activity, tag, { _, _ -> resetTags(dialog) })
          }
      ))
    }
    return options
  }

  fun setAddTagOption(dialog: Dialog) {
    val newTagButton = dialog.findViewById<UITextView>(R.id.new_tag_button);
    newTagButton.setTextColor(appTheme().get(ThemeColorType.HINT_TEXT))
    newTagButton.setImageTint(appTheme().get(ThemeColorType.HINT_TEXT))
    newTagButton.setOnClickListener { onNewTagClick() }
    newTagButton.icon.alpha = 0.6f
  }

  fun onNewTagClick() {
    val activity = context as MainActivity
    CreateOrEditTagBottomSheet.openSheet(activity, genEmptyTag(), { _, _ -> resetTags(dialog) })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_home_navigation

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = HomeNavigationBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}