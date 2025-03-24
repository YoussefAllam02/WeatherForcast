package com.youssef.weatherforcast
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.youssef.weatherforcast.Data.RemoteDataSource.RemoteDataSourceImpl
import com.youssef.weatherforcast.Data.RemoteDataSource.RetrofitHelper
import com.youssef.weatherforcast.Home.HomeViewModel
import com.youssef.weatherforcast.Home.WeatherFactory
import com.youssef.weatherforcast.Model.RepoImpl
import com.youssef.weatherforcast.Navigation.AppNavHost
import com.youssef.weatherforcast.Navigation.BottomNavigationBar
import com.youssef.weatherforcast.Navigation.Screen
import com.youssef.weatherforcast.Setting.SettingsPreferences
import com.youssef.weatherforcast.ui.theme.WeatherForcastTheme

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var homeViewModel: HomeViewModel
    private val REQUEST_LOCATION_PERMISSION = 1001

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val lat = location.latitude
            val lon = location.longitude
            homeViewModel.loadWeatherAndForecast(lat, lon)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiService = RetrofitHelper.service
        val remoteDataSource = RemoteDataSourceImpl.getInstance(apiService)
        val settingsPreferences = SettingsPreferences(this)
        val repo = RepoImpl(remoteDataSource, settingsPreferences)
        homeViewModel = ViewModelProvider(this, WeatherFactory(repo))[HomeViewModel::class.java]

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setContent {
            WeatherForcastTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val isBottomBarVisible = backStackEntry?.destination?.route != Screen.Splash.route

                Scaffold(
                    bottomBar = {
                        if (isBottomBarVisible) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavHost(navController, repo, homeViewModel)
                    }
                }
            }
        }

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestLocationUpdates()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }

    private fun requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (e: SecurityException) {
            Log.e("Location", "Security Exception: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocationUpdates()
                    homeViewModel.loadWeatherAndForecast(31.197729, 29.892540)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}