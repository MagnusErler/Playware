package com.example.ex6;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.Game;
import com.livelife.motolibrary.GameType;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.OnAntEventListener;

public class GameActivity extends AppCompatActivity implements OnAntEventListener {

    MotoConnection connection = MotoConnection.getInstance();
    GameClass game_object = new GameClass(); // Game object
    LinearLayout gt_container;

    Handler h = new Handler();

    int playerScore_old = 0;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            //h.postDelayed(this,delay);
            game_object.gameLogic();
        }
    };

    volatile boolean running = true;

    int playerScore = 0;

    TextView playerScore_TextView;
    TextView correctTilesPressed_TextView;
    TextView timePerRound;
    //Thread my_thread;
    Thread thread;
    View targetColor;
    int delay = 4000;
    //Stop the game when we exit activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        game_object.stopGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        connection.registerListener(this);
        connection.setAllTilesToInit();

        gt_container = findViewById(R.id.game_type_container);
        playerScore_TextView = findViewById(R.id.playerScore_TextView);
        correctTilesPressed_TextView = findViewById(R.id.correctTilesPressed_TextView);
        timePerRound = findViewById(R.id.timePerRound);

        for (final GameType gt : game_object.getGameTypes()) {

            Button b = new Button(this);
            b.setText(gt.getName());
            b.setOnClickListener(v -> {
                game_object.clearPlayersScore();
                delay = 4000;
                runOnUiThread(() -> playerScore_TextView.setText("Score: " + 0));
                runOnUiThread(() -> timePerRound.setText("Time: " + (float)delay/1000 + "s"));


                thread = new Thread(runnable);

                running = true;



                thread.start();

                h.postDelayed(runnable, delay);



                game_object.selectedGameType = gt;
                game_object.startGame();


            });
            gt_container.addView(b);
        }

        game_object.setOnGameEventListener(new Game.OnGameEventListener() {
            @Override
            public void onGameTimerEvent(int i) {
                //Log.d("tag", "time left: " + i);

            }

            @Override
            public void onGameScoreEvent(int i, int i1) {

                Log.d("tag", "i: " + i);
                Log.d("tag", "i1: " + i1);


                playerScore = i;

                if (playerScore - playerScore_old == 10) {
                    //Correct tile
                    if (delay >= 1000) {
                        delay -= 500;
                    }
                } else if (playerScore - playerScore_old == -5) {
                    //wrong tile
                    delay += 500;
                }

                runOnUiThread(() -> timePerRound.setText("Time: " + (float)delay/1000 + "s"));



                //playerScore = game_object.getPlayerScore()[1];

                playerScore_old = playerScore;
                //game_object.gameLogic();
                //thread.start();







                h.removeCallbacksAndMessages(null);
                h.postDelayed(runnable, delay);

                runOnUiThread(() -> playerScore_TextView.setText("Score: " + playerScore));
                runOnUiThread(() -> correctTilesPressed_TextView.setText("Correct tiles pressed: " + game_object.correctPressedTiles));

                //h.postDelayed(runnable, delay);
                }

            @Override
            public void onGameStopEvent() {
                Log.d("tag", "onGameStopEvent");
                //my_thread.interrupt();
                running = false;
                thread.interrupt();

                game_object.gameWon();
            }

            @Override
            public void onSetupMessage(String s) {}

            @Override
            public void onGameMessage(String s) {}

            @Override
            public void onSetupEnd() {}
        });

        // Displaying each colour for a certain period of time (default - 4000 ms)
        /*my_thread = new Thread() {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    game_object.gameLogic();
                    h.postDelayed(this,delay);
                }

            };
/*            @Override
            public void run(){
                game_object.gameLogic();
                h.postDelayed(r,delay);
            }

        };*/



        //thread = new Thread(runnable);

    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {
        //Log.d("tag", "Byte: " + new String(bytes, StandardCharsets.UTF_8));
        game_object.addEvent(bytes);
    }

    @Override
    public void onAntServiceConnected() {}

    @Override
    public void onNumbersOfTilesConnected(final int i) {}
}