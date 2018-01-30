package com.bijoysingh.quicknote.database;

import android.app.Activity;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity;
import com.bijoysingh.quicknote.activities.ThemedActivity;
import com.bijoysingh.quicknote.activities.WidgetConfigureActivity;
import com.bijoysingh.quicknote.activities.external.ExportableTag;
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.service.FloatingNoteService;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.IntentUtils;
import com.github.bijoysingh.starter.util.TextUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivityKt.INTENT_KEY_NOTE_ID;
import static com.bijoysingh.quicknote.activities.external.ExportNotesKt.searchInNote;
import static com.bijoysingh.quicknote.utils.TextInputUtilsKt.removeMarkdownHeaders;
import static com.bijoysingh.quicknote.utils.TextInputUtilsKt.renderMarkdown;

@Entity(
    tableName = "note",
    indices = {@Index("uid")}
)
public class Note {
  @PrimaryKey(autoGenerate = true)
  public Integer uid;

  @Deprecated
  public String title = "";

  public String description;

  @Deprecated
  public String displayTimestamp = "";

  public Long timestamp;

  public Integer color;

  public String state;

  public boolean locked;

  public String tags;

  public long updateTimestamp;

  public boolean pinned;

  public String uuid;

  public boolean isUnsaved() {
    return uid == null || uid == 0;
  }

  /*Content and Display Information Functions*/
  public String getTitle() {
    List<Format> formats = Format.Companion.getFormats(description);
    if (!formats.isEmpty() && formats.get(0).getFormatType() == FormatType.HEADING) {
      return formats.get(0).getText();
    }
    return "";
  }

  public String getText() {
    String text = "";
    List<Format> formats = Format.Companion.getFormats(description);
    for (Format format : formats) {
      if (format.getFormatType() == FormatType.HEADING) {
        continue;
      }
      text += format.getMarkdownText() + "\n";
    }
    return text.trim();
  }

  public String getFullText() {
    StringBuilder builder = new StringBuilder();
    List<Format> formats = Format.Companion.getFormats(description);
    for (Format format : formats) {
      builder.append(format.getMarkdownText());
      builder.append("\n\n");
    }
    return builder.toString().trim();
  }

  public CharSequence getLockedText(Context context, boolean isMarkdownEnabled) {
    if (locked) {
      return "******************\n***********\n****************";
    }
    if (!isMarkdownEnabled) {
      return getText();
    }

    return renderMarkdown(context, removeMarkdownHeaders(getText()));
  }

  public String getDisplayTime() {
    long time = updateTimestamp != 0 ? updateTimestamp : (timestamp == null ? 0 : timestamp);
    return DateFormatter.getDate("dd MMMM yyyy", time);
  }

  public List<Format> getFormats() {
    return Format.Companion.getFormats(description);
  }

  public NoteState getNoteState() {
    try {
      return NoteState.valueOf(state);
    } catch (Exception exception) {
      return NoteState.DEFAULT;
    }
  }

  /*Note Action Functions*/
  public boolean search(String keywords) {
    return searchInNote(this, keywords);
  }

  public void mark(Context context, NoteState noteState) {
    state = noteState.name();
    updateTimestamp = Calendar.getInstance().getTimeInMillis();
    save(context);
  }

  public void share(Context context) {
    new IntentUtils.ShareBuilder(context)
        .setSubject(getTitle())
        .setText(getText())
        .setChooserText(context.getString(R.string.share_using))
        .share();
  }

  public void copy(Context context) {
    TextUtils.copyToClipboard(context, getText());
  }

  public void popup(Activity activity) {
    FloatingNoteService.Companion.openNote(activity, this, true);
  }

  public void edit(final Context context) {
    if (context instanceof ThemedActivity && locked) {
      EnterPincodeBottomSheet.Companion.openUnlockSheet(
          (ThemedActivity) context,
          new EnterPincodeBottomSheet.PincodeSuccessListener() {
            @Override
            public void onFailure() {
              edit(context);
            }

            @Override
            public void onSuccess() {
              context.startActivity(getEditIntent(context));
            }
          },
          DataStore.get(context));
      return;
    } else if (locked) {
      return;
    }
    context.startActivity(getEditIntent(context));
  }

