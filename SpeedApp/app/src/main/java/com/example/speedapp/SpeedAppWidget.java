package com.example.speedapp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * Implementation of App Widget functionality.
 */
public class SpeedAppWidget extends AppWidgetProvider {

    static final String TAG = "SpeedAppWidget";
    String mCurrentSpeed="";
    String mCurrentLatitude="";
    String mCurrentLongitude="";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "update AppWidget");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.speed_app_widget);

        Intent serviceIntent = new Intent(context,SpeedService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }

        remoteViews.setTextViewText(R.id.appwidget_speed, mCurrentSpeed+" meters/second");
        remoteViews.setTextViewText(R.id.appwidget_latitude_value, mCurrentLatitude);
        remoteViews.setTextViewText(R.id.appwidget_longitude_value, mCurrentLongitude);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("speed") != "") {
            mCurrentSpeed = intent.getStringExtra("speed");
        }
        if(intent.getStringExtra("latitude") != "") {
            mCurrentLatitude = intent.getStringExtra("latitude");
        }
        if(intent.getStringExtra("longitude") != "") {
            mCurrentLongitude = intent.getStringExtra("longitude");
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}