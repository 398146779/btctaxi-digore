package com.btctaxi.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 结果集
 */
public class DataMap implements Map<String, Object> {
    private Map<String, Object> base;

    public DataMap() {
        base = new HashMap<>();
    }

    public DataMap(Map<String, Object> target) {
        base = target;
    }

    public Boolean getBoolean(String key) {
        Object o = base.get(key);
        if (o instanceof String)
            return Boolean.parseBoolean((String) o);
        return (Boolean) o;
    }

    public Integer getInt(String key) {
        Object o = base.get(key);
        if (o instanceof Long)
            return ((Long) o).intValue();
        return (Integer) o;
    }

    public Long getLong(String key) {
        Object o = base.get(key);
        if (o instanceof BigInteger)
            return ((BigInteger) o).longValue();
        if (o instanceof Integer)
            return ((Integer) o).longValue();
        return (Long) o;
    }

    public Double getDouble(String key) {
        Object o = base.get(key);
        if (o instanceof BigDecimal)
            return ((BigDecimal) o).doubleValue();
        if (o instanceof Float)
            return ((Float) o).doubleValue();
        return (Double) o;
    }

    public BigDecimal getBig(String key) {
        Object o = base.get(key);
        if (o instanceof Double)
            return new BigDecimal((Double) o);
        if (o instanceof Float)
            return new BigDecimal((Float) o);
        if (o instanceof String)
            return new BigDecimal((String) o);
        return (BigDecimal) o;
    }

    public Timestamp getTime(String key) {
        Object o = base.get(key);
        return (Timestamp) o;
    }

    public String getString(String key) {
        return (String) base.get(key);
    }

    @Override
    public DataMap put(String key, Object value) {
        base.put(key, value);
        return this;
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return base.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        base.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
        base.replaceAll(function);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return base.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return base.remove(key, value);
    }

    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        return base.replace(key, oldValue, newValue);
    }

    @Override
    public Object replace(String key, Object value) {
        return base.replace(key, value);
    }

    @Override
    public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
        return base.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return base.computeIfPresent(key, remappingFunction);
    }

    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
        return base.compute(key, remappingFunction);
    }

    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return base.merge(key, value, remappingFunction);
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return base.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return base.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return base.get(key);
    }

    @Override
    public Object remove(Object key) {
        return base.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        base.putAll(m);
    }

    @Override
    public void clear() {
        base.clear();
    }

    @Override
    public Set<String> keySet() {
        return base.keySet();
    }

    @Override
    public Collection<Object> values() {
        return base.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return base.entrySet();
    }

    @Override
    public int hashCode() {
        return base.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return base.equals(obj);
    }
}
