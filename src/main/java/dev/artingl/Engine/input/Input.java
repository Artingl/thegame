package dev.artingl.Engine.input;

import dev.artingl.Engine.Display;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.timer.ITick;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Input implements ITick {

    private final State[] kbKeyStates;
    private final State[] msKeyStates;
    private int kbModKeysState;

    private final Collection<IInput> subscribers;
    private final Collection<InputEvent> eventsStack;

    private Vector2i mouseWheel;
    private Vector2f mousePosition;

    public Input() {
        this.kbKeyStates = new State[1024];
        this.msKeyStates = new State[64];
        this.kbModKeysState = 0;
        this.mouseWheel = new Vector2i(0, 0);
        this.mousePosition = new Vector2f(0, 0);

        this.subscribers = new ConcurrentLinkedDeque<>();
        this.eventsStack = new ConcurrentLinkedDeque<>();

        Arrays.fill(this.kbKeyStates, State.KEY_RELEASED);
        Arrays.fill(this.msKeyStates, State.KEY_RELEASED);
    }

    public void init() {
        Engine.getInstance().getTimer().subscribe(this);}

    public void cleanup() {
        Engine.getInstance().getTimer().unsubscribe(this);
    }

    /**
     * Subscribe for input events
     */
    public void subscribe(IInput input) {
        this.subscribers.add(input);
    }

    /**
     * Unsubscribe from input events
     */
    public void unsubscribe(IInput input) {
        this.subscribers.remove(input);
    }

    /**
     * Get mod keys state
     */
    public int getModKeys() {
        return kbModKeysState;
    }

    /**
     * Get keyboard key state
     *
     * @param key Key ID
     */
    public State getKeyboardState(int key) {
        if (key < 0 || key > this.kbKeyStates.length)
            return State.KEY_RELEASED;

        return this.kbKeyStates[key];
    }

    /**
     * Get mouse wheel state
     */
    public Vector2i getMouseWheel() {
        return new Vector2i(mouseWheel);
    }

    /**
     * Get mouse position
     */
    public Vector2f getMousePosition() {
        return new Vector2f(mousePosition);
    }

    /**
     * Get mouse key state
     *
     * @param key Key ID
     */
    public State getMouseState(int key) {
        if (key < 0 || key > this.msKeyStates.length)
            return State.KEY_RELEASED;

        return this.msKeyStates[key];
    }

    /**
     * Update state value for the mouse key
     *
     * @param key   Key to be updated
     * @param state The state to be set
     */
    public void setMouseStateArray(int key, int state) {
        this.msKeyStates[key] = State.from(state);
        this.eventsStack.add(new InputEvent(InputEventType.MOUSE_BTN, State.from(state), key));
    }

    /**
     * Update state value for the keyboard key
     *
     * @param key   Key to be updated
     * @param state The state to be set
     */
    public void setKeyboardStateArray(int key, int state) {
        this.kbKeyStates[key] = State.from(state);
        this.eventsStack.add(new InputEvent(InputEventType.KEYBOARD, State.from(state), key));
    }

    /**
     * Update state value for the keyboard mod key
     *
     * @param state Current mod state
     */
    public void setKeyboardModState(int state) {
        this.kbModKeysState = state;
    }

    /**
     * Changes current cursor capture state, or leaves the same.
     *
     * @param state State to be set
     *
     * @return If cursor was captured, then true. On any error false is returned
     * */
    public boolean captureCursor(boolean state) {
        return Engine.getInstance().getDisplay().captureCursor(state);
    }

    /**
     * Returns cursor's delta
     *
     * @return Vec2f(0, 0) if cursor isn't captured, or the last calculated delta
     * */
    public Vector2f getCursorDelta() {
        Display display = Engine.getInstance().getDisplay();
        return new Vector2f(display.getMouseDelta().x, display.getMouseDelta().y);
    }

    /**
     * Updates mouse wheel values
     *
     * @param x Wheel X co-ordinate
     * @param y Wheel Y co-ordinate
     */
    public void setMouseWheel(int x, int y) {
        this.mouseWheel = new Vector2i(x, y);
        this.eventsStack.add(new InputEvent(InputEventType.MOUSE_WHEEL, x, y));
    }

    /**
     * Updates mouse position values
     *
     * @param x Mouse X co-ordinate
     * @param y Mouse Y co-ordinate
     */
    public void setMousePosition(float x, float y) {
        this.mousePosition = new Vector2f(x, y);
        this.eventsStack.add(new InputEvent(InputEventType.MOUSE_MOVE, x, y));
    }

    @Override
    public void tick(Timer timer) {
        // Make all event calls that were added to the stack by display callbacks
        synchronized (this.eventsStack) {
            for (InputEvent event: this.eventsStack) {
                for (IInput input : subscribers) {
                    switch (event.type) {
                        case KEYBOARD ->
                                input.keyboardEvent(this, (State) event.values[0], (Integer) event.values[1]);
                        case MOUSE_BTN ->
                                input.mouseButtonEvent(this, (State) event.values[0], (Integer) event.values[1]);
                        case MOUSE_WHEEL ->
                                input.mouseWheelEvent(this, (Integer) event.values[0], (Integer) event.values[1]);
                        case MOUSE_MOVE ->
                                input.mouseMoveEvent(this, (Float) event.values[0], (Float) event.values[1]);
                    }
                }
            }

            this.eventsStack.clear();
        }
    }


    private record InputEvent(InputEventType type, Object... values) {
    }

    private enum InputEventType {
        MOUSE_BTN, KEYBOARD, MOUSE_WHEEL, MOUSE_MOVE
    }

    public enum State {
        KEY_PRESSED(1),
        KEY_RELEASED(0),
        KEY_HELD(2),
        ;

        public final int state;

        State(int i) {
            this.state = i;
        }

        public static State from(int i) {
            if (i == 0) return KEY_RELEASED;
            if (i == 1) return KEY_PRESSED;
            return KEY_HELD;
        }

        public boolean isHeld() {
            return this == KEY_HELD || this == KEY_PRESSED;
        }

        public boolean isPressed() {
            return this == KEY_PRESSED;
        }

    }
}
