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
import com.bijoysingh.quicknote.activities.external.ExportableNote;
import com.bijoysingh.quicknote.activities.external.ExportableTag;
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.formats.NoteType;
import com.bijoysingh.quicknote.service.FloatingNoteService;
import com.bijoysingh.quicknote.utils.NoteState;
import com.github.bijoysingh.starter.prefs.DataStore;
import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.IntentUtils;
import com.github.bijoysingh.starter.util.RandomHelper;
import com.github.bijoysingh.starter.util.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity.NOTE_ID;
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

  public String getText() {
    String text = "";
    List<Format> formats = Format.getFormats(description);
    for (Format format : formats) {
      if (format.formatType == FormatType.HEADING) {
        continue;
      }
      text += format.getMarkdownText() + "\n";
    }
    return text.trim();
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

  public NoteState getNoteState() {
    try {
      return NoteState.valueOf(state);
    } catch (Exception exception) {
      return NoteState.DEFAULT;
    }
  }

  public String getTitle() {
    List<Format> formats = Format.getFormats(description);
    if (!formats.isEmpty() && formats.get(0).formatType == FormatType.HEADING) {
      return formats.get(0).text;
    }
    return "";
  }

  public boolean search(String keywords) {
    return searchInNote(this, keywords);
  }

  public void save(Context context) {
    long id = Note.db(context).insertNote(this);
    uid = isUnsaved() ? ((int) id) : uid;
  }

  public void delete(Context context) {
    if (isUnsaved()) {
      return;
    }
    Note.db(context).delete(this);
    description = Format.getNote(new ArrayList<Format>());
    uid = 0;
  }

  public void mark(Context context, NoteState noteState) {
    state = noteState.name();
    save(context);
  }

  public String getDisplayTime() {
    long time = updateTimestamp != 0 ? updateTimestamp : (timestamp == null ? 0 : timestamp);
    return DateFormatter.getDate("dd MMMM yyyy", time);
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

  public Intent editIntent(Context context) {
    Intent intent = new Intent(context, CreateOrEditAdvancedNoteActivity.class);
    intent.putExtra(NOTE_ID, uid);
    return intent;
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
              context.startActivity(editIntent(context));
            }
          },
          DataStore.get(context));
      return;
    } else if (locked) {
      return;
    }
    context.startActivity(editIntent(context));
  }

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

  public void toggleTag(Tag tag) {
    Set<Integer> tags = getTagIDs();
    if (tags.contains(tag.uid)) {
      tags.remove(tag.uid);
    } else {
      tags.add(tag.uid);
    }
    this.tags = android.text.TextUtils.join(",", tags);
  }

  public Set<Tag> getTags(Context context) {
    Set<Tag> tags = new HashSet<>();
    for (Integer tagID : getTagIDs()) {
      Tag tag = Tag.db(context).getByID(tagID);
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

  public void edit(Context context, boolean nightMode) {
    Intent intent = editIntent(context);
    intent.putExtra(ThemedActivity.Companion.getKey(), nightMode);
    context.startActivity(intent);
  }

  public List<Format> getFormats() {
    return Format.getFormats(description);
  }

  public static NoteDao db(Context context) {
    return AppDatabase.getDatabase(context).notes();
  }

  public static Note gen() {
    Note note = new Note();
    note.uuid = RandomHelper.getRandomString(24);
    note.state = NoteState.DEFAULT.name();
    note.timestamp = Calendar.getInstance().getTimeInMillis();
    note.updateTimestamp = note.timestamp;
    note.color = 0xFF00796B;
    return note;
  }

  public static Note genWithColor(int color) {
    Note note = Note.gen();
    note.color = color;
    return note;
  }

  public static Note genSave(Context context, ExportableNote exportableNote) {
    Note note = Note.gen();
    note.color = exportableNote.getColor();
    note.description = exportableNote.getDescription();
    note.timestamp = exportableNote.getTimestamp();
    note.updateTimestamp = note.timestamp;
    for (int index = 0; index < exportableNote.getTags().length(); index++) {
      try {
        Tag tag = ExportableTag.Companion.getBestPossibleTagObject(
            context,
            exportableNote.getTags().getJSONObject(index));
        note.toggleTag(tag);
      } catch (JSONException exception) {
        // Ignore this exception
      }
    }
    note.save(context);
    return note;
  }

  public static Note gen(String title, String description) {
    Note note = Note.gen();
    List<Format> formats = new ArrayList<>();
    if (!TextUtils.isNullOrEmpty(title)) {
      formats.add(new Format(FormatType.HEADING, title));
    }
    formats.add(new Format(FormatType.TEXT, description));
    note.description = Format.getNote(formats);
    return note;
  }
}
