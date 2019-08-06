package com.bijoysingh.quicknote.drive

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.gDriveDatabase
import com.facebook.litho.*
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.SolidColor
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.github.bijoysingh.starter.util.DateFormatter
import com.google.gson.Gson
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.data.getExportableNoteMeta
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.color
import com.maubis.scarlet.base.support.ui.ThemeColorType
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

data class PendingItem(
    val state: GDriveUploadData,
    val info: String?
)

@LayoutSpec
object PendingItemIconSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop(resType = ResType.STRING) label: String,
               @Prop(resType = ResType.DRAWABLE) icon: Drawable): Component {
    val secondaryColor = instance.themeController().get(ThemeColorType.SECONDARY_TEXT)
    return Row.create(context)
        .paddingDip(YogaEdge.HORIZONTAL, 8f)
        .paddingDip(YogaEdge.VERTICAL, 4f)
        .alignItems(YogaAlign.CENTER)
        .alignContent(YogaAlign.CENTER)
        .backgroundRes(R.drawable.secondary_rounded_bg)
        .child(Image.create(context)
            .drawable(icon.color(secondaryColor))
            .marginDip(YogaEdge.HORIZONTAL, 4f)
            .heightDip(24f))
        .child(Text.create(context)
            .text(label)
            .textSizeRes(R.dimen.font_size_xsmall)
            .typeface(CoreConfig.FONT_MONSERRAT_MEDIUM)
            .textColor(secondaryColor))
        .build()
  }
}

@LayoutSpec
object PendingItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(context: ComponentContext,
               @Prop option: PendingItem): Component {
    val theme = ApplicationBase.instance.themeController()
    val titleColor = theme.get(ThemeColorType.SECONDARY_TEXT)
    val subtitleColor = theme.get(ThemeColorType.TERTIARY_TEXT)
    val hintColor = theme.get(ThemeColorType.HINT_TEXT)

    val uuid = option.state.uuid
    val info = option.info ?: "N/A"
    val icon = when (option.state.type) {
      GDriveDataType.NOTE.name -> R.drawable.ic_note_white_48dp
      GDriveDataType.NOTE_META.name -> R.drawable.ic_info
      GDriveDataType.TAG.name -> R.drawable.ic_action_tags
      GDriveDataType.FOLDER.name -> R.drawable.ic_folder
      GDriveDataType.IMAGE.name -> R.drawable.ic_image_gallery
      else -> R.drawable.ic_action_lock
    }
    val label = when (option.state.type) {
      GDriveDataType.NOTE.name -> "Note"
      GDriveDataType.NOTE_META.name -> "Info"
      GDriveDataType.TAG.name -> "Tag"
      GDriveDataType.FOLDER.name -> "Folder"
      GDriveDataType.IMAGE.name -> "Image"
      else -> "Invalid"
    }
    val localState = when {
      option.state.lastUpdateTimestamp == 0L -> R.string.pending_backup_state_unavailable
      option.state.gDriveUpdateTimestamp == 0L && !option.state.localStateDeleted -> R.string.pending_backup_state_created
      option.state.localStateDeleted -> R.string.pending_backup_state_deleted
      else -> R.string.pending_backup_state_updated
    }
    val localUpdateTime = when {
      option.state.lastUpdateTimestamp == 0L -> ""
      else -> DateFormatter.getDate(DateFormatter.Formats.HH_MM_A_DD_MMM_YYYY.format, option.state.lastUpdateTimestamp)
    }

    val remoteState = when {
      option.state.gDriveUpdateTimestamp == 0L -> R.string.pending_backup_state_unavailable
      option.state.lastUpdateTimestamp == 0L && !option.state.gDriveStateDeleted -> R.string.pending_backup_state_created
      option.state.gDriveStateDeleted -> R.string.pending_backup_state_deleted
      else -> R.string.pending_backup_state_updated
    }
    val remoteUpdateTime = when {
      option.state.gDriveUpdateTimestamp == 0L -> ""
      else -> DateFormatter.getDate(DateFormatter.Formats.HH_MM_A_DD_MMM_YYYY.format, option.state.gDriveUpdateTimestamp)
    }

    val column = Column.create(context)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 12f)
        .child(
            Row.create(context)
                .paddingDip(YogaEdge.ALL, 2f)
                .alignItems(YogaAlign.CENTER)
                .child(
                    PendingItemIcon.create(context)
                        .iconRes(icon)
                        .label(label)
                        .marginDip(YogaEdge.END, 16f))
                .child(
                    Text.create(context)
                        .text(uuid)
                        .maxLines(1)
                        .ellipsize(TextUtils.TruncateAt.END)
                        .textSizeRes(R.dimen.font_size_small)
                        .typeface(CoreConfig.FONT_OPEN_SANS)
                        .textColor(subtitleColor)))
        .child(
            Text.create(context)
                .text(info)
                .maxLines(5)
                .paddingDip(YogaEdge.ALL, 8f)
                .marginDip(YogaEdge.ALL, 8f)
                .ellipsize(TextUtils.TruncateAt.MIDDLE)
                .textSizeRes(R.dimen.font_size_small)
                .typeface(CoreConfig.FONT_MONSERRAT)
                .backgroundRes(R.drawable.pending_note_background)
                .textColor(subtitleColor))
        .child(
            Row.create(context)
                .widthPercent(100f)
                .paddingDip(YogaEdge.ALL, 4f)
                .child(Text.create(context)
                    .textRes(R.string.pending_backup_local_state)
                    .textSizeRes(R.dimen.font_size_small)
                    .flexGrow(1f)
                    .typeface(CoreConfig.FONT_MONSERRAT_MEDIUM)
                    .textColor(hintColor))
                .child(Text.create(context)
                    .textRes(localState)
                    .textSizeRes(R.dimen.font_size_small)
                    .typeface(CoreConfig.FONT_OPEN_SANS)
                    .paddingDip(YogaEdge.HORIZONTAL, 6f)
                    .paddingDip(YogaEdge.VERTICAL, 2f)
                    .marginDip(YogaEdge.HORIZONTAL, 4f)
                    .backgroundRes(R.drawable.pending_note_background)
                    .textColor(hintColor))
                .child(Text.create(context)
                    .text(localUpdateTime)
                    .textSizeRes(R.dimen.font_size_small)
                    .typeface(CoreConfig.FONT_OPEN_SANS)
                    .paddingDip(YogaEdge.HORIZONTAL, 6f)
                    .paddingDip(YogaEdge.VERTICAL, 2f)
                    .backgroundRes(R.drawable.pending_note_background)
                    .textColor(hintColor))
        )
        .child(
            Row.create(context)
                .widthPercent(100f)
                .paddingDip(YogaEdge.ALL, 4f)
                .child(Text.create(context)
                    .textRes(R.string.pending_backup_remote_state)
                    .textSizeRes(R.dimen.font_size_small)
                    .flexGrow(1f)
                    .typeface(CoreConfig.FONT_MONSERRAT_MEDIUM)
                    .textColor(hintColor))
                .child(Text.create(context)
                    .textRes(remoteState)
                    .textSizeRes(R.dimen.font_size_small)
                    .typeface(CoreConfig.FONT_OPEN_SANS)
                    .paddingDip(YogaEdge.HORIZONTAL, 6f)
                    .paddingDip(YogaEdge.VERTICAL, 2f)
                    .marginDip(YogaEdge.HORIZONTAL, 4f)
                    .backgroundRes(R.drawable.pending_note_background)
                    .textColor(hintColor))
                .child(Text.create(context)
                    .text(remoteUpdateTime)
                    .textSizeRes(R.dimen.font_size_small)
                    .typeface(CoreConfig.FONT_OPEN_SANS)
                    .paddingDip(YogaEdge.HORIZONTAL, 6f)
                    .paddingDip(YogaEdge.VERTICAL, 2f)
                    .backgroundRes(R.drawable.pending_note_background)
                    .textColor(hintColor))
        )
        .child(SolidColor.create(context)
            .color(hintColor)
            .heightDip(0.5f)
            .widthDip(196f)
            .alignSelf(YogaAlign.CENTER)
            .marginDip(YogaEdge.TOP, 12f)
            .alpha(0.4f))
    return column.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

