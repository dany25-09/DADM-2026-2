package com.example.reto_9.data.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface OverpassApiService {
    @FormUrlEncoded
    @POST("api/interpreter")
    suspend fun getPois(
        @Field("data") query: String
    ): OverpassResponse
}

interface NominatimApiService {
    @GET("search")
    suspend fun searchAddress(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Header("User-Agent") userAgent: String = "Reto9GPSApp/1.0"
    ): List<NominatimResult>
}
