package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.misc.Color;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;

public class VerticesBuffer {

    public static final VerticesBuffer EMPTY = new VerticesBuffer(Attribute.INT);

    private final List<Attribute> fieldsDescription;
    private final List<Object> fields;
    private final List<Integer> indices;
    private final Attribute[] attributes;
    private int size;

    public VerticesBuffer(List<Attribute> fieldsDescription, List<Object> fields, List<Integer> indices, Attribute[] attributes, int size) {
        this.fieldsDescription = fieldsDescription;
        this.fields = fields;
        this.indices = indices;
        this.attributes = attributes;
        this.size = size;
    }

    public VerticesBuffer(Attribute... attributes) {
        this.fieldsDescription = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "VerticesBuffer{indices=" + indices.size() + ", fields=" + fields.size() + ", attributes=" + this.attributes.length + "}";
    }

    /**
     * Get attributes that are used in this buffer
     */
    public final Attribute[] getAttributes() {
        return attributes;
    }

    /**
     * Get list with the information about each field in the buffer
     */
    public final List<Attribute> getFieldsDescription() {
        return fieldsDescription;
    }

    /**
     * Get list of the fields
     */
    public final List<Object> getFields() {
        return fields;
    }

    public VerticesBuffer addAttribute(Vector3f value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC3F);
                this.fields.add(value);
                this.size += Attribute.VEC3F.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Color value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC4F);
                this.fields.add(value.asVector4f());
                this.size += Attribute.VEC4F.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Vector2f value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC2F);
                this.fields.add(value);
                this.size += Attribute.VEC2F.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Vector3i value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC3I);
                this.fields.add(value);
                this.size += Attribute.VEC3I.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Vector2i value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC2I);
                this.fields.add(value);
                this.size += Attribute.VEC2I.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Vector4i value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC4I);
                this.fields.add(value);
                this.size += Attribute.VEC4I.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Vector4f value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC4F);
                this.fields.add(value);
                this.size += Attribute.VEC4F.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(float value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.FLOAT);
                this.fields.add(value);
                this.size += Attribute.FLOAT.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(int value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.INT);
                this.fields.add(value);
                this.size += Attribute.INT.size;
            }
        }

        return this;
    }

    public VerticesBuffer addAttribute(Object value, Attribute type) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(type);
                this.fields.add(value);
                this.size += type.size;
            }
        }

        return this;
    }

    public VerticesBuffer addColor3f(Color value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.VEC3F);
                this.fields.add(value.asVector3f());
                this.size += Attribute.VEC3F.size;
            }
        }

        return this;
    }

    /**
     * Make float array of vertices in this buffer
     *
     * @return The constructed float array
     */
    public float[] makeFloatArray() {
        float[] buffer = new float[getBytesSize() / 4];
        int idx = 0;

        for (int i = 0; i < this.fields.size(); i++) {
            Attribute attribute = this.fieldsDescription.get(i);
            Object value = this.fields.get(i);

            switch (attribute) {
                case INT -> buffer[idx++] = (Integer) value;
                case FLOAT -> buffer[idx++] = (Float) value;
                case VEC2F -> {
                    buffer[idx++] = ((Vector2f) value).x;
                    buffer[idx++] = ((Vector2f) value).y;
                }
                case VEC3F -> {
                    buffer[idx++] = ((Vector3f) value).x;
                    buffer[idx++] = ((Vector3f) value).y;
                    buffer[idx++] = ((Vector3f) value).z;
                }
                case VEC4F -> {
                    buffer[idx++] = ((Vector4f) value).x;
                    buffer[idx++] = ((Vector4f) value).y;
                    buffer[idx++] = ((Vector4f) value).z;
                    buffer[idx++] = ((Vector4f) value).w;
                }
                case VEC2I -> {
                    buffer[idx++] = ((Vector2i) value).x;
                    buffer[idx++] = ((Vector2i) value).y;
                }
                case VEC3I -> {
                    buffer[idx++] = ((Vector3i) value).x;
                    buffer[idx++] = ((Vector3i) value).y;
                    buffer[idx++] = ((Vector3i) value).z;
                }
                case VEC4I -> {
                    buffer[idx++] = ((Vector4i) value).x;
                    buffer[idx++] = ((Vector4i) value).y;
                    buffer[idx++] = ((Vector4i) value).z;
                    buffer[idx++] = ((Vector4i) value).w;
                }
            }
        }

        return buffer;
    }

    /**
     * Add indices into the buffer
     *
     * @param indices Indices to be added
     */
    public void addIndices(int... indices) {
        for (int i : indices)
            this.indices.add(i);
    }

    /**
     * Writes vertices to the GL buffer and sets up attributes for the buffer.
     *
     * @return Either amount of vertices if no indices provided or amount if indices to be rendered
     */
    public int bake(int vao, int vbo, int ebo) {
        return synchronizedFields(() -> {
            // Put all fields to the buffer
            ByteBuffer buffer = BufferUtils.createByteBuffer(getBytesSize());
            for (int i = 0; i < this.fields.size(); i++) {
                Attribute attribute = this.fieldsDescription.get(i);
                Object value = this.fields.get(i);

                switch (attribute) {
                    case INT -> buffer.putInt((Integer) value);
                    case FLOAT -> buffer.putFloat((Float) value);
                    case VEC2F -> {
                        buffer.putFloat(((Vector2f) value).x);
                        buffer.putFloat(((Vector2f) value).y);
                    }
                    case VEC3F -> {
                        buffer.putFloat(((Vector3f) value).x);
                        buffer.putFloat(((Vector3f) value).y);
                        buffer.putFloat(((Vector3f) value).z);
                    }
                    case VEC4F -> {
                        buffer.putFloat(((Vector4f) value).x);
                        buffer.putFloat(((Vector4f) value).y);
                        buffer.putFloat(((Vector4f) value).z);
                        buffer.putFloat(((Vector4f) value).w);
                    }
                    case VEC2I -> {
                        buffer.putInt(((Vector2i) value).x);
                        buffer.putInt(((Vector2i) value).y);
                    }
                    case VEC3I -> {
                        buffer.putInt(((Vector3i) value).x);
                        buffer.putInt(((Vector3i) value).y);
                        buffer.putInt(((Vector3i) value).z);
                    }
                    case VEC4I -> {
                        buffer.putInt(((Vector4i) value).x);
                        buffer.putInt(((Vector4i) value).y);
                        buffer.putInt(((Vector4i) value).z);
                        buffer.putInt(((Vector4i) value).w);
                    }
                }
            }
            buffer.flip();

            glBindVertexArray(vao);

            // Send data to the GPU
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

            // Send indices to the GPU if we have them
            if (hasIndices()) {
                int[] indices = new int[this.indices.size()];
                for (int i = 0; i < indices.length; i++)
                    indices[i] = this.indices.get(i);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
            }

            // Calculate the stride
            int stride = 0;
            for (Attribute attribute : this.attributes)
                stride += attribute.size;

            // Tell OpenGL types of our attributes
            int ptr = 0, index = 0;
            for (Attribute attribute : this.attributes) {
                if (attribute.type == GL_FLOAT) {
                    glVertexAttribPointer(index, attribute.size / 4, GL_FLOAT, false, stride, ptr);
                } else {
                    glVertexAttribIPointer(index, attribute.size / 4, attribute.type, stride, ptr);
                }
                glEnableVertexAttribArray(index++);
                ptr += attribute.size;
            }

            buffer.clear();
            if (hasIndices())
                return this.indices.size();

            return this.fields.size() / this.attributes.length;
        });
    }

    public void cleanup() {
        this.fieldsDescription.clear();
        this.fields.clear();
        this.indices.clear();
        this.size = 0;
    }

    public int getBytesSize() {
        return this.size;
    }

    /**
     * Tells if the buffer has indices.
     */
    public boolean hasIndices() {
        return !this.indices.isEmpty();
    }

    /**
     * Fork (copy) this buffer into a new one.
     */
    public VerticesBuffer fork() {
        return synchronizedFields(() ->
                new VerticesBuffer(new ArrayList<>(fieldsDescription), new ArrayList<>(fields), new ArrayList<>(indices), attributes, size));
    }

    private <T> T synchronizedFields(Callable<T> handler) {
        // TODO: messy code...
        synchronized (fieldsDescription) {
            synchronized (fields) {
                synchronized (indices) {
                    synchronized (attributes) {
                        T result;
                        try {
                            result = handler.call();
                        } catch (Exception e) {
                            throw new EngineException(e);
                        }

                        return result;
                    }
                }
            }
        }
    }

    public enum Attribute {
        FLOAT(4, GL_FLOAT),
        INT(4, GL_INT),
        VEC3F(12, GL_FLOAT),
        VEC2F(8, GL_FLOAT),
        VEC2I(8, GL_INT),
        VEC3I(12, GL_INT),
        VEC4F(16, GL_FLOAT),
        VEC4I(16, GL_INT);

        public final int size;
        public final int type;

        Attribute(int size, int type) {
            this.size = size;
            this.type = type;
        }
    }
}
