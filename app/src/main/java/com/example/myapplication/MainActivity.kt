package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                CryptoTrackerScreen()
            }
        }
    }
}


@Composable
fun CryptoTrackerScreen() {

    val coins = listOf(
        Coin("bitcoin", "Bitcoin", "BTC"),
        Coin("ethereum", "Ethereum", "ETH"),
        Coin("solana", "Solana", "SOL"),
        Coin("binancecoin", "BNB", "BNB"),
        Coin("ripple", "XRP", "XRP")
    )

    var prices by remember {
        mutableStateOf<Map<String, Map<String, Double>>>(emptyMap())
    }

    var chartData by remember {
        mutableStateOf<Map<String, List<PricePoint>>>(emptyMap())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var lastUpdated by remember {
        mutableStateOf("")
    }

    var selectedCoin by remember {
        mutableStateOf<Coin?>(null)
    }

    var selectedTab by remember {
        mutableStateOf(0)
    }

    val scope = rememberCoroutineScope()


    // ====================================================
    // LOAD API DATA
    // ====================================================

    suspend fun loadPrices() {

        try {

            isLoading = true
            errorMessage = ""

            prices = coinTracker.api.getPrices(
                ids = "bitcoin,ethereum,solana,binancecoin,ripple",
                currency = "usd",
                include24hrChange = true
            )

            val results = coroutineScope {

                coins.map { coin ->

                    async {

                        val response =
                            coinTracker.api.getMarketChart(
                                id = coin.id,
                                currency = "usd",
                                days = 1
                            )

                        val points =
                            response.prices.mapNotNull { point ->

                                val timestamp =
                                    point.getOrNull(0)

                                val price =
                                    point.getOrNull(1)

                                if (
                                    timestamp != null &&
                                    price != null &&
                                    price.isFinite()
                                ) {

                                    PricePoint(
                                        timestamp = timestamp.toLong(),
                                        price = price
                                    )

                                } else {
                                    null
                                }
                            }

                        coin.id to points
                    }

                }.map {
                    it.await()
                }
            }

            chartData = results.toMap()

            lastUpdated =
                "Last updated: ${
                    SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.US
                    ).format(Date())
                }"

        } catch (e: Exception) {

            errorMessage =
                "Unable to load market data"

        } finally {

            isLoading = false
        }
    }


    // ====================================================
    // FIRST LOAD
    // ====================================================

    LaunchedEffect(Unit) {
        loadPrices()
    }


    // ====================================================
    // COIN DETAIL
    // ====================================================

    if (selectedCoin != null) {

        CoinDetailScreen(
            coin = selectedCoin!!,
            initialHistory =
                chartData[selectedCoin!!.id].orEmpty(),
            onBack = {
                selectedCoin = null
            }
        )

        return
    }


    // ====================================================
    // MAIN APP
    // ====================================================

    Scaffold(

        containerColor = Color(0xFF0D1117),

        bottomBar = {

            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {
                    selectedTab = it
                }
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(innerPadding)
        ) {

            if (isLoading) {

                LoadingScreen()

            } else if (errorMessage.isNotEmpty()) {

                ErrorScreen(
                    onRetry = {
                        scope.launch {
                            loadPrices()
                        }
                    }
                )

            } else {

                when (selectedTab) {

                    // HOME
                    0 -> {

                        HomeScreen(
                            coins = coins,
                            prices = prices,
                            lastUpdated = lastUpdated,
                            onCoinClick = {
                                selectedCoin = it
                            }
                        )
                    }


                    // MARKETS
                    1 -> {

                        MarketsScreen(
                            coins = coins,
                            prices = prices,
                            chartData = chartData,
                            onCoinClick = {
                                selectedCoin = it
                            },
                            onRefresh = {
                                scope.launch {
                                    loadPrices()
                                }
                            }
                        )
                    }


                    // SETTINGS
                    2 -> {

                        SettingsScreen()
                    }
                }
            }
        }
    }
}


// ====================================================
// HOME SCREEN
// ====================================================

