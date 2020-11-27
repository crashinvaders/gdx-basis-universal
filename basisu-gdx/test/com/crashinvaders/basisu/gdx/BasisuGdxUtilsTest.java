package com.crashinvaders.basisu.gdx;

import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import org.junit.Test;

public class BasisuGdxUtilsTest {

    /**
     * Ensures that all the declared BasisuTranscoderTextureFormat values are mapped to
     * their OpenGL internal texture format representation codes.
     */
    @Test
    public void testToGlTextureFormat() {
        for (BasisuTranscoderTextureFormat value : BasisuTranscoderTextureFormat.values()) {
            BasisuGdxUtils.toGlTextureFormat(value);
        }
    }
}
