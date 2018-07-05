package com.maubis.scarlet.base.export.data

import android.content.Context
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.generateUUID
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.tag.save
import com.maubis.scarlet.base.support.database.notesDB
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

class ExportableNote(
    var uuid: String,
    var description: String,
    var timestamp: Long,
    var updateTimestamp: Long,
    var color: Int,
    var state: String,
    var tags: String,
    var meta: Map<String, Any>,
    var folder: String
) : Serializable, INoteContainer {

  override fun uuid(): String = uuid

  override fun description(): String = description

  override fun timestamp(): Long = timestamp

  override fun updateTimestamp(): Long = updateTimestamp

  override fun color(): Int = color

  override fun state(): String = state

  override fun tags(): String = tags

  override fun meta(): Map<String, Any> = emptyMap()

  override fun locked(): Boolean = false

  override fun pinned(): Boolean = false

  override fun folder(): String = folder

  constructor(note: Note) : this(
      note.uuid,
      note.description,
      note.timestamp,
      note.updateTimestamp,
      note.color,
      note.state,
      note.tags ?: "",
      emptyMap(),
      note.folder
  )

  fun saveIfNeeded(context: Context) {
    val existingNote = notesDB.existingMatch(this)
    if (existingNote !== null && existingNote.updateTimestamp > this.updateTimestamp) {
      return
    }

    val note = NoteBuilder().copy(this)
    note.save(context)
  }

  companion object {

    val KEY_NOTES: String = "notes"

    fun fromJSONObjectV2(json: JSONObject): ExportableNote {
      return ExportableNote(
          generateUUID(),
          json["description"] as String,
          json["timestamp"] as Long,
          json["timestamp"] as Long,
          json["color"] as Int,
          "",
          "",
          emptyMap(),
          "")
    }

    fun fromJSONObjectV3(json: JSONObject): ExportableNote {
      return ExportableNote(
          generateUUID(),
          json["description"] as String,
          json["timestamp"] as Long,
          json["timestamp"] as Long,
          json["color"] as Int,
          json["state"] as String,
          convertTagsJSONArrayToString(json["tags"] as JSONArray),
          emptyMap(),
          "")
    }

    fun fromJSONObjectV4(json: JSONObject): ExportableNote {
      return ExportableNote(
          json["uuid"] as String,
          json["description"] as String,
          json["timestamp"] as Long,
          json["timestamp"] as Long,
          json["color"] as Int,
          json["state"] as String,
          convertTagsJSONArrayToString(json["tags"] as JSONArray),
          emptyMap(),
          "")
    }

    private fun convertTagsJSONArrayToString(tags: JSONArray): String {
      val noteTags = arrayListOf<Tag>()
      for (index in 0 until tags.length()) {
        val tag = ExportableTag.getBestPossibleTagObject(tags.getJSONObject(index))
        tag.save()
        noteTags.add(tag)
      }
      return noteTags.map { it.uuid }.joinToString(separator = ",")
    }
  }
}