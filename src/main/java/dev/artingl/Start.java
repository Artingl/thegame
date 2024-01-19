package dev.artingl;

import dev.artingl.Game.GameDirector;

public class Start {

    public static void main(String[] args) throws Exception {
        System.exit(new GameDirector().run());
    }

}