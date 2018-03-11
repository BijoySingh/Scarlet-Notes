package com.bijoysingh.quicknote.database.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.INTENT_KEY_NOTE_ID
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.WidgetConfigureActivity
import com.bijoysingh.quicknote.activities.external.ExportableTag
import com.bijoysingh.quicknote.activities.external.searchInNote
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.bijoysingh.quicknote.service.FloatingNoteService
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import org.json.JSONArray
import java.util.*
import kotlin.collections.HashMap

fun Note.log(context: Context): String {
  val log = HashMap<String, Any>()
  log["note"] = this
  log["_title"] = getTitle()
  log["_text"] = getText()
  log["_image"] = getImageFile()
  log["_locked"] = getLockedText(context, false)
  log["_fullText"] = getFullText()
  log["_displayTime"] = getDisplayTime()
  log["_tag"] = getTagString(context)
  log["_formats"] = getFormats()
  return Gson().toJson(log)
}

fun Note.isUnsaved(): Boolean {
  return this.uid == null || this.uid == 0
}

fun Note.isEqual(note: Note): Boolean {
  return TextUtils.areEqualNullIsEmpty(this.state, note.state)
      && TextUtils.areEqualNullIsEmpty(this.description, note.description)
      && TextUtils.areEqualNullIsEmpty(this.uuid, note.uuid)
      && TextUtils.areEqualNullIsEmpty(this.tags, note.tags)
      && this.timestamp.toLong() == note.timestamp.toLong()
      && this.color.toInt() == note.color.toInt()
      && this.locked == note.locked
      && this.pinned == note.pinned
}

fun Note.copyNote(reference: Note): Note {
  this.uid = reference.uid
  this.uuid = reference.uuid
  this.state = reference.state
  this.description = reference.description
  this.timestamp = reference.timestamp
  this.updateTimestamp = reference.updateTimestamp
  this.color = reference.color
  this.tags = reference.tags
  this.pinned = reference.pinned
  this.locked = reference.locked
  return this
}

/**************************************************************************************
 ************* Content and Display Information Functions Functions ********************
 **************************************************************************************/
fun Note.getTitle(): String {
  val formats = getFormats()
  if (formats.isEmpty()) {
    return ""
  }
  val format = formats.first()
  return when {
    format.formatType === FormatType.HEADING -> format.text
    format.formatType === FormatType.SUB_HEADING -> format.text
    else -> ""
  }
}

fun Note.getText(): String {
  val formats = getFormats().toMutableList()
  if (formats.isEmpty()) {
    return ""
  }

  val format = formats.first()
  if (format.formatType == FormatType.HEADING || format.formatType == FormatType.SUB_HEADING) {
    formats.removeAt(0)
  }

  return formats
      .map { it.markdownText }
      .joinToString(separator = "\n")
      .trim()
}

fun Note.getImageFile(): String {
  val formats = getFormats()
  val format = formats.find { it.formatType === FormatType.IMAGE }
  return format?.text ?: ""
}

fun Note.getMarkdownTitle(context: Context, isMarkdownEnabled: Boolean): CharSequence {
  val titleString = getTitle()
  return when {
    titleString.isBlank() -> ""
    !isMarkdownEnabled -> renderMarkdown(context, removeMarkdownHeaders(titleString))
    else -> titleString
  }
}

fun Note.getMarkdownText(context: Context, isMarkdownEnabled: Boolean): CharSequence {
  return when {
    isMarkdownEnabled -> renderMarkdown(context, removeMarkdownHeaders(getText()))
    else -> getText()
  }
}

fun Note.getFullText(): String {
  val formats = getFormats()
  return formats.map { it -> it.markdownText }.joinToString(separator = "\n\n").trim()
}

fun Note.getUnreliablyStrippedText(context: Context): String {
  val builder = StringBuilder()
  builder.append(renderMarkdown(context, removeMarkdownHeaders(getTitle())))
  builder.append(renderMarkdown(context, removeMarkdownHeaders(getText())))
  return builder.toString().trim { it <= ' ' }
}

