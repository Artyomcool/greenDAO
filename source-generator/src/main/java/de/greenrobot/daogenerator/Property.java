/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * This file is part of greenDAO Generator.
 * 
 * greenDAO Generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * greenDAO Generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with greenDAO Generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.greenrobot.daogenerator;

/** Model class for an entity's property: a Java property mapped to a data base column. */
public class Property {

    public static class PropertyBuilder {
        private final Property property;

        public PropertyBuilder(Schema schema, Entity entity, PropertyType propertyType, String javaType, String propertyName) {
            property = new Property(schema, entity, propertyType, javaType, propertyName);
        }

        public PropertyBuilder columnName(String columnName) {
            property.columnName = columnName;
            return this;
        }

        public PropertyBuilder columnType(String columnType) {
            property.columnType = columnType;
            return this;
        }

        public PropertyBuilder primaryKey() {
            property.primaryKey = true;
            return this;
        }

        public PropertyBuilder primaryKeyAsc() {
            property.primaryKey = true;
            property.pkAsc = true;
            return this;
        }

        public PropertyBuilder primaryKeyDesc() {
            property.primaryKey = true;
            property.pkDesc = true;
            return this;
        }

        public PropertyBuilder autoincrement() {
            if (!property.primaryKey || property.propertyType != PropertyType.Long) {
                throw new RuntimeException(
                        "AUTOINCREMENT is only available to primary key properties of type long/Long");
            }
            property.pkAutoincrement = true;
            return this;
        }

        public PropertyBuilder unique() {
            property.unique = true;
            return this;
        }

        public PropertyBuilder notNull() {
            property.notNull = true;
            return this;
        }

        public PropertyBuilder index() {
            Index index = new Index();
            index.addProperty(property);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder index(int since) {
            Index index = new Index();
            index.setSince(since);
            index.addProperty(property);
            property.entity.addIndex(index);
            property.schema.checkVersion(since);
            return this;
        }

        public PropertyBuilder indexAsc(String indexNameOrNull, boolean isUnique) {
            Index index = new Index();
            index.addPropertyAsc(property);
            if (isUnique) {
                index.makeUnique();
            }
            index.setName(indexNameOrNull);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder indexDesc(String indexNameOrNull, boolean isUnique) {
            Index index = new Index();
            index.addPropertyDesc(property);
            if (isUnique) {
                index.makeUnique();
            }
            index.setName(indexNameOrNull);
            property.entity.addIndex(index);
            return this;
        }

        public PropertyBuilder since(int version) {
            since(version, null);
            return this;
        }

        public PropertyBuilder since(int version, String _default) {
            property.since = version;
            property._default = "".equals(_default) ? null : _default;
            property.schema.checkVersion(version);
            return this;
        }

        public Property getProperty() {
            return property;
        }

        public void serializer(String serializerClass) {
            property.serializerClass = serializerClass;
        }
    }

    private final Schema schema;
    private final Entity entity;
    private PropertyType propertyType;
    private String javaType;
    private final String propertyName;

    private String columnName;
    private String columnType;
    private String serializerClass;

    private boolean primaryKey;
    private boolean pkAsc;
    private boolean pkDesc;
    private boolean pkAutoincrement;

    private boolean unique;
    private boolean notNull;

    private int since;
    private String _default;

    /** Initialized in 2nd pass */
    private String constraints;

    private int ordinal;

    public Property(Schema schema, Entity entity, PropertyType propertyType, String javaType, String propertyName) {
        this.schema = schema;
        this.entity = entity;
        this.propertyName = propertyName;
        this.javaType = javaType;
        this.propertyType = propertyType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isAutoincrement() {
        return pkAutoincrement;
    }

    public String getConstraints() {
        return constraints;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public String getJavaType() {
        return javaType;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Entity getEntity() {
        return entity;
    }

    public int getSince() {
        return since;
    }

    public String getDefault() {
        return _default;
    }

    public String getSerializerClass() {
        return serializerClass;
    }

    void init2ndPass() {
        initConstraint();
        if (columnType == null) {
            columnType = schema.mapToDbType(propertyType);
        }
        if (columnName == null) {
            columnName = DaoUtil.dbName(propertyName);
        }
        if (javaType == null) {
            if (notNull) {
                javaType = schema.mapToJavaTypeNotNull(propertyType);
            } else {
                javaType = schema.mapToJavaTypeNullable(propertyType);
            }
        }
    }

    private void initConstraint() {
        StringBuilder constraintBuilder = new StringBuilder();
        if (primaryKey) {
            constraintBuilder.append("PRIMARY KEY");
            if (pkAsc) {
                constraintBuilder.append(" ASC");
            }
            if (pkDesc) {
                constraintBuilder.append(" DESC");
            }
            if (pkAutoincrement) {
                constraintBuilder.append(" AUTOINCREMENT");
            }
        }
        // Always have String PKs NOT NULL because SQLite is pretty strange in this respect:
        // One could insert multiple rows with NULL PKs
        if (notNull || (primaryKey && propertyType == PropertyType.String)) {
            constraintBuilder.append(" NOT NULL");
        }
        if (unique) {
            constraintBuilder.append(" UNIQUE");
        }
        String newContraints = constraintBuilder.toString().trim();
        if (constraintBuilder.length() > 0) {
            constraints = newContraints;
        }
    }

    void init3ndPass() {
        // Nothing to do so far
    }

    @Override
    public String toString() {
        return "Property " + propertyName + " of " + entity.getClassName();
    }

}
