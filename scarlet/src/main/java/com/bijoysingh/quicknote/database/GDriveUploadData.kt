package com.bijoysingh.quicknote.database

import android.arch.persistence.room.*

enum class GDriveDataType {
  NOTE,
  TAG,
  FOLDER,
  IMAGE,
}

@Entity(tableName = "gdrive_upload", indices = [Index("uuid")])
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

  @Ignore
  fun save(dao: GDriveUploadDataDao) {
    val id = dao.insert(this)
    uid = if (uid == 0) id.toInt() else uid
  }
}

@Dao
interface GDriveUploadDataDao {
  @get:Query("SELECT * FROM gdrive_upload")
  val all: List<GDriveUploadData>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(note: GDriveUploadData): Long

  @Delete
  fun delete(note: GDriveUploadData)

  @Query("DELETE FROM gdrive_upload WHERE 1")
  fun drop()

  @Query("SELECT * FROM gdrive_upload WHERE uid = :uid LIMIT 1")
  fun getByID(uid: Int): GDriveUploadData?

  @Query("SELECT * FROM gdrive_upload WHERE uuid = :uuid AND type = :type LIMIT 1")
  fun getByUUID(type: String, uuid: String): GDriveUploadData?

  @Query("SELECT * FROM gdrive_upload WHERE type = :type")
  fun getByType(type: String): List<GDriveUploadData>
}
