package com.ionapi.database.cache;

import com.ionapi.database.annotations.Column;
import com.ionapi.database.annotations.PrimaryKey;
import com.ionapi.database.annotations.Table;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches reflection metadata for entity classes to avoid repeated reflection calls.
 * This provides significant performance improvements for ORM operations.
 *
 * @since 1.2.0
 */
public final class ReflectionCache {

    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    private ReflectionCache() {}

    /**
     * Gets or computes metadata for an entity class.
     *
     * @param entityClass the entity class
     * @return the cached metadata
     */
    public static EntityMetadata get(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, ReflectionCache::computeMetadata);
    }

    /**
     * Clears the cache. Useful for hot-reload scenarios.
     */
    public static void clear() {
        CACHE.clear();
    }

    /**
     * Removes a specific class from the cache.
     *
     * @param entityClass the class to remove
     */
    public static void evict(Class<?> entityClass) {
        CACHE.remove(entityClass);
    }

    private static EntityMetadata computeMetadata(Class<?> entityClass) {
        String tableName = resolveTableName(entityClass);
        Field primaryKeyField = null;
        List<FieldMetadata> fields = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
                java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String columnName = resolveColumnName(field);
            boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean isAutoGenerate = false;

            if (isPrimaryKey) {
                primaryKeyField = field;
                PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
                isAutoGenerate = pk.autoGenerate();
            }

            Column columnAnnotation = field.getAnnotation(Column.class);
            fields.add(new FieldMetadata(field, columnName, isPrimaryKey, isAutoGenerate, columnAnnotation));
        }

        return new EntityMetadata(entityClass, tableName, primaryKeyField, fields);
    }

    private static String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.value().isEmpty()) {
            return table.value();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private static String resolveColumnName(Field field) {
        if (field.isAnnotationPresent(PrimaryKey.class)) {
            PrimaryKey pk = field.getAnnotation(PrimaryKey.class);
            if (!pk.columnName().isEmpty()) return pk.columnName();
        }
        if (field.isAnnotationPresent(Column.class)) {
            Column col = field.getAnnotation(Column.class);
            if (!col.name().isEmpty()) return col.name();
        }
        return field.getName();
    }

    /**
     * Cached metadata for an entity class.
     */
    public static class EntityMetadata {
        private final Class<?> entityClass;
        private final String tableName;
        private final Field primaryKeyField;
        private final List<FieldMetadata> fields;
        private final Map<String, FieldMetadata> fieldsByColumn;

        EntityMetadata(Class<?> entityClass, String tableName, Field primaryKeyField, List<FieldMetadata> fields) {
            this.entityClass = entityClass;
            this.tableName = tableName;
            this.primaryKeyField = primaryKeyField;
            this.fields = Collections.unmodifiableList(fields);
            this.fieldsByColumn = new HashMap<>();
            for (FieldMetadata fm : fields) {
                fieldsByColumn.put(fm.columnName(), fm);
            }
        }

        public Class<?> entityClass() { return entityClass; }
        public String tableName() { return tableName; }
        public Field primaryKeyField() { return primaryKeyField; }
        public List<FieldMetadata> fields() { return fields; }
        public FieldMetadata getByColumn(String columnName) { return fieldsByColumn.get(columnName); }
    }

    /**
     * Cached metadata for a field.
     */
    public static class FieldMetadata {
        private final Field field;
        private final String columnName;
        private final boolean primaryKey;
        private final boolean autoGenerate;
        private final Column columnAnnotation;

        FieldMetadata(Field field, String columnName, boolean primaryKey, boolean autoGenerate, Column columnAnnotation) {
            this.field = field;
            this.columnName = columnName;
            this.primaryKey = primaryKey;
            this.autoGenerate = autoGenerate;
            this.columnAnnotation = columnAnnotation;
        }

        public Field field() { return field; }
        public String columnName() { return columnName; }
        public boolean isPrimaryKey() { return primaryKey; }
        public boolean isAutoGenerate() { return autoGenerate; }
        public Column columnAnnotation() { return columnAnnotation; }
    }
}
