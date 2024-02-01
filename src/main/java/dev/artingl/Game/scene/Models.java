package dev.artingl.Game.scene;

import dev.artingl.Engine.renderer.models.OBJModel;
import dev.artingl.Engine.resources.Resource;

public class Models {

    public static final OBJModel ROCK = new OBJModel(new Resource("thegame", "models/rock"));
    public static final OBJModel GRASS = new OBJModel(new Resource("thegame", "models/grass"));
    public static final OBJModel TREE = new OBJModel(new Resource("thegame", "models/tree"));
    public static final OBJModel SKULL = new OBJModel(new Resource("thegame", "models/skull"));
    public static final OBJModel SHELTER = new OBJModel(new Resource("thegame", "models/shelter"));
    public static final OBJModel DINGUS = new OBJModel(new Resource("thegame", "models/dingus"));


    // The same order as in EnvironmentObjects
    public static final OBJModel[] MODELS = new OBJModel[]{
            TREE,
            ROCK,
            GRASS,
            SKULL,
            SHELTER,
            DINGUS
    };

}
