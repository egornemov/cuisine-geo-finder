package com.nemov.cuisinegeofinder.api

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * Created by ynemov on 31.03.18.
 */
class RestaurantPresenter : IPresenter {

    lateinit var view: IView

    private val model = RestaurantModel()
    private var disposable: Disposable? = null

    // Per session cache due to the assumption that restaurant identity data and rating is quasi constant
    private var restaurantMap: Map<String, RestaurantModel.Companion.RestaurantResponse> = HashMap()

    override fun dispose() {
        disposable?.dispose()
    }

    override fun load(postcode: String) {
        if (restaurantMap.containsKey(postcode)) {
            val restaurants = restaurantMap[postcode]
            view.setResults(restaurants)
        } else {
            val headerMap = HashMap<String, String>()
            headerMap.put("Accept-Tenant", "uk")
            headerMap.put("Accept-Language", "en-GB")
            headerMap.put("Authorization", "Basic VGVjaFRlc3Q6bkQ2NGxXVnZreDVw")
            disposable = model.getRestaurantList(headerMap, postcode)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturn {
                        RestaurantModel.Companion.RestaurantResponse(
                                arrayListOf(),
                                arrayListOf(RestaurantModel.Companion.Error("Error", it.localizedMessage)),
                                true
                        )
                    }
                    .subscribe {
                        if (!it.hasErrors) restaurantMap = restaurantMap.plus(Pair(postcode, it))
                        view.setResults(it)
                    }
        }
    }

}