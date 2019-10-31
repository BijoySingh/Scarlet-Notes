package com.bijoysingh.quicknote.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import com.google.gson.Gson
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow

enum class RemoteDataType {
  NOTE,
  NOTE_META,
  TAG,
  FOLDER,
  IMAGE,
}

@Entity(tableName = "gdrive_upload", indices = [Index("uuid", "type", unique = true)])
class RemoteUploadData {
  @PrimaryKey(autoGenerate = true)
  var uid: Int = 0

  var uuid: String = ""

  var type: String = ""

  var fileId: String = ""

  var lastUpdateTimestamp: Long = 0L

  var localStateDeleted: Boolean = false

  @ColumnInfo(name = "gDriveUpdateTimestamp")
  var remoteUpdateTimestamp: Long = 0L

  @ColumnInfo(name = "gDriveStateDeleted")
  var remoteStateDeleted: Boolean = false

  var attempts: Long = 0L

  var lastAttemptTime: Long = 0L

  @Ignore
  fun save(dao: RemoteUploadDataDao) {
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

object RemoteDatabaseHelper {
  fun getByUUID(type: RemoteDataType, uuid: String): RemoteUploadData {
    return getByUUID(type.name, uuid)
  }

  fun getByUUID(itemType: String, itemUuid: String): RemoteUploadData {
    return remoteDatabase?.getByUUID(itemType, itemUuid) ?: RemoteUploadData().apply {
      this.uuid = itemUuid
      this.type = itemType
    }
  }
}

@Dao
interface RemoteUploadDataDao {
  @get:Query("SELECT * FROM gdrive_upload")
  val all: List<RemoteUploadData>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(note: RemoteUploadData): Long

  @Query("UPDATE gdrive_upload SET attempts = 0")
  fun resetAttempts()

  @Delete
  fun delete(note: RemoteUploadData)

  @Query(
    "SELECT * " +
      "FROM gdrive_upload " +
      "WHERE uid = :uid " +
      "LIMIT 1")
  fun getByID(uid: Int): RemoteUploadData?

  @Query(
    "SELECT * " +
      "FROM gdrive_upload " +
      "WHERE uuid = :uuid AND type = :type " +
      "LIMIT 1")
  fun getByUUID(type: String, uuid: String): RemoteUploadData?

  @Query(
    "SELECT * " +
      "FROM gdrive_upload " +
      "WHERE type = :type")
  fun getByType(type: String): List<RemoteUploadData>

  @Query(
    "SELECT COUNT(*) " +
      "FROM gdrive_upload " +
      "WHERE (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getPendingCount(): Int

  @Query(
    "SELECT * " +
      "FROM gdrive_upload " +
      "WHERE (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getAllPending(): List<RemoteUploadData>

  @Query(
    "SELECT * " +
      "FROM gdrive_upload " +
      "WHERE type = :type AND (lastUpdateTimestamp != gDriveUpdateTimestamp OR localStateDeleted != gDriveStateDeleted)")
  fun getPendingByType(type: String): List<RemoteUploadData>
}
