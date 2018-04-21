package com.bijoysingh.quicknote.firebase.data

import com.maubis.scarlet.base.core.database.room.tag.Tag


fun Tag.getFirebaseTag(): FirebaseTag = FirebaseTag(uuid, title)
