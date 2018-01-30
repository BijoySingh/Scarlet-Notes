package com.bijoysingh.quicknote.activities

import com.bijoysingh.quicknote.formats.FormatType

class CreateAdvancedListActivity : CreateOrEditAdvancedNoteActivity() {

  override fun addDefaultItem() {
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED)
  }
}
