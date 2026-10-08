package com.example.androidtic_tac_toe;

import android.app.AlertDialog;
import android.content.SharedPreferences;          // NUEVO
import android.os.Bundle;
import android.os.Handler;                          // NUEVO
import android.os.Looper;                           // NUEVO
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
    // NUEVO: PERSISTENCIA Y RETRASO DEL COMPUTADOR
    // ============================================================

    // Archivo de preferencias donde se guarda marcador y dificultad
    private SharedPreferences mPrefs;

    // Handler asociado al hilo principal para retrasar la jugada
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // Tarea que ejecuta la jugada de Android
    private final Runnable mComputerMoveRunnable = this::makeComputerMove;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Android elige solo entre layout/ y layout-land/
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
        mBoardView.setGame(mGame);

        // --------------------------------------------------------
        // OBTENER TEXTVIEWS
        // --------------------------------------------------------

        mInfoTextView = findViewById(R.id.info);
        mScoreTextView = findViewById(R.id.score);

        // --------------------------------------------------------
        // NUEVO: RESTAURAR MARCADOR Y DIFICULTAD (SharedPreferences)
        // --------------------------------------------------------

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        mHumanWins = mPrefs.getInt("mHumanWins", 0);
        mComputerWins = mPrefs.getInt("mComputerWins", 0);
        mTies = mPrefs.getInt("mTies", 0);

        // La dificultad se guardó como int (0, 1 o 2); la volvemos enum
        int savedDifficulty = mPrefs.getInt("difficulty", 2);

        TicTacToeGame.DifficultyLevel[] levels =
                TicTacToeGame.DifficultyLevel.values();

        if (savedDifficulty >= 0 && savedDifficulty < levels.length) {
            mGame.setDifficultyLevel(levels[savedDifficulty]);
        }

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
        // INICIAR O RESTAURAR PARTIDA
        // --------------------------------------------------------

        if (savedInstanceState == null) {

            // Primera ejecución: partida nueva
            newGame();

        } else {

            // NUEVO: venimos de una rotación; restauramos la partida
            mGame.setBoardState(
                    savedInstanceState.getCharArray("board"));

            mGameOver = savedInstanceState.getBoolean("mGameOver");
            mHumanTurn = savedInstanceState.getBoolean("mHumanTurn");
            mHumanStarts = savedInstanceState.getBoolean("mHumanStarts");

            mInfoTextView.setText(
                    savedInstanceState.getCharSequence("info"));

            mBoardView.invalidate();

            // EXTRA 2: la jugada pendiente de Android murió con la
            // actividad anterior, así que la reprogramamos.
            if (!mGameOver && !mHumanTurn) {
                scheduleComputerMove();
            }
        }

        updateScore();
    }

    // ============================================================
    // NUEVO: GUARDAR ESTADO (ROTACIÓN)
    // ============================================================

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putBoolean("mHumanTurn", mHumanTurn);     // corrige el bug del turno
        outState.putBoolean("mHumanStarts", mHumanStarts);
        outState.putCharSequence("info", mInfoTextView.getText());

        // El marcador NO se guarda aquí: vive en SharedPreferences.
    }

    // ============================================================
    // NUEVO: GUARDAR DATOS PERSISTENTES
    // ============================================================

    @Override
    protected void onStop() {

        super.onStop();

        SharedPreferences.Editor editor = mPrefs.edit();

        editor.putInt("mHumanWins", mHumanWins);
        editor.putInt("mComputerWins", mComputerWins);
        editor.putInt("mTies", mTies);

        // Enum -> int con ordinal(): Easy=0, Harder=1, Expert=2
        editor.putInt("difficulty",
                mGame.getDifficultyLevel().ordinal());

        editor.apply();
    }

    // ============================================================
    // NUEVO: LIMPIEZA AL DESTRUIR LA ACTIVIDAD
    // ============================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Cancela la jugada pendiente de ESTA instancia
        mHandler.removeCallbacks(mComputerMoveRunnable);
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

        // NUEVA PARTIDA
        if (id == R.id.menu_new_game) {

            newGame();

            return true;
        }

        // DIFICULTAD
        else if (id == R.id.menu_difficulty) {

            showDifficultyDialog();

            return true;
        }

        // NUEVO: REINICIAR MARCADOR
        else if (id == R.id.menu_reset_scores) {

            mHumanWins = 0;
            mComputerWins = 0;
            mTies = 0;

            updateScore();

            return true;
        }

        // (Se eliminó la opción SALIR, como pide el taller)

        return super.onOptionsItemSelected(item);
    }

    // ============================================================
    // TOUCH LISTENER DEL TABLERO
    // ============================================================

    private final View.OnTouchListener mTouchListener =
            (view, event) -> {

                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return true;
                }

                if (mGameOver) {
                    return true;
                }

                if (!mHumanTurn) {
                    return true;
                }

                float cellWidth = mBoardView.getWidth() / 3.0f;
                float cellHeight = mBoardView.getHeight() / 3.0f;

                int col = (int) (event.getX() / cellWidth);
                int row = (int) (event.getY() / cellHeight);

                if (row < 0 || row > 2 || col < 0 || col > 2) {
                    return true;
                }

                int location = row * 3 + col;

                if (mGame.getBoardOccupant(location)
                        != TicTacToeGame.OPEN_SPOT) {
                    return true;
                }

                // MOVIMIENTO DEL HUMANO
                mGame.setMove(TicTacToeGame.HUMAN_PLAYER, location);
                playMoveSound(TicTacToeGame.HUMAN_PLAYER);

                // NUEVO: redibujar YA, porque Android ahora tarda 1 s
                mBoardView.invalidate();

                // COMPROBAR GANADOR
                int winner = mGame.checkForWinner();

                if (winner != 0) {
                    gameOver(winner);
                    return true;
                }

                // CAMBIAR AL TURNO DE ANDROID
                mHumanTurn = false;
                mInfoTextView.setText(R.string.turn_computer);

                // CAMBIADO: antes era makeComputerMove() directo
                scheduleComputerMove();

                return true;
            };

    // ============================================================
    // NUEVO: PROGRAMAR LA JUGADA DE ANDROID (1 segundo)
    // ============================================================

    private void scheduleComputerMove() {

        // Evita que queden dos jugadas pendientes a la vez
        mHandler.removeCallbacks(mComputerMoveRunnable);

        mHandler.postDelayed(mComputerMoveRunnable, 1000);
    }

    // ============================================================
    // MOVIMIENTO DE ANDROID
    // ============================================================

    private void makeComputerMove() {

        // NUEVO: si la partida ya terminó, no hacemos nada
        if (mGameOver) {
            return;
        }

        int computerMove = mGame.getComputerMove();

        mGame.setMove(TicTacToeGame.COMPUTER_PLAYER, computerMove);
        playMoveSound(TicTacToeGame.COMPUTER_PLAYER);

        mBoardView.invalidate();

        int winner = mGame.checkForWinner();

        if (winner != 0) {

            gameOver(winner);

        } else {

            mHumanTurn = true;
            mInfoTextView.setText(R.string.turn_human);
        }
    }

    // ============================================================
    // NUEVA PARTIDA
    // ============================================================

    private void newGame() {

        // NUEVO: cancelar cualquier jugada pendiente de la partida anterior
        mHandler.removeCallbacks(mComputerMoveRunnable);

        mGame.clearBoard();

        mGameOver = false;
        mHumanTurn = mHumanStarts;

        mBoardView.invalidate();

        if (mHumanStarts) {

            mInfoTextView.setText(R.string.first_human);

        } else {

            mInfoTextView.setText(R.string.first_computer);

            mHumanTurn = false;

            // CAMBIADO: antes era makeComputerMove() directo
            scheduleComputerMove();
        }

        mHumanStarts = !mHumanStarts;

        updateScore();
    }

    // ============================================================
    // FIN DE PARTIDA
    // ============================================================

    private void gameOver(int winner) {

        mGameOver = true;
        mHumanTurn = false;

        if (winner == 1) {

            mTies++;
            mInfoTextView.setText(R.string.result_tie);

        } else if (winner == 2) {

            mHumanWins++;
            mInfoTextView.setText(R.string.result_human_wins);

        } else if (winner == 3) {

            mComputerWins++;
            mInfoTextView.setText(R.string.result_computer_wins);
        }

        updateScore();

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
    // DIÁLOGO DE DIFICULTAD (sin cambios)
    // ============================================================

    private void showDifficultyDialog() {

        String[] difficulties = {
                getString(R.string.difficulty_easy),
                getString(R.string.difficulty_harder),
                getString(R.string.difficulty_expert)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.difficulty_choose);

        int selectedDifficulty = getSelectedDifficulty();

        builder.setSingleChoiceItems(
                difficulties,
                selectedDifficulty,
                (dialog, which) -> {

                    if (which == 0) {
                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Easy);
                    } else if (which == 1) {
                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Harder);
                    } else if (which == 2) {
                        mGame.setDifficultyLevel(
                                TicTacToeGame.DifficultyLevel.Expert);
                    }

                    dialog.dismiss();
                });

        builder.setNegativeButton(R.string.no, null);

        builder.show();
    }

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
    // SONIDOS (sin cambios)
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(
                getApplicationContext(), R.raw.human_move);

        mComputerMediaPlayer = MediaPlayer.create(
                getApplicationContext(), R.raw.computer_move);
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

    // (showQuitDialog() eliminado: el taller pide quitar la opción Salir)
}