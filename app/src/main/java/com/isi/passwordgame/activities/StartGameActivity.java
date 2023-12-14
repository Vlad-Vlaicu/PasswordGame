package com.isi.passwordgame.activities;

import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.isi.passwordgame.databinding.ActivityStartGameBinding;
import com.isi.passwordgame.entities.Coordinates;
import com.isi.passwordgame.entities.Game;
import com.isi.passwordgame.entities.Player;
import com.isi.passwordgame.entities.PlayerTag;
import com.isi.passwordgame.qr.QRService;

import java.util.UUID;

public class StartGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var binding = ActivityStartGameBinding.inflate(getLayoutInflater());
        var view = binding.getRoot();
        var auth = FirebaseAuth.getInstance();
        var currentUser = auth.getCurrentUser();
        setContentView(view);
        var qrCode = binding.qrCode;

        var newGame = new Game();
        var uuid = UUID.randomUUID();
        newGame.setUuid(uuid.toString());
        var player = new Player();
        if (null == currentUser) {
            finish();
        }
        player.setUserId(currentUser.getUid());
        player.setUsername(requireNonNull(currentUser.getDisplayName()));
        var playerTags = player.getPlayerTag();
        playerTags.add(PlayerTag.GAME_MASTER);
        player.setPlayerTag(playerTags);
        newGame.getPlayers().add(player);

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        var coordinates = new Coordinates(latitude, longitude);
                        player.setPlayerPosition(coordinates);
                        newGame.setMapCenter(coordinates);
                        // Use latitude and longitude as needed
                        var db = FirebaseFirestore.getInstance();
                        db.collection("games")
                                .document(newGame.getUuid())
                                .set(newGame)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        qrCode.setImageBitmap(QRService.Companion.generateQRCode(newGame.getUuid(), 700, 700));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        finish();
                                    }
                                });

                    } else {
                        Log.d("ERROR", "No last location found");
                    }
                })
                .addOnFailureListener(this, e -> {
                    // Handle failure
                    Log.d("ERROR", "No last location found");
                });


    }
}