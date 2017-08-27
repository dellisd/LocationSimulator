package ca.llamabagel.locationsimulator

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.SystemClock
import com.google.android.gms.maps.model.LatLng

/**
 * @author derek
 */
class LocationProvider(private val providerName: String, private val context: Context) {
    private val locationManager: LocationManager by lazy { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    init {
        locationManager.addTestProvider(providerName, false, false, false, false, false, true, true, 0, 5)
    }

    fun pushLocation(location: LatLng) {
        val mockLocation = Location(providerName)
        with (mockLocation) {
            latitude = location.latitude
            longitude = location.longitude
            altitude = 0.0
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            accuracy = 0f
        }

        locationManager.setTestProviderLocation(providerName, mockLocation)
    }

    fun start() {
        locationManager.setTestProviderEnabled(providerName, true)
    }

    fun stop() {
        locationManager.removeTestProvider(providerName)
    }
}