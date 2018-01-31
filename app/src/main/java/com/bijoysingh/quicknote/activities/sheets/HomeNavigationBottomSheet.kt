package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.items.TagOptionsItem
import com.bijoysingh.quicknote.utils.HomeNavigationState
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.uibasics.views.UIActionView
import com.github.bijoysingh.uibasics.views.UITextView

class HomeNavigationBottomSheet : GridBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    Handler().postDelayed({
      resetOptions(dialog)
      resetTags(dialog)
      setAddTagOption(dialog)
    }, 500)
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
        title = R.string.nav_tags,
        subtitle = R.string.nav_tags_details,
        icon = R.drawable.ic_action_tags,
        selected = activity.mode == HomeNavigationState.TAG,
        listener = View.OnClickListener {
          TagOpenOptionsBottomSheet.openSheet(activity)
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
        titleView.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))

        val separator = dialog.findViewById<View>(R.id.separator)
        separator.setBackgroundColor(theme().get(themedContext(), ThemeColorType.HINT_TEXT))

        setOptions(dialog, result)
      }
    })
  }

  fun resetTags(dialog: Dialog) {
    MultiAsyncTask.execute(themedActivity(), object : MultiAsyncTask.Task<List<TagOptionsItem>> {
      override fun run(): List<TagOptionsItem> = getTagOptions()
      override fun handle(result: List<TagOptionsItem>) {
        val titleView = dialog.findViewById<TextView>(R.id.tag_options_title)
        titleView.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))

        val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
        layout.removeAllViews()
        setTagOptions(dialog, result)
      }
    })
  }

  fun setTagOptions(dialog: Dialog, options: List<TagOptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIActionView
      contentView.setTitle(option.tag.title)
      contentView.setOnClickListener(option.listener)
      contentView.subtitle.visibility = View.GONE
      contentView.setImageResource(option.getIcon())

      contentView.setActionResource(option.getEditIcon());
      contentView.setActionTint(theme().get(themedContext(), ThemeColorType.HINT_TEXT));
      contentView.setActionClickListener(option.editListener)

      contentView.setTitleColor(getOptionsTitleColor(option.selected))
      contentView.setSubtitleColor(getOptionsSubtitleColor(option.selected))
      contentView.setImageTint(getOptionsTitleColor(option.selected))

      layout.addView(contentView)
    }
  }

  fun getTagOptions(): List<TagOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<TagOptionsItem>()
    for (tag in Tag.db(context).all) {
      options.add(TagOptionsItem(
          tag = tag,
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
    newTagButton.setTextColor(theme().get(themedContext(), ThemeColorType.HINT_TEXT))
    newTagButton.setImageTint(theme().get(themedContext(), ThemeColorType.HINT_TEXT))
    newTagButton.setOnClickListener { onNewTagClick() }
    newTagButton.icon.alpha = 0.6f
  }

  fun onNewTagClick() {
    val activity = context as MainActivity
    CreateOrEditTagBottomSheet.openSheet(activity, Tag.gen(), { _, _ -> resetTags(dialog) })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_home_navigation

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = HomeNavigationBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}