package com.isi.passwordgame.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.geometry.Point
import com.arcgismaps.geometry.PolylineBuilder
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.location.LocationDisplayAutoPanMode
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.LocationDisplay
import com.arcgismaps.mapping.view.MapView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isi.passwordgame.databinding.ActivityGameBinding
import com.isi.passwordgame.entities.Coordinates
import com.isi.passwordgame.entities.Game
import com.isi.passwordgame.entities.PlayerTag
import com.isi.passwordgame.entities.User
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class GameActivity : AppCompatActivity() {
    private lateinit var gameMap: MapView
    private val locationDisplay: LocationDisplay by lazy { gameMap.locationDisplay }
    private lateinit var gameAreaOverlay: GraphicsOverlay
    private lateinit var outOfBoundsOverlay: GraphicsOverlay
    private lateinit var timer: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val gameUpdateTask = GameUpdateTask()
    val startTime = AtomicReference("")
    val gameDuration = AtomicReference("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        timer = binding.gameTimer
        gameMap = binding.gameMap
        lifecycle.addObserver(gameMap)
        setApiKey()
        val gameHeader = binding.gameHeader
        val gameBody = binding.gameBody
        val gameFooter = binding.gameFooter

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()
        val gameId = AtomicReference("")
        val mapCenter = AtomicReference(Coordinates(0.0, 0.0))
        val gameRadius = AtomicReference(0.0)
        val captureTime = AtomicInteger()
        val captureRadius = AtomicReference(0.0)

        val userRef = db.collection("users").document(currentUser?.uid ?: "")

        gameAreaOverlay = GraphicsOverlay()
        gameMap.graphicsOverlays.add(gameAreaOverlay)
        outOfBoundsOverlay = GraphicsOverlay()
        gameMap.graphicsOverlays.add(outOfBoundsOverlay)

        // Get currentUser gameId if exists or send it back
        userRef.addSnapshotListener { playerSnapshot, f ->
            if (f != null) {
                // Handle errors
                return@addSnapshotListener
            }

            if (playerSnapshot != null && playerSnapshot.exists()) {
                // Document exists, retrieve the updated Player object
                val user = playerSnapshot.toObject(User::class.java)

                // Update the UI with the updated Player object
                if (user != null) {
                    if (!user.isInGame) {
                        finish()
                    }
                    val gameRef = db.collection("games").document(user.currentGameId)
                    gameRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                // Document exists, retrieve the Game object
                                val game = documentSnapshot.toObject(Game::class.java)
                                startTime.set(game?.startTime ?: "")
                                gameDuration.set(game?.allocatedTime ?: "")
                                mapCenter.set(game?.mapCenter ?: Coordinates(0.0, 0.0))
                                gameRadius.set(game?.mapDiameter ?: 0.0)
                                captureTime.set(game?.captureTime ?: 0)
                                captureRadius.set(game?.captureDiameter ?: 0.0)

                                val map = ArcGISMap(BasemapStyle.ArcGISTopographic)
                                // LocationProvider requires an Android Context to properly interact with Android system
                                ArcGISEnvironment.applicationContext = applicationContext
                                // set the autoPanMode
                                locationDisplay.setAutoPanMode(LocationDisplayAutoPanMode.Recenter)

                                lifecycleScope.launch {
                                    // start the map view's location display
                                    locationDisplay.dataSource.start()
                                        .onFailure {
                                            // check permissions to see if failure may be due to lack of permissions

                                        }
                                }
                                gameMap.map = map


                                val centerPoint = com.arcgismaps.geometry.Point(mapCenter.get().xCoordinate, mapCenter.get().yCoordinate, SpatialReference.wgs84())
                                val graphicsOverlay = GraphicsOverlay()
                                gameMap.graphicsOverlays.add(graphicsOverlay)

                                // create a point symbol that is an small red circle
                                val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, Color.red, 10f)

                                // create a blue outline symbol and assign it to the outline property of the simple marker symbol
                                val blueOutlineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
                                simpleMarkerSymbol.outline = blueOutlineSymbol

                                // Create a polylineBuilder with a spatial reference and add three points to it.
                                // Then get the polyline from the polyline builder

                                // create a blue line symbol for the polyline
                                val polylineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 3f)
                                val degreesPerMeter = 1.0 / (111319.9)
                                val radiusDegrees = gameRadius.get() * degreesPerMeter

                                val polylineBuilder = PolylineBuilder(SpatialReference.wgs84()) {
                                    for (angle in 0 until 360 step 10) {
                                        val x = centerPoint.y + radiusDegrees  * Math.cos(Math.toRadians(angle.toDouble()))
                                        val y = centerPoint.x + radiusDegrees  * Math.sin(Math.toRadians(angle.toDouble()))
                                        addPoint(x, y)
                                    }
                                    val angle = 0
                                    val x = centerPoint.y + radiusDegrees  * Math.cos(Math.toRadians(angle.toDouble()))
                                    val y = centerPoint.x + radiusDegrees  * Math.sin(Math.toRadians(angle.toDouble()))
                                    addPoint(x, y)
                                }
                                val polyline = polylineBuilder.toGeometry()
                                val polylineGraphic = Graphic(polyline, polylineSymbol)

                                graphicsOverlay.graphics.add(polylineGraphic)

                                val players = game?.players

                                if (players != null) {

                                    var currentUserTags: List<PlayerTag>? = null

                                    for (player in players) {
                                        if (currentUser != null) {
                                            if (player.userId.equals(currentUser.uid)){
                                                currentUserTags = player.playerTag;
                                            }

                                        }
                                    }

                                    for (player in players) {

                                        if (currentUserTags != null) {
                                            var isEnemy = true
                                            for (userTag in currentUserTags){
                                                if (currentUser != null) {
                                                    if (userTag in player.playerTag && player.userId != currentUser.uid){
                                                        isEnemy = false;
                                                    }
                                                }
                                            }

                                            if (!isEnemy){

                                                val point = Point(player.playerPosition.yCoordinate, player.playerPosition.xCoordinate, SpatialReference.wgs84())

                                                // create a point symbol that is an small red circle
                                                val playerMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, Color.red, 10f)

                                                // create a blue outline symbol and assign it to the outline property of the simple marker symbol
                                                val blueOutlineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
                                                simpleMarkerSymbol.outline = blueOutlineSymbol

                                                // create a graphic with the point geometry and symbol
                                                val pointGraphic = Graphic(point, simpleMarkerSymbol)

                                                // add the point graphic to the graphics overlay
                                                graphicsOverlay.graphics.add(pointGraphic)
                                            }

                                        }
                                    }
                                }

                            }
                        }



                }
            } else {
                // Document does not exist
                // Handle the case where the player with the specified ID does not exist
            }
        }

        val map = ArcGISMap(BasemapStyle.ArcGISTopographic)
        gameMap.map = map
        handler.postDelayed(gameUpdateTask, 0)
    }

    inner class GameUpdateTask : Runnable {
        override fun run() {
            // Update the TextView with the current time
            val currentTime = getCurrentTime()
            timer.text = currentTime

            // Reschedule the task after a delay (e.g., 1000 milliseconds for 1 second)
            handler.postDelayed(this, 500)
        }

        private fun getCurrentTime(): String {

            if(startTime.toString() == "" || gameDuration.toString() == ""){
                return "Loading"
            }

            // Parse the start time
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val startTime = LocalDateTime.parse(startTime.toString(), formatter)

            // Parse the game duration
            val gameDuration = Duration.parse(gameDuration.toString())

            // Calculate the remaining time
            val endTime = startTime.plus(gameDuration)
            val currentTime = LocalDateTime.now()
            val remainingTime = Duration.between(currentTime, endTime)

            // Extract minutes and seconds from the remaining time
            val remainingMinutes = remainingTime.toMinutes()
            val remainingSeconds = remainingTime.minusMinutes(remainingMinutes).seconds

            // Format the remaining time as a string
            return String.format("%02d:%02d", remainingMinutes, remainingSeconds)
        }
    }

    private fun setApiKey() {
        ArcGISEnvironment.apiKey = ApiKey.create("AAPK1714ff05a97245d499de0c96db059d993Brc_5_QBH_JM6pr-6CeG-Pyi4DOLXhPxTBnZzIqv7Hx7M1tQhJq1A7cIMxsm32v")
    }
}