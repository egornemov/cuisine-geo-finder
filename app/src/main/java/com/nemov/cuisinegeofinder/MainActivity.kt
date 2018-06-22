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

    private val connectivityReceiver = ConnectivityReceiver()
    private var historicalPostcode: String? = DEFAULT_POSTCODE

    private var isConnected = false

    lateinit var actionSearch: MenuItem
    private lateinit var actionUseGPS: MenuItem

    override fun setResults(restaurants: RestaurantModel.Companion.RestaurantResponse?) {
        (restaurantList.adapter as IAdapter).clearAndSetAll(restaurants)
        if (restaurants == null) {
            Toast.makeText(this, resources.getText(R.string.toast_data_is_temporary_unavailable), Toast.LENGTH_SHORT).show()
        } else if (!restaurants.hasErrors && restaurants.Restaurants.isEmpty()) {
            Toast.makeText(this, resources.getText(R.string.toast_no_data_for_location), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter.view = this
        historicalPostcode = getPreferences(Context.MODE_PRIVATE).getString(HISTORICAL_POSTCODE, DEFAULT_POSTCODE)

        setSupportActionBar(findViewById(R.id.tbSearch))
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

        actionSearch = menu.findItem(R.id.action_search)
        val expandListener = object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                actionSearch.isVisible = true
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
                val searchView = actionSearch.actionView as SearchView
                if (historicalPostcode != null) searchView.setQuery(historicalPostcode, false)
                searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
                searchView.setIconifiedByDefault(false)
                actionSearch.isVisible = false
                return true
            }

        }
        actionSearch.setOnActionExpandListener(expandListener)

        actionUseGPS = menu.findItem(R.id.action_use_gps)

        return super.onCreateOptionsMenu(menu)
    }

    private fun searchByGPSLocation() {
        historicalPostcode = getCurrentPostalCode(this)
        emitSearchIntent(this, historicalPostcode)
        val searchView = actionSearch.actionView as SearchView
        if (historicalPostcode != null) searchView.setQuery(historicalPostcode, false)
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
                        searchByGPSLocation()
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onSearchRequested() = true

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) =
            when (requestCode) {
                PERMISSIONS_REQUEST_USE_LOCATION -> {
                    if (grantResults.filter { it == PackageManager.PERMISSION_GRANTED }.isNotEmpty()) {
                        searchByGPSLocation()
                        Unit
                    } else {
                        Toast.makeText(this, resources.getText(R.string.toast_absent_permission), Toast.LENGTH_SHORT).show()
                        actionUseGPS.isEnabled = false
                    }
                }
                else -> Unit
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
        else Toast.makeText(this, resources.getText(R.string.toast_offline), Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val presenter = RestaurantPresenter()
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