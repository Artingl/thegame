package dev.artingl.Engine.debug;

import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.visual.postprocessing.PostprocessEffect;
import dev.artingl.Engine.world.scene.SceneManager;
import dev.artingl.Engine.world.scene.components.annotations.ComponentFinalField;
import dev.artingl.Engine.world.scene.components.annotations.ComponentIgnoreField;
import dev.artingl.Engine.world.scene.components.annotations.ComponentInformation;
import dev.artingl.Engine.resources.Options;
import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.TextureManager;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.misc.FontAwesomeIcons;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
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
import java.util.Map;
import java.util.UUID;

public class Debugger {

    private ImGuiImplGl3 glImpl;
    private ImGuiImplGlfw glfwImpl;
    private boolean firstFrame = true;

    public void cleanup() {
        if (this.glfwImpl != null)
            this.glfwImpl.dispose();
        if (this.glImpl != null)
            this.glImpl.dispose();
    }

    public void init() {
        ImGui.createContext();

        this.glImpl = new ImGuiImplGl3();
        this.glImpl.init();

        this.glfwImpl = new ImGuiImplGlfw();
        this.glfwImpl.init(Engine.getInstance().getDisplay().getWindowId(), true);

        ImGuiIO io = ImGui.getIO();
//        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
//        io.setIniFilename("");
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

    public void frame() {
        this.glImpl.updateFontsTexture();
        this.glfwImpl.newFrame();
        ImGui.newFrame();
        ImGuizmo.beginFrame();

        Engine engine = Engine.getInstance();
        Renderer renderer = engine.getRenderer();
        TextureManager textureManager = engine.getResourceManager().getTextureManager();
        Profiler profiler = engine.getProfiler();
        Timer timer = engine.getTimer();
        Display display = engine.getDisplay();

        ImGuiIO io = ImGui.getIO();
        if (engine.getDisplay().isCursorCaptured()) {
            io.removeConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
            io.addConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard);
            io.addConfigFlags(ImGuiConfigFlags.NoMouse);
        }
        else {
            io.removeConfigFlags(ImGuiConfigFlags.NoMouse);
            io.removeConfigFlags(ImGuiConfigFlags.NavNoCaptureKeyboard);
            io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        }

        SceneManager sceneManager = engine.getSceneManager();
        BaseScene currentScene = sceneManager.getCurrentScene();

        // Window with debug info
        if (ImGui.begin("Debug",
                ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove |
                        ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoBringToFrontOnFocus |
                        ImGuiWindowFlags.AlwaysAutoResize)) {
            if (firstFrame) {
                ImVec2 size = ImGui.getWindowSize();
                ImGui.setWindowPos(2, 2);
                ImGui.setNextWindowPos(2, 10 + size.y);
                this.firstFrame = false;
            }

            ImGui.text("FPS: " + profiler.getFPS());
            ImGui.text("GPU time: " + profiler.getGpuTime());
            ImGui.text("Frame time: " + profiler.getFrameTime());
            ImGui.text("Framebuffer binds: " + profiler.getCounter(Profiler.Task.FRAMEBUFFER_BINDS));
            ImGui.text("Draw calls: " + profiler.getCounter(Profiler.Task.DRAW_CALLS));
            ImGui.text("Vertices drawn: " + profiler.getCounter(Profiler.Task.VERTICES_DRAWN));
            ImGui.text("System: " + System.getProperty("os.name") + "/" + System.getProperty("os.arch"));
            ImGui.separator();

            ImGui.text("Ticks: " + timer.getTicks());
            ImGui.text("Textures: " + textureManager.totalTextures());
            ImGui.separator();

            ImGui.text("Width: " + display.getWidth());
            ImGui.text("Height: " + display.getHeight());
            ImGui.text("GPU: " + engine.getGraphicsInfo());

            float[] rd = new float[]{engine.getOptions().getFloat(Options.Values.RENDER_DISTANCE)};
            Quality quality = (Quality) drawEnum((Quality) engine.getOptions().get(Options.Values.QUALITY_SETTING));
            ImGui.sliderFloat("Render Distance", rd, 0.1f, 1f);
            engine.getOptions().set(Options.Values.RENDER_DISTANCE, rd[0]);
            if (quality.equals(Quality.POTATO))
                engine.getOptions().set(Options.Values.RENDER_DISTANCE, 0.1f);
            engine.getOptions().set(Options.Values.QUALITY_SETTING, quality);
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

        // Window with information about current scene
        if (ImGui.begin("Main")) {
            if (ImGui.beginTabBar("##")) {
                if (ImGui.beginTabItem("Scene")) {
                    drawSceneInfo(sceneManager, currentScene, renderer);
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Scene Nodes")) {
                    drawSceneNodesInfo(sceneManager, currentScene, renderer);
                    ImGui.endTabItem();
                }

                if (ImGui.beginTabItem("Postprocessing")) {
                    drawPostprocessingInfo(renderer);
                    ImGui.endTabItem();
                }
            }
            ImGui.endTabBar();
        }
        ImGui.end();

        ImGui.endFrame();
        ImGui.render();
        glImpl.renderDrawData(ImGui.getDrawData());
    }

    private void drawSceneNodesInfo(SceneManager sceneManager, BaseScene currentScene, Renderer renderer) {
        ImGui.pushItemWidth(-1);
        // List of all nodes
        if (ImGui.beginListBox("Nodes", -1, -1)) {
            for (SceneNode node : currentScene.getNodes()) {
                if (node.isChild())
                    continue;
                drawSceneNode(node, currentScene, 0);
            }

            // Draw node's context menu
//            if (ImGui.beginPopupContextWindow()) {
//                // Delete selected node button
//                if (ImGui.menuItem("Delete") && selectedNode != null) {
//                    currentScene.removeNode(selectedNode);
//                    selectedNode = null;
//                }
//
//                if (ImGui.menuItem("Add Square")) {
//                    selectedNode = new SquareSprite();
//                    currentScene.addNode(selectedNode);
//                }
//
//                if (ImGui.menuItem("Add Camera")) {
//                    selectedNode = new CameraNode();
//                    currentScene.addNode(selectedNode);
//                }
//
//                ImGui.endPopup();
//            }

            ImGui.endListBox();
        }
    }

    private void drawPostprocessingInfo(Renderer renderer) {
        // Render list of postprocessing effects
        if (ImGui.beginListBox("Postprocessing Effects", -1, -1)) {
            for (PostprocessEffect effect : renderer.getPostprocessing().getEffects()) {
                drawPostprocessEffect(effect);
            }
            ImGui.endListBox();
        }
    }

    public void drawSceneInfo(SceneManager sceneManager, BaseScene scene, Renderer renderer) {
        // List with all available scenes
        if (ImGui.beginListBox("Scenes")) {
            for (Resource name : sceneManager.getSceneNames()) {
                if (ImGui.selectable(name.toString(), name.equals(sceneManager.getCurrentSceneName()))) {
                    // The scene was clicked, switch to it
                    sceneManager.switchScene(name);
                }
            }
            ImGui.endListBox();
        }
        ImGui.separator();

        ImGui.text("Nodes: " + scene.getNodes().size());
        ImGui.separator();

        ImGui.text("Geometry objects: undefined");
        ImGui.text("Gravity: undefined");
        ImGui.separator();

        drawSceneInterface(scene);
    }

    public void drawPostprocessEffect(PostprocessEffect effect) {
        if (ImGui.collapsingHeader(effect.getClass().getSimpleName() + "##" + effect.hashCode())) {
            Boolean isEnabled = (Boolean)drawElement(true, "Is Enabled", effect.isEnabled());
            if (isEnabled != null)
                effect.setEnabled(isEnabled);

            for (Map.Entry<String, Object> property: effect.getProperties()) {
                Object value = drawAsType(property.getValue().getClass(), Utils.prettify(property.getKey()), property.getValue(), true);
                if (value != null)
                    effect.setProperty(property.getKey(), value);
            }
            ImGui.separator();
        }
    }

    public String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public void drawSceneNode(SceneNode node, BaseScene scene, int depth) {
        if (node == null)
            return;

        // TODO: remove this
        if (node.getNametag().startsWith("Chunk"))
            return;

        if (ImGui.collapsingHeader(node.getNametag())) {
            // Draw all node's children
            ImGui.indent((15 * depth) + 15);
            for (SceneNode child : node.getChildrenNodes()) {
                drawSceneNode(child, scene, depth + 1);
            }

            ImGui.unindent();
            ImGui.indent((15 * depth) + 10);
            ImGui.separator();
            drawComponentsInterface(node, scene);
            ImGui.separator();
            ImGui.unindent();
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
        ImGui.text("LAYER: " + node.getLayer());
        ImGui.text("PARENT: " + (node.getParent() == null ? null : node.getParent().getUUID()));

        // Draw properties for the node itself
        for (Field field : node.getClass().getFields()) {
            drawField(field, node);
        }

        List<String> drawnFields = new ArrayList<>();
        List<String> components = new ArrayList<>();

        for (Component component : node.getComponents()) {
            String name = component.getName();
            int i = 1;

            while (components.contains(name)) {
                name = component.getName() + " (" + i++ + ")";
            }

            components.add(name);
            if (ImGui.collapsingHeader(name + "##" + node.getUUID())) {
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
            boolean canEdit = !field.isAnnotationPresent(ComponentFinalField.class);
            Object result = drawAsType(clazz, name, value, canEdit);

            if (result != null)
                field.set(parent, result);

        } catch (IllegalAccessException ignored) {
        }
    }

    private Object drawAsType(Class<?> clazz, String name, Object value, boolean canEdit) {
        name += "##" + clazz.hashCode();
        Object result = null;
        if (clazz.equals(Vector3f.class)) result = drawElement(canEdit, name, (Vector3f) value);
        if (clazz.equals(Vector3i.class)) result = drawElement(canEdit, name, (Vector3i) value);
        if (clazz.equals(Vector2f.class)) result = drawElement(canEdit, name, (Vector2f) value);
        if (clazz.equals(Vector2i.class)) result = drawElement(canEdit, name, (Vector2i) value);
        if (clazz.equals(Viewport.ViewType.class)) result = drawElement(name, (Viewport.ViewType) value);
        if (clazz.equals(IMesh.class)) result = drawElement(name, (IMesh) value);
        if (clazz.equals(String.class)) result = drawElement(canEdit, name, (String) value);
        if (clazz.equals(Color.class)) result = drawElement(name, (Color) value);
        if (value instanceof Float) result = drawElement(canEdit, name, (Float) value);
        if (value instanceof Integer) result = drawElement(canEdit, name, (Integer) value);
        if (value instanceof Boolean) result = drawElement(canEdit, name, (Boolean) value);
        if (value instanceof Double) result = drawElement(canEdit, name, (Double) value);
        if (clazz.isEnum()) result = drawEnum((Enum<?>) value);

        return result;
    }

    private Object drawEnum(Enum<?> e) {
        Object result = e;
        if (ImGui.beginCombo(e.getDeclaringClass().getSimpleName(), e.name())) {
            for (Field field : e.getDeclaringClass().getFields()) {
                boolean selected = field.getName().equals(e.name());
                if (ImGui.selectable(field.getName(), selected)) {
                    result = Enum.valueOf(e.getDeclaringClass(), field.getName());
                    break;
                }
            }
            ImGui.endCombo();
        }

        return result;
    }

    private Object drawElement(String name, Color v) {
        if (v == null)
            return Color.WHITE;

        float[] color = {v.red() / 255.0f, v.green() / 255.0f, v.blue() / 255.0f, v.alpha() / 255.0f};
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

    private Object drawElement(String name, Viewport.ViewType v) {
        if (v == null)
            return Viewport.ViewType.PERSPECTIVE;

        if (ImGui.button("Orthographic"))
            return Viewport.ViewType.ORTHOGRAPHIC;
        ImGui.sameLine();
        if (ImGui.button("Perspective"))
            return Viewport.ViewType.PERSPECTIVE;
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
