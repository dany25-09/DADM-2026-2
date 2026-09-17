package com.example.androidtic_tac_toe;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private TicTacToeGame mGame;
    private Button[] mBoardButtons;
    private TextView mInfoTextView;

    private boolean mGameOver;
    private boolean mHumanStarts = true;

    // Marcador
    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;

    private TextView mScoreTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Crear el juego
        mGame = new TicTacToeGame();
        mBoardButtons = new Button[9];

        // Conectar botones del tablero
        mBoardButtons[0] = findViewById(R.id.one);
        mBoardButtons[1] = findViewById(R.id.two);
        mBoardButtons[2] = findViewById(R.id.three);
        mBoardButtons[3] = findViewById(R.id.four);
        mBoardButtons[4] = findViewById(R.id.five);
        mBoardButtons[5] = findViewById(R.id.six);
        mBoardButtons[6] = findViewById(R.id.seven);
        mBoardButtons[7] = findViewById(R.id.eight);
        mBoardButtons[8] = findViewById(R.id.nine);

        // Textos
        mInfoTextView = findViewById(R.id.info);
        mScoreTextView = findViewById(R.id.score);

        // Botón Nueva partida
        Button newGameButton = findViewById(R.id.new_game);

        // Eventos de los botones del tablero
        for (int i = 0; i < mBoardButtons.length; i++) {

            final int location = i;

            mBoardButtons[i].setOnClickListener(view ->
                    onBoardButtonClick(location));
        }

        // Evento del botón Nueva partida
        newGameButton.setOnClickListener(view -> newGame());

        // Iniciar partida
        newGame();
    }

    // ============================================================
    // MENÚ
    // ============================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_new_game) {

            newGame();
            return true;

        } else if (id == R.id.menu_difficulty) {

            showDifficultyDialog();
            return true;

        } else if (id == R.id.menu_quit) {

            showQuitDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ============================================================
    // CLIC EN EL TABLERO
    // ============================================================

    private void onBoardButtonClick(int location) {

        if (mGameOver) {
            return;
        }

        if (!mBoardButtons[location].isEnabled()) {
            return;
        }

        // Turno del humano
        mGame.setMove(
                TicTacToeGame.HUMAN_PLAYER,
                location
        );

        setMove(
                TicTacToeGame.HUMAN_PLAYER,
                location
        );

        int winner = mGame.checkForWinner();

        if (winner != 0) {
            gameOver(winner);
            return;
        }

        // Turno de Android
        mInfoTextView.setText(R.string.turn_computer);

        int computerMove = mGame.getComputerMove();

        mGame.setMove(
                TicTacToeGame.COMPUTER_PLAYER,
                computerMove
        );

        setMove(
                TicTacToeGame.COMPUTER_PLAYER,
                computerMove
        );

        winner = mGame.checkForWinner();

        if (winner != 0) {

            gameOver(winner);

        } else {

            mInfoTextView.setText(R.string.turn_human);
        }
    }

    // ============================================================
    // MOSTRAR MOVIMIENTO
    // ============================================================

    private void setMove(char player, int location) {

        if (player == TicTacToeGame.HUMAN_PLAYER) {

            mBoardButtons[location].setText("X");
            mBoardButtons[location].setTextColor(Color.GREEN);

        } else if (player == TicTacToeGame.COMPUTER_PLAYER) {

            mBoardButtons[location].setText("O");
            mBoardButtons[location].setTextColor(Color.RED);
        }

        mBoardButtons[location].setEnabled(false);
    }

    // ============================================================
    // NUEVA PARTIDA
    // ============================================================

    private void newGame() {

        mGame.clearBoard();
        mGameOver = false;

        // Limpiar tablero
        for (Button button : mBoardButtons) {

            button.setText("");
            button.setEnabled(true);
            button.setTextColor(Color.BLACK);
        }

        // Alternar quién comienza
        if (mHumanStarts) {

            mInfoTextView.setText(R.string.first_human);

        } else {

            mInfoTextView.setText(R.string.first_computer);

            // Android comienza automáticamente
            int computerMove = mGame.getComputerMove();

            mGame.setMove(
                    TicTacToeGame.COMPUTER_PLAYER,
                    computerMove
            );

            setMove(
                    TicTacToeGame.COMPUTER_PLAYER,
                    computerMove
            );

            mInfoTextView.setText(R.string.turn_human);
        }

        // Cambiar quién comienza en la siguiente partida
        mHumanStarts = !mHumanStarts;

        updateScore();
    }

    // ============================================================
    // FIN DE PARTIDA
    // ============================================================

    private void gameOver(int winner) {

        mGameOver = true;

        if (winner == 1) {

            // Empate
            mTies++;

            mInfoTextView.setText(R.string.result_tie);

        } else if (winner == 2) {

            // Ganó el humano
            mHumanWins++;

            mInfoTextView.setText(R.string.result_human_wins);

        } else if (winner == 3) {

            // Ganó Android
            mComputerWins++;

            mInfoTextView.setText(R.string.result_computer_wins);
        }

        updateScore();

        // Desactivar tablero
        for (Button button : mBoardButtons) {
            button.setEnabled(false);
        }
    }

    // ============================================================
    // MARCADOR
    // ============================================================

    private void updateScore() {

        String score = getString(
                R.string.score_format,
                mHumanWins,
                mComputerWins,
                mTies
        );

        mScoreTextView.setText(score);
    }

    // ============================================================
    // DIÁLOGO DE DIFICULTAD
    // ============================================================

    private void showDifficultyDialog() {

        String[] difficulties = {
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_harder),
                getString(R.string.difficulty_expert)
        };

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(R.string.difficulty_choose);

        builder.setSingleChoiceItems(
                difficulties,
                getSelectedDifficulty(),
                (dialog, which) -> {

                    switch (which) {

                        case 0:
                            mGame.setDifficultyLevel(
                                    TicTacToeGame.DifficultyLevel.Easy
                            );
                            break;

                        case 1:
                            mGame.setDifficultyLevel(
                                    TicTacToeGame.DifficultyLevel.Harder
                            );
                            break;

                        case 2:
                            mGame.setDifficultyLevel(
                                    TicTacToeGame.DifficultyLevel.Expert
                            );
                            break;
                    }

                    dialog.dismiss();
                });

        builder.setNegativeButton(
                R.string.no,
                null
        );

        builder.show();
    }

    // Devuelve la dificultad actualmente seleccionada
    private int getSelectedDifficulty() {

        switch (mGame.getDifficultyLevel()) {

            case Easy:
                return 0;

            case Harder:
                return 1;

            case Expert:
                return 2;

            default:
                return 2;
        }
    }

    // ============================================================
    // DIÁLOGO PARA SALIR
    // ============================================================

    private void showQuitDialog() {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_title);

        builder.setMessage(R.string.quit_question);

        builder.setPositiveButton(
                R.string.yes,
                (dialog, which) -> finish()
        );

        builder.setNegativeButton(
                R.string.no,
                null
        );

        builder.show();
    }
}