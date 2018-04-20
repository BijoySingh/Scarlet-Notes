package com.bijoysingh.quicknote.export.data

import com.bijoysingh.quicknote.export.data.ExportableNote
import com.bijoysingh.quicknote.export.data.ExportableTag

class ExportableFileFormat(
    val version: Int,
    val notes: List<ExportableNote>,
    val tags: List<ExportableTag>)