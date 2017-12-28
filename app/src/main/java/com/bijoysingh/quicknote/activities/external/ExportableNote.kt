package com.bijoysingh.quicknote.activities.external

import android.content.Context
import android.util.Base64
import com.bijoysingh.quicknote.database.Note
import org.json.JSONArray
import org.json.JSONObject
import java.io.*


class ExportableNote(
    var title: String,
    var description: String,
    var displayTimestamp: String,
    var timestamp: Long,
    var color: Int,
    var state: String,
    var tags: JSONArray
): Serializable {

  constructor(context: Context, note: Note) : this(
      note.title,
      note.description,
      note.displayTimestamp,
      note.timestamp,
      note.color,
      note.state,
      note.getExportableTags(context)
  )

  @Deprecated("Do not use this unless no context is available, tags wont be saved")
  constructor(note: Note) : this(
      note.title,
      note.description,
      note.displayTimestamp,
      note.timestamp,
      note.color,
      note.state,
      JSONArray()
  )

  fun toJSONObject(): JSONObject {
    val map = HashMap<String, Any>()
    map["title"] = title
    map["description"] = description
    map["displayTimestamp"] = displayTimestamp
    map["timestamp"] = timestamp
    map["color"] = color
    map["state"] = state
    map["tags"] = tags
    return JSONObject(map)
  }

  companion object {

    val KEY_NOTES: String = "notes"

    fun fromJSONObjectV2(json: JSONObject): ExportableNote {
      return ExportableNote(
          json["title"] as String,
          json["description"] as String,
          json["displayTimestamp"] as String,
          json["timestamp"] as Long,
          json["color"] as Int,
          "",
          JSONArray())
    }

    fun fromJSONObjectV3(json: JSONObject): ExportableNote {
      return ExportableNote(
          json["title"] as String,
          json["description"] as String,
          json["displayTimestamp"] as String,
          json["timestamp"] as Long,
          json["color"] as Int,
          json["state"] as String,
          json["tags"] as JSONArray)
    }

    fun fromBase64String(base64: String): ExportableNote {
      try {
        val data = Base64.decode(base64, 0)
        val objectInputStream = ObjectInputStream(ByteArrayInputStream(data))
        val obj = objectInputStream.readObject()
        objectInputStream.close()
        return obj as ExportableNote
      } catch (exception: Exception) {
        return ExportableNote(Note.gen("", base64))
      }
    }
  }
}