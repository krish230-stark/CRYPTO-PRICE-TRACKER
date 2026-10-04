package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {

    var notificationsEnabled by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 18.dp)
    ) {

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = "Customize your Crypto Tracker experience",
            color = Color(0xFF8B949E),
            fontSize = 15.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        SettingsCard(
            icon = "🔔",
            title = "Notifications",
            subtitle = "Manage price and market alerts"
        ) {
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = {
                    notificationsEnabled = it
                }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsCard(
            icon = "🎨",
            title = "Appearance",
            subtitle = "Customize the look of the app"
        ) {
            Text(
                text = "Dark",
                color = Color(0xFF39D353),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsCard(
            icon = "💵",
            title = "Currency",
            subtitle = "Prices are displayed in USD"
        ) {
            Text(
                text = "USD",
                color = Color(0xFF39D353),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsCard(
            icon = "⭐",
            title = "Favorites",
            subtitle = "Manage your favorite cryptocurrencies"
        ) {
            Text(
                text = "›",
                color = Color(0xFF8B949E),
                fontSize = 28.sp
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        SettingsCard(
            icon = "ⓘ",
            title = "About",
            subtitle = "Information about Crypto Tracker"
        ) {
            Text(
                text = "›",
                color = Color(0xFF8B949E),
                fontSize = 28.sp
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "App Information",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF171D26)
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Crypto Tracker",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Track cryptocurrency prices and market information in real time.",
                    color = Color(0xFF8B949E),
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Text(
                    text = "Version 1.0",
                    color = Color(0xFF6E7681),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    icon: String,
    title: String,
    subtitle: String,
    trailingContent: @Composable () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF171D26)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = icon,
                    fontSize = 24.sp,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(
                    modifier = Modifier.size(12.dp)
                )

                Column {

                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
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

            trailingContent()
        }
    }
}