package com.wph.chinese.jchess.game;

public interface IGameCallbackWph {
    void postPlaySound(int soundIndex);

    void postShowMessage(String message);

    void postShowMessage(int messageId);

    void postStartThink();

    void postEndThink();
}
