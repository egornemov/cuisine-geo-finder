package com.nemov.cuisinegeofinder.commons

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat
import android.util.Log

const val PERMISSIONS_REQUEST_USE_LOCATION = 42

/**
 * origin: https://stackoverflow.com/questions/10279230/how-to-find-postcode-by-latitude-and-logitude-for-uk#answer-10279317
 */
class GPSTracker(private val context: Context) : Service(), LocationListener {

    // flag for GPS status
    internal var isGPSEnabled = false

    // flag for network status
    internal var isNetworkEnabled = false

    // flag for GPS status
    internal var canGetLocation = false

    internal var location: Location? = null // location
    internal var latitude: Double = 0.toDouble() // latitude
    internal var longitude: Double = 0.toDouble() // longitude

    // Declaring a Location Manager
    protected var locationManager: LocationManager? = null

    val accurecy: Float
        get() = location!!.accuracy

    init {
        getLocation()
    }

    fun getLocation(): Location? {
        try {
            locationManager = context
                    .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)


            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(context as Activity, Array<String>(2) {
                        Manifest.permission.ACCESS_COARSE_LOCATION;Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSIONS_REQUEST_USE_LOCATION)
                } else {

                    this.canGetLocation = true
                    if (isNetworkEnabled) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("Network", "Network")
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                    // if GPS Enabled get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
//                        checkPermission()
                            locationManager!!.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                            Log.d("GPS Enabled", "GPS Enabled")
                            if (locationManager != null) {
                                location = locationManager!!
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (location != null) {
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                }
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {}

        return location
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app.
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
        // return latitude
        return latitude
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }

        // return longitude
        return longitude
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(context)

        // Setting DialogHelp Title
        alertDialog.setTitle("GPS is settings")

        // Setting DialogHelp Message
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?")

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        // Showing Alert Message
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {
        val bestAccuracy = -1f
        if (location.accuracy != 0.0f && location.accuracy < bestAccuracy || bestAccuracy == -1f) {
            locationManager!!.removeUpdates(this)
        }
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {

        // The minimum distance to change Updates in meters
        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10 // 10 meters

        // The minimum time between updates in milliseconds
        private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute
    }

}