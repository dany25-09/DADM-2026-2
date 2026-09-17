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

    // Niveles de dificultad
    public enum DifficultyLevel {
        Easy,
        Harder,
        Expert
    }

    // Dificultad actual
    private DifficultyLevel mDifficultyLevel = DifficultyLevel.Expert;

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
     * Obtiene la dificultad actual.
     */
    public DifficultyLevel getDifficultyLevel() {
        return mDifficultyLevel;
    }

    /**
     * Cambia la dificultad.
     */
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        mDifficultyLevel = difficultyLevel;
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
     * Coloca X u O en una posición.
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
     * Determina el movimiento de la computadora
     * según el nivel de dificultad.
     */
    public int getComputerMove() {

        int move = -1;

        // EASY
        if (mDifficultyLevel == DifficultyLevel.Easy) {

            move = getRandomMove();

        }

        // HARDER
        else if (mDifficultyLevel == DifficultyLevel.Harder) {

            move = getWinningMove();

            if (move == -1) {
                move = getRandomMove();
            }

        }

        // EXPERT
        else if (mDifficultyLevel == DifficultyLevel.Expert) {

            move = getWinningMove();

            if (move == -1) {
                move = getBlockingMove();
            }

            if (move == -1) {
                move = getRandomMove();
            }
        }

        return move;
    }

    /**
     * Busca una casilla aleatoria disponible.
     */
    private int getRandomMove() {

        int move;

        do {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] != OPEN_SPOT);

        return move;
    }

    /**
     * Busca un movimiento que permita ganar a Android.
     *
     * @return posición ganadora o -1 si no existe
     */
    private int getWinningMove() {

        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {

                char current = mBoard[i];

                mBoard[i] = COMPUTER_PLAYER;

                if (checkForWinner() == 3) {

                    mBoard[i] = current;

                    return i;
                }

                mBoard[i] = current;
            }
        }

        return -1;
    }

    /**
     * Busca un movimiento para bloquear al jugador.
     *
     * @return posición para bloquear o -1 si no existe
     */
    private int getBlockingMove() {

        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {

                char current = mBoard[i];

                mBoard[i] = HUMAN_PLAYER;

                if (checkForWinner() == 2) {

                    mBoard[i] = current;

                    return i;
                }

                mBoard[i] = current;
            }
        }

        return -1;
    }

    /**
     * Comprueba el estado actual del juego.
     *
     * @return
     * 0 = continúa
     * 1 = empate
     * 2 = ganó X
     * 3 = ganó O
     */
    public int checkForWinner() {

        // Horizontales
        for (int i = 0; i <= 6; i += 3) {

            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 1] == HUMAN_PLAYER &&
                    mBoard[i + 2] == HUMAN_PLAYER) {

                return 2;
            }

            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 1] == COMPUTER_PLAYER &&
                    mBoard[i + 2] == COMPUTER_PLAYER) {

                return 3;
            }
        }

        // Verticales
        for (int i = 0; i <= 2; i++) {

            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i + 3] == HUMAN_PLAYER &&
                    mBoard[i + 6] == HUMAN_PLAYER) {

                return 2;
            }

            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i + 3] == COMPUTER_PLAYER &&
                    mBoard[i + 6] == COMPUTER_PLAYER) {

                return 3;
            }
        }

        // Diagonales de X
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||

                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER)) {

            return 2;
        }

        // Diagonales de O
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||

                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER)) {

            return 3;
        }

        // Empate o juego continúa
        for (int i = 0; i < BOARD_SIZE; i++) {

            if (mBoard[i] == OPEN_SPOT) {
                return 0;
            }
        }

        return 1;
    }
}
