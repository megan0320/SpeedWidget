package com.example.speedapp;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

public class SpeedAppWidget extends AppWidgetProvider {

    static final String TAG = "SpeedAppWidget";
    String mCurrentSpeed="";
    Integer mCurrentSpeedVal=0;
    String mCurrentLatitude="";
    String mCurrentLongitude="";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "update AppWidget");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.speed_app_widget);

        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.asuka.naviportal");
        if (intent != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.navi_image, pendingIntent);
        }


        Intent serviceIntent = new Intent(context,SpeedService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }

        remoteViews.setTextViewText(R.id.appwidget_speed, mCurrentSpeed+" km/s");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCurrentSpeedVal = intent.getIntExtra("speedVal",0);

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
        Log.d(TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}
