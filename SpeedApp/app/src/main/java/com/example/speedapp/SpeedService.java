package com.example.speedapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.RenderScript;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SpeedService extends Service implements IBaseGpsListener{
    static String LOG_TAG="SpeedAppSpeedService";
    private static Timer timer = new Timer();
    float mCurrentSpeed = 0;
    double mCurrentLatitude=0;
    double mCurrentLongitude=0;
    CLocation mLocation;

    public static Runnable runnable = null;
    public Handler handler = null;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(LOG_TAG, "onStartCommand()");
            /*location*/
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Log.i(LOG_TAG,"requesting permission failed");
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            //Log.i(LOG_TAG,"requesting permission succeeded");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 30, this);
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0, 30, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 30, this);

            this.updateSpeed(null);


            /*
            handler = new Handler();
            runnable = new Runnable() {
                public void run() {
                    Log.i(LOG_TAG,"Service is still running");
                    handler.postDelayed(runnable, 10000);
                }
            };
            handler.postDelayed(runnable, 15000);*/
            /*
            CheckBox chkUseMetricUntis = (CheckBox) this.findViewById(R.id.chkMetricUnits);
            chkUseMetricUntis.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // TODO Auto-generated method stub
                    this.updateSpeed(null);
                }
            });*/
            /*start foreground*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "speed";
                String description = "speed description";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("1", name, importance);
                channel.setDescription(description);

                // Don't see these lines in your code...
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);

                startForegroundService(intent);
            }

            //startForeground(this);
            //startService();
            /*
        }*/
        return START_STICKY;

    }

    private void startService() {
        Log.i(LOG_TAG,"startService");
        //timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
    }

    private class mainTask extends TimerTask {
        public void run() {
            //updateSpeed(mLocation);

            Log.i(LOG_TAG,"mainTask, current speed is "+mCurrentSpeed+ ", location is "+mLocation);
            //Log.i(LOG_TAG,", location is "+mLocation);
        }
    }
    private void startForeground(Context context) {
        Log.i(LOG_TAG,"startForeground");
        String channelId ="";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(LOG_TAG,"Build.VERSION.SDK_INT >= Build.VERSION_CODES.O");
            CharSequence name = "speed";
            String description = "speed description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);

            // Don't see these lines in your code...
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            startForegroundService(new Intent(context, SpeedAppWidget.class));
        }else{
            Log.i(LOG_TAG,"Build.VERSION.SDK_INT < Build.VERSION_CODES.O");
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(0)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(101, notification);

        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
    private void updateSpeed(CLocation location) {
        if(location==null) return;
        Log.i(LOG_TAG,"update speed: "+location);

        if(location != null) {
            location.setUseMetricunits(this.useMetricUnits());
            mCurrentSpeed = location.getSpeed();
            mCurrentLatitude=location.getLatitude();
            mCurrentLongitude=location.getLongitude();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", mCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(' ', '0');

        String strUnits = "miles/hour";
        if(this.useMetricUnits()) {
            strUnits = "meters/second";
        }
        Log.i(LOG_TAG,strCurrentSpeed + " " + strUnits);

        fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%,.2f", mCurrentLatitude);
        String strCurrentLatitude = fmt.toString();
        strCurrentLatitude = strCurrentLatitude.replace(' ', '0');

        fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%,.2f", mCurrentLongitude);
        String strCurrentLongitude = fmt.toString();
        strCurrentLongitude = strCurrentLongitude.replace(' ', '0');

        updateWorkoutsWidget(this,strCurrentSpeed,strCurrentLatitude,strCurrentLongitude);
    }

    public  void updateWorkoutsWidget(Context context,String speed,String latitude,String longitude ) {
        Intent intent = new Intent(context, SpeedAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("speed", speed);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, SpeedAppWidget.class));
        if(ids != null && ids.length > 0) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean useMetricUnits() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            mLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(mLocation);
        }
    }



    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
