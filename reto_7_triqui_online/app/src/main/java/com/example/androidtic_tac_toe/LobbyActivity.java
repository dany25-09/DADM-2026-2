package com.example.androidtic_tac_toe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LobbyActivity extends AppCompatActivity {

    // Referencia al nodo "games" y consulta de juegos en espera
    private DatabaseReference mGamesRef;
    private Query mWaitingQuery;

    // Datos de la lista (dos listas paralelas: id del juego y texto)
    private final List<String> mGameIds = new ArrayList<>();
    private final List<String> mLabels = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;

    private EditText mNameEdit;
    private SharedPreferences mPrefs;
    private String mPlayerId;

    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // --------------------------------------------------------
        // IDENTIDAD DEL JUGADOR
        // --------------------------------------------------------
        // Sin login, identificamos al dispositivo con un UUID que se
        // genera una vez y se guarda en SharedPreferences (Reto 6).

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        mPlayerId = mPrefs.getString("playerId", null);

        if (mPlayerId == null) {
            mPlayerId = UUID.randomUUID().toString();
            mPrefs.edit().putString("playerId", mPlayerId).apply();
        }

        // --------------------------------------------------------
        // VISTAS
        // --------------------------------------------------------

        mNameEdit = findViewById(R.id.player_name);
        mNameEdit.setText(mPrefs.getString("playerName", ""));

        ListView gamesList = findViewById(R.id.games_list);
        gamesList.setEmptyView(findViewById(R.id.empty_text));

        mAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                mLabels);

        gamesList.setAdapter(mAdapter);

        gamesList.setOnItemClickListener(
                (parent, view, position, id) ->
                        joinGame(mGameIds.get(position)));

        Button createButton = findViewById(R.id.create_game);
        createButton.setOnClickListener(v -> createGame());

        Button vsAndroidButton = findViewById(R.id.play_vs_android);
        vsAndroidButton.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        // --------------------------------------------------------
        // FIREBASE
        // --------------------------------------------------------

        mGamesRef = FirebaseDatabase.getInstance().getReference("games");

        // Solo los juegos con status == "waiting"
        mWaitingQuery = mGamesRef.orderByChild("status").equalTo("waiting");
    }

    // ============================================================
    // ESCUCHAR LA LISTA MIENTRAS LA PANTALLA ES VISIBLE
    // ============================================================

    private final ValueEventListener mListListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot snapshot) {

            mGameIds.clear();
            mLabels.clear();

            for (DataSnapshot child : snapshot.getChildren()) {

                String host = child.child("host").getValue(String.class);

                // No mostramos nuestros propios juegos
                if (mPlayerId.equals(host)) {
                    continue;
                }

                String hostName =
                        child.child("hostName").getValue(String.class);

                mGameIds.add(child.getKey());
                mLabels.add(getString(
                        R.string.lobby_join_label, hostName));
            }

            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError error) {

            Toast.makeText(
                    LobbyActivity.this,
                    getString(R.string.connection_error, error.getMessage()),
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mWaitingQuery.addValueEventListener(mListListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dejamos de escuchar para no gastar datos ni batería
        mWaitingQuery.removeEventListener(mListListener);
    }

    // ============================================================
    // CREAR JUEGO
    // ============================================================

    private void createGame() {

        String name = getPlayerName();

        // push() genera un id único y ordenado en el tiempo
        DatabaseReference gameRef = mGamesRef.push();

        Map<String, Object> game = new HashMap<>();

        game.put("host", mPlayerId);
        game.put("hostName", name);
        game.put("status", "waiting");
        game.put("board", "         ");     // 9 espacios
        game.put("turn", "X");
        game.put("winner", "");
        game.put("createdAt", ServerValue.TIMESTAMP);

        gameRef.setValue(game).addOnFailureListener(e ->
                Toast.makeText(
                        this,
                        getString(R.string.connection_error, e.getMessage()),
                        Toast.LENGTH_LONG).show());

        openGame(gameRef.getKey());
    }

    // ============================================================
    // UNIRSE A UN JUEGO (con transacción)
    // ============================================================

    private void joinGame(String gameId) {

        final String name = getPlayerName();

        DatabaseReference gameRef = mGamesRef.child(gameId);

        gameRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData data) {

                // Primera pasada sin datos en caché: Firebase reintenta
                // con el valor real del servidor.
                if (data.getValue() == null) {
                    return Transaction.success(data);
                }

                String status = data.child("status").getValue(String.class);
                String guest = data.child("guest").getValue(String.class);

                // Si ya no está en espera o alguien llegó antes: abortar
                if (!"waiting".equals(status) || guest != null) {
                    return Transaction.abort();
                }

                data.child("guest").setValue(mPlayerId);
                data.child("guestName").setValue(name);
                data.child("status").setValue("playing");

                return Transaction.success(data);
            }

            @Override
            public void onComplete(DatabaseError error,
                                   boolean committed,
                                   DataSnapshot snapshot) {

                if (committed && snapshot != null && snapshot.exists()) {
                    openGame(gameId);
                } else {
                    Toast.makeText(
                            LobbyActivity.this,
                            R.string.join_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private void openGame(String gameId) {

        Intent intent = new Intent(this, OnlineGameActivity.class);
        intent.putExtra(OnlineGameActivity.EXTRA_GAME_ID, gameId);
        startActivity(intent);
    }

    private String getPlayerName() {

        String name = mNameEdit.getText().toString().trim();

        if (name.isEmpty()) {
            name = getString(R.string.default_player_name);
        }

        mPrefs.edit().putString("playerName", name).apply();

        return name;
    }
}