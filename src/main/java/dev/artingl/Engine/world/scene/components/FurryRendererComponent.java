package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.renderer.shader.Shader;
import dev.artingl.Engine.renderer.shader.ShaderProgram;
import dev.artingl.Engine.renderer.shader.ShaderType;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FurryRendererComponent extends Component {

    public static final ShaderProgram FURRY_PROGRAM = new ShaderProgram(
            new Shader(ShaderType.VERTEX, new Resource("engine", "shaders/furry/furry.vert")),
            new Shader(ShaderType.FRAGMENT, new Resource("engine", "shaders/furry/furry.frag"))
    );

    public boolean isEnabled = true;
    public Type type;
    public Color color;
    public float layersStep;
    public int layers;
    public int density;

    private int currentLayers;
    private MeshComponent meshComponent;

    public FurryRendererComponent(Type type, int layers, int density, float layersStep) {
        this.layers = layers;
        this.layersStep = layersStep;
        this.type = type;
        this.color = Color.GREEN;
        this.density = density;
    }

    @Override
    public void init(SceneNode node) throws EngineException {
        super.init(node);

        MeshComponent mesh = node.getComponent(MeshComponent.class);
        if (mesh == null) {
            node.getEngine().getLogger().log(LogLevel.WARNING, getName() + " requires MeshComponent to be present!");
            return;
        }

        this.meshComponent = mesh;
    }

    @Override
    public void render(SceneNode node, RenderContext context) {
        if (!FURRY_PROGRAM.isBaked())
            FURRY_PROGRAM.bake();

        super.render(node, context);
        if (!this.meshComponent.enableRendering || !this.isEnabled)
            return;

        if (this.currentLayers != this.layers) {
            this.currentLayers = this.layers;
            this.makeLayers();
        }

        IMesh mesh = this.meshComponent.mesh;

        // Render the fur using custom shader
        ShaderProgram defaultShader = mesh.getInstancedShaderProgram();
        mesh.setInstancedShaderProgram(FURRY_PROGRAM);
        FURRY_PROGRAM.setUniformFloat("m_layersStep", layersStep * layers);
        FURRY_PROGRAM.setUniformInt("density", density);
        FURRY_PROGRAM.setUniformInt("m_objType", type.ordinal());
        FURRY_PROGRAM.setUniformFloat("m_totalLayers", layers);
        FURRY_PROGRAM.setUniformVector3f("furColor", color.asVector3f());
        mesh.renderInstanced(context);

        // Return the default instanced shader
        mesh.setInstancedShaderProgram(defaultShader);
    }

    @Override
    public String getName() {
        return "FurryPlaneComponent";
    }

    private void makeLayers() {
        this.meshComponent.mesh.clearInstances();
        // Add all layers of the fur to the mesh as instances to perform instanced rendering later
        for (int i = 0; i < this.layers; i++) {
//            Matrix4f mat = new Matrix4f();
//            mat.m00(((float)i) / this.layers);
            this.meshComponent.mesh.addInstance(VerticesBuffer.wrap(((float)i) / this.layers));
        }
        this.meshComponent.mesh.bake();
    }

    public enum Type {
        PLANE,
        MESH,
    }
}
