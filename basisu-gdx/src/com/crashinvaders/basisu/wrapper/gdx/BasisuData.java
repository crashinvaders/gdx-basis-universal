package com.crashinvaders.basisu.wrapper.gdx;

import com.badlogic.gdx.files.FileHandle;

public class BasisuData {
    private final byte[] encodedData;

    public BasisuData(FileHandle fileHandle) {
        this.encodedData = fileHandle.readBytes();

    }
}
