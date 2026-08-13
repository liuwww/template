package io.github.liuwww.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 大小写不敏感的Map实现
 * 键值统一转换为小写存储
 */
public class CaseInsensitiveMap<V> extends HashMap<String, V> {

    private static final long serialVersionUID = 1L;

    @Override
    public V put(String key, V value) {
        return super.put(key == null ? null : key.toLowerCase(), value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String) {
            return super.get(((String) key).toLowerCase());
        }
        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return super.containsKey(((String) key).toLowerCase());
        }
        return super.containsKey(key);
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String) {
            return super.remove(((String) key).toLowerCase());
        }
        return super.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
}
