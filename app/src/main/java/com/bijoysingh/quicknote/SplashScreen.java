package com.bijoysingh.quicknote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.bsk.floatingbubblelib.FloatingBubblePermissions;
import com.github.bijoysingh.starter.util.PermissionManager;

public class SplashScreen extends AppCompatActivity {

  SplashScreen activity;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    activity = this;
    if (!FloatingBubblePermissions.requiresPermission(this)) {
      startActivity(new Intent(this, MainActivity.class));
      finish();
    } else {
      setContentView(R.layout.activity_splash_screen);
      TextView getStarted = (TextView) findViewById(R.id.get_started);
      getStarted.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          FloatingBubblePermissions.startPermissionRequest(activity);
        }
      });
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PermissionManager.REQUEST_CODE_ASK_PERMISSIONS) {
      if (!FloatingBubblePermissions.requiresPermission(this)) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
      }
    }
  }
}
