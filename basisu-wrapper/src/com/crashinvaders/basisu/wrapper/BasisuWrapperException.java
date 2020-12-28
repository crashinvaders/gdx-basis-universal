package com.crashinvaders.basisu.wrapper;

/**
 * An exception related to gdx-basisu-wrapper code both for native and Java side.
 */
public class BasisuWrapperException extends RuntimeException {
    private static final long serialVersionUID = -6258402319222323567L;

    public BasisuWrapperException(String message) {
        super(message);
    }
}
