package com.example.weapredict

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task

object LocationFinder {

    private const val DEFAULT_LATITUDE: Double = 0.0
    private const val DEFAULT_LONGITUDE: Double = 0.0

    fun findLocation(fusedLocationClient: FusedLocationProviderClient, activity: Activity, callback: (Result<Pair<Double, Double>>) -> Unit) {
        when {
            hasLocationPermission(activity) -> {
                getLastLocation(fusedLocationClient, callback)
            }
            else -> {
                callback(Result.failure(SecurityException("Location permission not granted.")))
            }
        }
    }

    private fun hasLocationPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLastLocation(fusedLocationClient: FusedLocationProviderClient, callback: (Result<Pair<Double, Double>>) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        callback(Result.success(Pair(location.latitude, location.longitude)))
                    } else {
                        callback(Result.failure(Exception("Location is null")))
                    }
                }
                .addOnFailureListener { e ->
                    callback(Result.failure(e))
                }
        } catch (e: SecurityException) {
            callback(Result.failure(e))
        }
    }
}