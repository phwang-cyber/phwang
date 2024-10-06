package com.wph.chinese.jchess.game;

import android.support.annotation.NonNull;

import com.hzy.chinese.jchess.R;
import com.wph.chinese.jchess.xqwlight.PositionWph;
import com.wph.chinese.jchess.xqwlight.SearchWph;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.wph.chinese.jchess.game.GameConfigWph.RESP_CAPTURE;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_CAPTURE2;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_CHECK;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_CHECK2;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_CLICK;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_DRAW;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_ILLEGAL;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_LOSS;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_MOVE;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_MOVE2;
import static com.wph.chinese.jchess.game.GameConfigWph.RESP_WIN;

public class GameLogicWph {

    private IGameViewWph mGameView;
    private String currentFen;
    private int sqSelected, mvLast;
    private volatile boolean thinking = false;
    private boolean flipped = false;
    private int level = 0;
    private PositionWph pos = new PositionWph();
    private SearchWph searchWph = new SearchWph(pos, 16);
    private Deque<String> mHistoryList = new ArrayDeque<>();
    private IGameCallbackWph mGameCallback;
    private volatile boolean mDrawBoardFinish;

    public GameLogicWph(IGameViewWph gameView) {
        this(gameView, null);
    }

    public GameLogicWph(@NonNull IGameViewWph gameView, IGameCallbackWph callback) {
        mGameCallback = callback;
        mGameView = gameView;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCallback(IGameCallbackWph callback) {
        this.mGameCallback = callback;
    }

    public void drawGameBoard() {
        for (int x = PositionWph.FILE_LEFT; x <= PositionWph.FILE_RIGHT; x++) {
            for (int y = PositionWph.RANK_TOP; y <= PositionWph.RANK_BOTTOM; y++) {
                int sq = PositionWph.COORD_XY(x, y);
                sq = (flipped ? PositionWph.SQUARE_FLIP(sq) : sq);
                int xx = x - PositionWph.FILE_LEFT;
                int yy = y - PositionWph.RANK_TOP;
                int pc = pos.squares[sq];
                if (pc > 0) {
                    mGameView.drawPiece(pc, xx, yy);
                }
                if (sq == sqSelected || sq == PositionWph.SRC(mvLast) ||
                        sq == PositionWph.DST(mvLast)) {
                    mGameView.drawSelected(xx, yy);
                }
            }
        }
        mDrawBoardFinish = true;
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void restart() {
        restart(false, 0);
    }

    public void restart(boolean flipped, String newFen) {
        if (!thinking) {
            this.flipped = flipped;
            currentFen = newFen;
            mHistoryList.clear();
            startPlay();
        }
    }

    public void restart(boolean flipped, int handicap) {
        if (!thinking) {
            this.flipped = flipped;
            int index = (handicap >= PositionWph.STARTUP_FEN.length || handicap < 0) ? 0 : handicap;
            currentFen = PositionWph.STARTUP_FEN[index];
            mHistoryList.clear();
            startPlay();
        }
    }

    public void retract() {
        if (!thinking) {
            String fen = popHistory();
            if (fen != null) {
                currentFen = fen;
                startPlay();
            }
        }
    }

    private void startPlay() {
        pos.fromFen(currentFen);
        sqSelected = mvLast = 0;
        if (flipped && pos.sdPlayer == 0) {
            thinking();
        } else {
            mGameView.postRepaint();
        }
    }

    /**
     * Do not call this function in main thread
     * it will block the process util UI updated
     */
    private void blockRepaint() {
        mDrawBoardFinish = false;
        mGameView.postRepaint();
        while (!mDrawBoardFinish) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickSquare(int sq_) {
        if (thinking) {
            return;
        }
        int sq = (flipped ? PositionWph.SQUARE_FLIP(sq_) : sq_);
        int pc = pos.squares[sq];
        if ((pc & PositionWph.SIDE_TAG(pos.sdPlayer)) != 0) {
            if (sqSelected > 0) {
                drawSquare(sqSelected);
            }
            if (mvLast > 0) {
                drawMove(mvLast);
                mvLast = 0;
            }
            sqSelected = sq;
            drawSquare(sq);
            playSound(RESP_CLICK);
            mGameView.postRepaint();
        } else if (sqSelected > 0) {
            int mv = PositionWph.MOVE(sqSelected, sq);
            if (!pos.legalMove(mv)) {
                return;
            }
            if (!pos.makeMove(mv)) {
                playSound(RESP_ILLEGAL);
                return;
            }
            int response = pos.inCheck() ? RESP_CHECK :
                    pos.captured() ? RESP_CAPTURE : RESP_MOVE;
            if (pos.captured()) {
                pos.setIrrev();
            }
            mvLast = mv;
            sqSelected = 0;
            drawMove(mv);
            playSound(response);
            if (!getResult()) {
                thinking();
            } else {
                mGameView.postRepaint();
            }
        }
    }

    private void drawSquare(int sq_) {
        int sq = (flipped ? PositionWph.SQUARE_FLIP(sq_) : sq_);
        int x = PositionWph.FILE_X(sq) - PositionWph.FILE_LEFT;
        int y = PositionWph.RANK_Y(sq) - PositionWph.RANK_TOP;
        //canvas.postRepaint(x, y, SQUARE_SIZE, SQUARE_SIZE);
    }

    private void drawMove(int mv) {
        //drawSquare(PositionWph.SRC(mv));
        //drawSquare(PositionWph.DST(mv));
    }

    private void playSound(int response) {
        if (mGameCallback != null) {
            mGameCallback.postPlaySound(response);
        }
    }

    private void showMessage(String message) {
        if (mGameCallback != null) {
            mGameCallback.postShowMessage(message);
        }
    }

    private void showMessage(int stringResId) {
        if (mGameCallback != null) {
            mGameCallback.postShowMessage(stringResId);
        }
    }

    private void thinking() {
        thinking = true;
        new Thread() {
            public void run() {
                mGameCallback.postStartThink();
                int mv = mvLast;
                searchWph.prepareSearch();
                blockRepaint();
                mvLast = searchWph.searchMain(100 << level);
                pos.makeMove(mvLast);
                drawMove(mv);
                drawMove(mvLast);
                int response = pos.inCheck() ? RESP_CHECK2 :
                        pos.captured() ? RESP_CAPTURE2 : RESP_MOVE2;
                if (pos.captured()) {
                    pos.setIrrev();
                }
                getResult(response);
                thinking = false;
                mGameView.postRepaint();
                mGameCallback.postEndThink();
            }
        }.start();
    }

    private boolean getResult() {
        return getResult(-1);
    }

    private boolean getResult(int response) {
        if (pos.isMate()) {
            playSound(response < 0 ? RESP_WIN : RESP_LOSS);
            showMessage(response < 0 ?
                    R.string.congratulations_you_win : R.string.you_lose_and_try_again);
            return true;
        }
        int vlRep = pos.repStatus(3);
        if (vlRep > 0) {
            vlRep = (response < 0 ? pos.repValue(vlRep) : -pos.repValue(vlRep));
            playSound(vlRep > PositionWph.WIN_VALUE ? RESP_LOSS :
                    vlRep < -PositionWph.WIN_VALUE ? RESP_WIN : RESP_DRAW);
            showMessage(vlRep > PositionWph.WIN_VALUE ?
                    R.string.play_too_long_as_lose : vlRep < -PositionWph.WIN_VALUE ?
                    R.string.pc_play_too_long_as_lose : R.string.standoff_as_draw);
            return true;
        }
        if (pos.moveNum > 100) {
            playSound(RESP_DRAW);
            showMessage(R.string.both_too_long_as_draw);
            return true;
        }
        if (response >= 0) {
            playSound(response);
            pushHistory(currentFen);
            currentFen = pos.toFen();
        }
        return false;
    }

    private void pushHistory(String fen) {
        if (mHistoryList.size() >= GameConfigWph.MAX_HISTORY_SIZE) {
            mHistoryList.poll();
        }
        mHistoryList.offer(fen);
    }

    private String popHistory() {
        if (mHistoryList.size() == 0) {
            showMessage(R.string.no_more_histories);
            playSound(RESP_ILLEGAL);
            return null;
        }
        playSound(RESP_MOVE2);
        return mHistoryList.pollLast();
    }
}
