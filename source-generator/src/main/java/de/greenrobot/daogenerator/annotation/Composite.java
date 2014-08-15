package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface Composite {
    String[] value();
    int since() default 0;
}
