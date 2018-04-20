package com.maubis.scarlet.base.note

interface INoteContainer {
  fun uuid(): String

  fun description(): String

  fun timestamp(): Long

  fun updateTimestamp(): Long

  fun color(): Int

  fun state(): String

  fun tags(): String

  fun meta(): Map<String, Any>

  fun locked(): Boolean

  fun pinned(): Boolean
}