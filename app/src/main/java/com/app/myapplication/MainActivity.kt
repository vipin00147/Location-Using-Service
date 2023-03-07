package com.app.myapplication

import android.Manifest
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.app.myapplication.Constants.Constants
import com.app.myapplication.LocationService.LocationService
import com.google.android.gms.location.LocationListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    val REQUEST_CODE = 123
    private lateinit var mService: LocationService
    private var mBound: Boolean = false


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setViewListeners()

    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to LocationService, cast the IBinder and get LocalService instance
           // val binder = service as LocationService.LocalBinder
            //mService = binder.getService()
          //  mBound = true

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false

        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    private fun setViewListeners() {
        startLocation.setOnClickListener(this)
        stopLocation.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.startLocation -> {
                CheckSelfPermissions()
            }
            R.id.stopLocation -> {
                stopLocationService()
            }
        }
    }

    private fun CheckSelfPermissions() {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )  != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_CODE
            )
        } else {
            startLocationService()
        }
    }

    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val intent = Intent(this, LocationService::class.java)
            intent.action = Constants().ACTION_START_LOCATION_SERVICE
            startService(intent)
            Log.d("LocationServiceRunning", "Location Started")
        } else if (isLocationServiceRunning()) {
            Log.d("LocationServiceRunning", "Already Running")
        }
    }

    private fun stopLocationService() {
        if (isLocationServiceRunning()) {
            val intent = Intent(this, LocationService::class.java)
            intent.action = Constants().ACTION_STOP_LOCATION_SERVICE
            startService(intent)
            Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.size > 0) {
            if (grantResults[0]  == PackageManager.PERMISSION_GRANTED) {
                Log.d("LocationServiceRunning", "Permission Granted")
                startLocationService()
            } else {
                Toast.makeText(baseContext,"Please Allow Location Services", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (activityManager != null) {
            for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
                if (LocationService::class.java.name == service.service.className) {
                    if (service.started) {
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

}