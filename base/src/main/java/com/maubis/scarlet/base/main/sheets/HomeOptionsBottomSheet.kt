package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.graphics.Typeface
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.tag.sheet.CreateOrEditTagBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.SearchConfig
import com.maubis.scarlet.base.support.sheets.*
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.ThemeColorType

class LithoTagOptionsItem(
    val tag: Tag,
    val usages: Int = 0,
    val isSelected: Boolean = false,
    val isEditable: Boolean = false,
    val editListener: () -> Unit = {},
    val listener: () -> Unit = {}) {
}

@LayoutSpec
object TagItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop option: LithoTagOptionsItem): Component {
    val theme = ApplicationBase.instance.themeController()
    val titleColor = theme.get(ThemeColorType.SECONDARY_TEXT)
    val selectedColor = when (theme.isNightTheme()) {
      true -> context.getColor(R.color.material_blue_400)
      false -> context.getColor(R.color.material_blue_700)
    }

    val icon: Int
    val bgColor: Int
    val bgAlpha: Int
    val textColor: Int
    val typeface: Typeface
    when (option.isSelected) {
      true -> {
        icon = R.drawable.ic_action_label
        bgColor = selectedColor
        bgAlpha = 200
        textColor = selectedColor
        typeface = CoreConfig.FONT_MONSERRAT_MEDIUM
      }
      false -> {
        icon = R.drawable.ic_action_label_unselected
        bgColor = titleColor
        bgAlpha = 15
        textColor = titleColor
        typeface = CoreConfig.FONT_MONSERRAT
      }
    }

    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .paddingDip(YogaEdge.VERTICAL, 12f)
        .child(
            RoundIcon.create(context)
                .iconRes(icon)
                .bgColor(bgColor)
                .iconColor(titleColor)
                .iconSizeRes(R.dimen.toolbar_round_icon_size)
                .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
                .bgAlpha(bgAlpha)
                .onClick { }
                .isClickDisabled(true)
                .marginDip(YogaEdge.END, 16f))
        .child(Text.create(context)
            .flexGrow(1f)
            .text(option.tag.title)
            .textSizeRes(R.dimen.font_size_normal)
            .typeface(typeface)
            .textStyle(Typeface.BOLD)
            .textColor(textColor))

    if (option.usages > 0) {
      row.child(Text.create(context)
          .text("${option.usages}")
          .textSizeRes(R.dimen.font_size_normal)
          .textColor(titleColor)
          .marginDip(YogaEdge.HORIZONTAL, 8f))
    }

    if (option.isEditable) {
      row.child(RoundIcon.create(context)
          .iconRes(R.drawable.ic_edit_white_48dp)
          .bgColor(titleColor)
          .bgAlpha(15)
          .iconAlpha(0.9f)
          .iconColor(titleColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .onClick { option.editListener() }
          .isClickDisabled(false)
          .marginDip(YogaEdge.START, 12f))
    }

    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop option: LithoTagOptionsItem) {
    option.listener()
  }
}


class HomeOptionsBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val options = getOptions()
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .child(Column.create(componentContext)
            .paddingDip(YogaEdge.TOP, 20f)
            .paddingDip(YogaEdge.BOTTOM, 20f)
            .paddingDip(YogaEdge.HORIZONTAL, 20f)
            .child(
                Row.create(componentContext)
                    .child(OptionLabelItemLayout.create(componentContext).option(options[0]).onClick { options[0].listener() })
                    .child(OptionLabelItemLayout.create(componentContext).option(options[1]).onClick { options[1].listener() })
                    .child(OptionLabelItemLayout.create(componentContext).option(options[2]).onClick { options[2].listener() })
            )
            .child(
                Row.create(componentContext)
                    .child(OptionLabelItemLayout.create(componentContext).option(options[3]).onClick { options[3].listener() })
                    .child(OptionLabelItemLayout.create(componentContext).option(options[4]).onClick { options[4].listener() })
                    .child(OptionLabelItemLayout.create(componentContext).option(options[5]).onClick { options[5].listener() })
            ))

    val tagsComponent = Column.create(componentContext)
        .paddingDip(YogaEdge.TOP, 8f)
        .paddingDip(YogaEdge.BOTTOM, 20f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .backgroundRes(R.color.dark_hint_text)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.tag_sheet_choose_tag)
            .marginDip(YogaEdge.BOTTOM, 12f))
    getTagOptions().forEach {
      tagsComponent.child(TagItemLayout.create(componentContext).option(it))
    }

    val addTag = LithoOptionsItem(
        title = R.string.tag_sheet_new_tag_button,
        subtitle = 0,
        icon = R.drawable.icon_add_note,
        listener = { CreateOrEditTagBottomSheet.openSheet(activity, TagBuilder().emptyTag()) { _, _ -> reset(activity, dialog) } })
    tagsComponent.child(OptionItemLayout.create(componentContext).option(addTag).onClick { addTag.listener() })

    component.child(tagsComponent)
    return component.build()
  }

  override fun bottomMargin(): Float = 0f

  private fun getOptions(): List<LithoLabelOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoLabelOptionsItem>()
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_home,
        icon = R.drawable.ic_home_white_48dp,
        listener = {
          activity.onHomeClick()
          dismiss()
        }
    ))
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_favourites,
        icon = R.drawable.ic_favorite_white_48dp,
        listener = {
          activity.onFavouritesClick();
          dismiss();
        }
    ))
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_archived,
        icon = R.drawable.ic_archive_white_48dp,
        listener = {
          activity.onArchivedClick();
          dismiss();
        }
    ))
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_locked,
        icon = R.drawable.ic_action_lock,
        listener = {
          activity.onLockedClick();
          dismiss();
        }
    ))
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_trash,
        icon = R.drawable.ic_delete_white_48dp,
        listener = {
          activity.onTrashClick();
          dismiss();
        }
    ))
    options.add(LithoLabelOptionsItem(
        title = R.string.nav_settings,
        icon = R.drawable.ic_action_settings,
        listener = {
          SettingsOptionsBottomSheet.openSheet(activity)
          dismiss();
        }
    ))
    return options
  }

  private fun getTagOptions(): List<LithoTagOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoTagOptionsItem>()
    for (tag in CoreConfig.tagsDb.getAll()) {
      options.add(LithoTagOptionsItem(
          tag = tag,
          usages = CoreConfig.notesDb.getNoteCountByTag(tag.uuid),
          listener = {
            activity.config = SearchConfig(mode = HomeNavigationState.DEFAULT)
            activity.openTag(tag)
            dismiss()
          },
          isEditable = true,
          isSelected = false,
          editListener = {
            CreateOrEditTagBottomSheet.openSheet(activity, tag) { _, _ -> reset(activity, dialog) }
          }
      ))
    }
    options.sortByDescending { it.usages }
    return options
  }
}