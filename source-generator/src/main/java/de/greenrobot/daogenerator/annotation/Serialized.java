package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import de.greenrobot.dao.serialization.DefaultSerializer;
import de.greenrobot.dao.serialization.Serializer;

@Target(ElementType.FIELD)
public @interface Serialized {
  Class<? extends Serializer> value() default DefaultSerializer.class;
}
