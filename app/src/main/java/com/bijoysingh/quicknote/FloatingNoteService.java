package com.bijoysingh.quicknote;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsk.floatingbubblelib.FloatingBubbleConfig;
import com.bsk.floatingbubblelib.FloatingBubblePermissions;
import com.bsk.floatingbubblelib.FloatingBubbleService;
import com.github.bijoysingh.starter.util.DateFormatter;
import com.github.bijoysingh.starter.util.TextUtils;

/**
 * The floating not service
 * Created by bijoy on 3/29/17.
 */

public class FloatingNoteService extends FloatingBubbleService {

  NoteItem noteItem;
  TextView title;
  TextView description;
  TextView timestamp;
  ImageView editButton;
  ImageView copyButton;

  @Override
  protected FloatingBubbleConfig getConfig() {
    return new FloatingBubbleConfig.Builder()
        .bubbleIcon(ContextCompat.getDrawable(getContext(), R.drawable.app_icon))
        .removeBubbleIcon(ContextCompat.getDrawable(
            getContext(),
            com.bsk.floatingbubblelib.R.drawable.close_default_icon))
        .bubbleIconDp(72)
        .removeBubbleIconDp(72)
        .paddingDp(8)
        .borderRadiusDp(4)
        .physicsEnabled(true)
        .expandableColor(0xFFFAFAFA)
        .triangleColor(0xFFFAFAFA)
        .gravity(Gravity.END)
        .expandableView(loadView())
        .removeBubbleAlpha(0.7f)
        .build();
  }

  @Override
  protected boolean onGetIntent(@NonNull Intent intent) {
    noteItem = null;
    if (intent.hasExtra(NoteActivity.EXISTING_NOTE)) {
      noteItem = (NoteItem) intent.getSerializableExtra(NoteActivity.EXISTING_NOTE);
    }
    return true;
  }

  private View loadView() {
    View rootView = getInflater().inflate(R.layout.layout_add_note_overlay, null);
    title = (TextView) rootView.findViewById(R.id.title);
    description = (TextView) rootView.findViewById(R.id.description);
    timestamp = (TextView) rootView.findViewById(R.id.timestamp);
    editButton = (ImageView) rootView.findViewById(R.id.save_button);
    editButton.setImageResource(R.drawable.ic_edit_white_48dp);
    editButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openNote();
        stopSelf();
      }
    });
    copyButton = (ImageView) rootView.findViewById(R.id.panel_copy_button);
    copyButton.setVisibility(View.VISIBLE);
    copyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextUtils.copyToClipboard(getContext(), noteItem.description);
      }
    });
    setNote();
    return rootView;
  }

  private void openNote() {
    Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
    intent.putExtra(NoteActivity.EXISTING_NOTE, noteItem);
    startActivity(intent);
  }

  public void setNote() {
    if (noteItem == null) {
      noteItem = new NoteItem(DateFormatter.getToday());
    }

    title.setText(noteItem.title);
    description.setText(noteItem.description);
    timestamp.setText(noteItem.timestamp);

    title.setVisibility(TextUtils.isNullOrEmpty(noteItem.title) ? View.GONE : View.VISIBLE);
    description.setVisibility(
        TextUtils.isNullOrEmpty(noteItem.description) ? View.GONE : View.VISIBLE);
  }

  public static void openNote(Activity activity, NoteItem note, boolean finishOnOpen) {
    if (FloatingBubblePermissions.requiresPermission(activity)) {
      FloatingBubblePermissions.startPermissionRequest(activity);
    } else {
      Intent intent = new Intent(activity, FloatingNoteService.class);
      if (note != null) {
        intent.putExtra(NoteActivity.EXISTING_NOTE, note);
      }
      activity.startService(intent);
      if (finishOnOpen) {
        activity.finish();
      }
    }
  }
}
