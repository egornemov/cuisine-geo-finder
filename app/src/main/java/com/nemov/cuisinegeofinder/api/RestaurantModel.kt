package com.nemov.cuisinegeofinder.api

import com.nemov.cuisinegeofinder.commons.adapter.AdapterConstants
import com.nemov.cuisinegeofinder.commons.adapter.ViewType
import retrofit2.http.HeaderMap
import java.util.*

/**
 * Created by ynemov on 31.03.18.
 */
class RestaurantModel : IModel {

    private val model = IModel.create()

    override fun getRestaurantList(headerMap: Map<String, String>, postcode: String) =
            model.getRestaurantList(headerMap, postcode)

    companion object {
        data class RestaurantList(val Restaurants: ArrayList<Restaurant>)

        data class Restaurant(
                val Id: Int,
                val Name: String,
                val RatingAverage: Float,
                val CuisineTypes: ArrayList<CuisineType>,
                val Logo: ArrayList<Logo>
        ) : ViewType {
            override fun getViewType() = AdapterConstants.RESTAURANT
        }

        data class CuisineType(val Name: String)

        data class Logo(val StandardResolutionURL: String)
    }

}