package com.nemov.cuisinegeofinder.api

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.http.HeaderMap

/**
 * Created by ynemov on 31.03.18.
 */
class RestaurantPresenter(val view: IView) : IPresenter {

    private val model = RestaurantModel()
    private var disposable: Disposable? = null

    override fun dispose() {
        disposable?.dispose()
    }

    override fun load(postcode: String) {
        val headerMap = HashMap<String, String>()
        headerMap.put("Accept-Tenant", "uk")
        headerMap.put("Accept-Language", "en-GB")
        headerMap.put("Authorization", "Basic VGVjaFRlc3Q6bkQ2NGxXVnZreDVw")
        disposable = model.getRestaurantList(headerMap, postcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe { view.setResults(it) }
    }

}