package com.example.androidtic_tac_toe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    // ============================================================
    // VARIABLES PRINCIPALES
    // ============================================================



    private TicTacToeGame mGame;
    private BoardView mBoardView;
    private TextView mInfoTextView;
    private TextView mScoreTextView;

    // Indica si la partida terminó
    private boolean mGameOver;

    // Indica quién comienza la siguiente partida
    private boolean mHumanStarts = true;

    // Indica si actualmente es el turno del humano
    private boolean mHumanTurn = true;

    // ============================================================
    // MARCADOR
    // ============================================================

    private int mHumanWins = 0;
    private int mComputerWins = 0;
    private int mTies = 0;

    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Cargar el layout principal
        setContentView(R.layout.activity_main);

        // --------------------------------------------------------
        // TOOLBAR
        // --------------------------------------------------------

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        // --------------------------------------------------------
        // CREAR EL JUEGO
        // --------------------------------------------------------

        mGame = new TicTacToeGame();


        // --------------------------------------------------------
        // OBTENER EL BOARDVIEW
        // --------------------------------------------------------

        mBoardView = findViewById(R.id.board);

        // Entregar el objeto TicTacToeGame al BoardView
        mBoardView.setGame(mGame);


        // --------------------------------------------------------
        // OBTENER TEXTVIEWS
        // --------------------------------------------------------

        mInfoTextView = findViewById(R.id.info);

        mScoreTextView = findViewById(R.id.score);


        // --------------------------------------------------------
        // BOTÓN NUEVA PARTIDA
        // --------------------------------------------------------

        Button newGameButton = findViewById(R.id.new_game);

        newGameButton.setOnClickListener(view -> newGame());


        // --------------------------------------------------------
        // TOQUES SOBRE EL TABLERO
        // --------------------------------------------------------

        mBoardView.setOnTouchListener(mTouchListener);


        // --------------------------------------------------------
        // INICIAR PARTIDA
        // --------------------------------------------------------

        newGame();
    }


    // ============================================================
    // MENÚ
    // ============================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Cargar el archivo options_menu.xml
        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        // --------------------------------------------------------
        // NUEVA PARTIDA
        // --------------------------------------------------------

        if (id == R.id.menu_new_game) {

            newGame();

            return true;
        }


        // --------------------------------------------------------
        // DIFICULTAD
        // --------------------------------------------------------

        else if (id == R.id.menu_difficulty) {

            showDifficultyDialog();

            return true;
        }


        // --------------------------------------------------------
        // SALIR
        // --------------------------------------------------------

        else if (id == R.id.menu_quit) {

            showQuitDialog();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    // ============================================================
    // TOUCH LISTENER DEL TABLERO
    // ============================================================

    private final View.OnTouchListener mTouchListener =
            (view, event) -> {

                // Solo procesamos cuando el usuario toca
                // la pantalla.
                if (event.getAction() != MotionEvent.ACTION_DOWN) {

                    return true;
                }


                // ------------------------------------------------
                // COMPROBAR SI LA PARTIDA TERMINÓ
                // ------------------------------------------------

                if (mGameOver) {

                    return true;
                }


                // ------------------------------------------------
                // COMPROBAR SI ES EL TURNO DEL HUMANO
                // ------------------------------------------------

                if (!mHumanTurn) {

                    return true;
                }


                // ------------------------------------------------
                // OBTENER TAMAÑO DE CADA CELDA
                // ------------------------------------------------

                float cellWidth =
                        mBoardView.getWidth() / 3.0f;

                float cellHeight =
                        mBoardView.getHeight() / 3.0f;


                // ------------------------------------------------
                // DETERMINAR COLUMNA Y FILA
                // ------------------------------------------------

                int col =
                        (int) (event.getX() / cellWidth);

                int row =
                        (int) (event.getY() / cellHeight);


                // ------------------------------------------------
                // COMPROBAR QUE LA POSICIÓN SEA VÁLIDA
                // ------------------------------------------------

                if (row < 0 || row > 2 ||
                        col < 0 || col > 2) {

                    return true;
                }


                // ------------------------------------------------
                // CONVERTIR FILA + COLUMNA A POSICIÓN DEL TABLERO
                // ------------------------------------------------
                //
                //     0 | 1 | 2
                //     ---------
                //     3 | 4 | 5
                //     ---------
                //     6 | 7 | 8
                //
                // ------------------------------------------------

                int location =
                        row * 3 + col;


                // ------------------------------------------------
                // COMPROBAR QUE LA CASILLA ESTÉ LIBRE
                // ------------------------------------------------

                if (mGame.getBoardOccupant(location)
                        != TicTacToeGame.OPEN_SPOT) {

                    return true;
                }


                // ------------------------------------------------
                // MOVIMIENTO DEL HUMANO
                // ------------------------------------------------

                mGame.setMove(
                        TicTacToeGame.HUMAN_PLAYER,
                        location
                );
                playMoveSound(TicTacToeGame.HUMAN_PLAYER);

                // ------------------------------------------------
                // REDIBUJAR EL TABLERO
                // ------------------------------------------------

                mBoardView.invalidate();


                // ------------------------------------------------
                // COMPROBAR GANADOR
                // ------------------------------------------------

                int winner =
                        mGame.checkForWinner();


                if (winner != 0) {

                    gameOver(winner);

                    return true;
                }


                // ------------------------------------------------
                // CAMBIAR AL TURNO DE ANDROID
                // ------------------------------------------------

                mHumanTurn = false;

                mInfoTextView.setText(
                        R.string.turn_computer
                );


                // ------------------------------------------------
                // MOVIMIENTO DE ANDROID
                // ------------------------------------------------

                makeComputerMove();


                return true;
            };


    // ============================================================
    // MOVIMIENTO DE ANDROID
    // ============================================================

    private void makeComputerMove() {

        // Obtener movimiento según la dificultad seleccionada
        int computerMove =
                mGame.getComputerMove();


        // Registrar movimiento de Android
        mGame.setMove(
                TicTacToeGame.COMPUTER_PLAYER,
                computerMove
        );
        playMoveSound(TicTacToeGame.COMPUTER_PLAYER);

        // Redibujar tablero
        mBoardView.invalidate();


        // Comprobar si Android ganó o hubo empate
        int winner =
                mGame.checkForWinner();


        if (winner != 0) {

            gameOver(winner);

        } else {

            // Volver al turno del humano
            mHumanTurn = true;

            mInfoTextView.setText(
                    R.string.turn_human
            );
        }
    }


    // ============================================================
    // NUEVA PARTIDA
    // ============================================================

    private void newGame() {

        mGame.clearBoard();

        mGameOver = false;
        mHumanTurn = mHumanStarts;

        mBoardView.invalidate();

        if (mHumanStarts) {

            mInfoTextView.setText(
                    R.string.first_human
            );

        } else {

            mInfoTextView.setText(
                    R.string.first_computer
            );

            mHumanTurn = false;

            makeComputerMove();
        }

        mHumanStarts = !mHumanStarts;

        updateScore();
    }


    // ============================================================
    // FIN DE PARTIDA
    // ============================================================

    private void gameOver(int winner) {

        // Marcar la partida como terminada
        mGameOver = true;

        // Ya no es turno del humano
        mHumanTurn = false;


        // --------------------------------------------------------
        // DETERMINAR RESULTADO
        // --------------------------------------------------------

        if (winner == 1) {

            // Empate
            mTies++;

            mInfoTextView.setText(
                    R.string.result_tie
            );

        } else if (winner == 2) {

            // Ganó el humano
            mHumanWins++;

            mInfoTextView.setText(
                    R.string.result_human_wins
            );

        } else if (winner == 3) {

            // Ganó Android
            mComputerWins++;

            mInfoTextView.setText(
                    R.string.result_computer_wins
            );
        }


        // --------------------------------------------------------
        // ACTUALIZAR MARCADOR
        // --------------------------------------------------------

        updateScore();


        // --------------------------------------------------------
        // REDIBUJAR TABLERO
        // --------------------------------------------------------

        mBoardView.invalidate();
    }


    // ============================================================
    // ACTUALIZAR MARCADOR
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

        // Opciones que aparecerán en el diálogo
        String[] difficulties = {

                getString(R.string.difficulty_easy),

                getString(R.string.difficulty_harder),

                getString(R.string.difficulty_expert)
        };


        // Crear diálogo
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);


        // Título
        builder.setTitle(
                R.string.difficulty_choose
        );


        // Obtener dificultad actualmente seleccionada
        int selectedDifficulty =
                getSelectedDifficulty();


        // Mostrar opciones
        builder.setSingleChoiceItems(
                difficulties,
                selectedDifficulty,
                (dialog, which) -> {

                    // --------------------------------------------
                    // EASY
                    // --------------------------------------------

                    if (which == 0) {

                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Easy
                        );
                    }


                    // --------------------------------------------
                    // HARDER
                    // --------------------------------------------

                    else if (which == 1) {

                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Harder
                        );
                    }


                    // --------------------------------------------
                    // EXPERT
                    // --------------------------------------------

                    else if (which == 2) {

                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Expert
                        );
                    }


                    // Cerrar diálogo
                    dialog.dismiss();
                });


        // Botón cancelar
        builder.setNegativeButton(
                R.string.no,
                null
        );


        // Mostrar diálogo
        builder.show();
    }


    // ============================================================
    // OBTENER DIFICULTAD SELECCIONADA
    // ============================================================

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


    @Override
    protected void onResume() {

        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(
                getApplicationContext(),
                R.raw.human_move
        );

        mComputerMediaPlayer = MediaPlayer.create(
                getApplicationContext(),
                R.raw.computer_move
        );
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (mHumanMediaPlayer != null) {

            mHumanMediaPlayer.release();
            mHumanMediaPlayer = null;
        }

        if (mComputerMediaPlayer != null) {

            mComputerMediaPlayer.release();
            mComputerMediaPlayer = null;
        }
    }

    private void playMoveSound(char player) {

        if (player == TicTacToeGame.HUMAN_PLAYER) {

            if (mHumanMediaPlayer != null) {
                mHumanMediaPlayer.start();
            }

        } else if (player == TicTacToeGame.COMPUTER_PLAYER) {

            if (mComputerMediaPlayer != null) {
                mComputerMediaPlayer.start();
            }
        }
    }

    // ============================================================
    // DIÁLOGO PARA SALIR
    // ============================================================

    private void showQuitDialog() {

        // Crear diálogo
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);


        // Título
        builder.setTitle(
                R.string.quit_title
        );


        // Pregunta
        builder.setMessage(
                R.string.quit_question
        );


        // Botón Sí
        builder.setPositiveButton(
                R.string.yes,
                (dialog, which) -> finish()
        );


        // Botón No
        builder.setNegativeButton(
                R.string.no,
                null
        );


        // Mostrar diálogo
        builder.show();
    }
}