package com.nemov.cuisinegeofinder.api

import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.HeaderMap

/**
 * Created by ynemov on 02.04.18.
 */
interface IView {
    fun setResults(restaurants: RestaurantModel.Companion.RestaurantList)
}

interface IPresenter {
    fun load()
    fun dispose()
}

interface IModel {

    @GET("restaurants")
    fun getRestaurantList(
            @HeaderMap headerMap: Map<String, String>,
            @Query("q") postcode: String
    ): Observable<RestaurantModel.Companion.RestaurantList>

    companion object {
        fun create(): IModel {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)  // <-- this is the important line!

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("http://public.je-apis.com/")
                    .client(httpClient.build())
                    .build()

            return retrofit.create(IModel::class.java)
        }
    }
}

interface IAdapter {
    fun clearAndSetAll(restaurants: RestaurantModel.Companion.RestaurantList)
}