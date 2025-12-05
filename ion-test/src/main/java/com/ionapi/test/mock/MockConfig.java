package com.ionapi.test.mock;

import com.ionapi.api.config.IonConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Mock implementation of IonConfig for unit testing.
 * Stores values in memory without file I/O.
 */
public class MockConfig implements IonConfig {

    private final Map<String, Object> data = new LinkedHashMap<>();
    private int saveCount = 0;
    private int reloadCount = 0;

    @Override
    @Nullable
    public Object get(@NotNull String path) {
        return getNestedValue(path);
    }

    @Override
    @NotNull
    public Object get(@NotNull String path, @NotNull Object defaultValue) {
        Object value = get(path);
        return value != null ? value : defaultValue;
    }

    @Override
    @Nullable
    public String getString(@NotNull String path) {
        Object value = get(path);
        return value != null ? value.toString() : null;
    }

    @Override
    @NotNull
    public String getString(@NotNull String path, @NotNull String defaultValue) {
        String value = getString(path);
        return value != null ? value : defaultValue;
    }

    @Override
    public int getInt(@NotNull String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(@NotNull String path, int defaultValue) {
        Object value = get(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    @Override
    public double getDouble(@NotNull String path) {
        return getDouble(path, 0.0);
    }

    @Override
    public double getDouble(@NotNull String path, double defaultValue) {
        Object value = get(path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    @Override
    public boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(@NotNull String path, boolean defaultValue) {
        Object value = get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    @Override
    @NotNull
    public List<?> getList(@NotNull String path) {
        Object value = get(path);
        if (value instanceof List) {
            return (List<?>) value;
        }
        return Collections.emptyList();
    }

    @Override
    @NotNull
    public List<String> getStringList(@NotNull String path) {
        List<?> list = getList(path);
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        return result;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        setNestedValue(path, value);
    }

    @Override
    public boolean contains(@NotNull String path) {
        return get(path) != null;
    }

    @Override
    @NotNull
    public Set<String> getKeys(boolean deep) {
        if (!deep) {
            return new LinkedHashSet<>(data.keySet());
        }
        Set<String> keys = new LinkedHashSet<>();
        collectKeys("", data, keys);
        return keys;
    }

    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSection(@NotNull String path) {
        Object value = get(path);
        if (value instanceof Map) {
            return new LinkedHashMap<>((Map<String, Object>) value);
        }
        return Collections.emptyMap();
    }

    @Override
    public void save() {
        saveCount++;
    }

    @Override
    public void reload() {
        reloadCount++;
    }

    // ========== Test Utilities ==========

    /**
     * Gets how many times save() was called.
     */
    public int getSaveCount() {
        return saveCount;
    }

    /**
     * Gets how many times reload() was called.
     */
    public int getReloadCount() {
        return reloadCount;
    }

    /**
     * Clears all data.
     */
    public void clear() {
        data.clear();
    }

    /**
     * Gets the raw data map.
     */
    @NotNull
    public Map<String, Object> getRawData() {
        return new LinkedHashMap<>(data);
    }

    /**
     * Loads data from a map.
     */
    public void loadFromMap(@NotNull Map<String, Object> map) {
        data.clear();
        data.putAll(map);
    }

    // ========== Private Helpers ==========

    @SuppressWarnings("unchecked")
    private Object getNestedValue(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                return null;
            }
            current = (Map<String, Object>) next;
        }
        
        return current.get(parts[parts.length - 1]);
    }

    @SuppressWarnings("unchecked")
    private void setNestedValue(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;
        
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(parts[i], next);
            }
            current = (Map<String, Object>) next;
        }
        
        if (value == null) {
            current.remove(parts[parts.length - 1]);
        } else {
            current.put(parts[parts.length - 1], value);
        }
    }

    @SuppressWarnings("unchecked")
    private void collectKeys(String prefix, Map<String, Object> map, Set<String> keys) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            keys.add(key);
            if (entry.getValue() instanceof Map) {
                collectKeys(key, (Map<String, Object>) entry.getValue(), keys);
            }
        }
    }
}
