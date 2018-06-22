## Answers to technical questions [link](https://github.com/justeat/JustEat.RecruitmentTest#technical-questions)

**1** I spent 10 hours for the coding part. In case of bigger time budget I will:

**1.1** Add DI management tool (e.g., Dagger 2) to enhance modularity

**1.2** Use data management tool (e.g. LiveData, Room from Android Jetpack)

**1.3** Add better test coverage (mainly to model and presenter layers and also utility functions)

**1.4** Add hooks for unit tests execution on push to VCS host

**1.5** Move build and full test execution jobs to CI environment (e.g, Travis or Jenkins)

**1.6** Add map widget to better visual experience with restaurant data 
 
**2** The most useful feature in Kotlin programming language for me is data classes. It brings routine 
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

**3** For tracking down performance issues (and also general issues) I am using development version (with enabled logging on debug level) of application 
or module (configured to work with production environment). In case when usage of debug is impossible 
I used to grab system or application logs and filter them by target case. I's used to do this routine from time to time 
previously due to the necessity of understanding the point of failure of 3rd party proprietary component (e.g. media player). 

**4** Just Eat API (fetching restaurant list by postcode) seems to have improvement opportunity in following areas:
..1 In respect to the domain it is going to be useful to provide cache control directives (not only cacheability). 
With those politics data can be managed in more careful way
..2 Enable pagination for seamless communication with backend and cautious network usage 
..3 Integrate HATEOAS as a solution for splitting data representation logic from domain logic. It has to bring 
exhaustive modularity in the full product context but not only application level

**5** Proposed response JSON is dependant on HATEOAS convention that can be used. For example:
```json
 {
   "data": [
     {
      "RatingAverage": 5.35,
      "Id": 66965,
      "Name": "Chicken Trio",
      "CuisineTypes": [
        {
          "Id": 79,
          "Name": "Chicken",
          "SeoName": "chicken"
        },
        {
          "Id": 78,
          "Name": "Burgers",
          "SeoName": "burgers"
        }
      ],
      "Logo": [
        {
          "StandardResolutionURL": "http://d30v2pzvrfyzpo.cloudfront.net/uk/images/restaurants/66965.gif"
        }
      ],
      "links": [{
          "rel": "details",
          "href": "http://http://public.je-apis.com/restaurants/66965/details"
      }, {
         "rel": "menu",
          "href": "http://http://public.je-apis.com/restaurants/66965/menu"
      }]
    }
  ],
  "links": {
    "self": "http://http://public.je-apis.com/restaurants?page=1&page_size=1",
    "prev": "http://http://public.je-apis.com/restaurants?page=0&page_size=1",
    "next": "http://http://public.je-apis.com/restaurants?page=0&page_size=1"
  },
  "ShortResultText": "se19",
  "Errors": [],
  "HasErrors": false
}

```