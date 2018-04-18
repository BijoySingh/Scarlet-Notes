package com.bijoysingh.quicknote.activities.external

import com.maubis.scarlet.base.database.room.tag.Tag
import com.bijoysingh.quicknote.utils.genImportedTag
import com.bijoysingh.quicknote.utils.getNewNoteUUID
import org.json.JSONObject
import java.io.Serializable

class ExportableTag(
    var uuid: String,
    var title: String
) : Serializable {

  constructor(tag: Tag) : this(
      tag.uuid,
      tag.title
  )

  companion object {

    fun fromJSONObjectV1(json: JSONObject): ExportableTag {
      return ExportableTag(
          getNewNoteUUID(),
          json["title"] as String)
    }

    fun getBestPossibleTagObject(json: JSONObject): Tag {
      val version = if (json.has("version")) json.getInt("version") else 1
      val exportableTag = when (version) {
        1 -> ExportableTag.fromJSONObjectV1(json)
        else -> ExportableTag.fromJSONObjectV1(json)
      }
      return genImportedTag(exportableTag)
    }
  }
}