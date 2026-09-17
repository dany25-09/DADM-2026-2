package com.example.androidtic_tac_toe;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

public class MainActivity extends ComponentActivity {

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

        mGame = new TicTacToeGame();
        mBoardButtons = new Button[9];

        mBoardButtons[0] = findViewById(R.id.one);
        mBoardButtons[1] = findViewById(R.id.two);
        mBoardButtons[2] = findViewById(R.id.three);
        mBoardButtons[3] = findViewById(R.id.four);
        mBoardButtons[4] = findViewById(R.id.five);
        mBoardButtons[5] = findViewById(R.id.six);
        mBoardButtons[6] = findViewById(R.id.seven);
        mBoardButtons[7] = findViewById(R.id.eight);
        mBoardButtons[8] = findViewById(R.id.nine);

        mInfoTextView = findViewById(R.id.info);
        mScoreTextView = findViewById(R.id.score);

        Button newGameButton = findViewById(R.id.new_game);

        for (int i = 0; i < mBoardButtons.length; i++) {
            final int location = i;

            mBoardButtons[i].setOnClickListener(view ->
                    onBoardButtonClick(location));
        }

        newGameButton.setOnClickListener(view -> newGame());

        newGame();
    }

    private void onBoardButtonClick(int location) {

        if (mGameOver) {
            return;
        }

        if (!mBoardButtons[location].isEnabled()) {
            return;
        }

        // Turno del humano
        mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location);
        setMove(TicTacToeGame.HUMAN_PLAYER, location);

        int winner = mGame.checkForWinner();

        if (winner != 0) {
            gameOver(winner);
            return;
        }

        // Turno del computador
        mInfoTextView.setText(R.string.turn_computer);

        int computerMove = mGame.getComputerMove();

        mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove);
        setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove);

        winner = mGame.checkForWinner();

        if (winner != 0) {
            gameOver(winner);
        } else {
            mInfoTextView.setText(R.string.turn_human);
        }
    }

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

        // La siguiente partida será al revés
        mHumanStarts = !mHumanStarts;

        updateScore();
    }

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

    private void updateScore() {

        String score = getString(
                R.string.score_format,
                mHumanWins,
                mComputerWins,
                mTies
        );

        mScoreTextView.setText(score);
    }
}