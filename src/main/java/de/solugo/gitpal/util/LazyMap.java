package de.solugo.gitpal.util;

import java.util.HashMap;

/**
 *
 * @author Frederic Kneier
 */
public class LazyMap<K, V> extends HashMap<K, V> {

    private final LazyProvider<K, V> provider;

    public LazyMap(final LazyProvider<K, V> provider) {
        this.provider = provider;
    }

    @Override
    public V get(final Object key) {
        V value = super.get(key);
        if (value == null && provider != null) {
            try {
                value = provider.get((K) key);
                this.put((K) key, value);
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return value;
    }

    public static interface LazyProvider<K, V> {

        public V get(final K key) throws Exception;
    }
}
