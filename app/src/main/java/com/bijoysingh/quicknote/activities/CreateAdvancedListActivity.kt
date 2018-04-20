package com.bijoysingh.quicknote.activities

import com.maubis.scarlet.base.format.FormatType

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
