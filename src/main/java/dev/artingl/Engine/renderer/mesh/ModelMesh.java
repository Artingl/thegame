package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.models.BaseModel;
import dev.artingl.Engine.renderer.RenderContext;

public class ModelMesh extends BaseMesh {

    private final BaseModel model;
    private MeshQuality currentQuality;

    public ModelMesh(BaseModel model) {
        this.model = model;
        this.setMode(model.getRenderMode());
    }

    @Override
    public void bake() {
        if (currentQuality == null)
            this.setQuality(MeshQuality.HIGH);
        super.bake();
    }

    @Override
    public void setQuality(MeshQuality quality) {
        if (currentQuality != quality) {
            this.setVertices(model.load(quality));
        }

        this.currentQuality = quality;
    }

    @Override
    public void renderInstanced(RenderContext context, int mode) {
        if (getShaderProgram() != null && model.getTexture() != null)
            getShaderProgram().setMainTexture(model.getTexture());
        super.renderInstanced(context, mode);
    }

    @Override
    public void render(RenderContext context, int mode) {
        if (getShaderProgram() != null && model.getTexture() != null)
            getShaderProgram().setMainTexture(model.getTexture());
        super.render(context, mode);
    }

    @Override
    public MeshQuality getQuality() {
        return this.currentQuality;
    }
}
