## Answers to technical questions [link](https://github.com/justeat/JustEat.RecruitmentTest#technical-questions)

1. I spent 10 hours for the coding part. In case of bigger time budget I will:
..1 Add DI management tool (e.g., Dagger 2) to enhance modularity
..2 Use data management tool (e.g. LiveData, Room from Android Jetpack)
..3 Add better test coverage (mainly to model and presenter layers and also utility functions)
..4 Add hooks for unit tests execution on push to VCS host
..5 Move build and full test execution jobs to CI environment (e.g, Travis or Jenkins)
 
2. The most useful feature in Kotlin programming language for me is data classes. It brings routine 
tasks of writing boilerplate code away. So project with this feature gets two shots in a time: enhanced 
source readability and decreased delivery time.

```kotlin
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
```

3. 

4. Just Eat API (fetching restaurant list by postcode) seems to have improvement opportunity in following areas:
..1 In respect to the domain it is going to be useful to provide cache control directives (not only cacheability). 
With those politics data can be managed in more careful way
..2 Enable pagination for seamless communication with backend and cautious network usage 
..3 Integrate HATEOAS as a solution for splitting data representation logic from domain logic. It has to bring 
exhaustive modularity in the full product context but not only application level

5. 