class GDrivePendingBottomSheet : LithoBottomSheet() {

  val dataAvailable: AtomicBoolean = AtomicBoolean(false)
  val data: MutableList<PendingItem> = emptyList<PendingItem>().toMutableList()
  fun requestData(onDataAvailable: () -> Unit) {
    GlobalScope.launch {
      data.clear()
      gDriveDatabase?.getAllPending()?.forEach {
        val pendingItem = when (it.type) {
          GDriveDataType.NOTE.name -> PendingItem(state = it, info = instance.notesDatabase().getByUUID(it.uuid)?.getFullText())
          GDriveDataType.NOTE_META.name -> {
            val note = instance.notesDatabase().getByUUID(it.uuid)?.getExportableNoteMeta()
            when {
              (note == null) -> PendingItem(state = it, info = null)
              else -> PendingItem(state = it, info = Gson().toJson(note))
            }
          }
          GDriveDataType.TAG.name -> PendingItem(state = it, info = instance.tagsDatabase().getByUUID(it.uuid)?.title)
          GDriveDataType.FOLDER.name -> PendingItem(state = it, info = instance.foldersDatabase().getByUUID(it.uuid)?.title)
          GDriveDataType.IMAGE.name -> PendingItem(state = it, info = "Image")
          else -> null
        }
        if (pendingItem !== null) {
          data.add(pendingItem)
        }
      }
      dataAvailable.set(true)
      GlobalScope.launch(Main) {
        onDataAvailable()
      }
    }
  }

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    try {
      val component = Column.create(componentContext)
          .widthPercent(100f)
          .paddingDip(YogaEdge.VERTICAL, 8f)
          .paddingDip(YogaEdge.HORIZONTAL, 20f)
          .child(getLithoBottomSheetTitle(componentContext)
              .textRes(R.string.home_pending_backup_top_layout)
              .marginDip(YogaEdge.HORIZONTAL, 0f))
      data.forEach {
        component.child(
            PendingItemLayout.create(componentContext)
                .option(it)
                .onClick {})
      }
      return component.build()
    } finally {
      if (!dataAvailable.get()) {
        requestData { reset(componentContext.androidContext, dialog) }
      }
    }
  }
}