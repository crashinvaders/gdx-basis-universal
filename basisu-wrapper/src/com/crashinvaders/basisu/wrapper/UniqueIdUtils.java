package com.crashinvaders.basisu.wrapper;

public class UniqueIdUtils {
    static <T extends UniqueIdValue> T findOrThrow(T[] values, int id) {
        for (int i = 0; i < values.length; i++) {
            if (values[i].getId() == id) {
                return values[i];
            }
        }
        throw new IllegalArgumentException("Cannot find an enum value with ID: " + id);
    }

    public interface UniqueIdValue {
        int getId();
    }
}
