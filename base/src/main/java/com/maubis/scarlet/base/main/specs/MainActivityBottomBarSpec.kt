package com.maubis.scarlet.base.main.specs

import android.content.pm.ShortcutInfo
import android.graphics.Color
import android.graphics.drawable.Icon
import android.text.Layout
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LongClickEvent
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Progress
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.main.sheets.HomeOptionsBottomSheet
import com.maubis.scarlet.base.main.sheets.openDeleteTrashSheet
import com.maubis.scarlet.base.note.creation.activity.CreateNoteActivity
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.note.creation.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.note.folder.sheet.CreateOrEditFolderBottomSheet
import com.maubis.scarlet.base.support.addShortcut
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.ToolbarColorConfig
import com.maubis.scarlet.base.support.specs.bottomBarCard
import com.maubis.scarlet.base.support.specs.bottomBarRoundIcon
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@LayoutSpec
object MainActivityBottomBarSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop colorConfig: ToolbarColorConfig,
    @Prop disableNewFolderButton: Boolean,
    @Prop isInTrash: Boolean
  ): Component {
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .bgColor(Color.TRANSPARENT)
                .iconRes(R.drawable.ic_apps_white_48dp)
                .onClick {
                  openSheet(activity, HomeOptionsBottomSheet())
                })
    row.child(EmptySpec.create(context).heightDip(1f).flexGrow(1f))

    if (isInTrash) {
      row.child(
        bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.ic_delete_permanently)
          .onClick { openDeleteTrashSheet(activity) }
      )
    }
    else {
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.icon_add_notebook)
          .alpha(if (disableNewFolderButton) 0.4f else 1.0f)
          .isClickDisabled(disableNewFolderButton)
          .onClick {
            CreateOrEditFolderBottomSheet.openSheet(
                activity,
                FolderBuilder().emptyFolder(sNoteDefaultColor),
                { _, _ -> activity.loadData() })
          })
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.icon_add_list)
          .onClick {
            val intent = CreateNoteActivity.getNewChecklistNoteIntent(
                activity,
                activity.state.currentFolder?.uuid ?: "")
            activity.startActivity(intent)
          })
      row.child(bottomBarRoundIcon(context, colorConfig)
          .iconRes(R.drawable.icon_add_note)
          .isLongClickEnabled(true)
          .onLongClick {
            if (!OsVersionUtils.canAddLauncherShortcuts()) {
              return@onLongClick
            }

            val shortcut = ShortcutInfo.Builder(activity, "scarlet_notes___create_note")
                .setShortLabel(activity.getString(R.string.shortcut_add_note))
                .setLongLabel(activity.getString(R.string.shortcut_add_note))
                .setIcon(Icon.createWithResource(activity, R.mipmap.create_launcher))
                .setIntent(NoteIntentRouterActivity.create())
                .build()
            addShortcut(activity, shortcut)
          }
          .onClick {
            val intent = CreateNoteActivity.getNewNoteIntent(
                activity,
                activity.state.currentFolder?.uuid ?: "")
            activity.startActivity(intent)
          })
    }
    return bottomBarCard(context, row.build(), colorConfig).build()
  }
}

@LayoutSpec
object MainActivityFolderBottomBarSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop folder: Folder): Component {
    val colorConfig = ToolbarColorConfig(
      toolbarBackgroundColor = folder.color,
      toolbarIconColor = when (ColorUtil.isLightColored(folder.color)) {
        true -> context.getColor(R.color.dark_tertiary_text)
        false -> context.getColor(R.color.light_secondary_text)
      }
    )
    val activity = context.androidContext as MainActivity
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .bgColor(Color.TRANSPARENT)
                .iconRes(R.drawable.ic_close_white_48dp)
                .onClick { activity.onFolderChange(null) })
    row.child(
      Text.create(context)
        .typeface(sAppTypeface.title())
        .textAlignment(Layout.Alignment.ALIGN_CENTER)
        .flexGrow(1f)
        .text(folder.title)
        .textSizeRes(R.dimen.font_size_normal)
        .textColor(colorConfig.toolbarIconColor)
        .clickHandler(MainActivityFolderBottomBar.onClickEvent(context)))
    row.child(bottomBarRoundIcon(context, colorConfig)
                .iconRes(R.drawable.ic_more_options)
                .isClickDisabled(true)
                .clickHandler(MainActivityFolderBottomBar.onClickEvent(context))
                .onClick {})
    return bottomBarCard(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop folder: Folder) {
    val activity = context.androidContext as MainActivity
    if (activity.state.currentFolder != null) {
      CreateOrEditFolderBottomSheet.openSheet(activity, folder) { _, _ -> activity.loadData() }
    }
  }
}

