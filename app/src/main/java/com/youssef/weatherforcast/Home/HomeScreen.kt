package com.youssef.weatherforcast.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.youssef.weatherforcast.Model.ForecastResponse
import com.youssef.weatherforcast.Model.WeatherResponse
import com.youssef.weatherforcast.R




@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val weatherState by viewModel.weather.collectAsState()
    val forecastState by viewModel.forecast.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getWeather(lat = 31.197729, lon = 29.892540, units = "metric", language = "en")
        viewModel.getForecast(lat = 31.197729, lon =29.892540, units = "metric", language = "en")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F4C81))
                )
            )
    ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    weatherState?.let { weather ->
                        WeatherCard(weather)
                        Spacer(modifier = Modifier.height(16.dp))
                    } ?: Text(
                        text = "Loading current weather...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    forecastState?.let { forecast ->
                        if (!forecast.list.isNullOrEmpty()) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(forecast.list.take(5)) { item ->
                                    ForecastItem(item, weatherState) // Pass weatherState
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        } else {
                            Text(
                                text = "No forecast data available",
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } ?: Text(
                        text = "Loading forecast...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
}
@Composable
fun WeatherCard(weather: WeatherResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City Name
            Text(
                text = weather.name ?: "Unknown City",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Weather Icon
            val iconCode = weather.weather.firstOrNull()?.icon ?: "01d"
            Image(
                painter = painterResource(id = weather.weatherIconResourceId(iconCode)),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(100.dp)
            )

            // Temperature
            Text(
                text = "${weather.main.temp}°C",
                style = MaterialTheme.typography.displaySmall,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Weather Condition
            Text(
                text = weather.weather.firstOrNull()?.description ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetailItem("Humidity", "${weather.main.humidity}%")
                WeatherDetailItem("Wind", "${weather.wind.speed} m/s")
                WeatherDetailItem("Pressure", "${weather.main.pressure} hPa")
            }
        }
    }
}
@Composable
fun WeatherDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black
        )
    }
}
@Composable

fun ForecastItem(item: ForecastResponse.Item0, weatherResponse: WeatherResponse?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Weather Icon
            val iconCode = item.weather.firstOrNull()?.icon ?: "01d"
            Image(
                painter = painterResource(id = weatherResponse?.weatherIconResourceId(iconCode) ?: R.drawable.day_clear),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(50.dp)
            )

            // Date and Time
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.dt_txt ?: "No Date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "Temp: ${item.main.temp}°C",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Weather Condition
            Text(
                text = item.weather.firstOrNull()?.description ?: "No data",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}