package com.example.androidtic_tac_toe;

import java.util.Random;

public class TicTacToeGame {

    // Tamaño del tablero
    public static final int BOARD_SIZE = 9;

    // Jugadores
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';

    // Casilla vacía
    public static final char OPEN_SPOT = ' ';

    // Tablero
    private final char[] mBoard = new char[BOARD_SIZE];

    // Generador de números aleatorios
    private final Random mRand = new Random();

    /**
     * Constructor.
     */
    public TicTacToeGame() {
        clearBoard();
    }

    /**
     * Limpia el tablero.
     */
    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    /**
     * Coloca X u O en una posición del tablero.
     *
     * @param player X u O
     * @param location posición de 0 a 8
     */
    public void setMove(char player, int location) {

        if (location >= 0 && location < BOARD_SIZE) {

            if (mBoard[location] == OPEN_SPOT) {
                mBoard[location] = player;
            }
        }
    }

    /**
     * Determina el movimiento de la computadora.
     *
     * La computadora:
     * 1. Intenta ganar.
     * 2. Si no puede ganar, intenta bloquear al jugador.
     * 3. Si no puede hacer ninguna de las anteriores,
     *    selecciona una casilla aleatoria.
     *
     * @return posición donde debe jugar la computadora
     */
    public int getComputerMove() {

        int move;

        // -----------------------------------------
        // 1. Intentar ganar
        // -----------------------------------------

        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {

                char current = mBoard[i];

                // Simular movimiento de la computadora
                mBoard[i] = COMPUTER_PLAYER;

                if (checkForWinner() == 3) {

                    // Restaurar casilla
                    mBoard[i] = current;

                    return i;
                }

                // Restaurar casilla
                mBoard[i] = current;
            }
        }

        // -----------------------------------------
        // 2. Intentar bloquear al jugador
        // -----------------------------------------

        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {

                char current = mBoard[i];

                // Simular movimiento del jugador
                mBoard[i] = HUMAN_PLAYER;

                if (checkForWinner() == 2) {

                    // Restaurar casilla
                    mBoard[i] = current;

                    return i;
                }

                // Restaurar casilla
                mBoard[i] = current;
            }
        }

        // -----------------------------------------
        // 3. Movimiento aleatorio
        // -----------------------------------------

        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }

    /**
     * Comprueba el estado actual del juego.
     *
     * @return
     * 0 = el juego continúa
     * 1 = empate
     * 2 = ganó X
     * 3 = ganó O
     */
    public int checkForWinner() {

        // -----------------------------------------
        // Horizontales
        // -----------------------------------------

        for (int i = 0; i <= 6; i += 3) {

            // X gana
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 1] == HUMAN_PLAYER &&
                    mBoard[i + 2] == HUMAN_PLAYER) {

                return 2;
            }

            // O gana
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 1] == COMPUTER_PLAYER &&
                    mBoard[i + 2] == COMPUTER_PLAYER) {

                return 3;
            }
        }

        // -----------------------------------------
        // Verticales
        // -----------------------------------------

        for (int i = 0; i <= 2; i++) {

            // X gana
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 3] == HUMAN_PLAYER &&
                    mBoard[i + 6] == HUMAN_PLAYER) {

                return 2;
            }

            // O gana
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 3] == COMPUTER_PLAYER &&
                    mBoard[i + 6] == COMPUTER_PLAYER) {

                return 3;
            }
        }

        // -----------------------------------------
        // Diagonales de X
        // -----------------------------------------

        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||

                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER)) {

            return 2;
        }

        // -----------------------------------------
        // Diagonales de O
        // -----------------------------------------

        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||

                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER)) {

            return 3;
        }

        // -----------------------------------------
        // Comprobar empate
        // -----------------------------------------

        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {
                return 0;
            }
        }

        return 1;
    }
}
