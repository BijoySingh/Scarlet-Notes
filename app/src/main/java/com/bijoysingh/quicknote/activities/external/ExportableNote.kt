package com.bijoysingh.quicknote.activities.external

import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.utils.getNewNoteUUID
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
    var meta: Map<String, Any>
) : Serializable {

  constructor(note: Note) : this(
      note.uuid,
      note.description,
      note.timestamp,
      note.updateTimestamp,
      note.color,
      note.state,
      note.tags,
      emptyMap()
  )

  companion object {

    val KEY_NOTES: String = "notes"

    fun fromJSONObjectV2(json: JSONObject): ExportableNote {
      return ExportableNote(
          getNewNoteUUID(),
          json["description"] as String,
          json["timestamp"] as Long,
          json["timestamp"] as Long,
          json["color"] as Int,
          "",
          "",
          emptyMap())
    }

    fun fromJSONObjectV3(json: JSONObject): ExportableNote {
      return ExportableNote(
          getNewNoteUUID(),
          json["description"] as String,
          json["timestamp"] as Long,
          json["timestamp"] as Long,
          json["color"] as Int,
          json["state"] as String,
          convertTagsJSONArrayToString(json["tags"] as JSONArray),
          emptyMap())
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
          emptyMap())
    }

    private fun convertTagsJSONArrayToString(tags: JSONArray): String {
      val noteTags = arrayListOf<Tag>()
      for (index in 0 until tags.length()) {
        noteTags.add(ExportableTag.getBestPossibleTagObject(tags.getJSONObject(index)))
      }
      return noteTags.map { it.uuid }.joinToString(separator = ",")
    }
  }
}