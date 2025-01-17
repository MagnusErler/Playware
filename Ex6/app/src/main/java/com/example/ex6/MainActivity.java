package com.example.ex6;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;
import com.livelife.motolibrary.MotoSound;

public class MainActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection;
    MotoSound sound;
    Button pairingButton, startGameButton;

    boolean isPairing;

    TextView numberOfConnectedTiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connection=MotoConnection.getInstance();
        connection.startMotoConnection(MainActivity.this);
        connection.saveRfFrequency(66);         // Check the back of your tiles for the RF
        connection.setDeviceId(2);              // Your group number
        connection.registerListener(MainActivity.this);

        sound = MotoSound.getInstance();
        sound.initializeSounds(MainActivity.this);

        numberOfConnectedTiles = findViewById(R.id.numberOfConnectedTiles);
        pairingButton = findViewById(R.id.pairingButton);
        startGameButton = findViewById(R.id.startGameButton);


        pairingButton.setOnClickListener(view -> {

            //sound.playAnimalSound(1);
            //sound.playCountdown();
            //sound.playPianoSound(1);
            sound.playStart();
            //sound.playError();

            if(!isPairing) {
                connection.pairTilesStart();
                pairingButton.setText("Stop pairing!");
            }
            else {
                connection.pairTilesStop();
                pairingButton.setText("Start pairing!");
            }
            isPairing = !isPairing;
        });

        startGameButton.setOnClickListener(view -> {
            connection.unregisterListener(MainActivity.this);
            Intent i = new Intent(MainActivity.this, GameActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {}

    @Override
    public void onAntServiceConnected()
    {
        connection.setAllTilesToInit();
    }

    @Override
    public void onNumbersOfTilesConnected(final int i) {
        runOnUiThread(() -> numberOfConnectedTiles.setText(i +" connected tiles"));
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        connection.registerListener(MainActivity.this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.stopMotoConnection();
        connection.unregisterListener(MainActivity.this);
    }

}