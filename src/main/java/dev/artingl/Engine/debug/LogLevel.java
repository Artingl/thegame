package dev.artingl.Engine.debug;

public enum LogLevel {

    INFO("INFO"),
    WARNING("WARN"),
    ERROR("ERROR"),

    UNIMPLEMENTED("UNIMPLEMENTED");

    public final String name;

    LogLevel(String name)
    {
        this.name = name;
    }

}
