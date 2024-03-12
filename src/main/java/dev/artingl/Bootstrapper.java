package dev.artingl;

import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.GameDirector;

public class Bootstrapper {

    public static void main(String[] args) throws Exception {
        Resource.setResourcesLocation("./src/main/resources/");
        int exitCode = new GameDirector().run();
        System.exit(exitCode);
    }

}