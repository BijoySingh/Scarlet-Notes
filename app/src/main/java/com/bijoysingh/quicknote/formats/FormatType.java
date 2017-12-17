package com.bijoysingh.quicknote.formats;

public enum FormatType {
  MARKDOWN, // Ideally use TEXT. This Format will force markdown independent of user preferences
  TEXT,
  BULLET_LIST,
  NUMBERED_LIST,
  IMAGE,
  HEADING,
  SUB_HEADING,
  CHECKLIST_UNCHECKED,
  CHECKLIST_CHECKED,
  CODE,
  QUOTE,
}
