package com.example.androidtic_tac_toe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BoardView extends View {

    // Grosor de las líneas del tablero
    public static final int GRID_WIDTH = 6;

    // Imágenes de X y O
    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;

    // Juego
    private TicTacToeGame mGame;

    // Paint para dibujar las líneas
    private Paint mPaint;

    // ------------------------------------------------------------
    // CONSTRUCTOR 1
    // ------------------------------------------------------------

    public BoardView(Context context) {
        super(context);
        initialize();
    }

    // ------------------------------------------------------------
    // CONSTRUCTOR 2
    // ------------------------------------------------------------

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    // ------------------------------------------------------------
    // CONSTRUCTOR 3
    // ------------------------------------------------------------

    public BoardView(
            Context context,
            AttributeSet attrs,
            int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        initialize();
    }

    // ------------------------------------------------------------
    // INICIALIZACIÓN
    // ------------------------------------------------------------

    private void initialize() {

        // Cargar imagen X
        mHumanBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.x_img
        );

        // Cargar imagen O
        mComputerBitmap = BitmapFactory.decodeResource(
                getResources(),
                R.drawable.o_img
        );

        // Crear Paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    // ------------------------------------------------------------
    // ASIGNAR EL JUEGO AL BOARDVIEW
    // ------------------------------------------------------------

    public void setGame(TicTacToeGame game) {

        mGame = game;

        // Pedir que se vuelva a dibujar
        invalidate();
    }

    // ------------------------------------------------------------
    // DIBUJAR EL TABLERO
    // ------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        // Obtener tamaño real del View
        int boardWidth = getWidth();
        int boardHeight = getHeight();

        // Configurar líneas
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(GRID_WIDTH);

        // Calcular tamaño de cada celda
        int cellWidth = boardWidth / 3;
        int cellHeight = boardHeight / 3;

        // --------------------------------------------------------
        // LÍNEAS VERTICALES
        // --------------------------------------------------------

        canvas.drawLine(
                cellWidth,
                0,
                cellWidth,
                boardHeight,
                mPaint
        );

        canvas.drawLine(
                cellWidth * 2,
                0,
                cellWidth * 2,
                boardHeight,
                mPaint
        );

        // --------------------------------------------------------
        // LÍNEAS HORIZONTALES
        // --------------------------------------------------------

        canvas.drawLine(
                0,
                cellHeight,
                boardWidth,
                cellHeight,
                mPaint
        );

        canvas.drawLine(
                0,
                cellHeight * 2,
                boardWidth,
                cellHeight * 2,
                mPaint
        );

        // --------------------------------------------------------
        // DIBUJAR X Y O
        // --------------------------------------------------------

        if (mGame == null) {
            return;
        }

        for (int i = 0; i < TicTacToeGame.BOARD_SIZE; i++) {

            // Columna
            int col = i % 3;

            // Fila
            int row = i / 3;

            // Coordenadas de la celda
            int left = col * cellWidth;
            int top = row * cellHeight;

            int right = left + cellWidth;
            int bottom = top + cellHeight;

            // Margen para hacer X y O más pequeñas
            int marginX = (int) (cellWidth * 0.20f);
            int marginY = (int) (cellHeight * 0.20f);

            // Rectángulo más pequeño dentro de la celda
            Rect destination = new Rect(
                    left + marginX,
                    top + marginY,
                    right - marginX,
                    bottom - marginY
            );

            // X
            if (mGame.getBoardOccupant(i)
                    == TicTacToeGame.HUMAN_PLAYER) {

                canvas.drawBitmap(
                        mHumanBitmap,
                        null,
                        destination,
                        null
                );

                // O
            } else if (mGame.getBoardOccupant(i)
                    == TicTacToeGame.COMPUTER_PLAYER) {

                canvas.drawBitmap(
                        mComputerBitmap,
                        null,
                        destination,
                        null
                );
            }
        }
    }

    // ------------------------------------------------------------
    // ANCHO DE UNA CELDA
    // ------------------------------------------------------------

    public int getBoardCellWidth() {

        return getWidth() / 3;
    }

    // ------------------------------------------------------------
    // ALTO DE UNA CELDA
    // ------------------------------------------------------------

    public int getBoardCellHeight() {

        return getHeight() / 3;
    }
}