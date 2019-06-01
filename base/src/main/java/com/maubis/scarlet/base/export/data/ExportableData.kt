package com.maubis.scarlet.base.export.data

import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.tag.Tag
import org.json.JSONObject
import java.io.Serializable
import java.util.*

/**
 * Data class containing the note as a string of the content which can be stored in user
 * readable format (markdown) and meta data object
 */
class ExportableSplitNote(
    val content: String,
    val meta: ExportableNoteMeta) {

  // Default failsafe constructor for Gson to use
  constructor() : this(
      "",
      ExportableNoteMeta())
}

/**
 * Data class containing only the meta data for the note which makes it unique to Scarlet
 */
class ExportableNoteMeta(
    val uuid: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int,
    val state: String,
    val tags: String,
    val locked: Boolean,
    val pinned: Boolean,
    val folder: String) {

  // Default failsafe constructor for Gson to use
  constructor() : this(
      "invalid",
      Calendar.getInstance().timeInMillis,
      Calendar.getInstance().timeInMillis,
      -0xff8695,
      NoteState.DEFAULT.name,
      "",
      false,
      false,
      "")
}

/**
 * Data class for the exportability of tags
 */
class ExportableTag(
    var uuid: String,
    var title: String
) : Serializable, ITagContainer {

  override fun title(): String = title

  override fun uuid(): String = uuid

  // Default failsafe constructor for Gson to use
  constructor() : this("invalid", "")

  constructor(tag: Tag) : this(
      tag.uuid,
      tag.title
  )

  companion object {
    fun fromJSON(json: JSONObject): ExportableTag {
      val version = if (json.has("version")) json.getInt("version") else 1
      return when (version) {
        1 -> fromJSONObjectV1(json)
        else -> fromJSONObjectV1(json)
      }
    }

    fun fromJSONObjectV1(json: JSONObject): ExportableTag {
      return ExportableTag(
          generateUUID(),
          json["title"] as String)
    }

    fun getBestPossibleTagObject(json: JSONObject): Tag {
      return TagBuilder().copy(fromJSON(json))
    }
  }
}

/**
 * Data class for the exportability of folder
 */
class ExportableFolder(
    val uuid: String,
    val title: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int
) : Serializable, IFolderContainer {
  override fun timestamp(): Long = timestamp
  override fun updateTimestamp(): Long = updateTimestamp
  override fun color(): Int = color
  override fun title(): String = title
  override fun uuid(): String = uuid

  constructor(folder: Folder) : this(
      folder.uuid ?: "",
      folder.title ?: "",
      folder.timestamp ?: 0,
      folder.updateTimestamp,
      folder.color ?: 0)

  // Default failsafe constructor for Gson to use
  constructor() : this(
      "invalid",
      "",
      Calendar.getInstance().timeInMillis,
      Calendar.getInstance().timeInMillis,
      -0xff8695)
}