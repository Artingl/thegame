package dev.artingl.Engine.renderer.mesh;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.misc.Color;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.w3c.dom.Attr;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class VerticesBuffer {

    // -------------

    public static VerticesBuffer wrap(Matrix4f v) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.MAT4F);
        buffer.addAttribute(v);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector3f value) {
            VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC3F);
            buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Color value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC4F);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector2f value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC2F);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector3i value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC3I);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector2i value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC2I);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector4i value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC4I);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(Vector4f value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.VEC4F);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(float value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.FLOAT);
        buffer.addAttribute(value);
        return buffer;
    }

    public static VerticesBuffer wrap(int value) {
        VerticesBuffer buffer = new VerticesBuffer(Attribute.INT);
        buffer.addAttribute(value);
        return buffer;
    }

    // -------------

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

    public VerticesBuffer addAttribute(Matrix4f value) {
        synchronized (this.fields) {
            synchronized (this.fieldsDescription) {
                this.fieldsDescription.add(Attribute.MAT4F);
                this.fields.add(value);
                this.size += Attribute.MAT4F.size;
            }
        }

        return this;
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
     * @param vao The VAO buffer
     * @param vbo The VBO buffer
     * @param ebo The EBO buffer
     *
     * @return Either amount of vertices if no indices provided or amount if indices to be rendered
     */
    public int bake(int vao, int vbo, int ebo) {
        return bake(vao, vbo, ebo, 0);
    }

    /**
     * Writes vertices to the GL buffer and sets up attributes with an index offset for the buffer.
     *
     * @param vao The VAO buffer
     * @param vbo The VBO buffer
     * @param ebo The EBO buffer
     * @param indexOffset The offset for the attributes
     *
     * @return Either amount of vertices if no indices provided or amount if indices to be rendered
     */
    public int bake(int vao, int vbo, int ebo, int indexOffset) {
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
                    case MAT4F -> {
                        buffer.putFloat(((Matrix4f) value).m00());
                        buffer.putFloat(((Matrix4f) value).m01());
                        buffer.putFloat(((Matrix4f) value).m02());
                        buffer.putFloat(((Matrix4f) value).m03());
                        buffer.putFloat(((Matrix4f) value).m10());
                        buffer.putFloat(((Matrix4f) value).m11());
                        buffer.putFloat(((Matrix4f) value).m12());
                        buffer.putFloat(((Matrix4f) value).m13());
                        buffer.putFloat(((Matrix4f) value).m20());
                        buffer.putFloat(((Matrix4f) value).m21());
                        buffer.putFloat(((Matrix4f) value).m22());
                        buffer.putFloat(((Matrix4f) value).m23());
                        buffer.putFloat(((Matrix4f) value).m30());
                        buffer.putFloat(((Matrix4f) value).m31());
                        buffer.putFloat(((Matrix4f) value).m32());
                        buffer.putFloat(((Matrix4f) value).m33());
                    }
                }
            }
            buffer.flip();

            if (vao != -1)
                glBindVertexArray(vao);

            // Send data to the GPU
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

            // Send indices to the GPU if we have them
            if (hasIndices() && ebo != -1) {
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
            int ptr = 0, index = indexOffset;
            for (Attribute attribute : this.attributes) {
                if (attribute.isMatrix) {
                    int i0 = index++, i1 = index++, i2 = index++, i3 = index++;
                    glVertexAttribPointer(i0, 4, GL_FLOAT, false, stride, ptr + 0);
                    glVertexAttribPointer(i1, 4, GL_FLOAT, false, stride, ptr + 16);
                    glVertexAttribPointer(i2, 4, GL_FLOAT, false, stride, ptr + 32);
                    glVertexAttribPointer(i3, 4, GL_FLOAT, false, stride, ptr + 48);
                    glEnableVertexAttribArray(i0);
                    glEnableVertexAttribArray(i1);
                    glEnableVertexAttribArray(i2);
                    glEnableVertexAttribArray(i3);
                    glVertexAttribDivisor(i0, 1);
                    glVertexAttribDivisor(i1, 1);
                    glVertexAttribDivisor(i2, 1);
                    glVertexAttribDivisor(i3, 1);
                }
                else {
                    if (attribute.type == GL_FLOAT) {
                        glVertexAttribPointer(index, attribute.size / 4, GL_FLOAT, false, stride, ptr);
                    } else {
                        glVertexAttribIPointer(index, attribute.size / 4, attribute.type, stride, ptr);
                    }
                    glEnableVertexAttribArray(index++);
                }
                ptr += attribute.size;
            }

            buffer.clear();
            if (hasIndices() && ebo != -1)
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
        FLOAT(4, GL_FLOAT, false),
        INT(4, GL_INT, false),
        VEC3F(12, GL_FLOAT, false),
        VEC2F(8, GL_FLOAT, false),
        VEC2I(8, GL_INT, false),
        VEC3I(12, GL_INT, false),
        VEC4F(16, GL_FLOAT, false),
        VEC4I(16, GL_INT, false),
        MAT4F(64, GL_FLOAT, true);

        public final int size;
        public final int type;
        public final boolean isMatrix;

        Attribute(int size, int type, boolean isMatrix) {
            this.size = size;
            this.type = type;
            this.isMatrix = isMatrix;
        }
    }
}
