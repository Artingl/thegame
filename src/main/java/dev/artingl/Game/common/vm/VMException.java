package dev.artingl.Game.common.vm;

public class VMException extends RuntimeException {

    public VMException(String s) {
        super(s);
    }

    public VMException(Exception e) {
        super(e);
    }


}
