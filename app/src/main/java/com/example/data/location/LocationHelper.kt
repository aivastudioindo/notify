package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Izin lokasi belum diberikan di HP anak.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get best last known location immediately as a candidate
                val bestLastKnown = getBestLastKnownLocation()

                // 2. Try getting a fresh location with a 7-second timeout
                val freshLocation = withTimeoutOrNull(7000L) {
                    fetchFreshLocation()
                }

                val finalLocation = freshLocation ?: bestLastKnown

                if (finalLocation != null) {
                    onSuccess(finalLocation)
                } else {
                    onError("GPS/Lokasi HP anak tidak memberikan koordinat. Pastikan GPS/Lokasi HP anak diaktifkan.")
                }
            } catch (e: Exception) {
                Log.e("LocationHelper", "Error getting location: ${e.message}", e)
                val fallback = getBestLastKnownLocation()
                if (fallback != null) {
                    onSuccess(fallback)
                } else {
                    onError("Gagal mengakses sensor lokasi: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    private suspend fun fetchFreshLocation(): Location? = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            if (continuation.isActive) continuation.resume(null, null)
            return@suspendCancellableCoroutine
        }

        var isResumed = false
        fun safeResume(loc: Location?) {
            if (!isResumed && continuation.isActive) {
                isResumed = true
                continuation.resume(loc, null)
            }
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    safeResume(loc)
                } else {
                    requestLocationUpdatesFallback { loc2 ->
                        safeResume(loc2)
                    }
                }
            }.addOnFailureListener {
                requestLocationUpdatesFallback { loc2 ->
                    safeResume(loc2)
                }
            }
        } catch (e: Exception) {
            requestLocationUpdatesFallback { loc2 ->
                safeResume(loc2)
            }
        }
    }

    private fun requestLocationUpdatesFallback(onResult: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        var handled = false
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                fusedLocationClient.removeLocationUpdates(this)
                if (!handled) {
                    handled = true
                    onResult(result.lastLocation)
                }
            }
        }

        try {
            val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000L)
                .setMaxUpdates(1)
                .build()

            fusedLocationClient.requestLocationUpdates(req, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error requesting location updates: ${e.message}")
            trySystemLocationManager(onResult)
        }
    }

    private fun trySystemLocationManager(onResult: (Location?) -> Unit) {
        if (locationManager == null) {
            onResult(null)
            return
        }

        try {
            var handled = false
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!handled) {
                        handled = true
                        try {
                            locationManager.removeUpdates(this)
                        } catch (e: Exception) {}
                        onResult(location)
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.getMainLooper())
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.getMainLooper())
            } else {
                onResult(getBestLastKnownLocation())
            }
        } catch (e: Exception) {
            onResult(getBestLastKnownLocation())
        }
    }

    private fun getBestLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        var bestLocation: Location? = null

        try {
            val providers = listOfNotNull(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                try {
                    val loc = locationManager?.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error getting best last known location: ${e.message}")
        }

        return bestLocation
    }
}

