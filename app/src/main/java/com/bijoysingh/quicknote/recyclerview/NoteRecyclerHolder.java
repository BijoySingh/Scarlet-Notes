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
import com.bijoysingh.quicknote.activities.MainActivity;
import com.bijoysingh.quicknote.activities.NoteActivity;
import com.bijoysingh.quicknote.R;
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
    title.setText(data.title);
    title.setVisibility(data.title.isEmpty() ? GONE : VISIBLE);

    description.setText(data.description);
    timestamp.setText(data.displayTimestamp);

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra(NoteActivity.NOTE_ID, data.uid);
        context.startActivity(intent);
      }
    });
    view.setCardBackgroundColor(data.color);
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Note.db(context).delete(data);
      }
    });
    share.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new TextUtils.ShareBuilder(view.getContext())
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
        CharSequence colors[] = new CharSequence[]{"Open In Popup", "Delete Note"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose...");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                FloatingNoteService.openNote(activity, data, true);
                break;
              case 1:
                Note.db(context).delete(data);
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
