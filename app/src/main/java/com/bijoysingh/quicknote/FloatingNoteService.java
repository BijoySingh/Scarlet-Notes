package com.bijoysingh.quicknote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity;
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity;
import com.bijoysingh.quicknote.database.Note;
import com.bsk.floatingbubblelib.FloatingBubbleConfig;
import com.bsk.floatingbubblelib.FloatingBubblePermissions;
import com.bsk.floatingbubblelib.FloatingBubbleService;
import com.github.bijoysingh.starter.util.TextUtils;

import static com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity.NOTE_ID;

/**
 * The floating not service
 * Created by bijoy on 3/29/17.
 */

public class FloatingNoteService extends FloatingBubbleService {

  private Note note;
  private TextView title;
  private TextView description;
  private TextView timestamp;
  private View panel;

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
    note = null;
    if (intent.hasExtra(NOTE_ID)) {
      note = Note.db(getContext()).getByID(intent.getIntExtra(NOTE_ID, 0));
    }
    return true;
  }

  private View loadView() {
    View rootView = getInflater().inflate(R.layout.layout_add_note_overlay, null);

    title = (TextView) rootView.findViewById(R.id.title);
    description = (TextView) rootView.findViewById(R.id.description);
    timestamp = (TextView) rootView.findViewById(R.id.timestamp);


    ImageView editButton = (ImageView) rootView.findViewById(R.id.panel_edit_button);
    editButton.setImageResource(R.drawable.ic_edit_white_48dp);
    editButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        try {
          note.edit(getContext());
        } catch (Exception exception) {
          // Some issue
        }
        stopSelf();
      }
    });

    ImageView shareButton = (ImageView) rootView.findViewById(R.id.panel_share_button);
    shareButton.setImageResource(R.drawable.ic_share_white_48dp);
    shareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        note.share(getContext());
        stopSelf();
      }
    });

    ImageView copyButton = (ImageView) rootView.findViewById(R.id.panel_copy_button);
    copyButton.setVisibility(View.VISIBLE);
    copyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        note.copy(getContext());
        setState(false);
      }
    });

    panel = rootView.findViewById(R.id.panel_layout);
    panel.setBackgroundColor(note.color);

    setNote();
    return rootView;
  }

  public void setNote() {
    if (note == null) {
      note = Note.gen();
    }

    String noteTitle = note.getTitle();
    String noteDescription = note.getText();
    title.setText(noteTitle);
    description.setText(noteDescription);
    timestamp.setText(note.displayTimestamp);

    title.setVisibility(TextUtils.isNullOrEmpty(noteTitle) ? View.GONE : View.VISIBLE);
    description.setVisibility(TextUtils.isNullOrEmpty(noteDescription) ? View.GONE : View.VISIBLE);
  }

  public static void openNote(Activity activity, Note note, boolean finishOnOpen) {
    if (FloatingBubblePermissions.requiresPermission(activity)) {
      FloatingBubblePermissions.startPermissionRequest(activity);
    } else {
      Intent intent = new Intent(activity, FloatingNoteService.class);
      if (note != null) {
        intent.putExtra(NOTE_ID, note.uid);
      }
      activity.startService(intent);
      if (finishOnOpen) activity.finish();
    }
  }
}
