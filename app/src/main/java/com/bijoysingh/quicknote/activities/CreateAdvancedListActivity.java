package com.bijoysingh.quicknote.activities;

import com.bijoysingh.quicknote.formats.FormatType;

public class CreateAdvancedListActivity extends CreateOrEditAdvancedNoteActivity {

  protected void addDefaultItem() {
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
    addEmptyItem(FormatType.CHECKLIST_UNCHECKED);
  }

}
