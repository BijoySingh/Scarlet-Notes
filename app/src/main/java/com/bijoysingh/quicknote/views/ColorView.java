package com.bijoysingh.quicknote.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.utils.CircleDrawable;

public class ColorView extends LinearLayout {

  public View root;
  ImageView icon;

  public ColorView(Context context) {
    super(context);
    init(context);
  }

  public void init(Context context) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    root = inflater.inflate(R.layout.layout_color, null);

    LayoutParams params =
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    root.setLayoutParams(params);
    icon = (ImageView) root.findViewById(R.id.color_icon);
    addView(root);
  }

  public ColorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ColorView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @TargetApi(21)
  public ColorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  public void setColor(final int color, boolean selected) {
    this.icon.setImageResource(selected ? R.drawable.ic_done_white_48dp : 0);
    this.icon.setBackground(new CircleDrawable(color));
    this.icon.setColorFilter(Color.WHITE);
  }
}
