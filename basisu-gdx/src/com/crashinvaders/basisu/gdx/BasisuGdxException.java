package com.crashinvaders.basisu.gdx;

public class BasisuGdxException extends RuntimeException {
    private static final long serialVersionUID = 7954047253558223708L;

    public BasisuGdxException() {
    }

    public BasisuGdxException(String message) {
        super(message);
    }

    public BasisuGdxException(String message, Throwable cause) {
        super(message, cause);
    }

    public BasisuGdxException(Throwable cause) {
        super(cause);
    }

    public BasisuGdxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
