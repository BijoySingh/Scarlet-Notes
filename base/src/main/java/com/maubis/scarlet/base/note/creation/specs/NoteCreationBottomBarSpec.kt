package com.maubis.scarlet.base.note.creation.specs

import android.graphics.Color
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.EmptyComponent
import com.facebook.litho.widget.HorizontalScroll
import com.facebook.litho.widget.TransparencyEnabledCard
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.ThemeColorType

fun bottomBarRoundIcon(context: ComponentContext,
                       iconColor: Int = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)): RoundIcon.Builder {
  return RoundIcon.create(context)
      .bgColor(iconColor)
      .iconColor(iconColor)
      .iconSizeRes(R.dimen.toolbar_round_icon_size)
      .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
      .iconMarginVerticalRes(R.dimen.toolbar_round_icon_margin_vertical)
      .iconMarginHorizontalRes(R.dimen.toolbar_round_icon_margin_horizontal)
      .bgAlpha(15)
}

fun bottomBarCard(context: ComponentContext,
                  child: Component,
                  toolbarColor: Int = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_BACKGROUND)): Column.Builder {
  return Column.create(context)
      .widthPercent(100f)
      .paddingDip(YogaEdge.ALL, 2f)
      .backgroundColor(Color.TRANSPARENT)
      .child(
          TransparencyEnabledCard.create(context)
              .widthPercent(100f)
              .backgroundColor(Color.TRANSPARENT)
              .cardBackgroundColor(toolbarColor)
              .cornerRadiusDip(4f)
              .elevationDip(2f)
              .content(child))
}

enum class NoteCreateBottomBarType {
  DEFAULT_SEGMENTS,
  DEFAULT_MARKDOWNS,
  ALL_SEGMENTS,
  ALL_MARKDOWNS,
  OPTIONS,
}

@LayoutSpec
object NoteCreationBottomBarSpec {

  @OnCreateInitialState
  fun onCreateInitialState(
      context: ComponentContext,
      state: StateValue<NoteCreateBottomBarType>) {
    state.set(NoteCreateBottomBarType.DEFAULT_SEGMENTS)
  }

  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int,
               @Prop(resType = ResType.COLOR) toolbarColor: Int,
               @State state: NoteCreateBottomBarType): Component {
    val row = Row.create(context)
        .widthPercent(100f)
        .alignItems(YogaAlign.CENTER)

    val content = when (state) {
      NoteCreateBottomBarType.DEFAULT_SEGMENTS ->
        NoteCreationSegmentsBottomBar.create(context)
            .iconColor(iconColor)
            .flexGrow(1f)
            .toggleButtonClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.ALL_SEGMENTS))
      NoteCreateBottomBarType.DEFAULT_MARKDOWNS -> NoteCreationMarkdownsBottomBar.create(context)
          .iconColor(iconColor)
          .flexGrow(1f)
          .toggleButtonClick(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.ALL_MARKDOWNS))
      NoteCreateBottomBarType.ALL_SEGMENTS -> bottomBarRoundIcon(context, iconColor)
          .iconRes(R.drawable.ic_close_white_48dp)
          .onClick { }
          .isClickDisabled(true)
          .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_SEGMENTS))
      NoteCreateBottomBarType.ALL_MARKDOWNS -> bottomBarRoundIcon(context, iconColor)
          .iconRes(R.drawable.ic_close_white_48dp)
          .onClick { }
          .isClickDisabled(true)
          .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_MARKDOWNS))
      NoteCreateBottomBarType.OPTIONS ->
        NoteCreationOptionsBottomBar.create(context)
            .iconColor(iconColor)
            .flexGrow(1f)
    }
    row.child(content)

    val icon = when (state) {
      NoteCreateBottomBarType.DEFAULT_SEGMENTS, NoteCreateBottomBarType.ALL_SEGMENTS -> bottomBarRoundIcon(context, iconColor)
          .iconRes(R.drawable.ic_markdown_logo)
          .marginDip(YogaEdge.START, 8f)
          .onClick { }
          .isClickDisabled(true)
          .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_MARKDOWNS))
      NoteCreateBottomBarType.OPTIONS -> EmptyComponent.create(context)
      else -> bottomBarRoundIcon(context, iconColor)
          .iconRes(R.drawable.ic_formats_logo)
          .marginDip(YogaEdge.START, 8f)
          .onClick { }
          .isClickDisabled(true)
          .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.DEFAULT_SEGMENTS))
    }
    row.child(icon)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_more_options)
            .onClick { }
            .isClickDisabled(true)
            .clickHandler(NoteCreationBottomBar.onStateChangeClick(context, NoteCreateBottomBarType.OPTIONS)))

    val column = Column.create(context).widthPercent(100f)
        .paddingDip(YogaEdge.ALL, 2f)
    when (state) {
      NoteCreateBottomBarType.ALL_SEGMENTS -> column.child(
          HorizontalScroll.create(context).widthPercent(100f).contentProps(NoteCreationAllSegmentsBottomBar.create(context).iconColor(iconColor)))
      NoteCreateBottomBarType.ALL_MARKDOWNS -> column.child(
          HorizontalScroll.create(context).widthPercent(100f).contentProps(NoteCreationAllMarkdownsBottomBar.create(context).iconColor(iconColor)))
      else -> {
      }
    }
    column.child(row)

    return bottomBarCard(context, column.build(), toolbarColor).build()
  }

  @OnEvent(ClickEvent::class)
  fun onStateChangeClick(context: ComponentContext,
                         @State state: NoteCreateBottomBarType,
                         @Param nextState: NoteCreateBottomBarType) {
    if (state == NoteCreateBottomBarType.OPTIONS && nextState == NoteCreateBottomBarType.OPTIONS) {
      NoteCreationBottomBar.onStateChange(context, NoteCreateBottomBarType.DEFAULT_SEGMENTS)
      return
    }
    if (state == NoteCreateBottomBarType.ALL_MARKDOWNS && nextState == NoteCreateBottomBarType.DEFAULT_SEGMENTS) {
      NoteCreationBottomBar.onStateChange(context, NoteCreateBottomBarType.ALL_SEGMENTS)
      return
    }
    if (state == NoteCreateBottomBarType.ALL_SEGMENTS && nextState == NoteCreateBottomBarType.DEFAULT_MARKDOWNS) {
      NoteCreationBottomBar.onStateChange(context, NoteCreateBottomBarType.ALL_MARKDOWNS)
      return
    }
    NoteCreationBottomBar.onStateChange(context, nextState)
  }

  @OnUpdateState
  fun onStateChange(state: StateValue<NoteCreateBottomBarType>, @Param nextState: NoteCreateBottomBarType) {
    state.set(nextState)
  }
}

