package com.example.speedapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.speed.speedapp.IBaseGpsListener;

import java.util.Formatter;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SpeedService extends Service implements IBaseGpsListener {
    static String LOG_TAG="SpeedAppSpeedService";
    float mCurrentSpeed = 0;
    double mCurrentLatitude=0;
    double mCurrentLongitude=0;
    CLocation mLocation;

    static long LOCATION_UPDATE_TIME=1000L;
    static float LOCATION_UPDATE_DISTANCE=2000.0f;

    @Override
    public void onCreate() {
        int d = Log.d(LOG_TAG, "onCreate()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "speed";
            String description = "speed description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(),"1").build();

            startForeground(1, notification);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d(LOG_TAG, "onStartCommand()");
        /*location*/
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(LOG_TAG,"requesting permission failed");

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }else {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, this);
            //locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, this);

            this.updateSpeed(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "speed";
            String description = "speed description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            startForegroundService(intent);
        }


        return START_STICKY;

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
        fmt.format(Locale.US, "%,.1f", mCurrentSpeed);
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

        updateWorkoutsWidget(this, (int) mCurrentSpeed,strCurrentSpeed,strCurrentLatitude,strCurrentLongitude);
    }

    public  void updateWorkoutsWidget(Context context,Integer speedVal,String speed,String latitude,String longitude ) {
        Intent intent = new Intent(context, SpeedAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("speedVal", speedVal);
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
