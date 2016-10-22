package com.bijoysingh.quicknote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.github.bijoysingh.starter.recyclerview.RVHolder;

/**
 * Note item view holder
 * Created by bijoy on 5/4/16.
 */
public class NoteRVHolder extends RVHolder<NoteItem> {
    View view;
    TextView timestamp;
    TextView title;
    TextView description;
    MainActivity activity;

    public NoteRVHolder(Context context, View view) {
        super(context, view);
        this.view = view;
        timestamp = (TextView) view.findViewById(R.id.timestamp);
        title = (TextView) view.findViewById(R.id.title);
        description = (TextView) view.findViewById(R.id.description);
        activity = (MainActivity) context;
    }

    @Override
    public void populate(final NoteItem data) {
        super.populate(data);
        timestamp.setText(data.timestamp);
        title.setText(data.title);
        description.setText(data.description);

        if (data.title.isEmpty()) {
            title.setVisibility(View.GONE);
        } else {
            title.setVisibility(View.VISIBLE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra(NoteActivity.EXISTING_NOTE, data);
                context.startActivity(intent);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                CharSequence colors[] = new CharSequence[] {"Open In Popup", "Delete Note"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose...");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (Utils.requiresPermission(activity)) {
                                    Utils.startPermissionRequest(activity);
                                } else {
                                    Intent intent = new Intent(activity, NoteService.class);
                                    intent.putExtra(NoteActivity.EXISTING_NOTE, data);
                                    activity.startService(intent);
                                }
                                break;
                            case 1:
                                activity.deleteNote(data);
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
