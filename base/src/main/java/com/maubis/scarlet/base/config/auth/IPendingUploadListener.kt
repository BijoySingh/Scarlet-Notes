package com.maubis.scarlet.base.config.auth

interface IPendingUploadListener {
  /**
   * Fires when the pending state changes.
   */
  fun onPendingStateUpdate(isDataSyncPending: Boolean)
}