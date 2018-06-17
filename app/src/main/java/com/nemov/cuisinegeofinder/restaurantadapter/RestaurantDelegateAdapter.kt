package com.nemov.cuisinegeofinder.restaurantadapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nemov.cuisinegeofinder.R
import com.nemov.cuisinegeofinder.api.RestaurantModel
import com.nemov.cuisinegeofinder.commons.adapter.ViewType
import com.nemov.cuisinegeofinder.commons.adapter.ViewTypeDelegateAdapter
import com.nemov.cuisinegeofinder.commons.loadUrl

/**
 * Created by ynemov on 4/2/18.
 */
class RestaurantDelegateAdapter : ViewTypeDelegateAdapter {
    override fun onCreateViewHolder(parent: ViewGroup) =
            RestaurantViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_layout, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        holder as RestaurantViewHolder
        holder.bind(item as RestaurantModel.Companion.Restaurant)
    }

    inner class RestaurantViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val imgIcon = itemView.findViewById<ImageView>(R.id.iv_icon)
        private val txtName = itemView.findViewById<TextView>(R.id.tv_name)
        private val txtRating = itemView.findViewById<TextView>(R.id.tv_rating)
        private val txtCuisineTypes = itemView.findViewById<TextView>(R.id.tv_cuisine_types)

        fun bind(item: RestaurantModel.Companion.Restaurant) {
            imgIcon.loadUrl(item.Logo[0].StandardResolutionURL)
            txtName.text = item.Name
            txtRating.text = item.RatingAverage.toString()
            txtCuisineTypes.text = item.CuisineTypes.joinToString(
                    prefix = "Cuisine: ", transform = { it.Name }
            )
        }
    }
}