@LayoutSpec
object MainActivityDisabledSyncSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext): Component {
    val colorConfig = ToolbarColorConfig(
      toolbarBackgroundColor = context.getColor(R.color.material_blue_grey_800),
      toolbarIconColor = context.getColor(R.color.light_secondary_text)
    )
    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 4f)
    row.child(bottomBarRoundIcon(context, colorConfig)
                .bgColor(Color.TRANSPARENT)
                .iconRes(R.drawable.ic_info)
                .onClick {
                  GlobalScope.launch {

                  }
                })
    row.child(
      Column.create(context)
        .flexGrow(1f)
        .paddingDip(YogaEdge.ALL, 8f)
        .child(
          Text.create(context)
            .typeface(sAppTypeface.subHeading())
            .textRes(R.string.firebase_no_sync_warning)
            .textSizeRes(R.dimen.font_size_normal)
            .textColor(colorConfig.toolbarIconColor))
        .child(
          Text.create(context)
            .typeface(sAppTypeface.title())
            .textRes(R.string.firebase_no_sync_warning_details)
            .textSizeRes(R.dimen.font_size_small)
            .textColor(colorConfig.toolbarIconColor)))
    row.clickHandler(MainActivityDisabledSync.onClickEvent(context))
    return bottomBarCard(context, row.build(), colorConfig).build()
  }

  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

@LayoutSpec
object MainActivitySyncingNowSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext, @Prop isSyncHappening: Boolean): Component {
    val colorConfig = ToolbarColorConfig(
      toolbarBackgroundColor = sAppTheme.get(ThemeColorType.TOOLBAR_BACKGROUND),
      toolbarIconColor = sAppTheme.get(ThemeColorType.TOOLBAR_ICON)
    )
    val syncText = when (isSyncHappening) {
      true -> R.string.home_syncing_top_layout
      false -> R.string.home_pending_backup_top_layout
    }
    val syncIcon = when (isSyncHappening) {
      true -> Progress.create(context)
        .widthDip(24f)
        .alpha(0.8f)
        .marginDip(YogaEdge.END, 8f)
        .color(colorConfig.toolbarIconColor)
      false -> Image.create(context)
        .heightDip(24f)
        .widthDip(24f)
        .marginDip(YogaEdge.END, 8f)
        .alpha(0.8f)
        .drawableRes(R.drawable.icon_folder_sync)
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 8f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .alpha(0.8f)
      .child(EmptySpec.create(context).flexGrow(1f))
      .child(
        Row.create(context)
          .alignItems(YogaAlign.CENTER)
          .alignContent(YogaAlign.CENTER)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .paddingDip(YogaEdge.HORIZONTAL, 12f)
          .backgroundRes(R.drawable.pending_sync_capsule)
          .clickHandler(MainActivitySyncingNow.onClickEvent(context))
          .longClickHandler(MainActivitySyncingNow.onLongClickEvent(context))
          .child(syncIcon)
          .child(
            Text.create(context)
              .typeface(sAppTypeface.title())
              .textRes(syncText)
              .textSizeRes(R.dimen.font_size_normal)
              .textColorRes(R.color.light_secondary_text)))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }

  @OnEvent(LongClickEvent::class)
  fun onLongClickEvent(context: ComponentContext, @Prop onLongClick: () -> Unit): Boolean {
    onLongClick()
    return true
  }
}