@Composable
fun HomeScreen(
    coins: List<Coin>,
    prices: Map<String, Map<String, Double>>,
    lastUpdated: String,
    onCoinClick: (Coin) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),

        contentPadding =
            PaddingValues(
                top = 28.dp,
                bottom = 20.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text = "Crypto Tracker",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "Your cryptocurrency dashboard",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "↻",
                    color = Color.White,
                    fontSize = 30.sp
                )
            }
        }


        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF171D26)
                )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp)
                ) {

                    Text(
                        text = "Market Overview",
                        color = Color(0xFF8B949E),
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(
                        text = "Live Crypto Market",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = "Track prices and market trends in real time",
                        color = Color(0xFF39D353),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }


        item {

            Text(
                text = "Quick Access",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }


        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = "📈",
                    title = "Markets",
                    subtitle = "Explore",
                    onClick = {
                        // handled by bottom navigation
                    }
                )

                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = "⚙",
                    title = "Settings",
                    subtitle = "Preferences",
                    onClick = {
                        // handled by bottom navigation
                    }
                )
            }
        }


        item {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Top Coins",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }


        items(
            coins
        ) { coin ->

            val price =
                prices[coin.id]?.get("usd")

            val change =
                prices[coin.id]
                    ?.get("usd_24h_change")

            SimpleCoinCard(
                coin = coin,
                price = price,
                change = change,
                onClick = {
                    onCoinClick(coin)
                }
            )
        }


        item {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Explore Crypto Tracker",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Track live cryptocurrency prices, explore market trends, and discover digital assets.",
                color = Color(0xFF8B949E),
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = lastUpdated,
                color = Color(0xFF6E7681),
                fontSize = 12.sp
            )
        }
    }
}


// ====================================================
// MARKETS SCREEN
// ====================================================

@Composable
fun MarketsScreen(
    coins: List<Coin>,
    prices: Map<String, Map<String, Double>>,
    chartData: Map<String, List<PricePoint>>,
    onCoinClick: (Coin) -> Unit,
    onRefresh: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {

        Spacer(
            modifier = Modifier.height(26.dp)
        )

        Text(
            text = "Markets",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Explore cryptocurrency markets",
            color = Color(0xFF8B949E),
            fontSize = 14.sp
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding =
                PaddingValues(bottom = 14.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            items(coins) { coin ->

                CoinCard(
                    coin = coin,
                    price = prices[coin.id]?.get("usd"),
                    change24h =
                        prices[coin.id]
                            ?.get("usd_24h_change"),
                    history =
                        chartData[coin.id].orEmpty(),
                    onClick = {
                        onCoinClick(coin)
                    }
                )
            }
        }

        Button(
            onClick = onRefresh,

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),

            shape = RoundedCornerShape(16.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {

            Text(
                text = "↻  Refresh Prices",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )
    }
}


// ====================================================
// SIMPLE COIN CARD
// ====================================================

@Composable
fun SimpleCoinCard(
    coin: Coin,
    price: Double?,
    change: Double?,
    onClick: () -> Unit
) {

    val positive =
        (change ?: 0.0) >= 0

    val changeColor =
        if (positive) {
            Color(0xFF39D353)
        } else {
            Color(0xFFFF5C5C)
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171D26)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = coin.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = coin.symbol,
                    color = Color(0xFF8B949E),
                    fontSize = 13.sp
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

                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                if (change != null) {

                    Text(
                        text = String.format(
                            Locale.US,
                            "%+.2f%%",
                            change
                        ),
                        color = changeColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ====================================================
// QUICK ACCESS CARD
// ====================================================

@Composable
fun QuickAccessCard(
    modifier: Modifier,
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier.clickable {
            onClick()
        },

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171D26)
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = icon,
                fontSize = 25.sp
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = subtitle,
                color = Color(0xFF8B949E),
                fontSize = 13.sp
            )
        }
    }
}


// ====================================================
// BOTTOM NAVIGATION
// ====================================================

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11161E))
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),

        horizontalArrangement =
            Arrangement.SpaceEvenly
    ) {

        BottomNavItem(
            icon = "⌂",
            title = "Home",
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
            }
        )

        BottomNavItem(
            icon = "⌁",
            title = "Markets",
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
            }
        )

        BottomNavItem(
            icon = "⚙",
            title = "Settings",
            selected = selectedTab == 2,
            onClick = {
                onTabSelected(2)
            }
        )
    }
}


@Composable
fun BottomNavItem(
    icon: String,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor =
        if (selected) {
            Color(0xFF183D29)
        } else {
            Color.Transparent
        }

    val contentColor =
        if (selected) {
            Color(0xFF39D353)
        } else {
            Color(0xFF8B949E)
        }

    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(
                horizontal = 24.dp,
                vertical = 8.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = icon,
            color = contentColor,
            fontSize = 23.sp
        )

        Text(
            text = title,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight =
                if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
        )
    }
}


// ====================================================
// LOADING
// ====================================================

@Composable
fun LoadingScreen() {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        CircularProgressIndicator(
            color = Color(0xFF39D353)
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            text = "Loading market data...",
            color = Color.White
        )
    }
}


// ====================================================
// ERROR
// ====================================================

@Composable
fun ErrorScreen(
    onRetry: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "Unable to load market data",
            color = Color.White,
            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            )
        ) {

            Text("Retry")
        }
    }
}