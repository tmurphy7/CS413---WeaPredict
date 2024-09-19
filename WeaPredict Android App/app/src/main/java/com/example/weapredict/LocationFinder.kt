package com.example.weapredict

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

object LocationFinder {

    // Values to use if location data is rejected
    private var DEFAULT_LATITUDE: Double = 0.0
    private var DEFAULT_LONGITUDE: Double = 0.0

    // Detects if the user needs to approve the use of location data
    private var INITIALIZED: Boolean = false

    fun findLocation(fusedLocationClient: FusedLocationProviderClient, activity: Activity, callback: (Pair<Double, Double>) -> Unit) {
        // Check to see if the user needs to approve location data permissions
        if (!INITIALIZED) {
            initializeLocationServices(fusedLocationClient, activity)
            INITIALIZED = true
        }

        // Make sure permission has been granted
        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission is denied, return default values
            INITIALIZED = false
            callback(Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null)
            {
                callback(Pair(location.latitude, location.longitude))
            }
            else
            {
                INITIALIZED = false
                callback(Pair(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
            }
        }
    }

    // Numbers don't actually matter, just need to be unique
    private const val REQUEST_PERMISSION_COARSE_LOCATION = 1
    private const val REQUEST_PERMISSION_FINE_LOCATION = 2

    // Requests location information permissions from user
    private fun initializeLocationServices(fusedLocationClient: FusedLocationProviderClient, activity: Activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_FINE_LOCATION)
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_COARSE_LOCATION)
        }
    }
}