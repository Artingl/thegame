package dev.artingl;

import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.GameDirector;

public class Start {

    public static void main(String[] args) throws Exception {
        Resource.setResourcesLocation("C:\\stuff\\workspace\\local\\thegame\\src\\main\\resources\\");
        System.exit(new GameDirector().run());
    }

}