package com.example.speedapp;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of App Widget functionality.
 */
public class SpeedAppWidget extends AppWidgetProvider {

    static final String TAG = "SpeedAppWidget";
    String mCurrentSpeed="";
    Integer mCurrentSpeedVal=0;
    String mCurrentLatitude="";
    String mCurrentLongitude="";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "update AppWidget");

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.speed_app_widget);

        Intent serviceIntent = new Intent(context,SpeedService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }

        remoteViews.setTextViewText(R.id.appwidget_speed, mCurrentSpeed+" m/s");


        Bitmap bitmap = Bitmap.createBitmap(400, 180, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        remoteViews.setImageViewBitmap(R.id.widget_image, bitmap);

        getWidgetBitmap(context,mCurrentSpeedVal,bitmap);

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
    private Bitmap getWidgetBitmap(Context context, int percentage,Bitmap bitmap) {

        int width = 400;
        int height = 400;
        int stroke = 30;
        int padding = 5;
        float density = context.getResources().getDisplayMetrics().density;

        //Paint for arc stroke.
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        //paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setPathEffect(new CornerPathEffect(10) );

        //Paint for text values.
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(50);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final RectF arc = new RectF();
        arc.set((stroke/2) + padding, (stroke/2) + padding, width-padding-(stroke/2), height-padding-(stroke/2));

        //Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //First draw full arc as background.
        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawArc(arc, 135, 275, false, paint);
        //Then draw arc progress with actual value.
        paint.setColor(Color.WHITE);
        canvas.drawArc(arc, percentage, 200, false, paint);
        //Draw text value.
        canvas.drawText(percentage + "m/s", bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent()) / 2, mTextPaint);
        //Draw widget title.
        mTextPaint.setTextSize(50);
        //canvas.drawText(context.getString(R.string.widget_text_arc_battery), bitmap.getWidth() / 2, bitmap.getHeight()-(stroke+padding), mTextPaint);

        return  bitmap;
    }
}