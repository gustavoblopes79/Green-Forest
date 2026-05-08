package com.greenforest;

public enum GameState {
    TITLE,        // tela inicial
    PLAYING,      // gameplay normal
    PAUSED,       // ESC pausou o jogo
    BOSS_LESSON,  // tela de ensinamento apos matar um boss
    GAME_OVER     // tela de game over
}