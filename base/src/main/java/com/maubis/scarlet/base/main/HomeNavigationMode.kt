package com.maubis.scarlet.base.main

import com.maubis.scarlet.base.R

/**
 * Superset of the Note State class
 */
enum class HomeNavigationMode {
  DEFAULT {
    override val toolbarTitleResourceId: Int = R.string.app_name
    override val toolbarIconResourceId: Int = R.drawable.app_icon_round
  },
  TRASH {
    override val toolbarTitleResourceId: Int = R.string.nav_trash
    override val toolbarIconResourceId: Int = R.drawable.ic_delete_white_48dp
  },
  FAVOURITE {
    override val toolbarTitleResourceId: Int = R.string.nav_favourites
    override val toolbarIconResourceId: Int = R.drawable.ic_favorite_white_48dp
  },
  ARCHIVED {
    override val toolbarTitleResourceId: Int = R.string.nav_archived
    override val toolbarIconResourceId: Int = R.drawable.ic_archive_white_48dp
  },
  LOCKED {
    override val toolbarTitleResourceId: Int = R.string.nav_locked
    override val toolbarIconResourceId: Int = R.drawable.ic_action_lock
  };

  abstract val toolbarTitleResourceId: Int
  abstract val toolbarIconResourceId: Int
}