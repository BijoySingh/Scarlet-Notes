package com.bijoysingh.quicknote.drive

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.bijoysingh.quicknote.database.RemoteDataType
import com.bijoysingh.quicknote.database.RemoteUploadData
import com.bijoysingh.quicknote.database.remoteDatabase
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.annotations.ResType
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.SolidColor
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.google.gson.Gson
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.export.data.getExportableNoteMeta
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.color
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.sDateFormat
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

data class PendingItem(
  val state: RemoteUploadData,
  val info: String?
)

@LayoutSpec
object PendingItemIconSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop(resType = ResType.STRING) label: String,
    @Prop(resType = ResType.DRAWABLE) icon: Drawable): Component {
    val secondaryColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    return Row.create(context)
      .paddingDip(YogaEdge.HORIZONTAL, 8f)
      .paddingDip(YogaEdge.VERTICAL, 4f)
      .alignItems(YogaAlign.CENTER)
      .alignContent(YogaAlign.CENTER)
      .backgroundRes(R.drawable.secondary_rounded_bg)
      .child(
        Image.create(context)
          .drawable(icon.color(secondaryColor))
          .marginDip(YogaEdge.HORIZONTAL, 4f)
          .heightDip(24f))
      .child(
        Text.create(context)
          .text(label)
          .textSizeRes(R.dimen.font_size_xsmall)
          .typeface(sAppTypeface.subHeading())
          .textColor(secondaryColor))
      .build()
  }
}

@LayoutSpec
object PendingItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: PendingItem): Component {
    val titleColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    val subtitleColor = sAppTheme.get(ThemeColorType.TERTIARY_TEXT)
    val hintColor = sAppTheme.get(ThemeColorType.HINT_TEXT)

    val uuid = option.state.uuid
    val info = option.info ?: "N/A"
    val icon = when (option.state.type) {
      RemoteDataType.NOTE.name -> R.drawable.ic_note_white_48dp
      RemoteDataType.NOTE_META.name -> R.drawable.ic_info
      RemoteDataType.TAG.name -> R.drawable.ic_action_tags
      RemoteDataType.FOLDER.name -> R.drawable.ic_folder
      RemoteDataType.IMAGE.name -> R.drawable.ic_image_gallery
      else -> R.drawable.ic_action_lock
    }
    val label = when (option.state.type) {
      RemoteDataType.NOTE.name -> "Note"
      RemoteDataType.NOTE_META.name -> "Info"
      RemoteDataType.TAG.name -> "Tag"
      RemoteDataType.FOLDER.name -> "Folder"
      RemoteDataType.IMAGE.name -> "Image"
      else -> "Invalid"
    }
    val localState = when {
      option.state.lastUpdateTimestamp == 0L -> R.string.pending_backup_state_unavailable
      option.state.remoteUpdateTimestamp == 0L && !option.state.localStateDeleted -> R.string.pending_backup_state_created
      option.state.localStateDeleted -> R.string.pending_backup_state_deleted
      else -> R.string.pending_backup_state_updated
    }
    val localUpdateTime = when {
      option.state.lastUpdateTimestamp == 0L -> ""
      else -> sDateFormat.readableFullTime(option.state.lastUpdateTimestamp)
    }

    val remoteState = when {
      option.state.remoteUpdateTimestamp == 0L -> R.string.pending_backup_state_unavailable
      option.state.lastUpdateTimestamp == 0L && !option.state.remoteStateDeleted -> R.string.pending_backup_state_created
      option.state.remoteStateDeleted -> R.string.pending_backup_state_deleted
      else -> R.string.pending_backup_state_updated
    }
    val remoteUpdateTime = when {
      option.state.remoteUpdateTimestamp == 0L -> ""
      else -> sDateFormat.readableFullTime(option.state.remoteUpdateTimestamp)
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
              .typeface(sAppTypeface.text())
              .textColor(subtitleColor)))
      .child(
        Text.create(context)
          .text(info)
          .maxLines(5)
          .paddingDip(YogaEdge.ALL, 8f)
          .marginDip(YogaEdge.ALL, 8f)
          .ellipsize(TextUtils.TruncateAt.MIDDLE)
          .textSizeRes(R.dimen.font_size_small)
          .typeface(sAppTypeface.title())
          .backgroundRes(R.drawable.pending_note_background)
          .textColor(subtitleColor))
      .child(
        Row.create(context)
          .widthPercent(100f)
          .paddingDip(YogaEdge.ALL, 4f)
          .child(
            Text.create(context)
              .textRes(R.string.pending_backup_local_state)
              .textSizeRes(R.dimen.font_size_small)
              .flexGrow(1f)
              .typeface(sAppTypeface.subHeading())
              .textColor(hintColor))
          .child(
            Text.create(context)
              .textRes(localState)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(sAppTypeface.text())
              .paddingDip(YogaEdge.HORIZONTAL, 6f)
              .paddingDip(YogaEdge.VERTICAL, 2f)
              .marginDip(YogaEdge.HORIZONTAL, 4f)
              .backgroundRes(R.drawable.pending_note_background)
              .textColor(hintColor))
          .child(
            Text.create(context)
              .text(localUpdateTime)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(sAppTypeface.text())
              .paddingDip(YogaEdge.HORIZONTAL, 6f)
              .paddingDip(YogaEdge.VERTICAL, 2f)
              .backgroundRes(R.drawable.pending_note_background)
              .textColor(hintColor))
      )
      .child(
        Row.create(context)
          .widthPercent(100f)
          .paddingDip(YogaEdge.ALL, 4f)
          .child(
            Text.create(context)
              .textRes(R.string.pending_backup_remote_state)
              .textSizeRes(R.dimen.font_size_small)
              .flexGrow(1f)
              .typeface(sAppTypeface.subHeading())
              .textColor(hintColor))
          .child(
            Text.create(context)
              .textRes(remoteState)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(sAppTypeface.text())
              .paddingDip(YogaEdge.HORIZONTAL, 6f)
              .paddingDip(YogaEdge.VERTICAL, 2f)
              .marginDip(YogaEdge.HORIZONTAL, 4f)
              .backgroundRes(R.drawable.pending_note_background)
              .textColor(hintColor))
          .child(
            Text.create(context)
              .text(remoteUpdateTime)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(sAppTypeface.text())
              .paddingDip(YogaEdge.HORIZONTAL, 6f)
              .paddingDip(YogaEdge.VERTICAL, 2f)
              .backgroundRes(R.drawable.pending_note_background)
              .textColor(hintColor))
      )
      .child(
        SolidColor.create(context)
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
      remoteDatabase?.getAllPending()?.forEach {
        val pendingItem = when (it.type) {
          RemoteDataType.NOTE.name -> PendingItem(state = it, info = instance.notesDatabase().getByUUID(it.uuid)?.getFullText())
          RemoteDataType.NOTE_META.name -> {
            val note = instance.notesDatabase().getByUUID(it.uuid)?.getExportableNoteMeta()
            when {
              (note == null) -> PendingItem(state = it, info = null)
              else -> PendingItem(state = it, info = Gson().toJson(note))
            }
          }
          RemoteDataType.TAG.name -> PendingItem(state = it, info = instance.tagsDatabase().getByUUID(it.uuid)?.title)
          RemoteDataType.FOLDER.name -> PendingItem(state = it, info = instance.foldersDatabase().getByUUID(it.uuid)?.title)
          RemoteDataType.IMAGE.name -> PendingItem(state = it, info = "Image")
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
        .child(
          getLithoBottomSheetTitle(componentContext)
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