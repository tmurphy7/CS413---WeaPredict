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

    var CURRENT_LOCATION_NAME: String = "Location Unknown"
    var INITIALIZED: Boolean = false

    fun FindLocation(fusedLocationClient: FusedLocationProviderClient, activity: Activity) : String {
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
        ) { }

        var longitude : Double = 0.0
        var latitude : Double = 0.0
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Use the location here (latitude and longitude)
                latitude = location.latitude
                longitude = location.longitude
                println("Location - Latitude: $latitude, Longitude: $longitude")
            }
        }

        return ("$latitude, $longitude")
    }

    private const val REQUEST_PERMISSION_COARSE_LOCATION = 1
    private const val REQUEST_PERMISSION_FINE_LOCATION = 2

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
            return
        }
    }
}