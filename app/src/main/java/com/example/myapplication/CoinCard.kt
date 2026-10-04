package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Coin
import com.example.myapplication.PriceChart
import com.example.myapplication.PricePoint
import java.util.Locale



@Composable
fun CoinCard(
    coin: Coin,
    price: Double?,
    change24h: Double?,
    history: List<PricePoint>,
    onClick: () -> Unit
) {

    val isPositive =
        (change24h ?: 0.0) >= 0

    val changeColor =
        if (isPositive) {
            Color(0xFF16A34A)
        } else {
            Color(0xFFDC2626)
        }

    val high =
        history
            .maxByOrNull { it.price }
            ?.price

    val low =
        history
            .minByOrNull { it.price }
            ?.price


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {


            // =================================================
            // TOP
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.Top
            ) {

                Column {

                    Text(
                        text = coin.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = coin.symbol,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }


                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Text(
                        text =
                            if (price != null) {

                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    price
                                )

                            } else {
                                "N/A"
                            },

                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )


                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )


                    if (change24h != null) {

                        Text(
                            text = String.format(
                                Locale.US,
                                "%s %.2f%%",
                                if (isPositive) "▲" else "▼",
                                change24h
                            ),

                            color = changeColor,

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            // =================================================
            // CLICKABLE REAL CHART
            // =================================================

            if (history.size >= 2) {

                PriceChart(
                    prices = history,
                    lineColor = changeColor
                )

            } else {

                Text(
                    text = "Chart data unavailable",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }


            Spacer(
                modifier = Modifier.height(14.dp)
            )


            HorizontalDivider(
                color = Color(0xFFE5E7EB)
            )


            Spacer(
                modifier = Modifier.height(12.dp)
            )


            // =================================================
            // HIGH LOW
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column {

                    Text(
                        text = "24H LOW",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            if (low != null) {

                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    low
                                )

                            } else {
                                "N/A"
                            },

                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }


                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Text(
                        text = "24H HIGH",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            if (high != null) {

                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    high
                                )

                            } else {
                                "N/A"
                            },

                        fontSize = 13.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}