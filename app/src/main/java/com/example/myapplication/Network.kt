package com.example.myapplication

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Network {

    // Current price + 24h change
    @GET("api/v3/simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") currency: String,
        @Query("include_24hr_change") include24hrChange: Boolean
    ): Map<String, Map<String, Double>>

    // Real historical price data for graph
    @GET("api/v3/coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: Int
    ): MarketChartResponse
}

data class MarketChartResponse(
    val prices: List<List<Double>>
)