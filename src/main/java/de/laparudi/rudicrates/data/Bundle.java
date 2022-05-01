package de.laparudi.rudicrates.data;

import lombok.Getter;

import javax.annotation.Nonnull;

public class Bundle<K,V> {
    
    private final @Nonnull @Getter K key;
    private final @Nonnull @Getter V value;

    public Bundle(final @Nonnull K key, final @Nonnull V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Bundle<K, V> of(final @Nonnull K key, final @Nonnull V value) {
        return new Bundle<>(key, value);
    }
}
