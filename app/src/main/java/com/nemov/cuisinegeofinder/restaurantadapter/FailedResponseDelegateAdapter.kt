package com.nemov.cuisinegeofinder.restaurantadapter;

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nemov.cuisinegeofinder.R
import com.nemov.cuisinegeofinder.api.RestaurantModel
import com.nemov.cuisinegeofinder.commons.adapter.ViewType
import com.nemov.cuisinegeofinder.commons.adapter.ViewTypeDelegateAdapter

/**
 * Created by ynemov on 6/22/18.
 */
class FailedResponseDelegateAdapter : ViewTypeDelegateAdapter {
    override fun onCreateViewHolder(parent: ViewGroup) =
            FailedResponseViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_failed_response, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        holder as FailedResponseViewHolder
        holder.bind(item as RestaurantModel.Companion.Error)
    }

    inner class FailedResponseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val txtError = itemView.findViewById<TextView>(R.id.tv_error)

        fun bind(item: RestaurantModel.Companion.Error) {
            txtError.text = item.Message
        }
    }
}