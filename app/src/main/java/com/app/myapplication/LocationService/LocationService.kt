package com.app.myapplication.LocationService

import android.content.Intent
import android.util.Log
import com.app.myapplication.UserCurrentLocation
import com.google.android.gms.location.LocationCallback
import java.lang.UnsupportedOperationException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import androidx.core.app.NotificationCompat
import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.app.myapplication.Constants.Constants
import com.app.myapplication.MainActivity
import java.util.*
import kotlin.math.roundToInt

class LocationService : Service() {

    private var CHANNEL_ID = ""
    private var CHANNEL_ONE_NAME = "Channel One"
    var notificationChannel: NotificationChannel? = null
    var notification: Notification? = null

    var latitude : Double = 0.0
    var longitude : Double = 0.0

    private val binder = LocalBinder()

    // Random number generator
    private val mGenerator = Random()

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("myBinder",intent?.action.toString())
        return binder
    }

    inner class LocalBinder : Binder() {

        // Return this instance of LocationService so clients can call public methods
        fun getService(): LocationService = this@LocationService
    }

    @SuppressLint("MissingPermission")
    fun startLocationService() {
        val channelId = "location_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val bundle = Bundle()
        bundle.putString("state", "Active")
        val resultIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("Notication_state", bundle)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            channelId
        )
        builder.setSmallIcon(R.drawable.ic_menu_mylocation)
        builder.setContentTitle("Your Current Location ")
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentText("Running")
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)
        builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)

        builder.priority = NotificationCompat.PRIORITY_MAX

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    "Location Service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannel(notificationChannel)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            }
        }
        startForeground(Constants().LOCATION_SERVICE_ID, builder.build())
        //requestNewLocationUpdate()
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            latitude = locationResult.lastLocation.latitude
            longitude = locationResult.lastLocation.longitude

            UserCurrentLocation.latitude = latitude
            UserCurrentLocation.longitude = longitude

            UserCurrentLocation.speed = locationResult.lastLocation.speedAccuracyMetersPerSecond.roundToInt().toString()
            Log.e("Location_update", UserCurrentLocation.latitude.toString() + " , " + UserCurrentLocation.longitude+", Speed : " + UserCurrentLocation.speed)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationUpdate() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action != null) {
                if (action == Constants().ACTION_START_LOCATION_SERVICE) {
                    startLocationService()
                } else if (action == Constants().ACTION_STOP_LOCATION_SERVICE) {
                    stopLocationService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}