package com.bijoysingh.quicknote.database.external

data class FirebaseNote(
    val uuid: String,
    val description: String,
    val timestamp: Long,
    val updateTimestamp: Long,
    val color: Int,
    val state: String,
    val tags: String,
    val locked: Boolean,
    val pinned: Boolean)