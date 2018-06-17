package com.nemov.cuisinegeofinder

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Toast
import com.nemov.cuisinegeofinder.api.IAdapter
import com.nemov.cuisinegeofinder.api.IView
import com.nemov.cuisinegeofinder.api.RestaurantModel
import com.nemov.cuisinegeofinder.api.RestaurantPresenter
import com.nemov.cuisinegeofinder.commons.ConnectivityReceiver
import com.nemov.cuisinegeofinder.commons.findFirstVisiblePosition
import com.nemov.cuisinegeofinder.restaurantadapter.RestaurantAdapter

class MainActivity : AppCompatActivity(), IView, ConnectivityReceiver.ConnectivityReceiverListener {
    private val BORDER_ID_KEY = "BORDER_ID_KEY"

    private val DEFAULT_BORDER_ID = 0
    private val presenter = RestaurantPresenter(this)

    private val connectivityReceiver = ConnectivityReceiver()
    private var borderId = 0

    private var isConnected = false
    private lateinit var rvRestaurantList: RecyclerView

    override fun setResults(restaurants: RestaurantModel.Companion.RestaurantList) {
        (rvRestaurantList.adapter as IAdapter).clearAndSetAll(restaurants)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        borderId = savedInstanceState?.getInt(BORDER_ID_KEY)?: DEFAULT_BORDER_ID

        rvRestaurantList = findViewById<RecyclerView>(R.id.restaurantList)
        rvRestaurantList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvRestaurantList.adapter = RestaurantAdapter()
        presenter.load()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityReceiver)
        ConnectivityReceiver.connectivityReceiverListener = null
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            // Todo use previous instance state
            val firstVisible = rvRestaurantList.findFirstVisiblePosition()
            borderId = 0
            putInt(BORDER_ID_KEY, borderId)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        this.isConnected = isConnected
        if (isConnected) presenter.load()
        else Toast.makeText(this, "You are offline", Toast.LENGTH_SHORT).show()
    }

}