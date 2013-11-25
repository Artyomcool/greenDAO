package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Entity {
  String table() default "";
  int since() default 0;
  Class<?> dao() default Object.class;
}
