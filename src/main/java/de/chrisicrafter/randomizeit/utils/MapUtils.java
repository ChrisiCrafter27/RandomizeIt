package de.chrisicrafter.randomizeit.utils;

import java.util.Map;

public class MapUtils {
    public static <K, V> K getKey(Map<K, V> map, V value) {
        for(Map.Entry<K, V> entry : map.entrySet()) {
            if(entry.getValue().equals(value)) return entry.getKey();
        }
        return null;
    }
}
