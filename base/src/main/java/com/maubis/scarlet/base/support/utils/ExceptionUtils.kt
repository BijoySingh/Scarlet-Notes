package com.maubis.scarlet.base.support.utils

import com.maubis.scarlet.base.BuildConfig
import java.lang.Exception

fun maybeThrow(message: String) {
  if (BuildConfig.DEBUG) {
    throw IllegalStateException(message)
  }
}

fun maybeThrow(exception: Exception) {
  if (BuildConfig.DEBUG) {
    throw exception
  }
}

fun <DataType> throwOrReturn(message: String, result: DataType): DataType {
  if (BuildConfig.DEBUG) {
    throw IllegalStateException(message)
  }
  return result
}

fun <DataType> throwOrReturn(exception: Exception, result: DataType): DataType {
  if (BuildConfig.DEBUG) {
    throw exception
  }
  return result
}
