package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun isGpsEnabled(): Boolean {
        if (locationManager == null) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManagerCompat.isLocationEnabled(locationManager)
        } else {
            val gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsOn || networkOn
        }
    }

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

    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasLocationPermission()
        }
    }

    fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onError("Izin lokasi belum diberikan di HP anak. Silakan izinkan akses lokasi di Pengaturan HP.")
            return
        }

        if (!isGpsEnabled()) {
            onError("GPS / Layanan Lokasi pada HP anak sedang NONAKTIF. Mohon aktifkan GPS di pengaturan HP.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Get best last known location immediately as a fallback candidate
                val lastKnown = getBestLastKnownLocation()

                // 2. Attempt to fetch fresh location within 8 seconds
                val freshLocation = withTimeoutOrNull(8000L) {
                    fetchFreshLocation()
                }

                val finalLocation = freshLocation ?: lastKnown

                if (finalLocation != null) {
                    onSuccess(finalLocation)
                } else {
                    onError("Gagal memperoleh koordinat GPS. Pastikan HP berada di area terbuka atau terhubung ke internet & GPS aktif.")
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

    suspend fun getBestLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        var bestLocation: Location? = null

        // Try FusedLocationClient last location
        try {
            val fusedLast = getFusedLastLocation()
            if (fusedLast != null) {
                bestLocation = fusedLast
            }
        } catch (e: Exception) {
            Log.w("LocationHelper", "Fused lastLocation failed: ${e.message}")
        }

        // Try LocationManager providers
        if (locationManager != null) {
            try {
                val providers = listOf(
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    LocationManager.PASSIVE_PROVIDER
                )
                for (provider in providers) {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.time > bestLocation.time || loc.accuracy < bestLocation.accuracy) {
                                bestLocation = loc
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("LocationHelper", "LocationManager getLastKnownLocation failed: ${e.message}")
            }
        }

        return bestLocation
    }

    private suspend fun getFusedLastLocation(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (continuation.isActive) continuation.resume(loc)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(null)
        }
    }

    private suspend fun fetchFreshLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        var isResumed = false
        fun safeResume(loc: Location?) {
            if (!isResumed && continuation.isActive) {
                isResumed = true
                continuation.resume(loc)
            }
        }

        try {
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    safeResume(loc)
                } else {
                    // Fallback to balanced accuracy
                    tryBalancedLocation { balancedLoc ->
                        if (balancedLoc != null) {
                            safeResume(balancedLoc)
                        } else {
                            trySystemLocationListeners { sysLoc ->
                                safeResume(sysLoc)
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                tryBalancedLocation { balancedLoc ->
                    if (balancedLoc != null) {
                        safeResume(balancedLoc)
                    } else {
                        trySystemLocationListeners { sysLoc ->
                            safeResume(sysLoc)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            trySystemLocationListeners { sysLoc ->
                safeResume(sysLoc)
            }
        }
    }

    private fun tryBalancedLocation(onResult: (Location?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }

        try {
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cts.token
            ).addOnSuccessListener { loc ->
                onResult(loc)
            }.addOnFailureListener {
                onResult(null)
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    private fun trySystemLocationListeners(onResult: (Location?) -> Unit) {
        if (locationManager == null || !hasLocationPermission()) {
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

            var registeredAny = false

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
                registeredAny = true
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
                registeredAny = true
            }

            if (!registeredAny) {
                onResult(null)
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Error in system location listeners: ${e.message}")
            onResult(null)
        }
    }
}
