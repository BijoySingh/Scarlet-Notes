package com.bijoysingh.quicknote.utils;

import android.content.Context;

import com.bijoysingh.quicknote.database.Note;
import com.github.bijoysingh.starter.prefs.StorageManager;

import static com.bijoysingh.quicknote.utils.StorageKeys.TRANSITION_ID;

public class TransitionUtils {
  public static void transition(Context context) {
    StorageManager manager = new StorageManager(context);
    int transitionId = manager.get(TRANSITION_ID.name(), 1);
    if (transitionId <= 1) {
      transition1To2(context);
      manager.put(TRANSITION_ID.name(), 2);
    }
  }

  private static void transition1To2(Context context) {
    Note.transition(context);
  }
}
