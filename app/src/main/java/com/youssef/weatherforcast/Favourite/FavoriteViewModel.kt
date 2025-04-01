package com.youssef.weatherforcast.Favourite
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.collect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youssef.weatherforcast.Model.Repo
import com.youssef.weatherforcast.Model.FavoriteLocation
import com.youssef.weatherforcast.Model.ForecastResponse
import com.youssef.weatherforcast.Model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.launch

class FavoriteViewModel(private val repo: Repo) : ViewModel() {
    // Changed to StateFlow with initial value
    private var _lat = MutableStateFlow<Double?>(null)
    private var _lon = MutableStateFlow<Double?>(null)

    private var _weather = MutableStateFlow<WeatherResponse?>(null)
    val weather = _weather.asStateFlow()

    private var _forecast = MutableStateFlow<ForecastResponse?>(null)
    val forecast = _forecast.asStateFlow()

    private var _language = MutableStateFlow(repo.getSetting("language", "en"))
    val language = _language.asStateFlow()

    private var _units = MutableStateFlow(repo.getSetting("temperature", "Celsius"))
    val units = _units.asStateFlow()

    private var _location = MutableStateFlow(repo.getSetting("location", "GPS"))
    val location = _location.asStateFlow()

    private var _windSpeed = MutableStateFlow(repo.getSetting("windSpeed", "Meter/sec"))
    val windSpeed = _windSpeed.asStateFlow()


    private val _favorites = MutableStateFlow<List<FavoriteLocation>>(emptyList())
    val favorites: StateFlow<List<FavoriteLocation>> = _favorites

    // Added error handling state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadFavorites()
    }

    // Modified to use stateIn for better Flow handling
    private fun loadFavorites() {
        viewModelScope.launch {
            repo.getAllFavorites()
                .catch { e ->
                    _errorMessage.value = "Error loading favorites: ${e.message}"
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
                .collect { list ->
                    _favorites.value = list
                }
        }
    }
    fun convertTemperature(tempFromApi: Double, unit: String): Double {
        // API returns Celsius for metric, Fahrenheit for imperial, Kelvin for standard
        return when (unit) {
            "Celsius" -> tempFromApi // Already in Celsius
            "Fahrenheit" -> tempFromApi // Already in Fahrenheit if unit=imperial
            "Kelvin" -> tempFromApi + 273.15 // Convert Celsius to Kelvin
            else -> tempFromApi
        }
    }

    fun reloadSettings() {
        _language.value = repo.getSetting("language", "en")
        _units.value = repo.getSetting("temperature", "Celsius")
        _location.value = repo.getSetting("location", "GPS")
        _windSpeed.value = repo.getSetting("windSpeed", "Meter/sec")

    }
    fun reloadData() {
        _lat.value?.let { lat ->
            _lon.value?.let { lon ->
                val unitParam = when (units.value) {
                    "Celsius" -> "metric"
                    "Fahrenheit" -> "imperial"
                    "Kelvin" -> "standard"
                    else -> "metric"
                }

                viewModelScope.launch {
                    // Get weather with correct units
                    getWeatherSafely(lat, lon, unitParam)?.collect { weather ->
                        _weather.value = weather
                    }
                }

                viewModelScope.launch {
                    // Get forecast with correct units
                    repo.getForecast(lat, lon, unitParam, language.value).collect { forecast ->
                        _forecast.value = forecast
                    }
                }
            }
        }
    }

    fun setLocation(lat: Double, lon: Double) {
        _lat.value = lat
        _lon.value = lon
    }

    fun formatTemperature(temp: Double): Int {
        return Math.round(temp).toInt()
    }

    suspend fun getWeatherSafely(lat: Double, lon: Double, unitParam: String): Flow<WeatherResponse>? {
        return try {
            repo.getWeather(lat, lon, unitParam, language.value)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get weather: ${e.message}"
            null
        }
    }
    // Modified to handle possible null values
    fun removeFavorite(location: FavoriteLocation) {
        viewModelScope.launch {
            try {
                repo.deleteFavorite(location)
            } catch (e: Exception) {
                _errorMessage.value = "Delete failed: ${e.message}"
            }
        }
    }
}


class FavoriteFactory(private val repo: Repo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Added null check and type safety
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(repo) as T
        }
        throw IllegalArgumentException("Invalid ViewModel class")
    }
}