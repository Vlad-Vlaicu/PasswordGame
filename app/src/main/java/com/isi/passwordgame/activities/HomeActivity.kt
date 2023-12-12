package com.isi.passwordgame.activities

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.view.LocationDisplay
import com.arcgismaps.mapping.view.MapView
import com.google.firebase.Firebase
import android.graphics.Color
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.isi.passwordgame.R
import com.isi.passwordgame.databinding.HomeLayoutBinding
import com.isi.passwordgame.entities.User
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    private lateinit var binding: HomeLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var mapView: MapView
    private val locationDisplay: LocationDisplay by lazy { mapView.locationDisplay }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapView = binding.mapHomepage
        mapView.setOnTouchListener { _, _ -> true }

        // Set a dark background color with some transparency
        mapView.setBackgroundColor(Color.parseColor("#80000000"))
        lifecycle.addObserver(mapView)
        setApiKey()

        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("name")

        auth = Firebase.auth
        val currentUser = auth.currentUser

        db = Firebase.firestore

        val userId = currentUser!!.uid
        val userRef = db.collection("users").document(userId)

        createNewUSerIfNecessary(userRef, displayName, userId)

        setupMap()
    }


    //TODO: check if user in users collection with uid from auth, if it is not, add it
    //TODO: ask for permissions
    //TODO: button for Join/Create Game -> QR CODE SCANNER/GENERATOR
    //TODO: Create Game: initialize a Game

    private fun setApiKey() {

        // set your API key
        // Note: it is not best practice to store API keys in source code. The API key is referenced
        // here for the convenience of this tutorial.

        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1714ff05a97245d499de0c96db059d993Brc_5_QBH_JM6pr-6CeG-Pyi4DOLXhPxTBnZzIqv7Hx7M1tQhJq1A7cIMxsm32v")

    }

    private fun setupMap() {
        // create a map with the BasemapStyle Topographic
        val map = ArcGISMap(BasemapStyle.ArcGISTopographic)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        if (!isLocationEnabled()) {
            // Location services are not enabled, prompt the user to enable them
            showEnableLocationDialog()
        }

        // LocationProvider requires an Android Context to properly interact with Android system
        ArcGISEnvironment.applicationContext = applicationContext
        // set the autoPanMode
        locationDisplay.setAutoPanMode(LocationDisplayAutoPanMode.Recenter)

        lifecycleScope.launch {
            // start the map view's location display
            locationDisplay.dataSource.start()
                .onFailure {
                    // check permissions to see if failure may be due to lack of permissions
                    requestPermissions()
                }
        }

    }

    private fun requestPermissions() {
        // coarse location permission
        val permissionCheckCoarseLocation =
            ContextCompat.checkSelfPermission(
                this@HomeActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED
        // fine location permission
        val permissionCheckFineLocation =
            ContextCompat.checkSelfPermission(
                this@HomeActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED

        // if permissions are not already granted, request permission from the user
        if (!(permissionCheckCoarseLocation && permissionCheckFineLocation)) {
            ActivityCompat.requestPermissions(
                this@HomeActivity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                2
            )
        } else {

            // permission already granted, so start the location display
            lifecycleScope.launch {
                locationDisplay.dataSource.start()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showEnableLocationDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.enable_location_message))
            .setPositiveButton(getString(R.string.enable_location)) { _, _ ->
                // Open device location settings
                startActivity(Intent(ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                // Handle user's choice (e.g., show a message or take alternative action)
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if request is cancelled, the results array is empty
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch {
                locationDisplay.dataSource.start()
            }
        }

        else {
            val errorMessage = getString(R.string.location_permissions_denied)
            showError(errorMessage)
        }

    }

    private fun showError(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        Log.e(localClassName, message)
    }



    private fun createNewUSerIfNecessary(
        userRef: DocumentReference,
        displayName: String?,
        userId: String
    ) {
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val userData = document.data
                } else {
                    // User doesn't exist, add a new user
                    if (displayName != null) {
                        addUserToFirestore(userId, displayName)
                    }
                }
            } else {
                // Handle errors
                println("Error getting user document: ${task.exception}")
            }
        }
    }

    private fun addUserToFirestore(userId: String, username: String) {
        val newUser = User(
            userID = userId,
            userName = username,
            history = emptyList(),
            currentGameId = "",
            isInGame = false
        )

        db.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                println("User added successfully!")
            }
            .addOnFailureListener { e ->
                println("Error adding user: $e")
            }
    }
}

