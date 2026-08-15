package com.recipe.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {
    @GET("search.php")
    suspend fun searchMeals(@Query("s") query: String): MealsResponse

    @GET("search.php")
    suspend fun mealsByFirstLetter(@Query("f") letter: String): MealsResponse

    @GET("lookup.php")
    suspend fun mealById(@Query("i") id: String): MealsResponse
}
