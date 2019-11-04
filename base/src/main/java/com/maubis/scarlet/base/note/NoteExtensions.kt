package com.maubis.scarlet.base.note

import android.content.Context
import com.google.gson.Gson
import com.maubis.markdown.Markdown
import com.maubis.markdown.MarkdownConfig
import com.maubis.markdown.spannable.MarkdownType
import com.maubis.markdown.spannable.bold
import com.maubis.markdown.spannable.font
import com.maubis.markdown.spannable.relativeSize
import com.maubis.markdown.spannable.strike
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.core.note.getTagUUIDs
import com.maubis.scarlet.base.core.note.isUnsaved
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.note.creation.activity.NoteIntentRouterActivity
import com.maubis.scarlet.base.note.creation.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.security.controller.PinLockController.needsLockCheck
import com.maubis.scarlet.base.security.sheets.openUnlockSheet
import com.maubis.scarlet.base.settings.sheet.sInternalShowUUID
import com.maubis.scarlet.base.settings.sheet.sSecurityAppLockEnabled
import com.maubis.scarlet.base.settings.sheet.sUIMarkdownEnabledOnHome
import com.maubis.scarlet.base.support.BitmapHelper
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.sThemeDarkenNoteColor
import com.maubis.scarlet.base.support.utils.sDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun Note.log(): String {
  val log = HashMap<String, Any>()
  log["note"] = this
  log["_text"] = getFullText()
  log["_image"] = getImageFile()
  log["_fullText"] = getFullText()
  log["_displayTime"] = getDisplayTime()
  log["_tag"] = getTagString()
  log["_formats"] = getFormats()
  return Gson().toJson(log)
}

/**************************************************************************************
 ************* Content and Display Information Functions Functions ********************
 **************************************************************************************/

fun Note.getFullTextForDirectMarkdownRender(): String {
  var text = getFullText()
  text = text.replace("\n[x] ", "\n\u2611 ")
  text = text.replace("\n[ ] ", "\n\u2610 ")
  text = text.replace("\n- ", "\n\u2022 ")
  return text
}

fun Note.getMarkdownForListView(): CharSequence {
  var text = getFullTextForDirectMarkdownRender()
  if (sUIMarkdownEnabledOnHome) {
    return markdownFormatForList(text)
  }
  return text
}

