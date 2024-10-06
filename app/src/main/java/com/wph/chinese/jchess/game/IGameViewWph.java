package com.wph.chinese.jchess.game;


public interface IGameViewWph {

    void postRepaint();

    void drawPiece(int pc, int xx, int yy);

    void drawSelected(int xx, int yy);
}
