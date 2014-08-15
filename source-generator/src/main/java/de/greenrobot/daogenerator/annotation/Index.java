package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface Index {
    int since() default 0;
}
