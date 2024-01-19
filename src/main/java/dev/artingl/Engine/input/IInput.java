package dev.artingl.Engine.input;

public interface IInput {

    void keyboardEvent(Input input, Input.State state, int key);
    void mouseButtonEvent(Input input, Input.State state, int key);
    void mouseWheelEvent(Input input, int wheelX, int wheelY);
    void mouseMoveEvent(Input input, float x, float y);

}
