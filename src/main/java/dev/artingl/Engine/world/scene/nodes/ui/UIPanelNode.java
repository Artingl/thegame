package dev.artingl.Engine.world.scene.nodes.ui;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.SphereMesh;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;

public class UIPanelNode extends SceneNode {
    public static final ShaderProgram PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/ui/ui.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/ui/ui.frag"))
    );

    static {
        try {
            PROGRAM.bake();
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    private final CameraNode uiCamera;

    public UIPanelNode() {
        this.uiCamera = new CameraNode();

        SphereMesh mesh = new SphereMesh(0.1f);
        mesh.setShaderProgram(PROGRAM);

        this.addComponent(new MeshComponent(mesh));

        getTransform().position.z = -1;
    }

    @Override
    public void render(RenderContext context) {
        // Setup camera for UI rendering and after rendering return the main one
//        BaseScene scene = getScene();
//        Viewport viewport = context.getViewport();
//        CameraNode mainCamera = scene.getMainCamera();
//
//        viewport.setViewport(uiCamera);
//        viewport.update();
//        super.render(context);
//        viewport.setViewport(mainCamera);
//        viewport.update();
    }
}