internal fun markdownFormatForList(text: String): CharSequence {
  return Markdown.renderWithCustomFormatting(text, true) { spannable, spanInfo ->
    val s = spanInfo.start
    val e = spanInfo.end
    when (spanInfo.markdownType) {
      MarkdownType.HEADING_1 -> {
        spannable.relativeSize(1.2f, s, e)
          .font(MarkdownConfig.config.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.HEADING_2 -> {
        spannable.relativeSize(1.1f, s, e)
          .font(MarkdownConfig.config.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.HEADING_3 -> {
        spannable.relativeSize(1.0f, s, e)
          .font(MarkdownConfig.config.spanConfig.headingTypeface, s, e)
          .bold(s, e)
        true
      }
      MarkdownType.CHECKLIST_CHECKED -> {
        spannable.strike(s, e)
        true
      }
      else -> false
    }
  }
}

fun Note.getTitleForSharing(): String {
  val formats = getFormats()
  if (formats.isEmpty()) {
    return ""
  }
  val format = formats.first()
  val headingFormats = listOf(FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3)
  return when {
    headingFormats.contains(format.formatType) -> format.text
    else -> ""
  }
}

fun Note.getTextForSharing(): String {
  val formats = getFormats().toMutableList()
  if (formats.isEmpty()) {
    return ""
  }

  val format = formats.first()
  if (format.formatType == FormatType.HEADING || format.formatType == FormatType.SUB_HEADING) {
    formats.removeAt(0)
  }

  val stringBuilder = StringBuilder()
  formats.forEach {
    stringBuilder.append(it.markdownText)
    stringBuilder.append("\n")
    if (it.formatType == FormatType.QUOTE) {
      stringBuilder.append("\n")
    }
  }

  val text = stringBuilder.toString().trim()
  if (sInternalShowUUID) {
    return "`$uuid`\n\n$text"
  }
  return text
}

fun Note.getSmartFormats(): List<Format> {
  val formats = getFormats()
  var maxIndex = formats.size
  val smartFormats = ArrayList<Format>()
  formats.forEach {
    if (it.formatType == FormatType.TEXT) {
      val moreFormats = it.text.toInternalFormats()
      moreFormats.forEach { format ->
        format.uid = maxIndex
        smartFormats.add(format)
        maxIndex += 1
      }
    } else {
      smartFormats.add(it)
    }
  }
  return smartFormats
}

fun Note.getImageFile(): String {
  val formats = getFormats()
  val format = formats.find { it.formatType === FormatType.IMAGE }
  return format?.text ?: ""
}

fun Note.getFullText(): String {
  val fullText = getFormats().map { it -> it.markdownText }.joinToString(separator = "\n").trim()
  if (sInternalShowUUID) {
    return "`$uuid`\n$fullText"
  }
  return fullText
}

fun Note.isNoteLockedButAppUnlocked(): Boolean {
  return this.locked && !needsLockCheck() && sSecurityAppLockEnabled
}

fun Note.getLockedAwareTextForHomeList(): CharSequence {
  val lockedText = "******************\n***********\n****************"
  return when {
    isNoteLockedButAppUnlocked() || !this.locked -> getMarkdownForListView()
    !sUIMarkdownEnabledOnHome -> "${getTitleForSharing()}\n$lockedText"
    else -> markdownFormatForList("# ${getTitleForSharing()}\n\n```\n$lockedText\n```")
  }
}

fun Note.getDisplayTime(): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    (this.timestamp != null) -> this.timestamp
    else -> 0
  }

  val format = when {
    Calendar.getInstance().timeInMillis - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return sDateFormat.readableTime(format, time)
}

fun Note.getTagString(): String {
  val tags = getTags()
  return tags.map { it -> "` ${it.title} `" }.joinToString(separator = " ")
}

fun Note.getTags(): Set<Tag> {
  val tags = HashSet<Tag>()
  for (tagID in getTagUUIDs()) {
    val tag = tagsDb.getByUUID(tagID)
    if (tag != null) {
      tags.add(tag)
    }
  }
  return tags
}

fun Note.toggleTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> tags.remove(tag.uuid)
    false -> tags.add(tag.uuid)
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.addTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> return
    false -> tags.add(tag.uuid)
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.removeTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> tags.remove(tag.uuid)
    false -> return
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.adjustedColor(): Int {
  return when(sThemeDarkenNoteColor) {
    true -> ColorUtil.darkOrDarkerColor(color ?: sNoteDefaultColor)
    false -> color ?: sNoteDefaultColor
  }
}

/**************************************************************************************
 ******************************* Note Action Functions ********************************
 **************************************************************************************/

fun Note.mark(context: Context, noteState: NoteState) {
  this.state = noteState.name
  this.updateTimestamp = Calendar.getInstance().timeInMillis
  save(context)
}

fun Note.edit(context: Context) {
  if (this.locked) {
    if (context is ThemedActivity) {
      openUnlockSheet(
        activity = context,
        onUnlockSuccess = { context.startActivity(NoteIntentRouterActivity.edit(context, this)) },
        onUnlockFailure = { edit(context) })
    }
    return
  }
  context.startActivity(NoteIntentRouterActivity.edit(context, this))
}

fun Note.share(context: Context) {
  ApplicationBase.instance.noteActions(this).share(context)
}

fun Note.hasImages(): Boolean {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  return imageFormats.isNotEmpty()
}

fun Note.shareImages(context: Context) {
  val imageFormats = getFormats().filter { it.formatType == FormatType.IMAGE }
  val bitmaps = imageFormats
    .map { ApplicationBase.sAppImageStorage.getFile(uuid, it.text) }
    .filter { it.exists() }
    .map { BitmapHelper.loadFromFile(it) }
    .filterNotNull()
  when {
    bitmaps.size == 1 -> BitmapHelper.send(context, bitmaps.first())
    bitmaps.size > 1 -> BitmapHelper.send(context, bitmaps)
  }
}

fun Note.copy(context: Context) {
  ApplicationBase.instance.noteActions(this).copy(context)
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Note.applySanityChecks() {
  folder = folder ?: ""
  description = description ?: ""
  timestamp = timestamp ?: System.currentTimeMillis()
  color = color ?: sNoteDefaultColor
  state = state ?: NoteState.DEFAULT.name
  tags = tags ?: ""
  uuid = uuid ?: generateUUID()
}

fun Note.save(context: Context) {
  applySanityChecks()
  if (disableBackup) {
    saveWithoutSync(context)
    return
  }
  ApplicationBase.instance.noteActions(this).save(context)
}

fun Note.unsafeSave_INTERNAL_USE_ONLY() {
  applySanityChecks()

  val id = CoreConfig.notesDb.database().insertNote(this)
  uid = if (isUnsaved()) id.toInt() else uid
  CoreConfig.notesDb.notifyInsertNote(this)
}

fun Note.saveWithoutSync(context: Context) {
  ApplicationBase.instance.noteActions(this).offlineSave(context)
}

fun Note.saveToSync(context: Context) {
  ApplicationBase.instance.noteActions(this).onlineSave(context)
}

fun Note.delete(context: Context) {
  if (disableBackup) {
    deleteWithoutSync(context)
    return
  }
  ApplicationBase.instance.noteActions(this).delete(context)
}

fun Note.deleteWithoutSync(context: Context) {
  ApplicationBase.instance.noteActions(this).offlineDelete(context)
}

fun Note.deleteToSync(context: Context) {
  ApplicationBase.instance.noteActions(this).onlineDelete(context)
}

fun Note.softDelete(context: Context) {
  ApplicationBase.instance.noteActions(this).softDelete(context)
}
