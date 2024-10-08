package com.wph.chinese.jchess.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hzy.chinese.jchess.R;
import com.wph.chinese.jchess.game.GameConfigWph;
import com.wph.chinese.jchess.game.GameLogicWph;
import com.wph.chinese.jchess.game.IGameViewWph;
import com.wph.chinese.jchess.xqwlight.PositionWph;



public class GameBoardViewWph extends View implements IGameViewWph {


    private static final int WIDTH_CELL_COUNT = 9;
    private static final int HEIGHT_CELL_COUNT = 10;
    private int mPieceTheme = GameConfigWph.PIECE_THEME_CARTOON;

    private float mCellWidth;
    private Bitmap[] mPiecesBitmap;
    private Canvas mCanvas;
    private GameLogicWph mGameLogic;
    private RectF mPieceDstRectF;

    public GameBoardViewWph(Context context) {
        this(context, null);
    }

    public GameBoardViewWph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameBoardViewWph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributeSet(context, attrs);
        initView();
    }

    private void loadAttributeSet(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GameBoardView);
        TypedValue outValue = new TypedValue();
        ta.getValue(R.styleable.GameBoardView_pieceTheme, outValue);
        mPieceTheme = outValue.data;
        ta.recycle();
    }

    private void initView() {
        mGameLogic = new GameLogicWph(this);
        mPieceDstRectF = new RectF();
        setBackgroundResource(R.drawable.board);
        loadBitmapResources();
    }

    public GameLogicWph getGameLogic() {
        return mGameLogic;
    }

    public void setPieceTheme(int theme) {
        if (theme == mPieceTheme)
            return;
        mPieceTheme = theme;
        loadBitmapResources();
    }

    private void loadBitmapResources() {
        int[] pieceResArray = GameConfigWph.PIECE_RES_CARTOON;
        if (mPieceTheme == GameConfigWph.PIECE_THEME_WOOD) {
            pieceResArray = GameConfigWph.PIECE_RES_WOOD;
        }
        mPiecesBitmap = new Bitmap[pieceResArray.length];
        for (int i = 0; i < pieceResArray.length; i++) {
            if (mPiecesBitmap[i] != null && !mPiecesBitmap[i].isRecycled()) {
                mPiecesBitmap[i].recycle();
            }
            mPiecesBitmap[i] = BitmapFactory.decodeResource(getResources(), pieceResArray[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float widthCell = widthSize * 1.0f / WIDTH_CELL_COUNT;
        float heightCell = heightSize * 1.0f / HEIGHT_CELL_COUNT;
        float cellWidth;
        if (widthCell < 0.1f || heightCell < 0.1f) {
            cellWidth = Math.max(widthCell, heightCell);
        } else {
            cellWidth = Math.min(widthCell, heightCell);
        }
        setMeasuredDimension((int) (cellWidth * WIDTH_CELL_COUNT),
                (int) (cellWidth * HEIGHT_CELL_COUNT));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCellWidth = w * 1.0f / WIDTH_CELL_COUNT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.mCanvas = canvas;
        mGameLogic.drawGameBoard();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int xx = (int) (event.getX() / mCellWidth);
            int yy = (int) (event.getY() / mCellWidth);
            int sq = PositionWph.COORD_XY(xx + PositionWph.FILE_LEFT, yy + PositionWph.RANK_TOP);
            mGameLogic.clickSquare(sq);
            return true;
        }
        return true;
    }

    @Override
    public void postRepaint() {
        postInvalidate();
    }

    @Override
    public void drawPiece(int pc, int xx, int yy) {
        if (mCanvas != null) {
            float x = xx * mCellWidth;
            float y = yy * mCellWidth;
            pc -= 8;
            if (pc > 6) {
                pc--;
            }
            mPieceDstRectF.set(x, y, x + mCellWidth, y + mCellWidth);
            mCanvas.drawBitmap(mPiecesBitmap[pc], null, mPieceDstRectF, null);
        }
    }

    @Override
    public void drawSelected(int xx, int yy) {
        if (mCanvas != null) {
            float x = xx * mCellWidth;
            float y = yy * mCellWidth;
            mPieceDstRectF.set(x, y, x + mCellWidth, y + mCellWidth);
            mCanvas.drawBitmap(mPiecesBitmap[14], null, mPieceDstRectF, null);
        }
    }
}
