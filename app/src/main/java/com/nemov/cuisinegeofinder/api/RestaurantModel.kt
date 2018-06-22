package com.nemov.cuisinegeofinder.api

import com.nemov.cuisinegeofinder.commons.adapter.AdapterConstants
import com.nemov.cuisinegeofinder.commons.adapter.ViewType
import kotlin.collections.ArrayList

/**
 * Created by ynemov on 31.03.18.
 */
class RestaurantModel(val model: IModel = IModel.create()) : IModel {

    override fun getRestaurantList(headerMap: Map<String, String>, postcode: String) =
            model.getRestaurantList(headerMap, postcode)

    companion object {
        data class RestaurantResponse(
                val Restaurants: ArrayList<Restaurant>,
                val Errors: ArrayList<Error>,
                val hasErrors: Boolean
        )

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

        data class Error(val ErrorType: String, val Message: String) : ViewType {
            override fun getViewType() = AdapterConstants.ERROR
        }
    }

}