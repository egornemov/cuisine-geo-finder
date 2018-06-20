package com.nemov.cuisinegeofinder

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.nemov.cuisinegeofinder.api.IAdapter
import com.nemov.cuisinegeofinder.api.IView
import com.nemov.cuisinegeofinder.api.RestaurantModel
import com.nemov.cuisinegeofinder.api.RestaurantPresenter
import com.nemov.cuisinegeofinder.commons.ConnectivityReceiver
import com.nemov.cuisinegeofinder.commons.GPSTracker
import com.nemov.cuisinegeofinder.commons.PERMISSIONS_REQUEST_USE_LOCATION
import com.nemov.cuisinegeofinder.restaurantadapter.RestaurantAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), IView, ConnectivityReceiver.ConnectivityReceiverListener {

    private val HISTORICAL_POSTCODE = "HISTORICAL_POSTCODE"

    private val DEFAULT_POSTCODE: String? = null
    private val presenter = RestaurantPresenter(this)

    private val connectivityReceiver = ConnectivityReceiver()
    private var historicalPostcode: String? = DEFAULT_POSTCODE

    private var isConnected = false

    override fun setResults(restaurants: RestaurantModel.Companion.RestaurantList) {
        (restaurantList.adapter as IAdapter).clearAndSetAll(restaurants)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        historicalPostcode = getPreferences(Context.MODE_PRIVATE).getString(HISTORICAL_POSTCODE, DEFAULT_POSTCODE)

        setSupportActionBar(findViewById<Toolbar>(R.id.tbSearch))
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        historicalPostcode =
                if (Intent.ACTION_SEARCH == intent.action) intent.getStringExtra(SearchManager.QUERY)
                else historicalPostcode

        historicalPostcode ?: return
        (restaurantList.adapter as IAdapter).loading()
        presenter.load(historicalPostcode as String)
    }

    override fun onStart() {
        super.onStart()
        restaurantList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        restaurantList.adapter = RestaurantAdapter()
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(HISTORICAL_POSTCODE, historicalPostcode)
            commit()
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityReceiver)
        ConnectivityReceiver.connectivityReceiverListener = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        val actionMenuItem = menu.findItem(R.id.action_search)
        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
                val searchView = actionMenuItem?.actionView as SearchView
                if (historicalPostcode != null) searchView.setQuery(historicalPostcode, false)
                searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
                searchView.setIconifiedByDefault(false)
                return true // Return true to expand action view
            }
        }
        actionMenuItem?.setOnActionExpandListener(expandListener)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                R.id.action_search -> {
                    item.expandActionView()
                    true
                }
                R.id.action_use_gps -> {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, Array<String>(2) {
                            Manifest.permission.ACCESS_COARSE_LOCATION;Manifest.permission.ACCESS_FINE_LOCATION
                        }, PERMISSIONS_REQUEST_USE_LOCATION)
                    } else {
                        emitSearchIntent(this, getCurrentPostalCode(this))
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onSearchRequested() = true

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) =
            when (requestCode) {
                PERMISSIONS_REQUEST_USE_LOCATION -> {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.filter { it == PackageManager.PERMISSION_GRANTED }.isNotEmpty()) {
                        emitSearchIntent(this, getCurrentPostalCode(this))
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                }
                // Add other 'when' lines to check for other
                // permissions this app might request.
                else -> {
                    // Ignore all other requests.
                }
            }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString(HISTORICAL_POSTCODE, historicalPostcode)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        historicalPostcode = savedInstanceState?.getString(HISTORICAL_POSTCODE)?: historicalPostcode
        handleIntent(intent)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        this.isConnected = isConnected
        if (isConnected) handleIntent(intent)
        else Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show()
    }

}

fun getCurrentPostalCode(context: Context): String? {
    val gps = GPSTracker(context);
    val latitude = gps.getLatitude();
    val longitude = gps.getLongitude();

    val geoCoder = Geocoder(context, Locale.getDefault())
    val address = geoCoder.getFromLocation(latitude, longitude, 1)
    var query: String? = null
    if (address.isNotEmpty()) query = address[0].getPostalCode()
    return query
}

fun emitSearchIntent(context: Context, query: String?) {
    val searchIntent = Intent(context, MainActivity::class.java)
    searchIntent.action = Intent.ACTION_SEARCH
    searchIntent.putExtra(SearchManager.QUERY, query)
    context.startActivity(searchIntent)
}