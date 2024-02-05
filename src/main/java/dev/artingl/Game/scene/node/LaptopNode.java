package dev.artingl.Game.scene.node;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.mesh.ModelMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.nodes.sprites.SpriteNode;
import dev.artingl.Game.Constants;
import dev.artingl.Game.common.vm.Computer;
import dev.artingl.Game.common.vm.components.BlockDevice;
import dev.artingl.Game.common.vm.components.RamDevice;
import dev.artingl.Game.common.vm.components.TerminalDevice;
import dev.artingl.Game.scene.Models;
import li.cil.sedna.buildroot.Buildroot;
import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import java.io.IOException;

public class LaptopNode extends SpriteNode {

    private final Computer computer;

    public LaptopNode() {
        super(new ModelMesh(Models.LAPTOP));
        this.addComponent(new MeshColliderComponent(getMesh()));
        this.addComponent(new RigidBodyComponent());

        try {
            this.computer = new Computer(
                    new RamDevice(32 * Constants.MEGABYTE),
                    new TerminalDevice(80, 40),
                    new BlockDevice(Buildroot.getRootFilesystem())
            );
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public void init() throws EngineException {
        super.init();
        this.computer.init();
        this.computer.turnOn();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.computer.cleanup();
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        Engine engine = getEngine();
        Texture screen = engine.getTextureManager().getTexture(new Resource("thegame", "models/laptop/screen"));
        FrustumIntersection frustum = engine.getRenderer().getViewport().getFrustum();
        Vector3f position = getTransform().position;

        TerminalDevice terminal = computer.getDevice(TerminalDevice.class);

        // Update the laptops screen if it's visible by the player
        if (terminal != null && frustum.testAab(position.x - 2, position.y - 2, position.z - 2,
                position.x + 2, position.y + 2, position.z + 2)) {
            screen.updateTexture(terminal.getFramebufferImage());
        }

    }

    public Computer getComputerInstance() {
        return computer;
    }
}
