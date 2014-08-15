package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Indexes {
    Composite[] value();
}
