package com.nemov.cuisinegeofinder.restaurantadapter

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.nemov.cuisinegeofinder.api.IAdapter
import com.nemov.cuisinegeofinder.api.RestaurantModel
import com.nemov.cuisinegeofinder.commons.adapter.AdapterConstants
import com.nemov.cuisinegeofinder.commons.adapter.ViewType
import com.nemov.cuisinegeofinder.commons.adapter.ViewTypeDelegateAdapter

/**
 * Created by ynemov on 01.04.18.
 */
class RestaurantAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), IAdapter {

    private var items: ArrayList<ViewType>
    private var delegateAdapters = SparseArrayCompat<ViewTypeDelegateAdapter>()
    private val loadingItem = object : ViewType {
        override fun getViewType() = AdapterConstants.LOADING
    }

    init {
        delegateAdapters.put(AdapterConstants.LOADING, LoadingDelegateAdapter())
        delegateAdapters.put(AdapterConstants.RESTAURANT, RestaurantDelegateAdapter())
        items = ArrayList()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            delegateAdapters.get(viewType).onCreateViewHolder(parent)


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegateAdapters.get(getItemViewType(position)).onBindViewHolder(holder, items[position])
    }

    override fun getItemViewType(position: Int) = items[position].getViewType()

    override fun clearAndSetAll(restaurants: RestaurantModel.Companion.RestaurantList?) {
        val count = items.size
        items.clear()
        notifyItemRangeRemoved(0, count)

        restaurants ?: return
        items.addAll(restaurants.Restaurants)
        notifyItemRangeChanged(0, items.size - 1)
    }

    override fun loading() {
        items.add(0, loadingItem)
        notifyDataSetChanged()
    }

}