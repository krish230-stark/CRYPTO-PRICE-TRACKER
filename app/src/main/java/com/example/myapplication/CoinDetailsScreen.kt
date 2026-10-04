package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.TimeRangeButton
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CoinDetailScreen(
    coin: Coin,
    initialHistory: List<PricePoint>,
    onBack: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    var selectedDays by remember {
        mutableStateOf(1)
    }


    var history by remember {
        mutableStateOf(initialHistory)
    }


    var price by remember {
        mutableStateOf<Double?>(null)
    }


    var change24h by remember {
        mutableStateOf<Double?>(null)
    }


    var isLoading by remember {
        mutableStateOf(true)
    }


    var errorMessage by remember {
        mutableStateOf("")
    }


    // =================================================
    // LOAD DETAIL DATA
    // =================================================

    suspend fun loadCoinData(
        days: Int
    ) {

        try {

            isLoading = true
            errorMessage = ""


            // Current price

            val currentPrice =
                coinTracker.api.getPrices(
                    ids = coin.id,
                    currency = "usd",
                    include24hrChange = true
                )


            price =
                currentPrice[coin.id]
                    ?.get("usd")


            change24h =
                currentPrice[coin.id]
                    ?.get("usd_24h_change")


            // Historical chart

            val response =
                coinTracker.api.getMarketChart(
                    id = coin.id,
                    currency = "usd",
                    days = days
                )


            history =
                response.prices.mapNotNull { point ->

                    val timestamp =
                        point.getOrNull(0)

                    val value =
                        point.getOrNull(1)


                    if (
                        timestamp != null &&
                        value != null &&
                        value.isFinite()
                    ) {

                        PricePoint(
                            timestamp =
                                timestamp.toLong(),

                            price = value
                        )

                    } else {

                        null
                    }
                }

        } catch (e: Exception) {

            // API rate limit / network error
            // Existing chart ko keep karo
            if (history.size < 2) {
                errorMessage = ""
            }

        } finally {
            isLoading = false
        }
    }


    // =================================================
    // LOAD WHEN RANGE CHANGES
    // =================================================

    LaunchedEffect(selectedDays) {

        loadCoinData(
            selectedDays
        )
    }


    val isPositive =
        (change24h ?: 0.0) >= 0


    val chartColor =
        if (isPositive) {

            Color(0xFF16A34A)

        } else {

            Color(0xFFDC2626)
        }


    // =================================================
    // DETAIL UI
    // =================================================

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFF7F8FA)
            )
            .padding(16.dp)
    ) {


        // BACK BUTTON

        Button(

            onClick = onBack,

            shape =
                RoundedCornerShape(14.dp)

        ) {

            Text("← Back")
        }


        Spacer(
            modifier = Modifier.height(18.dp)
        )


        // NAME

        Text(

            text = coin.name,

            fontSize = 30.sp,

            fontWeight =
                FontWeight.Bold
        )


        Text(

            text = coin.symbol,

            fontSize = 14.sp,

            color = Color.Gray,

            fontWeight =
                FontWeight.Medium
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // CURRENT PRICE

        Text(

            text =
                if (price != null) {

                    String.format(
                        Locale.US,
                        "$%,.2f",
                        price
                    )

                } else {

                    "Loading..."
                },

            fontSize = 30.sp,

            fontWeight =
                FontWeight.Bold
        )


        if (change24h != null) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )


            Text(

                text = String.format(
                    Locale.US,
                    "%s %.2f%% (24H)",
                    if (isPositive) "▲" else "▼",
                    change24h
                ),

                color = chartColor,

                fontSize = 15.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =================================================
        // TIME RANGE
        // =================================================

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {


            TimeRangeButton(
                text = "1D",
                selected =
                    selectedDays == 1
            ) {

                selectedDays = 1
            }


            TimeRangeButton(
                text = "7D",
                selected =
                    selectedDays == 7
            ) {

                selectedDays = 7
            }


            TimeRangeButton(
                text = "30D",
                selected =
                    selectedDays == 30
            ) {

                selectedDays = 30
            }


            TimeRangeButton(
                text = "90D",
                selected =
                    selectedDays == 90
            ) {

                selectedDays = 90
            }
        }


        Spacer(
            modifier = Modifier.height(18.dp)
        )


        // =================================================
        // LOADING
        // =================================================

        if (isLoading) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                CircularProgressIndicator()

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Text(
                    text = "Loading chart..."
                )
            }

        }


        // =================================================
        // ERROR
        // =================================================

        else if (
            errorMessage.isNotEmpty()
        ) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Text(

                    text = errorMessage,

                    color =
                        Color(0xFFDC2626)
                )


                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )


                Button(

                    onClick = {

                        scope.launch {

                            loadCoinData(
                                selectedDays
                            )
                        }
                    }
                ) {

                    Text("Retry")
                }
            }

        }


        // =================================================
        // CHART
        // =================================================

        else {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(22.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
            ) {

                Column(

                    modifier =
                        Modifier.padding(16.dp)
                ) {


                    Text(

                        text =
                            when (selectedDays) {

                                1 ->
                                    "24 Hour Price"

                                7 ->
                                    "7 Day Price"

                                30 ->
                                    "30 Day Price"

                                else ->
                                    "90 Day Price"
                            },

                        fontSize = 17.sp,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )


                    if (
                        history.size >= 2
                    ) {

                        PriceChart(

                            prices = history,

                            lineColor =
                                chartColor
                        )

                    } else {

                        Text(
                            text =
                                "Chart data unavailable",

                            color =
                                Color.Gray
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(18.dp)
            )


            // =================================================
            // HIGH LOW CARD
            // =================================================

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(20.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            Color.White
                    )
            ) {

                Row(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(18.dp),

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {


                    Column {

                        Text(

                            text =
                                "PERIOD LOW",

                            fontSize = 11.sp,

                            color =
                                Color.Gray,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )


                        Text(

                            text =
                                history
                                    .minByOrNull {
                                        it.price
                                    }
                                    ?.price
                                    ?.let {

                                        String.format(
                                            Locale.US,
                                            "$%,.2f",
                                            it
                                        )

                                    }
                                    ?: "N/A",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    Column(

                        horizontalAlignment =
                            Alignment.End
                    ) {

                        Text(

                            text =
                                "PERIOD HIGH",

                            fontSize = 11.sp,

                            color =
                                Color.Gray,

                            fontWeight =
                                FontWeight.Bold
                        )


                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )


                        Text(

                            text =
                                history
                                    .maxByOrNull {
                                        it.price
                                    }
                                    ?.price
                                    ?.let {

                                        String.format(
                                            Locale.US,
                                            "$%,.2f",
                                            it
                                        )

                                    }
                                    ?: "N/A",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// ====================================================
// TIME RANGE BUTTON
// ====================================================

@Composable
fun RowScope.TimeRangeButton(

    text: String,

    selected: Boolean,

    onClick: () -> Unit

) {

    Button(

        onClick = onClick,

        modifier =
            Modifier.weight(1f),

        shape =
            RoundedCornerShape(12.dp),

        colors =
            ButtonDefaults.buttonColors(

                containerColor =

                    if (selected) {

                        Color(0xFF2563EB)

                    } else {

                        Color(0xFFE5E7EB)
                    },

                contentColor =

                    if (selected) {

                        Color.White

                    } else {

                        Color(0xFF374151)
                    }
            )
    ) {

        Text(

            text = text,

            fontWeight =
                FontWeight.Bold
        )
    }
}