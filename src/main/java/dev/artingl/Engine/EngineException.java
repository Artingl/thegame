package dev.artingl.Engine;

public class EngineException extends RuntimeException {

    public EngineException(String s) {
        super(s);
    }

    public EngineException(Exception e) {
        super(e);
    }

}
