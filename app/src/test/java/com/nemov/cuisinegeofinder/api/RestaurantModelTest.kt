package com.nemov.cuisinegeofinder.api

import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Created by ghoshak on 22.06.18.
 */
class RestaurantModelTest {

    val logoURL = "https://en.wikipedia.org/wiki/Android_(operating_system)#/media/File:Android_robot_2014.svg"

    val restaurantResponse =
            RestaurantModel.Companion.RestaurantList(arrayListOf(
                    RestaurantModel.Companion.Restaurant(
                            42,
                            "Apollo",
                            4.2F,
                            arrayListOf(
                                    RestaurantModel.Companion.CuisineType("Pizza")
                            ),
                            arrayListOf(
                                    RestaurantModel.Companion.Logo(logoURL)
                            )
                    )
            ))

    private var model = object : IModel {
        override fun getRestaurantList(headerMap: Map<String, String>, postcode: String): Observable<RestaurantModel.Companion.RestaurantList> {
            return Observable.just(restaurantResponse)
        }
    }

    private lateinit var restaurantModel: RestaurantModel

    @Before
    fun setUp() {
        restaurantModel = RestaurantModel(model)
    }

    @Test
    fun `when restaurants are requested, should call model and return response`() {
        val headerMap = HashMap<String, String>()
        val postcode = "SE19"

        val result = restaurantModel.getRestaurantList(headerMap, postcode)

        val testObserver = TestObserver<RestaurantModel.Companion.RestaurantList>()
        result.subscribe(testObserver)
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        val observerResult = testObserver.values()[0]
        assertThat(observerResult.Restaurants.size, `is`(1))
        assertThat(observerResult.Restaurants[0].Id, `is`(42))
        assertThat(observerResult.Restaurants[0].Name, `is`("Apollo"))
        assertThat(observerResult.Restaurants[0].RatingAverage, `is`(4.2F))
        assertThat(observerResult.Restaurants[0].CuisineTypes.size, `is`(1))
        assertThat(observerResult.Restaurants[0].CuisineTypes[0].Name, `is`("Pizza"))
        assertThat(observerResult.Restaurants[0].Logo.size, `is`(1))
        assertThat(observerResult.Restaurants[0].Logo[0].StandardResolutionURL, `is`(logoURL))
    }

}