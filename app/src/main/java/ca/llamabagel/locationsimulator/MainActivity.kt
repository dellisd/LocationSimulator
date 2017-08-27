package ca.llamabagel.locationsimulator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    private val locationManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    private val mockLocationManager: LocationProvider by lazy { LocationProvider(LocationManager.GPS_PROVIDER, this) }

    private var selectedLocation: LatLng? = null

    private var isSimulatingLocation = false

    companion object {
        private val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)


        locationFloatingActionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                        moveToLocation(LatLng(it.latitude, it.longitude), animate = true)
                    }.addOnFailureListener {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Request location permissions
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
                }
            } else {
                LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                    moveToLocation(LatLng(it.latitude, it.longitude), animate = true)
                }.addOnFailureListener {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Clicking the state floating action button will toggle the location simulator
        stateFloatingActionButton.setOnClickListener {
            if (selectedLocation == null) return@setOnClickListener
            isSimulatingLocation = if (!isSimulatingLocation) {
                with(mockLocationManager) {
                    start()
                    pushLocation(selectedLocation!!)
                }

                stateFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause_black_24dp))

                true
            } else {
                mockLocationManager.stop()
                stateFloatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow_black_24dp))

                false
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setOnMapClickListener {
            googleMap.clear()

            // Display a marker where the map was clicked
            googleMap.addMarker(MarkerOptions()
                    .position(it))

            moveToLocation(it, googleMap.cameraPosition.zoom, true)

            selectedLocation = it

            //mockLocationManager.pushLocation(it)
        }

        // Show the user's current location on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true

            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                moveToLocation(LatLng(it.latitude, it.longitude))
            }.addOnFailureListener {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle UI settings related stuff
        googleMap.uiSettings.apply {
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
            isMapToolbarEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        mockLocationManager.stop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener {
                moveToLocation(LatLng(it.latitude, it.longitude), animate = true)
            }.addOnFailureListener {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }

            googleMap.isMyLocationEnabled = true
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "This action requires location permission", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Moves the center of the map to a given location and zoom
     * @param location The location to center to
     * @param zoom How much zoom
     * @param animate Whether to animate the camera changes
     */
    private fun moveToLocation(location: LatLng, zoom: Float = 15f, animate: Boolean = false) {
        // Check if the map has been initialized.
        try {
            googleMap.mapType
        } catch (exception: Exception) {
            Log.e("MainActivity", "Map has not been initialized")
            return
        }

        // Moves the camera into position
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
        }
    }
}
