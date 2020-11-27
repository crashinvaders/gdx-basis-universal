package com.crashinvaders.basisu.gdx;

import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;
import org.junit.Ignore;
import org.junit.Test;

public class BasisuGdxUtilsTest {

    /**
     * Make sure all the declared BasisuTranscoderTextureFormat values are mapped to
     * their OpenGL internal texture format representation codes.
     */
    @Test
    @Ignore("Temporary disabled.")
    public void testToGlTextureFormat() {
        for (BasisuTranscoderTextureFormat value : BasisuTranscoderTextureFormat.values()) {
            BasisuGdxUtils.toGlTextureFormat(value);
        }
    }
}
