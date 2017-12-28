package com.bijoysingh.quicknote.activities.external

import android.content.Context
import com.bijoysingh.quicknote.database.Tag
import org.json.JSONObject
import java.io.Serializable

const val TAG_EXPORT_VERSION = 1

class ExportableTag(
    var title: String
) : Serializable {

  constructor(tag: Tag) : this(
      tag.title
  )

  fun toJSONObject(): JSONObject {
    val map = HashMap<String, Any>()
    map["version"] = TAG_EXPORT_VERSION
    map["title"] = title
    return JSONObject(map)
  }

  companion object {

    fun fromJSONObjectV1(json: JSONObject): ExportableTag {
      return ExportableTag(
          json["title"] as String)
    }

    fun getBestPossibleTagObject(context: Context, json: JSONObject): Tag {
      val version = if (json.has("version")) json.getInt("version") else 1
      val exportableTag = when(version) {
        1 -> ExportableTag.fromJSONObjectV1(json)
        else -> ExportableTag.fromJSONObjectV1(json)
      }
      val tag = Tag.gen(exportableTag)
      tag.saveIfUnique(context)
      return tag
    }
  }
}