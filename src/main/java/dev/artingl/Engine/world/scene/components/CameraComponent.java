package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.viewport.IViewport;

public class CameraComponent extends Component {

    public IViewport.Type type;
    public Color backgroundColor = Color.BLACK;
    public float fov;
    public float nearPlane, farPlane;
    public float size;
    public boolean postprocessing;

    public CameraComponent() {
        super();
        this.type = IViewport.Type.PERSPECTIVE;
        this.fov = 90;
        this.nearPlane = 0.03f;
        this.size = 0.02f;
        this.farPlane = 1000.0f;
        this.postprocessing = false;
    }

    @Override
    public String getName() {
        return "Camera";
    }

}
