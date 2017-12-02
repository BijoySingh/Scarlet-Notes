package com.bijoysingh.quicknote.activities.external

import android.util.Base64
import com.bijoysingh.quicknote.database.Note
import org.json.JSONObject
import java.io.*


class ExportableNote(
    var title: String,
    var description: String,
    var displayTimestamp: String,
    var timestamp: Long,
    var color: Int
): Serializable {

  constructor(note: Note) : this(
      note.title,
      note.description,
      note.displayTimestamp,
      note.timestamp,
      note.color
  )

  fun toJSONObject(): JSONObject {
    val map = HashMap<String, Any>()
    map["title"] = title
    map["description"] = description
    map["displayTimestamp"] = displayTimestamp
    map["timestamp"] = timestamp
    map["color"] = color
    return JSONObject(map)
  }

  fun toBase64String(): String {
    try {
      val byteArrayOutputStream = ByteArrayOutputStream()
      val outputStream = ObjectOutputStream(byteArrayOutputStream)
      outputStream.writeObject(this)
      outputStream.close()
      return Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0)
    } catch (exception: Exception) {
      // Ignore the response
      return description
    }
  }

  companion object {

    val KEY_NOTES: String = "notes"

    fun fromJSONObject(json: JSONObject): ExportableNote {
      return ExportableNote(
          json["title"] as String,
          json["description"] as String,
          json["displayTimestamp"] as String,
          json["timestamp"] as Long,
          json["color"] as Int)
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