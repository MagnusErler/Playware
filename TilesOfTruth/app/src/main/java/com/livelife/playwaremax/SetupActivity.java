package com.livelife.playwaremax;

import static com.livelife.motolibrary.AntData.EVENT_PRESS;
import static com.livelife.motolibrary.AntData.LED_COLOR_GREEN;
import static com.livelife.motolibrary.AntData.LED_COLOR_OFF;
import static com.livelife.motolibrary.AntData.LED_COLOR_RED;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.livelife.motolibrary.AntData;
import com.livelife.motolibrary.MotoConnection;
import com.livelife.motolibrary.MotoSound;
import com.livelife.motolibrary.OnAntEventListener;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity implements OnAntEventListener {

    // ------ Moto ------
    MotoConnection connection;
    MotoSound sound;

    // ------ TextView ------
    TextView numberOfPlayersTextView;

    int connectedTiles = 0;

    RadioGroup playersRadioGroup;

    ImageView positioningImageView;


    int numberOfPlayers = 1;
    int difficulty = 2;

    int player1_trueTile = 0, player2_trueTile = 0, player3_trueTile = 0, player4_trueTile = 0;
    int player1_falseTile = 0, player2_falseTile = 0, player3_falseTile = 0, player4_falseTile = 0;

    public static TextToSpeech textToSpeechSystem;
    // -------------------------

    int setupMode = 1;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Log.d("tot", "Starting setup game...");

        setTitle("Setup Game");

        // Enable Back-button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        connection = MotoConnection.getInstance();
        sound = MotoSound.getInstance();

        connection.startMotoConnection(this);
        
        connection.saveRfFrequency(66);
        connection.setDeviceId(2);
        //connection.registerListener(this);

        // ------ Difficulty ------
        Button easyDifficultyButton = findViewById(R.id.easyDifficultyButton);
        Button normalDifficultyButton = findViewById(R.id.normalDifficultyButton);
        Button hardDifficultyButton = findViewById(R.id.hardDifficultyButton);

        easyDifficultyButton.setOnClickListener(v -> {
            difficulty = 1;
        });

        normalDifficultyButton.setOnClickListener(v -> {
            difficulty = 2;
        });

        hardDifficultyButton.setOnClickListener(v -> {
            difficulty = 3;
        });

        // ------ Text to Speech initialization ------
        textToSpeechSystem = new TextToSpeech(SetupActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeechSystem.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    }
                    textToSpeechSystem.setSpeechRate(1.5F);
                    } else
                        Log.e("error", "Initialization Failed!");
            }
        });

        // ------ Pairing Tiles ------
        Button pairingButton = findViewById(R.id.pairButton);
        pairingButton.setOnClickListener(v -> {
            switch(setupMode) {
                case 1:
                    //Starting pairing tiles -> tiles a spinning
                    connection.registerListener(this);
                    connection.pairTilesStart();
                    textToSpeechSystem.speak("Turn on and press " + numberOfPlayers*2 + "tiles you want to use", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    pairingButton.setText("Next");
                    setupMode = 2;
                    break;
                case 2:
                    //Stopping pairing tiles -> tiles are OFF
                    connection.pairTilesStop();
                    textToSpeechSystem.speak("Place the " + numberOfPlayers*2 + " tiles 3 meters apart and stand between them", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    pairingButton.setText("Next");
                    setupMode = 3;
                    break;
                case 3:
                    setupTilesPosition(numberOfPlayers);
                    textToSpeechSystem.speak("Setup complete", TextToSpeech.QUEUE_FLUSH, null,"ID");
                    connection.unregisterListener(this);
                    pairingButton.setText("Start Pairing");
                    setupMode = 1;
                    /*
                    for (int numaa = 0; numaa < connection.connectedTiles.size(); numaa++) { //DEBUG
                        Log.d("tag","Connected tiles' ID " + connection.connectedTiles.get(numaa));
                    }
                     */
                    break;
                default:
                    pairingButton.setText("Error");
                    break;
            }

        });

        // ------ Number of players ------
        numberOfPlayersTextView = findViewById(R.id.numberOfPlayersTextView);
        playersRadioGroup = findViewById(R.id.playersRadioGroup);
        positioningImageView = findViewById(R.id.positioningImageView);

        playersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch(checkedId) {
                case R.id.twoPlayersButton:
                    positioningImageView.setImageResource(R.drawable.two_players);
                    numberOfPlayers = 2;
                    break;
                case R.id.threePlayersButton:
                    positioningImageView.setImageResource(R.drawable.three_players);
                    numberOfPlayers = 3;
                    break;
                case R.id.fourPlayersButton:
                    positioningImageView.setImageResource(R.drawable.four_players);
                    numberOfPlayers = 4;
                    break;
                default:
                    positioningImageView.setImageResource(R.drawable.one_players);
                    numberOfPlayers = 1;
                    break;
            }
            setupTilesPosition(numberOfPlayers);
        });

        Button startGameButton = findViewById(R.id.startGameButton);
        startGameButton.setOnClickListener(v -> {

            /*if (numberOfPlayers > connectedTiles/2) {
                Toast.makeText(this, "Not enough tiles connected", Toast.LENGTH_SHORT).show();
                return;
            }*/

            final AlertDialog.Builder builder = new AlertDialog.Builder(SetupActivity.this);
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_question_set, null);

            NumberPicker picker = dialogView.findViewById(R.id.number_picker);

            /*String[] questionSets = new String[50];

            questionSets[0] = "Default Question-set";
            int numberOfQuestionSets = 0;

            File[] files = getFilesDir().listFiles();
            assert files != null;
            for (File file : files) {
                if (file.getName().startsWith("question_")) {
                    numberOfQuestionSets++;
                    questionSets[numberOfQuestionSets] = file.getName().replace("question_", "").replace(".csv", "");
                }
            }
            numberOfQuestionSets++;

            questionSets[49] = String.valueOf(numberOfQuestionSets);

            //Transfer questionSets to questionSets2 with correct size (numberOfQuestionSets)
            String[] questionSets2 = new String[numberOfQuestionSets];
            System.arraycopy(questionSets, 0, questionSets2, 0, numberOfQuestionSets);
            questionSets = questionSets2;

            Log.d("tag", "j: " + numberOfQuestionSets);

            // Toast questionSets
            for (String questionSet : questionSets) {
                Log.d("tag", "Question set: " + questionSet);
            }*/


            Pair<String[], Integer> questionSetsPair = getAllQuestionSets();
            String[] questionSets = questionSetsPair.first;
            int numQuestionSets = questionSetsPair.second;
            Log.d("tag", "Question sets array: " + Arrays.toString(questionSets));
            Log.d("tag", "Number of question sets: " + numQuestionSets);
            String[] displayedQuestionSets = Arrays.copyOf(questionSets, numQuestionSets);
            Log.d("tag", "Displayed question sets array: " + Arrays.toString(displayedQuestionSets));
            picker.setDisplayedValues(displayedQuestionSets);

            //picker.setMinValue(0);

            builder.setView(dialogView)
                    .setTitle("Choose between Question sets")
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> {
                        Toast.makeText(this, "here: " + questionSets[picker.getValue()], Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            /*Intent intent = new Intent(SetupActivity.this, GameActivity.class);
            intent.putExtra("setup_data", new int[]{numberOfPlayers, difficulty});
            intent.putExtra("tile_ids", new int[]{player1_trueTile, player1_falseTile, player2_trueTile, player2_falseTile, player3_trueTile, player3_falseTile, player4_trueTile, player4_falseTile});
            startActivity(intent);*/
        });
    }

    // ------------------------------- //
    // For going back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            killTTS();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    Pair<String[], Integer> getAllQuestionSets() {

        String[] questionSets = new String[50];

        questionSets[0] = "Default Question-set";
        int i = 1;

        File[] files = getFilesDir().listFiles();
        assert files != null;
        for (File file : files) {
            if (file.getName().startsWith("question_")) {

                questionSets[i] = file.getName().replace("question_", "").replace(".csv", "");
                i++;
            }
        }

        questionSets[49] = String.valueOf(i);

        return new Pair<>(questionSets, i);

    }

    // ------------------------------- //
    // Setup of the tiles
    public void setupTilesPosition(int numberOfPlayers) {
        connection.setAllTilesIdle(LED_COLOR_OFF);

        switch (numberOfPlayers) {
            case 1:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);
                break;
            case 2:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                break;
            case 3:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                player3_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);

                player3_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);

                break;
            case 4:
                player1_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player1_trueTile, 1);

                player1_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player1_falseTile, 1);

                player2_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player2_trueTile, 2);

                player2_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player2_falseTile, 2);

                player3_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player3_trueTile, 3);

                player3_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player3_falseTile, 3);

                player4_trueTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_GREEN, player4_trueTile, 4);

                player4_falseTile = connection.randomIdleTile();
                connection.setTileNumLeds(LED_COLOR_RED, player4_falseTile, 4);

                break;
            default:
                Log.d("tag", "ERROR: Wrong number of players");
                break;
        }
    }

     private void killTTS() {
        textToSpeechSystem.stop();
        textToSpeechSystem.shutdown();
    }

    @Override
    public void onMessageReceived(byte[] bytes, long l) {

        int command = AntData.getCommand(bytes);
        int tileId = AntData.getId(bytes);
        int color = AntData.getColorFromPress(bytes);

        if(command == EVENT_PRESS) {
            Log.d("tag", "tileID: " + tileId);
        }
    }

    @Override
    public void onAntServiceConnected() {
        connection.setAllTilesToInit();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onNumbersOfTilesConnected(int i) {
        connectedTiles = i;
        TextView pairTextView = findViewById(R.id.pairTextView);
        pairTextView.setText("Tiles pairing (connected tiles: " + i + ")");
        Log.d("tag", "Number of connected tiles " + i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //connection.stopMotoConnection();
        connection.unregisterListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //connection.startMotoConnection(this);
        //connection.registerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //connection.stopMotoConnection();
        connection.unregisterListener(this);
    }

}