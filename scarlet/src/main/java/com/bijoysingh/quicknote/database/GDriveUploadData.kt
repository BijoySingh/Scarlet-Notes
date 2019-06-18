package com.bijoysingh.quicknote.database

import android.arch.persistence.room.*
import com.google.gson.Gson
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow

enum class GDriveDataType {
  NOTE,
  NOTE_META,
  TAG,
  FOLDER,
  IMAGE,
}

@Entity(tableName = "gdrive_upload", indices = [Index("uuid", "type", unique = true)])
class GDriveUploadData {
  @PrimaryKey(autoGenerate = true)
  var uid: Int = 0

  var uuid: String = ""

  var type: String = ""

  var fileId: String = ""

  var lastUpdateTimestamp: Long = 0L

  var localStateDeleted: Boolean = false

  var gDriveUpdateTimestamp: Long = 0L

  var gDriveStateDeleted: Boolean = false

  var attempts: Long = 0L

  var lastAttemptTime: Long = 0L

  @Ignore
  fun save(dao: GDriveUploadDataDao) {
    if (uuid.isBlank() || type.isBlank()) {
      maybeThrow("Invalid Dao")
      return
    }

    log("GDrive", "data = ${Gson().toJson(this)}")
    val id = dao.insert(this)
    uid = if (uid == 0) id.toInt() else uid
  }

  @Ignore
  fun unsaved(): Boolean {
    return uid == 0
  }
}

object GDriveDatabaseHelper {
  fun getByUUID(type: GDriveDataType, uuid: String): GDriveUploadData {
    return getByUUID(type.name, uuid)
  }

  fun getByUUID(itemType: String, itemUuid: String): GDriveUploadData {
    return gDriveDatabase?.getByUUID(itemType, itemUuid) ?: GDriveUploadData().apply {
      this.uuid = itemUuid
      this.type = itemType
    }
  }}

@Dao
interface GDriveUploadDataDao {
  @get:Query("SELECT * FROM gdrive_upload")
  val all: List<GDriveUploadData>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(note: GDriveUploadData): Long

  @Query("UPDATE gdrive_upload SET attempts = 0")
  fun resetAttempts()

  @Delete
  fun delete(note: GDriveUploadData)

  @Query("SELECT * FROM gdrive_upload WHERE uid = :uid LIMIT 1")
  fun getByID(uid: Int): GDriveUploadData?

  @Query("SELECT * FROM gdrive_upload WHERE uuid = :uuid AND type = :type LIMIT 1")
  fun getByUUID(type: String, uuid: String): GDriveUploadData?

  @Query("SELECT * FROM gdrive_upload WHERE type = :type")
  fun getByType(type: String): List<GDriveUploadData>

  @Query("SELECT COUNT(*) FROM gdrive_upload WHERE (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getPendingCount(): Int

  @Query("SELECT * FROM gdrive_upload WHERE (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getAllPending(): List<GDriveUploadData>

  @Query("SELECT * FROM gdrive_upload WHERE type = :type AND (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getPendingByType(type: String): List<GDriveUploadData>
}
