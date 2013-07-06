package de.greenrobot.daogenerator;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.AbstractBaseJavaEntity;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.Type;
import de.greenrobot.daogenerator.annotation.Serialized;
import de.greenrobot.daogenerator.annotation.ToOne;
import de.greenrobot.daogenerator.annotation.ToMany;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaGenerator {
  
  private String sourceDirectory;
  private Map<String, PropertyType> javaTypeToPropertyType;
  private Map<Type, Entity> typeToEntity = new HashMap<>();

  public SchemaGenerator(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
    
    javaTypeToPropertyType = new HashMap<>();
    javaTypeToPropertyType.put("boolean", PropertyType.Boolean);
    javaTypeToPropertyType.put("Boolean", PropertyType.Boolean);
    javaTypeToPropertyType.put("byte", PropertyType.Byte);
    javaTypeToPropertyType.put("Byte", PropertyType.Byte);
    javaTypeToPropertyType.put("short", PropertyType.Short);
    javaTypeToPropertyType.put("Short", PropertyType.Short);
    javaTypeToPropertyType.put("int", PropertyType.Int);
    javaTypeToPropertyType.put("Integer", PropertyType.Int);
    javaTypeToPropertyType.put("long", PropertyType.Long);
    javaTypeToPropertyType.put("Long", PropertyType.Long);
    javaTypeToPropertyType.put("float", PropertyType.Float);
    javaTypeToPropertyType.put("Float", PropertyType.Float);
    javaTypeToPropertyType.put("double", PropertyType.Double);
    javaTypeToPropertyType.put("Double", PropertyType.Double);
    javaTypeToPropertyType.put("byte[]", PropertyType.ByteArray);
    javaTypeToPropertyType.put("Byte[]", PropertyType.ByteArray);
    javaTypeToPropertyType.put("java.lang.String", PropertyType.String);
    javaTypeToPropertyType.put("java.util.Date", PropertyType.Date);
  }

  public Schema createSchema(int version, String defaultJavaPackage) {
    Schema schema = new Schema(version, defaultJavaPackage);
    
    // Look for the java files.
    JavaDocBuilder builder = new JavaDocBuilder();
    builder.addSourceTree(new File(sourceDirectory));
    
    // Creates the entities.
    System.out.println("Creates the entities ...\n");
    for (JavaSource javaSource : builder.getSources()) {
      for (JavaClass javaClass : javaSource.getClasses()) {
        Annotation entityAnnotation = getAnnotation(javaClass, de.greenrobot.daogenerator.annotation.Entity.class);
        if (entityAnnotation != null) {
          createEntity(schema, javaClass, entityAnnotation);
        }
      }
    }
    
    // Links the entities 1.
    System.out.println("Links the entities ...\n");
    for (JavaSource javaSource : builder.getSources()) {
      for (JavaClass javaClass : javaSource.getClasses()) {
        Annotation entityAnnotation = getAnnotation(javaClass, de.greenrobot.daogenerator.annotation.Entity.class);
        if (entityAnnotation != null) {
          linkEntity1(schema, javaClass);
        }
      }
    }
    
    // Links the entities 2.
    System.out.println("Links the entities ...\n");
    for (JavaSource javaSource : builder.getSources()) {
      for (JavaClass javaClass : javaSource.getClasses()) {
        Annotation entityAnnotation = getAnnotation(javaClass, de.greenrobot.daogenerator.annotation.Entity.class);
        if (entityAnnotation != null) {
          linkEntity2(schema, javaClass);
        }
      }
    }
    
    return schema;
  }

  private Annotation getAnnotation(AbstractBaseJavaEntity javaEntity, Class annotationClass) {
    for (Annotation annotation : javaEntity.getAnnotations()) {
      if (annotationClass.getName().equals(annotation.getType().getFullyQualifiedName())) {
        return annotation;
      }
    }
    
    return null;
  }
  
  private Property findProperty(Entity entity, String propertyName) {
    for (Property property : entity.getProperties()) {
      if (property.getPropertyName().equals(propertyName)) {
        return property;
      }
    }
    
    return null;
  }
  
  /**
   * Creates the entity in the schema.
   * Skips all the relational aspect.
   */
  private void createEntity(Schema schema, JavaClass javaClass, Annotation entityAnnotation) {
    System.out.println("className: " + javaClass.getFullyQualifiedName());
    Entity entity = schema.addEntity(javaClass.getName());
    typeToEntity.put(javaClass.asType(), entity);

    String tableName = unString((String) entityAnnotation.getNamedParameter("table"));
    entity.setTableName(tableName);

    // Create the entity's id.
    entity.addIdProperty();

    for (JavaField javaField : javaClass.getFields()) {
      System.out.println("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());
      Property.PropertyBuilder propertyBuilder;
      
      PropertyType propertyType = javaTypeToPropertyType.get(javaField.getType().getFullyQualifiedName());
      Annotation toOneAnnotation = getAnnotation(javaField, ToOne.class);
      Annotation serializedAnnotation = getAnnotation(javaField, Serialized.class);
      Annotation notNullAnnotation = getAnnotation(javaField, de.greenrobot.daogenerator.annotation.NotNull.class);
      
      if (propertyType != null) {
        propertyBuilder = entity.addProperty(propertyType, null, javaField.getName());
      } else if (serializedAnnotation != null) {
        propertyBuilder = entity.addProperty(PropertyType.Serialized, javaField.getType().getFullyQualifiedName(), javaField.getName());
      } else if (toOneAnnotation != null) {
        propertyBuilder = entity.addLongProperty(javaField.getName() + "Id");
      } else {
        continue;
      }
      
      if (notNullAnnotation != null) {
        propertyBuilder.notNull();
      }
    }
  }
  
  /**
   * Sets up all the relational aspects of the entity.
   */
  private void linkEntity1(Schema schema, JavaClass javaClass) {
    System.out.println("className: " + javaClass.getFullyQualifiedName());
    
    Entity entity = typeToEntity.get(javaClass.asType());
    
    for (JavaField javaField : javaClass.getFields()) {
      System.out.println("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());
      
      Annotation toOneAnnotation = getAnnotation(javaField, ToOne.class);
      if (toOneAnnotation != null) {
        Entity targetEntity = typeToEntity.get(javaField.getType());
        entity.addToOne(targetEntity, findProperty(entity, javaField.getName() + "Id"), javaField.getName());
      }
    }
  }
  
  /**
   * Sets up all the relational aspects of the entity.
   */
  private void linkEntity2(Schema schema, JavaClass javaClass) {
    System.out.println("className: " + javaClass.getFullyQualifiedName());
    
    Entity entity = typeToEntity.get(javaClass.asType());
    
    for (JavaField javaField : javaClass.getFields()) {
      System.out.println("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());
      
      Annotation toManyAnnotation = getAnnotation(javaField, ToMany.class);
      if (toManyAnnotation != null && javaField.getType().getFullyQualifiedName().equals(List.class.getName())) {
        
        Entity targetEntity = typeToEntity.get(javaField.getType().getActualTypeArguments()[0]);
        System.out.println("targetEntity = " + targetEntity);
        
        System.out.println("toManyAnnotation.getParameterValue() = " + toManyAnnotation.getParameterValue());
        
        String relationName = unString((String) toManyAnnotation. getNamedParameter("relation"));
        System.out.println("relationName = " + relationName);
        
        de.greenrobot.daogenerator.ToMany toMany = entity.addToMany(targetEntity,
                findProperty(targetEntity, relationName + "Id"),
                javaField.getName());
        
        String orderedByStr = unString((String) toManyAnnotation.getNamedParameter("orderedBy"));
        
        toMany.orderAsc(findProperty(targetEntity, orderedByStr));
      }
    }
  }

  private String unString(String str) {
    if (str.length() >= 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
      return str.substring(1, str.length() - 1);
    }
    else {
      return str;
    }
  }
  
}
