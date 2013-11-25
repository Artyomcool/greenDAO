package de.greenrobot.daogenerator;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.dao.serialization.DefaultSerializer;
import de.greenrobot.daogenerator.annotation.Serialized;
import de.greenrobot.daogenerator.annotation.ToMany;
import de.greenrobot.daogenerator.annotation.ToOne;

public class SchemaGenerator {

  private static class EntityDescriptor {
    final JavaClass javaClass;
    final Annotation entityAnnotation;

    EntityDescriptor(JavaClass javaClass, Annotation entityAnnotation) {
        this.javaClass = javaClass;
        this.entityAnnotation = entityAnnotation;
    }
  }

  private File sourceDirectory;
  private Map<String, PropertyType> javaTypeToPropertyType;
  private Map<Type, Entity> typeToEntity = new HashMap<Type, Entity>();

  public SchemaGenerator(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;

    javaTypeToPropertyType = new HashMap<String, PropertyType>();
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

  public Schema createSchema(String genSrcPackage) {
    Schema schema = new Schema(genSrcPackage);

    // Look for the java files.
    JavaDocBuilder builder = new JavaDocBuilder();
    builder.addSourceTree(sourceDirectory);

    List<EntityDescriptor> descriptors = findEntities(builder);

    debug("Creates the entities ...\n");
    for (EntityDescriptor descriptor : descriptors) {
        createEntity(schema, descriptor.javaClass, descriptor.entityAnnotation);
    }

    debug("Links the entities 1 ...\n");
    for (EntityDescriptor descriptor : descriptors) {
      linkEntity1(schema, descriptor.javaClass);
    }

    debug("Links the entities 2 ...\n");
    for (EntityDescriptor descriptor : descriptors) {
      linkEntity2(schema, descriptor.javaClass);
    }

    return schema;
  }

    private List<EntityDescriptor> findEntities(JavaDocBuilder builder) {
      List<EntityDescriptor> descriptors = new ArrayList<EntityDescriptor>();
      for (JavaSource javaSource : builder.getSources()) {
        for (JavaClass javaClass : javaSource.getClasses()) {
          Annotation entityAnnotation = getAnnotation(javaClass, de.greenrobot.daogenerator.annotation.Entity.class);
          if (entityAnnotation != null) {
            descriptors.add(new EntityDescriptor(javaClass, entityAnnotation));
          }
        }
      }
      return descriptors;
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
    debug("className: " + javaClass.getFullyQualifiedName());

    String since = (String) entityAnnotation.getNamedParameter("since");

    Entity entity = schema.addEntity(javaClass.getName(), since == null ? 0 : Integer.parseInt(since));
    typeToEntity.put(javaClass.asType(), entity);

    String tableName = unString((String) entityAnnotation.getNamedParameter("table"));
    entity.setTableName(tableName);

    for (JavaClass i : javaClass.getImplementedInterfaces()) {
        entity.implementsInterface(i.getFullyQualifiedName());
    }

    String daoImpl = (String) entityAnnotation.getNamedParameter("dao");
    if (daoImpl != null) {
      daoImpl = daoImpl.substring(0, daoImpl.lastIndexOf(".class"));
      if (!Object.class.getName().equals(daoImpl)) {
        entity.setClassNameDaoImpl(daoImpl);
      }
    }

    // Create the entity's id.
    entity.addIdProperty();

    for (JavaField javaField : javaClass.getFields()) {
      debug("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());
      Property.PropertyBuilder propertyBuilder;

      PropertyType propertyType = javaTypeToPropertyType.get(javaField.getType().getFullyQualifiedName());
      Annotation toOneAnnotation = getAnnotation(javaField, ToOne.class);
      Annotation serializedAnnotation = getAnnotation(javaField, Serialized.class);
      Annotation notNullAnnotation = getAnnotation(javaField, de.greenrobot.daogenerator.annotation.NotNull.class);
      Annotation sinceAnnotation = getAnnotation(javaField, de.greenrobot.daogenerator.annotation.Since.class);
      Annotation uniqueAnnotation = getAnnotation(javaField, de.greenrobot.daogenerator.annotation.Unique.class);

      if (propertyType != null) {
        propertyBuilder = entity.addProperty(propertyType, null, javaField.getName());
      } else if (serializedAnnotation != null) {
        propertyBuilder = entity.addSerializedProperty(javaField.getType().getFullyQualifiedName(), javaField.getName());
        String serializer = (String)serializedAnnotation.getNamedParameter("value");
        if (serializer == null) {
            serializer = DefaultSerializer.class.getName() + ".class";
        }
        propertyBuilder.serializer(serializer);
      } else if (toOneAnnotation != null) {
        propertyBuilder = entity.addLongProperty(javaField.getName() + "Id");
      } else {
        continue;
      }

      if (notNullAnnotation != null) {
        propertyBuilder.notNull();
      }
      if (uniqueAnnotation != null) {
        propertyBuilder.unique();
      }
      if (sinceAnnotation != null) {
        String version = (String) sinceAnnotation.getNamedParameter("value");
        propertyBuilder.since(Integer.parseInt(version));
      }
    }
  }

  /**
   * Sets up all the relational aspects of the entity.
   */
  private void linkEntity1(Schema schema, JavaClass javaClass) {
    debug("className: " + javaClass.getFullyQualifiedName());

    Entity entity = typeToEntity.get(javaClass.asType());

    for (JavaField javaField : javaClass.getFields()) {
      debug("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());

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
    debug("className: " + javaClass.getFullyQualifiedName());

    Entity entity = typeToEntity.get(javaClass.asType());

    for (JavaField javaField : javaClass.getFields()) {
      debug("field: " + javaField.getType().getFullyQualifiedName() + " " + javaField.getName());

      Annotation toManyAnnotation = getAnnotation(javaField, ToMany.class);
      if (toManyAnnotation != null && javaField.getType().getFullyQualifiedName().equals(List.class.getName())) {

        Entity targetEntity = typeToEntity.get(javaField.getType().getActualTypeArguments()[0]);
        debug("targetEntity = " + targetEntity);

        debug("toManyAnnotation.getParameterValue() = " + toManyAnnotation.getParameterValue());

        String relationName = unString((String) toManyAnnotation. getNamedParameter("relation"));
        debug("relationName = " + relationName);

        de.greenrobot.daogenerator.ToMany toMany = entity.addToMany(targetEntity,
                findProperty(targetEntity, relationName + "Id"),
                javaField.getName());

        String orderedByStr = unString((String) toManyAnnotation.getNamedParameter("orderedBy"));

        toMany.orderAsc(findProperty(targetEntity, orderedByStr));
      }
    }
  }

  private String unString(String str) {
    if (str == null) {
      return null;
    }
    if (str.length() >= 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
      return str.substring(1, str.length() - 1);
    }
    else {
      return str;
    }
  }

  protected void debug(String text) {
      System.out.println(text);
  }

}
