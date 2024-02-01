package dev.artingl.Engine.debug;

import dev.artingl.Engine.world.scene.SceneManager;
import dev.artingl.Engine.world.scene.components.annotations.ComponentFinalField;
import dev.artingl.Engine.world.scene.components.annotations.ComponentIgnoreField;
import dev.artingl.Engine.world.scene.components.annotations.ComponentInformation;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.pipeline.IPipeline;
import dev.artingl.Engine.renderer.pipeline.PipelineInstance;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.world.scene.nodes.sprites.SquareNode;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.TextureManager;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.ui.FontAwesomeIcons;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Debugger implements IPipeline {

    // Imgui
    private ImGuiImplGl3 glImpl;
    private ImGuiImplGlfw glfwImpl;

    // Debug variables
    private SceneNode selectedNode = null;

    @Override
    public void pipelineCleanup(PipelineInstance instance) {
        this.glfwImpl.dispose();
        this.glImpl.dispose();
    }

    @Override
    public void pipelineInit(PipelineInstance instance) {
        ImGui.createContext();

        this.glImpl = new ImGuiImplGl3();
        this.glImpl.init();

        this.glfwImpl = new ImGuiImplGlfw();
        this.glfwImpl.init(Engine.getInstance().getDisplay().getWindowId(), true);

        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.setConfigViewportsNoTaskBarIcon(true);

        // TODO: set the scale according to the display DPI
        io.setFontGlobalScale(1);//1.25f);

        io.getFonts().addFontDefault();
        final ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder(); // Glyphs ranges provide
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());
        rangesBuilder.addRanges(FontAwesomeIcons._IconRange);
    }

    @Override
    public void pipelineRender(RenderContext renderContext, PipelineInstance instance) {
        this.glImpl.updateFontsTexture();
        this.glfwImpl.newFrame();
        ImGui.newFrame();
        ImGuizmo.beginFrame();

        Engine engine = Engine.getInstance();
        Renderer renderer = engine.getRenderer();
        TextureManager textureManager = engine.getTextureManager();
        Profiler profiler = engine.getProfiler();
        Timer timer = engine.getTimer();
        Display display = engine.getDisplay();

        SceneManager sceneRegistry = engine.getSceneManager();
        BaseScene currentScene = sceneRegistry.getCurrentScene();

        // Window with information about current scene
        if (ImGui.begin("Main", ImGuiWindowFlags.AlwaysAutoResize)) {
            // List with all available scenes
            if (ImGui.beginListBox("Scenes")) {
                for (Resource name : sceneRegistry.getSceneNames()) {
                    if (ImGui.selectable(name.toString(), name.equals(sceneRegistry.getCurrentSceneName()))) {
                        // The scene was clicked, switch to it
                        sceneRegistry.switchScene(name);
                        selectedNode = null;
                    }
                }
                ImGui.endListBox();
            }
            ImGui.separator();

            ImGui.text("Nodes: " + currentScene.getNodes().size());
            ImGui.separator();

            ImGui.text("Geometry objects: undefined");
            ImGui.text("Gravity: undefined");
            ImGui.separator();

            drawSceneInterface(currentScene);
        }
        ImGui.end();

        // Window with all nodes on the scene
        if (ImGui.begin("Scene Nodes", ImGuiWindowFlags.AlwaysAutoResize)) {
            // List of all nodes
            if (ImGui.beginListBox("Nodes")) {
                for (SceneNode node : currentScene.getNodes()) {
                    if (node.isChild())
                        continue;
                    drawSceneNode(node, 0);
                }

                // Draw node's context menu
                if (ImGui.beginPopupContextWindow()) {
                    // Delete selected node button
                    if (ImGui.menuItem("Delete") && selectedNode != null) {
                        currentScene.removeNode(selectedNode);
                        selectedNode = null;
                    }

                    if (ImGui.menuItem("Add Square")) {
                        selectedNode = new SquareNode();
                        currentScene.addNode(selectedNode);
                    }

                    if (ImGui.menuItem("Add Camera")) {
                        selectedNode = new CameraNode();
                        currentScene.addNode(selectedNode);
                    }

                    ImGui.endPopup();
                }

                ImGui.endListBox();
            }

            ImGui.separator();

            // Selected node properties
            if (selectedNode != null)
                drawComponentsInterface(selectedNode, currentScene);
        }
        ImGui.end();

        // Window with debug info
        if (ImGui.begin("Debug", ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("FPS: " + profiler.getFPS());
            ImGui.text("Frame time: " + profiler.getFrameTime());
            ImGui.text("Framebuffer binds: " + profiler.getCounter(Profiler.Task.FRAMEBUFFER_BINDS));
            ImGui.text("Draw calls: " + profiler.getCounter(Profiler.Task.DRAW_CALLS));
            ImGui.text("Vertices drawn: " + profiler.getCounter(Profiler.Task.VERTICES_DRAWN));
            ImGui.text("Pipeline size: " + renderer.getPipeline().totalElements());
            ImGui.separator();

            ImGui.text("Ticks: " + timer.getTicks());
            ImGui.text("Textures: " + textureManager.totalTextures());
            ImGui.separator();

            ImGui.text("Width: " + display.getWidth());
            ImGui.text("Height: " + display.getHeight());
            ImGui.text("GPU: " + engine.getGraphicsInfo());

            float[] rd = new float[]{engine.getOptions().getFloat(Options.Values.RENDER_DISTANCE)};
            ImGui.sliderFloat("Render Distance", rd, 0.1f, 1f);
            engine.getOptions().set(Options.Values.RENDER_DISTANCE, rd[0]);
            if (ImGui.button("Potato graphics"))
                engine.getOptions().set(Options.Values.RENDER_DISTANCE, 0.0f);
            ImGui.separator();

            if (ImGui.button("Reload"))
                engine.reload();

            if (ImGui.checkbox("vsync", display.isVsyncEnabled())) {
                display.setVsync(!display.isVsyncEnabled());
            }

            if (ImGui.checkbox("wireframe", renderer.isWireframeEnabled())) {
                renderer.setWireframe(!renderer.isWireframeEnabled());
            }
        }
        ImGui.end();

        ImGui.endFrame();
        ImGui.render();
        glImpl.renderDrawData(ImGui.getDrawData());
    }

    @Override
    public int pipelineFlags() {
        return Flags.RENDER_DIRECTLY;
    }

    public void drawSceneNode(SceneNode node, int depth) {
        // TODO: remove this
        if (node.getNametag().startsWith("Chunk"))
            return;

        if (ImGui.selectable((" ".repeat(depth*2)) + node.getNametag(), selectedNode == node)) {
            selectedNode = node;
        }

        // Draw all node's children
        for (SceneNode child: node.getChildrenNodes()) {
            drawSceneNode(child, depth + 1);
        }
    }

    private void drawSceneInterface(BaseScene scene) {
        // Draw properties for the node itself
        for (Field field : scene.getClass().getFields()) {
            drawField(field, scene);
        }
    }

    private void drawComponentsInterface(SceneNode node, BaseScene scene) {
        ImGui.text(node.getNametag());
        ImGui.text("UUID: " + node.getUUID());
        ImGui.text("PARENT: " + (node.getParent() == null ? null : node.getParent().getUUID()));

        // Draw properties for the node itself
        for (Field field : node.getClass().getFields()) {
            drawField(field, node);
        }

        List<String> drawnFields = new ArrayList<>();

        for (Component component : node.getComponents()) {
            if (ImGui.collapsingHeader(component.getName())) {

                // Render properties for all known fields
                for (Field field : component.getClass().getFields()) {
                    if (drawnFields.contains(field.getName()))
                        continue;

                    drawnFields.add(field.getName());
                    drawField(field, component);
                }
            }
        }

        drawnFields.clear();
    }

    private void drawField(Field field, Object parent) {
        try {
            Object value = field.get(parent);
            Class<?> clazz = field.getType();

            String name = Utils.prettify(field.getName());

            // Check if we need to ignore this field
            if (field.isAnnotationPresent(ComponentIgnoreField.class))
                return;

            // Check if we have additional information for this field
            if (field.isAnnotationPresent(ComponentInformation.class)) {
                ComponentInformation info = field.getAnnotation(ComponentInformation.class);
                ImGui.textColored(Color.from("#ffcccccc").argb(), info.desc());
                ImGui.separator();
            }

            // Draw according to its type
            Object result = null;
            boolean canEdit = !field.isAnnotationPresent(ComponentFinalField.class);
            if (clazz.equals(Vector3f.class)) result = drawElement(canEdit, name, (Vector3f) value);
            if (clazz.equals(Vector3i.class)) result = drawElement(canEdit, name, (Vector3i) value);
            if (clazz.equals(Vector2f.class)) result = drawElement(canEdit, name, (Vector2f) value);
            if (clazz.equals(Vector2i.class)) result = drawElement(canEdit, name, (Vector2i) value);
            if (clazz.equals(IViewport.Type.class)) result = drawElement(name, (IViewport.Type) value);
            if (clazz.equals(IMesh.class)) result = drawElement(name, (IMesh) value);
            if (clazz.equals(String.class)) result = drawElement(canEdit, name, (String) value);
            if (clazz.equals(Color.class)) result = drawElement(name, (Color) value);
            if (value instanceof Float) result = drawElement(canEdit, name, (Float) value);
            if (value instanceof Integer) result = drawElement(canEdit, name, (Integer) value);
            if (value instanceof Boolean) result = drawElement(canEdit, name, (Boolean) value);
            if (value instanceof Double) result = drawElement(canEdit, name, (Double) value);

            if (result != null)
                field.set(parent, result);

        } catch (IllegalAccessException ignored) {
        }
    }

    private Object drawElement(String name, Color v) {
        if (v == null)
            return Color.WHITE;

        float[] color = { v.red() / 255.0f, v.green() / 255.0f, v.blue() / 255.0f, v.alpha() / 255.0f };
        if (ImGui.colorPicker4(name, color))
            return Color.from(color);

        return null;
    }

    private Object drawElement(boolean canEdit, String name, String v) {
        if (v == null)
            return "";

        ImString value = new ImString(v);
        if (canEdit)
            ImGui.inputText(name, value);
        else ImGui.labelText(name, v);
        return value.get();
    }

    private Object drawElement(String name, IMesh mesh) {
        if (mesh != null) {
            ImGui.text("Mesh: " + mesh.getClass().getSimpleName());
//            ImGui.text("Shader program: " + mesh.getShaderProgram());
            ImGui.text("Is baked: " + mesh.isBaked());
            ImGui.text("Is dirty: " + mesh.isDirty());
            ImGui.text("Quality: " + mesh.getQuality());

            if (ImGui.button("Make dirty"))
                mesh.makeDirty();
        } else {
            ImGui.textColored(Color.from("#ff0000").rgba(), "No mesh is set.");
        }

        return null;
    }

    private Object drawElement(boolean canEdit, String name, Boolean v) {
        if (canEdit) {
            if (ImGui.checkbox(name, v))
                return !v;
            return v;
        }

        ImGui.labelText(name, v ? "true" : "false");
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Integer v) {
        if (canEdit) {
            ImInt value = new ImInt(v);
            ImGui.inputInt(name, value);
            return value.get();
        }

        ImGui.labelText(name, String.valueOf(v));
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Float v) {
        if (canEdit) {
            ImFloat value = new ImFloat(v);
            ImGui.inputFloat(name, value);
            return value.get();
        }

        ImGui.labelText(name, String.valueOf(v));
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Double v) {
        if (canEdit) {
            ImDouble value = new ImDouble(v);
            ImGui.inputDouble(name, value);
            return value.get();
        }

        ImGui.labelText(name, String.valueOf(v));
        return null;
    }

    private Object drawElement(String name, IViewport.Type v) {
        if (v == null)
            return IViewport.Type.PERSPECTIVE;

        if (ImGui.button("Orthographic"))
            return IViewport.Type.ORTHOGRAPHIC;
        ImGui.sameLine();
        if (ImGui.button("Perspective"))
            return IViewport.Type.PERSPECTIVE;
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Vector3f vec) {
        if (vec == null)
            return new Vector3f();

        float[] position = Utils.wrapVector(vec);
        ImGui.inputFloat3(name, position);
        if (canEdit)
            return vec.set(position);
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Vector3i vec) {
        if (vec == null)
            return new Vector3i();

        int[] position = Utils.wrapVector(vec);
        ImGui.inputInt3(name, position);
        if (canEdit)
            return vec.set(position);
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Vector2f vec) {
        if (vec == null)
            return new Vector2f();

        float[] position = Utils.wrapVector(vec);
        ImGui.inputFloat2(name, position);
        if (canEdit)
            return vec.set(position);
        return null;
    }

    private Object drawElement(boolean canEdit, String name, Vector2i vec) {
        if (vec == null)
            return new Vector2i();

        int[] position = Utils.wrapVector(vec);
        ImGui.inputInt2(name, position);
        if (canEdit)
            return vec.set(position);
        return null;
    }

}
