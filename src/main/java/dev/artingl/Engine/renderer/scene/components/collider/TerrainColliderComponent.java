package dev.artingl.Engine.renderer.scene.components.collider;

import org.joml.Vector2f;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DVector3;
import org.ode4j.ode.DGeom;
import org.ode4j.ode.DHeightfield;
import org.ode4j.ode.DHeightfieldData;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxHeightfield;

import static org.ode4j.ode.DRotation.dRFromAxisAndAngle;

public class TerrainColliderComponent extends BaseColliderComponent {

    private final int width;
    private final int depth;
    private final int widthStep;
    private final int depthStep;
    private final boolean wrap;
    private final ITerrainHeightHandler handler;

    private DxHeightfield terrainGeometry;
    private DHeightfieldData heightMap;

    public TerrainColliderComponent(int width, int depth, boolean wrap, ITerrainHeightHandler handler) {
        this.width = width; this.widthStep = width;
        this.depth = depth; this.depthStep = depth;
        this.wrap = wrap;
        this.handler = handler;
    }

    public float getWidth() {
        return width;
    }

    public float getDepth() {
        return depth;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.heightMap != null) {
            this.heightMap.destroy();
            this.heightMap = null;
        }
    }

    @Override
    public DGeom getGeometry() {
        return terrainGeometry;
    }

    @Override
    protected void buildCollider() {
        DHeightfield.DHeightfieldGetHeight handler = (pUserData, x, z) ->
                this.handler.run(
                        // Some scary formula...
                        ((int)(-width/2.f)) + x * (width / (widthStep-1f)),
                        -(((int)(-depth/2.f)) + z * (depth / (depthStep-1f))));

        if (this.heightMap != null)
            this.heightMap.destroy();

        this.heightMap = OdeHelper.createHeightfieldData();
        this.heightMap.buildCallback(null, handler, width, depth, widthStep, depthStep, 1.0f, 0.0f, 2.0f, wrap);
//        this.heightMap.setBounds(0, 10);

        this.terrainGeometry = (DxHeightfield) OdeHelper.createHeightfield(null, heightMap, true);

        DMatrix3 R = new DMatrix3();
        R.setIdentity();
        dRFromAxisAndAngle(R, 1, 0, 0, Math.toRadians(90));
        this.terrainGeometry.setRotation(R);

        this.isColliderBuilt = true;
    }

    @Override
    public String getName() {
        return "Terrain Collider";
    }

    public interface ITerrainHeightHandler {
        float run(float x, float z);
    }

}
