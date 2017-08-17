package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.FloatingNoteService;
import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.AdvancedNoteActivity;
import com.bijoysingh.quicknote.activities.MainActivity;
import com.bijoysingh.quicknote.activities.NoteActivity;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.items.NoteRecyclerItem;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;
import com.github.bijoysingh.starter.util.TextUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NoteRecyclerHolder extends RecyclerViewHolder<NoteRecyclerItem> {

  private CardView view;
  private TextView timestamp;
  private TextView title;
  private TextView description;
  private ImageView share;
  private ImageView delete;
  private ImageView copy;
  private MainActivity activity;

  /**
   * Constructor for the recycler view holder
   *
   * @param context the application/activity context
   * @param view    the view of the current item
   */
  public NoteRecyclerHolder(Context context, View view) {
    super(context, view);
    this.view = (CardView) view;
    timestamp = (TextView) view.findViewById(R.id.timestamp);
    title = (TextView) view.findViewById(R.id.title);
    description = (TextView) view.findViewById(R.id.description);
    share = (ImageView) view.findViewById(R.id.share_button);
    delete = (ImageView) view.findViewById(R.id.delete_button);
    activity = (MainActivity) context;
    copy = (ImageView) view.findViewById(R.id.copy_button);
  }

  @Override
  public void populate(NoteRecyclerItem item, Bundle extra) {
    final Note data = item.note;
    String noteTitle = data.getTitle();
    title.setText(noteTitle);
    title.setVisibility(noteTitle.isEmpty() ? GONE : VISIBLE);

    description.setText(data.getText());
    timestamp.setText(data.displayTimestamp);

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(context, AdvancedNoteActivity.class);
        intent.putExtra(NoteActivity.NOTE_ID, data.uid);
        context.startActivity(intent);
      }
    });
    view.setCardBackgroundColor(data.color);
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.deleteItem(data);
      }
    });
    share.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new TextUtils.ShareBuilder(context)
            .setSubject(data.title)
            .setText(data.description)
            .share();
      }
    });
    copy.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextUtils.copyToClipboard(context, data.description);
      }
    });

    view.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        CharSequence colors[] = new CharSequence[]{
            context.getString(R.string.open_in_popup),
            context.getString(R.string.delete_note),
            context.getString(R.string.send_note),
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_action);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                FloatingNoteService.openNote(activity, data, true);
                break;
              case 1:
                activity.deleteItem(data);
                break;
              case 2:
                new TextUtils.ShareBuilder(context)
                    .setSubject(data.title)
                    .setText(data.description)
                    .share();
                break;
            }
          }
        });
        builder.setCancelable(true);
        builder.show();
        return false;
      }
    });
  }
}