fun Note.getLockedText(context: Context, isMarkdownEnabled: Boolean): CharSequence {
  return when {
    this.locked -> "******************\n***********\n****************"
    else -> getMarkdownText(context, isMarkdownEnabled)
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
  return DateFormatter.getDate(format, time)
}

fun Note.getTagString(context: Context): String {
  val tags = getTags(context)
  return tags.map { it -> '`' + it.title + '`' }.joinToString(separator = " ")
}

/**************************************************************************************
 ********************************* Object Functions ***********************************
 **************************************************************************************/

fun Note.getFormats(): List<Format> {
  return Format.getFormats(this.description)
}

fun Note.getNoteState(): NoteState {
  try {
    return NoteState.valueOf(this.state)
  } catch (exception: Exception) {
    return NoteState.DEFAULT
  }
}

fun Note.getTags(context: Context): Set<Tag> {
  val tags = HashSet<Tag>()
  for (tagID in getTagUUIDs()) {
    val tag = Tag.db().getByUUID(tagID)
    if (tag != null) {
      tags.add(tag)
    }
  }
  return tags
}

/**************************************************************************************
 ********************************** Tags Functions ************************************
 **************************************************************************************/
@Deprecated("")
fun Note.getTagIDs(): Set<Int> {
  val tags = if (this.tags == null) "" else this.tags
  val split = tags.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
  val tagIDs = HashSet<Int>()
  for (tagIDString in split) {
    try {
      val tagID = Integer.parseInt(tagIDString)
      tagIDs.add(tagID)
    } catch (exception: Exception) {
      // Ignore the exception
    }

  }
  return tagIDs
}

fun Note.getTagUUIDs(): MutableSet<String> {
  val tags = if (this.tags == null) "" else this.tags
  val split = tags.split(",")
  return HashSet<String>(split)
}

fun Note.toggleTag(tag: Tag) {
  val tags = getTagUUIDs()
  when (tags.contains(tag.uuid)) {
    true -> tags.remove(tag.uuid)
    false -> tags.add(tag.uuid)
  }
  this.tags = tags.joinToString(separator = ",")
}

fun Note.getExportableTags(context: Context): JSONArray {
  val tags = getTags(context)
  val exportableTags = JSONArray()
  for (tag in tags) {
    exportableTags.put(ExportableTag(tag).toJSONObject())
  }
  return exportableTags
}


/**************************************************************************************
 ******************************* Note Action Functions ********************************
 **************************************************************************************/
fun Note.search(keywords: String): Boolean {
  return searchInNote(this, keywords)
}

fun Note.mark(context: Context, noteState: NoteState) {
  this.state = noteState.name
  this.updateTimestamp = Calendar.getInstance().timeInMillis
  save(context)
}

fun Note.share(context: Context) {
  IntentUtils.ShareBuilder(context)
      .setSubject(getTitle())
      .setText(getText())
      .setChooserText(context.getString(R.string.share_using))
      .share()
}

fun Note.copy(context: Context) {
  TextUtils.copyToClipboard(context, getText())
}

fun Note.popup(activity: Activity) {
  FloatingNoteService.openNote(activity, this, true)
}

fun Note.edit(context: Context) {
  if (this.locked) {
    if (context is ThemedActivity) {
      EnterPincodeBottomSheet.openUnlockSheet(context, object : EnterPincodeBottomSheet.PincodeSuccessListener {
        override fun onFailure() {
          edit(context)
        }

        override fun onSuccess() {
          openEdit(context)
        }
      })
    }
    return
  }
  openEdit(context)
}

fun Note.openEdit(context: Context) {
  val intent = Intent(context, CreateOrEditAdvancedNoteActivity::class.java)
  intent.putExtra(INTENT_KEY_NOTE_ID, this.uid)
  context.startActivity(intent)
}


/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/
fun Note.save(context: Context) {
  saveWithoutSync(context)
  saveToSync()
}

fun Note.saveWithoutSync(context: Context) {
  val id = Note.db().insertNote(this)
  this.uid = if (isUnsaved()) id.toInt() else this.uid
  updateAsyncContent(context)
}

fun Note.saveToSync() {
  // Notify change to online/offline sync
}

private fun Note.updateAsyncContent(context: Context) {
  WidgetConfigureActivity.notifyNoteChange(context, this)
  val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
  if (Build.VERSION.SDK_INT >= 23 && notificationManager != null) {
    for (notification in notificationManager.activeNotifications) {
      if (notification.id == this.uid) {
        val handler = NotificationHandler(context, this)
        handler.createNotificationChannel()
        handler.openNotification()
      }
    }
  }
}

fun Note.delete(context: Context) {
  deleteWithoutSync(context)
  deleteToSync()
  WidgetConfigureActivity.notifyNoteChange(context, this)
  val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
  notificationManager?.cancel(this.uid)
}

fun Note.deleteWithoutSync(context: Context) {
  deleteAllFiles(context, this)
  if (isUnsaved()) {
    return
  }
  Note.db().delete(this)
  this.description = Format.getNote(ArrayList())
  this.uid = 0
}

fun Note.deleteToSync() {
  // Notify change to online/offline sync
}

fun Note.deleteOrMoveToTrash(context: Context) {
  if (getNoteState() === NoteState.TRASH) {
    delete(context)
    return
  }
  mark(context, NoteState.TRASH)
}
