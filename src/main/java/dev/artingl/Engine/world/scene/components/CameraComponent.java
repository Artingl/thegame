package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.viewport.Viewport;

public class CameraComponent extends Component {

    public Viewport.ViewType viewType;
    public Viewport.RenderType renderType;
    public Color backgroundColor = Color.BLACK;
    public float fov;
    public float nearPlane, farPlane;
    public float size;
    public boolean enablePostprocessing;
    public boolean enableShadowMapping;

    public CameraComponent() {
        super();
        this.viewType = Viewport.ViewType.PERSPECTIVE;
        this.renderType = Viewport.RenderType.MAIN;
        this.fov = 90;
        this.size = 0.02f;
        this.nearPlane = 0.1f;
        this.farPlane = 1000.0f;
        this.enablePostprocessing = false;
        this.enableShadowMapping = false;
    }

    @Override
    public String getName() {
        return "Camera";
    }

}
