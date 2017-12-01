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

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.CreateSimpleNoteActivity;
import com.bijoysingh.quicknote.activities.ThemedActivity;
import com.bijoysingh.quicknote.activities.MainActivity;
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity;
import com.bijoysingh.quicknote.activities.sheets.NoteOptionsBottomSheet;
import com.bijoysingh.quicknote.database.Note;
import com.bijoysingh.quicknote.items.NoteRecyclerItem;
import com.bijoysingh.quicknote.items.RecyclerItem;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NoteRecyclerHolder extends RecyclerViewHolder<RecyclerItem> {

  private CardView view;
  private TextView timestamp;
  private TextView title;
  private TextView description;
  private ImageView edit;
  private ImageView share;
  private ImageView delete;
  private ImageView moreOptions;
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
    timestamp = view.findViewById(R.id.timestamp);
    title = view.findViewById(R.id.title);
    description = view.findViewById(R.id.description);
    share = view.findViewById(R.id.share_button);
    delete = view.findViewById(R.id.delete_button);
    activity = (MainActivity) context;
    moreOptions = view.findViewById(R.id.options_button);
    edit = view.findViewById(R.id.edit_button);
  }

  @Override
  public void populate(RecyclerItem itemData, Bundle extra) {
    NoteRecyclerItem item = (NoteRecyclerItem) itemData;
    final Note data = item.note;
    String noteTitle = data.getTitle();
    title.setText(noteTitle);
    title.setVisibility(noteTitle.isEmpty() ? GONE : VISIBLE);

    description.setText(data.getText());
    timestamp.setText(data.displayTimestamp);

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(context, ViewAdvancedNoteActivity.class);
        intent.putExtra(CreateSimpleNoteActivity.NOTE_ID, data.uid);
        intent.putExtra(ThemedActivity.Companion.getKey(), ((ThemedActivity) context).isNightMode());
        context.startActivity(intent);
      }
    });
    view.setCardBackgroundColor(data.color);
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        activity.moveItemToTrashOrDelete(data);
      }
    });
    share.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        data.share(context);
      }
    });
    moreOptions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        NoteOptionsBottomSheet.Companion.openSheet(activity, data);
      }
    });
    edit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        data.edit(context);
      }
    });

    view.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View view) {
        CharSequence colors[] = new CharSequence[]{
            context.getString(R.string.edit_note),
            context.getString(R.string.send_note),
            context.getString(R.string.copy_note),
            context.getString(R.string.delete_note),
            context.getString(R.string.open_in_popup),
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_action);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which) {
              case 0:
                data.edit(context);
                break;
              case 1:
                data.share(context);
                break;
              case 2:
                data.copy(context);
                break;
              case 3:
                activity.moveItemToTrashOrDelete(data);
                break;
              case 4:
                data.popup(activity);
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
