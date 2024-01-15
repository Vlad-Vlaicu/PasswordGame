package com.isi.passwordgame.activities;

import static java.util.Objects.requireNonNull;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.isi.passwordgame.adaptors.NameAdaptor;
import com.isi.passwordgame.databinding.ActivityStartGameBinding;
import com.isi.passwordgame.entities.Coordinates;
import com.isi.passwordgame.entities.Game;
import com.isi.passwordgame.entities.Player;
import com.isi.passwordgame.entities.PlayerTag;
import com.isi.passwordgame.entities.User;
import com.isi.passwordgame.qr.QRService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        Intent intent = getIntent();
        var recycleView = binding.nameRecycleView;
        var startButton = binding.startGameButton;
        startButton.setVisibility(View.GONE);
        var nextIntent = new Intent(this, GameActivity.class);
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        //if player joined the game
        AtomicBoolean isJoinGamePlayer = new AtomicBoolean(intent.getBooleanExtra("IS_GUEST", false));
        var gameID = "";
        if (isJoinGamePlayer.get()){
            //hide start button
            startButton.setVisibility(View.GONE);
            //get the game UUID
            String gameUUID = intent.getStringExtra("GAME_UUID");
            //display the QR CODE of the game for easier distribution
            qrCode.setImageBitmap(QRService.Companion.generateQRCode(gameUUID, 700, 700));
            gameID = gameUUID;
            //add the new player data to the game
            var db = FirebaseFirestore.getInstance();
            db.collection("games")
                    .document(gameUUID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Document exists, retrieve the Game object
                            Game game = documentSnapshot.toObject(Game.class);

                            // Now, update the properties of the Game object
                            if (game != null) {
                                var player = new Player();
                                if (null == currentUser) {
                                    finish();
                                }

                                //check if the player reentered the game
                                var playerIds = game.getPlayers().stream().map(s -> s.getUserId()).collect(Collectors.toList());
                                if (playerIds.contains(currentUser.getUid())){
                                    var myPlayer = game.getPlayers().stream().filter(s -> s.getUserId().equals(currentUser.getUid())).findFirst();
                                    if (myPlayer.isPresent() && myPlayer.get().getPlayerTag().contains(PlayerTag.GAME_MASTER)){
                                        isJoinGamePlayer.set(false);
                                        if (game.getPlayers().size() > 1){
                                            startButton.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    if(!game.isJoinEligible()){
                                        var userRef = db.collection("users").document(currentUser.getUid());
                                        userRef.addSnapshotListener((playerSnapshot, f) -> {
                                            if (f != null) {
                                                // Handle errors
                                                return;
                                            }

                                            if (playerSnapshot != null && playerSnapshot.exists()) {
                                                // Document exists, retrieve the updated Player object
                                                User user = playerSnapshot.toObject(User.class);

                                                // Update the UI with the updated Player object
                                                if (user != null) {
                                                    user.setInGame(true);
                                                    user.setCurrentGameId(game.getUuid());

                                                    userRef.set(user).addOnSuccessListener(aVoid -> {
                                                                // Document updated successfully
                                                                // You can perform any additional actions here
                                                            })
                                                            .addOnFailureListener(s -> {
                                                                // Update failed
                                                                // Handle the failure
                                                            });

                                                    //Start Game
                                                    startActivity(nextIntent);
                                                    finish();
                                                }
                                            } else {
                                                // Document does not exist
                                                // Handle the case where the player with the specified ID does not exist
                                            }
                                        });
                                    }

                                    return;
                                }

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

                                                player.setUserId(currentUser.getUid());
                                                player.setUsername(requireNonNull(currentUser.getDisplayName()));
                                                var playerTags = player.getPlayerTag();
                                                player.setPlayerTag(playerTags);
                                                game.getPlayers().add(player);

                                                // Update the game object in Firestore
                                                db.collection("games")
                                                        .document(gameUUID)
                                                        .set(game)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Update successful
                                                            // You can perform any additional actions here
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            // Update failed
                                                            // Handle the failure
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
                        } else {
                            // Document does not exist
                            // Handle the case where the game with the specified ID does not exist
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while fetching the document
                        // Handle the error
                    });


        } else {
            //the player is creating a new game
            var newGame = new Game();
            var uuid = UUID.randomUUID();
            gameID = uuid.toString();
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
                            newGame.setJoinEligible(true);
                            // Use latitude and longitude as needed
                            var db = FirebaseFirestore.getInstance();
                            db.collection("games")
                                    .document(newGame.getUuid())
                                    .set(newGame)
                                    .addOnSuccessListener(aVoid -> {

                                        qrCode.setImageBitmap(QRService.Companion.generateQRCode(newGame.getUuid(), 700, 700));
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("ERROR", "Could not create game");
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


        //if game data is updated
        var db = FirebaseFirestore.getInstance();
        DocumentReference gameRef = db.collection("games").document(gameID);

        var nameList = new ArrayList<String>();
        recycleView.setLayoutManager(new LinearLayoutManager(this));
        var stringAdapter = new NameAdaptor(nameList, this);
        recycleView.setAdapter(stringAdapter);

        // Listen for real-time updates
        String finalGameID = gameID;
        gameRef.addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                // Handle errors
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Document exists, retrieve the updated Game object
                Game updatedGame = documentSnapshot.toObject(Game.class);

                // Now, update the UI with the updated Game object
                if (updatedGame != null) {
                    // Update your UI components here
                    // For example, if you have a TextView to display the score:
                    var playerList = updatedGame.getPlayers().stream().map(Player::getUsername).collect(Collectors.toList());
                    nameList.clear();
                    nameList.addAll(playerList);
                    stringAdapter.updateData(nameList);

                    if (nameList.size() > 1 && !isJoinGamePlayer.get()){
                        startButton.setVisibility(View.VISIBLE);
                    }

                    //Start Game
                    if(!updatedGame.isJoinEligible()){
                        var userRef = db.collection("users").document(currentUser.getUid());
                        userRef.addSnapshotListener((playerSnapshot, f) -> {
                            if (f != null) {
                                // Handle errors
                                return;
                            }

                            if (playerSnapshot != null && playerSnapshot.exists()) {
                                // Document exists, retrieve the updated Player object
                                User user = playerSnapshot.toObject(User.class);

                                // Update the UI with the updated Player object
                                if (user != null) {
                                    user.setInGame(true);
                                    user.setCurrentGameId(finalGameID);

                                    userRef.set(user).addOnSuccessListener(aVoid -> {
                                                // Document updated successfully
                                                // You can perform any additional actions here
                                            })
                                            .addOnFailureListener(s -> {
                                                // Update failed
                                                // Handle the failure
                                            });

                                    //Start Game
                                    startActivity(nextIntent);
                                    finish();
                                }
                            } else {
                                // Document does not exist
                                // Handle the case where the player with the specified ID does not exist
                            }
                        });
                    }

                }
            } else {
                // Document does not exist
                // Handle the case where the game with the specified ID does not exist
            }
        });


        //set up start game event
        String finalGameID1 = gameID;
        startButton.setOnClickListener(v -> {
            var gameReference = db.collection("games").document(finalGameID1);
            gameReference.addSnapshotListener((gameSnapshot, f) -> {
                if (f != null) {
                    // Handle errors
                    return;
                }

                if (gameSnapshot != null && gameSnapshot.exists()) {

                    Game game = gameSnapshot.toObject(Game.class);

                    if (!game.isJoinEligible()){
                        return;
                    }

                    // update game data, make players have specific roles, set the password
                    if (game != null) {
                        game.setJoinEligible(false);
                        game.setStartTime(LocalDateTime.now().toString());
                        int hackers = 0;
                        int agents = 0;

                        for (Player player: game.getPlayers()){
                            if (hackers <= agents){
                                player.getPlayerTag().add(PlayerTag.HACKER);
                                hackers++;
                            } else {
                                player.getPlayerTag().add(PlayerTag.FBI);
                                agents++;
                            }
                        }

                        String inputString = "PasswordGames";
                        int numberOfPieces = (int) game.getPlayers().stream().filter(s -> s.getPlayerTag().contains(PlayerTag.HACKER)).count();
                        String[] pieces = splitString(inputString, numberOfPieces);

                        for (int index = 0; index < numberOfPieces; index++){
                            var player = game.getPlayers().get(index);
                            player.getPasswordPiece().setPasswordPiece(pieces[index]);
                            player.getPasswordPiece().setPasswordPiecePlace(index);
                        }

                        game.setCaptureDiameter(7);
                        game.setCaptureTime(7);
                        game.setMapDiameter(150);
                        game.setAllocatedTime(Duration.ofMinutes(15).toString());

                        gameReference.set(game);

                        //Start Game
                        startActivity(nextIntent);
                        finish();

                    }
                } else {
                    // Document does not exist
                    // Handle the case where the player with the specified ID does not exist
                }
            });
        });

    }

    private static String[] splitString(String input, int numberOfPieces) {
        int length = input.length();
        int pieceLength = (int) Math.ceil((double) length / numberOfPieces);

        String[] pieces = new String[numberOfPieces];

        for (int i = 0; i < numberOfPieces; i++) {
            int start = i * pieceLength;
            int end = Math.min((i + 1) * pieceLength, length);
            pieces[i] = input.substring(start, end);
        }

        return pieces;
    }
}