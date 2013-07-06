package de.greenrobot.daogenerator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface ToMany {
  
  /**
   * The name of the property in the class of the 'many' side of the relation.
   */
  String relation();
  
  /**
   * The order to use when retrieving the list of the entities of the 'many' side of the relation.
   */
  String orderedBy();
  
}
