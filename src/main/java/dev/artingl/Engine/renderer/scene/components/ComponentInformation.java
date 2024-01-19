package dev.artingl.Engine.renderer.scene.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentInformation {
    String desc() default "No description provided.";

}