@LayoutSpec
object NoteCreationOptionsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_settings_white_48dp)
            .onClick { })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_undo_history)
            .onClick { activity.onHistoryClick(true) })
        .child(bottomBarRoundIcon(context, iconColor)
            .bgColor(activity.note().color)
            .bgAlpha(255)
            .iconRes(R.drawable.ic_empty)
            .onClick { activity.onColorChangeClick() }
            .showBorder(true)
            .iconMarginHorizontalRes(R.dimen.toolbar_round_small_icon_margin_horizontal)
            .iconSizeRes(R.dimen.toolbar_round_small_icon_size))
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_redo_history)
            .onClick { activity.onHistoryClick(false) })
        .child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))
        .build()
  }
}

@LayoutSpec
object NoteCreationSegmentsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int,
               @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_subject_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_check_box_white_24dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_format_quote_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.QUOTE) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_more_horiz_white_48dp)
            .onClick { }
            .isClickDisabled(true)
            .clickHandler(toggleButtonClick))
        .build()
  }
}

@LayoutSpec
object NoteCreationMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int,
               @Prop toggleButtonClick: EventHandler<ClickEvent>): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.HEADER) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_bold)
            .onClick { activity.triggerMarkdown(MarkdownType.BOLD) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_italics)
            .onClick { activity.triggerMarkdown(MarkdownType.ITALICS) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.UNORDERED) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_more_horiz_white_48dp)
            .onClick { }
            .isClickDisabled(true)
            .clickHandler(toggleButtonClick))
        .build()
  }
}


@LayoutSpec
object NoteCreationAllSegmentsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignSelf(YogaAlign.CENTER)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.HEADING) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
            .onClick { activity.addEmptyItemAtFocused(FormatType.SUB_HEADING) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_subject_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.TEXT) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_check_box_white_24dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_format_quote_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.QUOTE) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_code_white_48dp)
            .onClick { activity.addEmptyItemAtFocused(FormatType.CODE) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_image_gallery)
            .onClick { activity.addEmptyItemAtFocused(FormatType.IMAGE) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_format_separator)
            .onClick { activity.addEmptyItemAtFocused(FormatType.SEPARATOR) })
        .build()
  }
}

@LayoutSpec
object NoteCreationAllMarkdownsBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.COLOR) iconColor: Int): Component {
    val activity = context.androidContext as CreateNoteActivity
    return Row.create(context)
        .alignSelf(YogaAlign.CENTER)
        .alignItems(YogaAlign.CENTER)
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.HEADER) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_title_white_48dp)
            .iconPaddingRes(R.dimen.toolbar_round_icon_padding_subsize)
            .onClick { activity.triggerMarkdown(MarkdownType.SUB_HEADER) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_bold)
            .onClick { activity.triggerMarkdown(MarkdownType.BOLD) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_italics)
            .onClick { activity.triggerMarkdown(MarkdownType.ITALICS) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_check_box_white_24dp)
            .onClick { activity.triggerMarkdown(MarkdownType.CHECKLIST_UNCHECKED) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_format_list_bulleted_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.UNORDERED) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_underline)
            .onClick { activity.triggerMarkdown(MarkdownType.UNDERLINE) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_markdown_strikethrough)
            .onClick { activity.triggerMarkdown(MarkdownType.STRIKE_THROUGH) })
        .child(bottomBarRoundIcon(context, iconColor)
            .iconRes(R.drawable.ic_code_white_48dp)
            .onClick { activity.triggerMarkdown(MarkdownType.BOLD) })
        .build()
  }
}