package com.hakancevik.travelbookkt.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.material.snackbar.Snackbar
import com.hakancevik.travelbookkt.R
import com.hakancevik.travelbookkt.databinding.ActivityMapsBinding
import com.hakancevik.travelbookkt.model.Place
import com.hakancevik.travelbookkt.roomdb.PlaceDao
import com.hakancevik.travelbookkt.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // room database
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao

    //rxJava
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.hakancevik.travelbookkt", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0



        db = Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places")
            .build()

        placeDao = db.placeDao()


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        if (intent.getStringExtra("info").equals("new")) {
            binding.savePlaceButton.visibility = View.VISIBLE
            binding.deletePlaceButton.visibility = View.GONE

        }else{
            binding.deletePlaceButton.visibility = View.VISIBLE
        }

        // casting
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                trackBoolean = sharedPreferences.getBoolean("info", false)

                if (trackBoolean == false) {

                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    sharedPreferences.edit().putBoolean("info", true).apply()

                }


            }

        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                // request permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            // permission granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
            }

            mMap.isMyLocationEnabled = true

        }


        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                //permission granted
                if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    }

                }
            } else {
                //permission denied
                Toast.makeText(this@MapsActivity, "Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {

        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0))
        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

    }


    fun savePlace(view: View) {

        // Main(UI) Thread, Default -> CPU, IO Thread (Internet / Database)

        if (selectedLatitude != null && selectedLongitude != null) {
            val place = Place(binding.placeNameText.text.toString(), selectedLatitude!!, selectedLongitude!!)

            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )

        }

    }


    fun deletePlace(view: View) {

        if (selectedLatitude != null && selectedLongitude != null) {
            val place = Place(binding.placeNameText.text.toString(), selectedLatitude!!, selectedLongitude!!)

            compositeDisposable.add(
                placeDao.delete(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }

    }

    private fun handleResponse(){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }


}