  public void startEditActivity(Context context) {
    Intent intent = getEditIntent(context);
    context.startActivity(intent);
  }

  private Intent getEditIntent(Context context) {
    Intent intent = new Intent(context, CreateOrEditAdvancedNoteActivity.class);
    intent.putExtra(INTENT_KEY_NOTE_ID, uid);
    return intent;
  }

  /*Tags Functions*/
  @Deprecated
  public Set<Integer> getTagIDs() {
    tags = tags == null ? "" : tags;
    String[] split = tags.split(",");
    Set<Integer> tagIDs = new HashSet<>();
    for (String tagIDString : split) {
      try {
        int tagID = Integer.parseInt(tagIDString);
        tagIDs.add(tagID);
      } catch (Exception exception) {
        // Ignore the exception
      }
    }
    return tagIDs;
  }

  public Set<String> getTagUUIDs() {
    tags = tags == null ? "" : tags;
    String[] split = tags.split(",");
    Set<String> tagIDs = new HashSet<>();
    Collections.addAll(tagIDs, split);
    return tagIDs;
  }

  public void toggleTag(Tag tag) {
    Set<String> tags = getTagUUIDs();
    if (tags.contains(tag.uuid)) {
      tags.remove(tag.uuid);
    } else {
      tags.add(tag.uuid);
    }
    this.tags = android.text.TextUtils.join(",", tags);
  }

  public Set<Tag> getTags(Context context) {
    Set<Tag> tags = new HashSet<>();
    for (String tagID : getTagUUIDs()) {
      Tag tag = Tag.db(context).getByUUID(tagID);
      if (tag != null) {
        tags.add(tag);
      }
    }
    return tags;
  }

  public String getTagString(Context context) {
    Set<Tag> tags = getTags(context);
    return getTagString(tags);
  }

  public JSONArray getExportableTags(Context context) {
    Set<Tag> tags = getTags(context);
    JSONArray exportableTags = new JSONArray();
    for (Tag tag : tags) {
      exportableTags.put((new ExportableTag(tag).toJSONObject()));
    }
    return exportableTags;
  }

  @NonNull
  public static String getTagString(Set<Tag> tags) {
    StringBuilder builder = new StringBuilder();
    for (Tag tag : tags) {
      builder.append('`');
      builder.append(tag.title);
      builder.append('`');
      builder.append(" ");
    }
    return builder.toString();
  }

  /*Database Functions*/
  public void save(Context context) {
    saveWithoutSync(context);
    saveToSync();
    WidgetConfigureActivity.Companion.notifyNoteChange(context, this);
  }

  public void saveWithoutSync(Context context) {
    long id = Note.db(context).insertNote(this);
    uid = isUnsaved() ? ((int) id) : uid;
  }

  public static NoteDao db(Context context) {
    return AppDatabase.getDatabase(context).notes();
  }

  public void saveToSync() {
    // Notify change to online/offline sync
  }

  public void delete(Context context) {
    deleteWithoutSync(context);
    deleteToSync();
    WidgetConfigureActivity.Companion.notifyNoteChange(context, this);
  }

  public void deleteWithoutSync(Context context) {
    if (isUnsaved()) {
      return;
    }
    Note.db(context).delete(this);
    description = Format.Companion.getNote(new ArrayList<Format>());
    uid = 0;
  }

  public void deleteToSync() {
    // Notify change to online/offline sync
  }

  public boolean isEqual(Note note) {
    return TextUtils.areEqualNullIsEmpty(state, note.state)
        && TextUtils.areEqualNullIsEmpty(description, note.description)
        && TextUtils.areEqualNullIsEmpty(uuid, note.uuid)
        && TextUtils.areEqualNullIsEmpty(tags, note.tags)
        && (timestamp.longValue() == note.timestamp.longValue())
        && color.intValue() == note.color.intValue()
        && locked == note.locked
        && pinned == note.pinned;
  }

  public Note copyNote(Note reference) {
    uid = reference.uid;
    uuid = reference.uuid;
    state = reference.state;
    description = reference.description;
    timestamp = reference.timestamp;
    updateTimestamp = reference.updateTimestamp;
    color = reference.color;
    tags = reference.tags;
    pinned = reference.pinned;
    locked = reference.locked;
    return this;
  }
}
