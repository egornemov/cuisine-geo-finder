package com.nemov.cuisinegeofinder.api

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

/**
 * Created by ynemov on 02.04.18.
 */
interface IView {
    fun setResults(restaurants: RestaurantModel.Companion.RestaurantResponse?)
}

interface IPresenter {
    fun load(postcode: String)
    fun dispose()
}

interface IModel {

    @GET("restaurants")
    fun getRestaurantList(
            @HeaderMap headerMap: Map<String, String>,
            @Query("q") postcode: String
    ): Observable<RestaurantModel.Companion.RestaurantResponse>

    companion object {
        fun create(): IModel {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY

            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)

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
    fun clearAndSetAll(restaurants: RestaurantModel.Companion.RestaurantResponse?)
    fun loading()
}