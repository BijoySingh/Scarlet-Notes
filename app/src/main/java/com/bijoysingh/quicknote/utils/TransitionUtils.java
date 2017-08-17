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
    if (transitionId <= 2) {
      transition2To3(context);
      manager.put(TRANSITION_ID.name(), 3);
    }
    if (transitionId <= 3) {
      transition3To4(context);
      manager.put(TRANSITION_ID.name(), 4);
    }
  }

  private static void transition1To2(Context context) {
    Note.transition1To2(context);
  }

  private static void transition2To3(Context context) {
    Note.transition2To3(context);
  }

  private static void transition3To4(Context context) {
    Note.transition3To4(context);
  }
}
