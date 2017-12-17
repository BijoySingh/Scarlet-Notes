package com.bijoysingh.quicknote.formats;

import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.recyclerview.FormatListViewHolder;
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder;
import com.github.bijoysingh.starter.recyclerview.MultiRecyclerViewControllerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Format {

  public static final String KEY_NOTE = "note";

  public FormatType formatType;

  public int uid;

  public String text;

  public Format() {
  }

  public Format(FormatType formatType) {
    this.formatType = formatType;
  }

  public Format(FormatType formatType, String text) {
    this.formatType = formatType;
    this.text = text;
  }

  public JSONObject toJson() {
    if (text == null || text.trim().isEmpty()) {
      return null;
    }

    Map<String, Object> map = new HashMap<>();
    map.put("format", formatType.name());
    map.put("text", text);
    return new JSONObject(map);
  }

  public static Format fromJson(JSONObject json) throws JSONException {
    Format format = new Format();
    format.formatType = FormatType.valueOf(json.getString("format"));
    format.text = json.getString("text");
    return format;
  }

  public static String getNote(List<Format> formats) {
    JSONArray array = new JSONArray();
    for (Format format : formats) {
      JSONObject json = format.toJson();
      if (json != null) array.put(json);
    }

    Map<String, Object> cache = new HashMap<>();
    cache.put(KEY_NOTE, array);
    return new JSONObject(cache).toString();
  }

  private int[] findFirstTwoOccurance(String source, String pattern) {
    Matcher matcher = Pattern.compile("(?=(aa))").matcher(source);
    List<Integer> position = new ArrayList<>();
    while (matcher.find()) {
      position.add(matcher.start());
    }

    if (position.size() < 2) {
      return null;
    }
    return new int[]{position.get(0), position.get(1)};
  }

  public static List<Format> getFormats(String note) {
    List<Format> formats = new ArrayList<>();
    try {
      JSONObject json = new JSONObject(note);
      JSONArray array = json.getJSONArray(KEY_NOTE);
      for (int index = 0; index < array.length(); index++) {
        try {
          Format format = fromJson(array.getJSONObject(index));
          format.uid = formats.size();
          formats.add(format);
        } catch (JSONException innerException) {
          Log.d(Format.class.getSimpleName(), innerException.toString());
        }
      }
    } catch (Exception exception) {
      Log.d(Format.class.getSimpleName(), exception.toString());
    }
    return formats;
  }

  public static List<MultiRecyclerViewControllerItem<Format>> getList() {
    List<MultiRecyclerViewControllerItem<Format>> list = new ArrayList<>();
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.MARKDOWN.ordinal())
            .layoutFile(R.layout.item_format_text)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.TEXT.ordinal())
            .layoutFile(R.layout.item_format_text)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.HEADING.ordinal())
            .layoutFile(R.layout.item_format_heading)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.SUB_HEADING.ordinal())
            .layoutFile(R.layout.item_format_sub_heading)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.QUOTE.ordinal())
            .layoutFile(R.layout.item_format_quote)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.CODE.ordinal())
            .layoutFile(R.layout.item_format_code)
            .holderClass(FormatTextViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.CHECKLIST_CHECKED.ordinal())
            .layoutFile(R.layout.item_format_list)
            .holderClass(FormatListViewHolder.class)
            .build());
    list.add(
        new MultiRecyclerViewControllerItem.Builder<Format>()
            .viewType(FormatType.CHECKLIST_UNCHECKED.ordinal())
            .layoutFile(R.layout.item_format_list)
            .holderClass(FormatListViewHolder.class)
            .build());
    return list;
  }

  public static FormatType getNextFormatType(FormatType type) {
    switch (type) {
      case BULLET_LIST:
        return FormatType.BULLET_LIST;
      case NUMBERED_LIST:
        return FormatType.NUMBERED_LIST;
      case HEADING:
        return FormatType.SUB_HEADING;
      case CHECKLIST_CHECKED:
      case CHECKLIST_UNCHECKED:
        return FormatType.CHECKLIST_UNCHECKED;
      case IMAGE:
      case SUB_HEADING:
      case CODE:
      case QUOTE:
      case TEXT:
      default:
        return FormatType.TEXT;
    }
  }
}
