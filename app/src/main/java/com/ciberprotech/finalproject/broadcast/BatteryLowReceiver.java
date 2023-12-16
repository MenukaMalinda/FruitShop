package com.ciberprotech.finalproject.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.widget.Toast;
import android.provider.Settings;
import android.view.WindowManager;

public class BatteryLowReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context.getApplicationContext(), "Turn on your battery server.", Toast.LENGTH_SHORT).show();
        lowerBrightness(context);
    }

    private void lowerBrightness(Context context) {

        int brightnessValue = 30;
        ContentResolver contentResolver = context.getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.screenBrightness = brightnessValue / 255.0f;

        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            if (window != null) {
                window.setAttributes(layoutParams);
            }
        }
    }

}
