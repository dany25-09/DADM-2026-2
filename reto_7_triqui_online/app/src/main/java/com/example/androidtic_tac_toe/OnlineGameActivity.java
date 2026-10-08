package com.example.androidtic_tac_toe;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class OnlineGameActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID = "gameId";

    private TicTacToeGame mGame;
    private BoardView mBoardView;
    private TextView mInfoTextView;
    private TextView mScoreTextView;

    private DatabaseReference mGameRef;
    private String mPlayerId;

    // Mi ficha: 'X' si soy el creador, 'O' si soy el invitado
    private char mMyMark = ' ';

    // Último estado conocido de la partida (viene de Firebase)
    private String mStatus = "";
    private String mTurn = "X";
    private String mHostName = "";
    private String mGuestName = "";

    // Evita sonar al cargar el primer snapshot (p. ej. tras rotar)
    private boolean mHasSnapshot = false;

    private MediaPlayer mHumanMediaPlayer;
    private MediaPlayer mComputerMediaPlayer;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Reutilizamos tus layouts: layout/ y layout-land/
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.online_title);
        }

        mBoardView = findViewById(R.id.board);
        mInfoTextView = findViewById(R.id.info);
        mScoreTextView = findViewById(R.id.score);

        mGame = new TicTacToeGame();
        mBoardView.setGame(mGame);

        // El botón "Nueva partida" pasa a ser "Salir de la partida"
        Button leaveButton = findViewById(R.id.new_game);
        leaveButton.setText(R.string.leave_game);
        leaveButton.setOnClickListener(v -> finish());

        mBoardView.setOnTouchListener(mTouchListener);
        mInfoTextView.setText(R.string.connecting);

        // --------------------------------------------------------
        // DATOS DE LA PARTIDA
        // --------------------------------------------------------

        String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);

        if (gameId == null) {
            finish();
            return;
        }

        mPlayerId = getSharedPreferences("ttt_prefs", MODE_PRIVATE)
                .getString("playerId", "");

        mGameRef = FirebaseDatabase.getInstance()
                .getReference("games")
                .child(gameId);

        // Si se pierde la conexión de este dispositivo, el servidor borra
        // la partida (evita juegos "fantasma" en la lista).
        mGameRef.onDisconnect().removeValue();
    }

    // ============================================================
    // ESCUCHAR LA PARTIDA
    // ============================================================

    private final ValueEventListener mGameListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            updateFromSnapshot(snapshot);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Toast.makeText(
                    OnlineGameActivity.this,
                    getString(R.string.connection_error, error.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (mGameRef != null) {
            mGameRef.addValueEventListener(mGameListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGameRef != null) {
            mGameRef.removeEventListener(mGameListener);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // isFinishing() es true si el usuario sale (botón o Atrás) y
        // false si la Activity se destruye por una ROTACIÓN.
        if (isFinishing() && mGameRef != null) {
            mGameRef.removeValue();
            mGameRef.onDisconnect().cancel();
        }
    }

    // ============================================================
    // ACTUALIZAR LA PANTALLA SEGÚN FIREBASE
    // ============================================================

    private void updateFromSnapshot(DataSnapshot snapshot) {

        // El nodo desapareció: el otro jugador salió
        if (!snapshot.exists()) {

            if (!"finished".equals(mStatus)) {
                Toast.makeText(this, R.string.opponent_left,
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }

        String host = snapshot.child("host").getValue(String.class);
        String guest = snapshot.child("guest").getValue(String.class);
        String board = snapshot.child("board").getValue(String.class);
        String turn = snapshot.child("turn").getValue(String.class);
        String status = snapshot.child("status").getValue(String.class);
        String winner = snapshot.child("winner").getValue(String.class);

        mHostName = nonNull(snapshot.child("hostName").getValue(String.class));
        mGuestName = nonNull(snapshot.child("guestName").getValue(String.class));

        if (board == null || board.length() != TicTacToeGame.BOARD_SIZE
                || turn == null || status == null) {
            return;
        }

        // ¿Qué ficha soy?
        if (mPlayerId.equals(host)) {
            mMyMark = 'X';
        } else if (mPlayerId.equals(guest)) {
            mMyMark = 'O';
        } else {
            finish();   // no pertenezco a esta partida
            return;
        }

        // Sonido de la ficha nueva (comparando antes de actualizar)
        if (mHasSnapshot) {
            playSoundForNewPiece(board);
        }
        mHasSnapshot = true;

        // Actualizar el tablero reutilizando tu TicTacToeGame
        mGame.setBoardState(board.toCharArray());
        mBoardView.invalidate();

        mStatus = status;
        mTurn = turn;

        // ----- Texto de información -----

        if ("waiting".equals(status)) {

            mInfoTextView.setText(R.string.waiting_opponent);

        } else if ("playing".equals(status)) {

            if (turn.equals(String.valueOf(mMyMark))) {
                mInfoTextView.setText(R.string.turn_human);
            } else {
                mInfoTextView.setText(getString(
                        R.string.turn_opponent, opponentName()));
            }

        } else if ("finished".equals(status)) {

            if ("TIE".equals(winner)) {
                mInfoTextView.setText(R.string.result_tie);
            } else if (String.valueOf(mMyMark).equals(winner)) {
                mInfoTextView.setText(R.string.result_human_wins);
            } else {
                mInfoTextView.setText(getString(
                        R.string.online_lost, opponentName()));
            }
        }

        // ----- "Tú (X) vs Luis (O)" -----

        char opponentMark = (mMyMark == 'X') ? 'O' : 'X';

        mScoreTextView.setText(getString(
                R.string.online_players,
                String.valueOf(mMyMark),
                opponentName(),
                String.valueOf(opponentMark)));
    }

    // ============================================================
    // TOQUES SOBRE EL TABLERO
    // ============================================================

    private final View.OnTouchListener mTouchListener =
            (view, event) -> {

                if (event.getAction() != MotionEvent.ACTION_DOWN) {
                    return true;
                }

                // Solo se juega si la partida está en curso Y es mi turno
                if (!"playing".equals(mStatus)
                        || !String.valueOf(mMyMark).equals(mTurn)) {
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

                makeMove(location);

                return true;
            };

    // ============================================================
    // HACER UNA JUGADA (transacción)
    // ============================================================

    private void makeMove(final int location) {

        final char mark = mMyMark;

        mGameRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData data) {

                String status = data.child("status").getValue(String.class);
                String turn = data.child("turn").getValue(String.class);
                String board = data.child("board").getValue(String.class);

                // Sin datos todavía: Firebase reintentará con los reales
                if (status == null || turn == null || board == null) {
                    return Transaction.success(data);
                }

                // Validaciones en el "servidor": estado, turno y casilla
                if (!"playing".equals(status)
                        || !turn.equals(String.valueOf(mark))
                        || board.length() != TicTacToeGame.BOARD_SIZE
                        || board.charAt(location) != TicTacToeGame.OPEN_SPOT) {
                    return Transaction.abort();
                }

                char[] cells = board.toCharArray();
                cells[location] = mark;

                // Reutilizamos checkForWinner() con un juego temporal
                TicTacToeGame temp = new TicTacToeGame();
                temp.setBoardState(cells);
                int result = temp.checkForWinner();

                data.child("board").setValue(new String(cells));

                if (result == 0) {

                    // Sigue la partida: cambia el turno
                    data.child("turn").setValue(mark == 'X' ? "O" : "X");

                } else {

                    // Terminó: 1 = empate, 2 = ganó X, 3 = ganó O
                    data.child("status").setValue("finished");
                    data.child("winner").setValue(
                            result == 1 ? "TIE" : (result == 2 ? "X" : "O"));
                }

                return Transaction.success(data);
            }

            @Override
            public void onComplete(DatabaseError error,
                                   boolean committed,
                                   DataSnapshot snapshot) {

                if (error != null) {
                    Toast.makeText(
                            OnlineGameActivity.this,
                            getString(R.string.connection_error,
                                    error.getMessage()),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private String opponentName() {
        String name = (mMyMark == 'X') ? mGuestName : mHostName;
        return name.isEmpty() ? "—" : name;
    }

    private String nonNull(String value) {
        return value == null ? "" : value;
    }

    // ============================================================
    // SONIDOS (mismo ciclo de vida que en MainActivity)
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

    // Busca la casilla que pasó de vacía a ocupada y suena según su ficha
    private void playSoundForNewPiece(String newBoard) {

        for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {

            if (mGame.getBoardOccupant(i) == TicTacToeGame.OPEN_SPOT
                    && newBoard.charAt(i) != TicTacToeGame.OPEN_SPOT) {

                playMoveSound(newBoard.charAt(i));
                return;
            }
        }
    }

    private void playMoveSound(char mark) {

        if (mark == TicTacToeGame.HUMAN_PLAYER) {
            if (mHumanMediaPlayer != null) {
                mHumanMediaPlayer.start();
            }
        } else if (mark == TicTacToeGame.COMPUTER_PLAYER) {
            if (mComputerMediaPlayer != null) {
                mComputerMediaPlayer.start();
            }
        }
    }
}