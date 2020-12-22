package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.database.room.tag.Tag

// TODO: Remove this on Firebase deprecation
fun Tag.getFirebaseTag(): FirebaseTag = FirebaseTag(uuid, title)
