package com.example.myapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriceChart(
    prices: List<PricePoint>,
    lineColor: Color
) {

    val safePrices =
        prices.filter {
            it.price.isFinite()
        }

    if (safePrices.size < 2) {
        return
    }


    var selectedIndex by remember {
        mutableStateOf<Int?>(null)
    }


    // IMPORTANT:
    // Canvas width ko yahan store karenge.
    // pointerInput ke andar direct size.width use nahi karna.
    var chartWidth by remember {
        mutableStateOf(0)
    }


    val minPrice =
        safePrices.minOf {
            it.price
        }


    val maxPrice =
        safePrices.maxOf {
            it.price
        }


    val range =
        maxPrice - minPrice


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                Color(0xFFF8FAFC)
            )
    ) {


        Canvas(

            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)

                // Get actual canvas size
                .onSizeChanged { newSize ->
                    chartWidth = newSize.width
                }

                // =================================================
                // CLICK / TAP
                // =================================================

                .then(

                    if (chartWidth > 0) {

                        Modifier.pointerInput(
                            safePrices,
                            chartWidth
                        ) {

                            detectTapGestures { offset ->

                                val width =
                                    chartWidth.toFloat()

                                if (width > 0f) {

                                    val ratio =
                                        (
                                                offset.x / width
                                                ).coerceIn(
                                                0f,
                                                1f
                                            )

                                    val index =
                                        (
                                                ratio *
                                                        (
                                                                safePrices.size - 1
                                                                )
                                                ).toInt()

                                    selectedIndex =
                                        index
                                }
                            }
                        }

                    } else {

                        Modifier
                    }
                )

        ) {


            // =================================================
            // GRID
            // =================================================

            val gridColor =
                Color(0xFFE5E7EB)


            for (i in 1..3) {

                val y =
                    size.height *
                            i / 4f

                drawLine(
                    color = gridColor,

                    start =
                        androidx.compose.ui.geometry.Offset(
                            x = 0f,
                            y = y
                        ),

                    end =
                        androidx.compose.ui.geometry.Offset(
                            x = size.width,
                            y = y
                        ),

                    strokeWidth = 1f
                )
            }


            // =================================================
            // PRICE PATH
            // =================================================

            val path = Path()


            safePrices.forEachIndexed { index, point ->

                val x =

                    if (safePrices.size == 1) {

                        0f

                    } else {

                        index.toFloat() /
                                (safePrices.size - 1).toFloat() *
                                size.width
                    }


                val normalized =

                    if (range == 0.0) {

                        0.5f

                    } else {

                        (
                                (
                                        point.price -
                                                minPrice
                                        ) / range
                                ).toFloat()
                    }


                val y =
                    size.height -
                            (
                                    normalized *
                                            size.height
                                    )


                if (index == 0) {

                    path.moveTo(
                        x,
                        y
                    )

                } else {

                    path.lineTo(
                        x,
                        y
                    )
                }
            }


            drawPath(

                path = path,

                color = lineColor,

                style = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )


            // =================================================
            // SELECTED POINT
            // =================================================

            val index =
                selectedIndex


            if (
                index != null &&
                index in safePrices.indices
            ) {

                val point =
                    safePrices[index]


                val x =

                    if (safePrices.size == 1) {

                        0f

                    } else {

                        index.toFloat() /
                                (safePrices.size - 1).toFloat() *
                                size.width
                    }


                val normalized =

                    if (range == 0.0) {

                        0.5f

                    } else {

                        (
                                (
                                        point.price -
                                                minPrice
                                        ) / range
                                ).toFloat()
                    }


                val y =
                    size.height -
                            (
                                    normalized *
                                            size.height
                                    )


                // Vertical selection line

                drawLine(

                    color = lineColor,

                    start =
                        androidx.compose.ui.geometry.Offset(
                            x = x,
                            y = 0f
                        ),

                    end =
                        androidx.compose.ui.geometry.Offset(
                            x = x,
                            y = size.height
                        ),

                    strokeWidth = 2f
                )


                // Selected circle

                drawCircle(

                    color = Color.White,

                    radius = 8f,

                    center =
                        androidx.compose.ui.geometry.Offset(
                            x = x,
                            y = y
                        )
                )


                drawCircle(

                    color = lineColor,

                    radius = 5f,

                    center =
                        androidx.compose.ui.geometry.Offset(
                            x = x,
                            y = y
                        )
                )
            }
        }


        // =================================================
        // TOOLTIP
        // =================================================

        selectedIndex?.let { index ->

            if (index in safePrices.indices) {

                val selectedPoint =
                    safePrices[index]


                val timeText =
                    SimpleDateFormat(
                        "HH:mm",
                        Locale.US
                    ).format(
                        Date(
                            selectedPoint.timestamp
                        )
                    )


                Card(

                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),

                    shape =
                        RoundedCornerShape(10.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            )
                    ) {

                        Text(
                            text =
                                String.format(
                                    Locale.US,
                                    "$%,.2f",
                                    selectedPoint.price
                                ),

                            fontSize = 13.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color = lineColor
                        )


                        Text(
                            text = timeText,

                            fontSize = 10.sp,

                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}