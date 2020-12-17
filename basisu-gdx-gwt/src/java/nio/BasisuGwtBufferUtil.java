package java.nio;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Provides an access to the package-private methods of the GWT buffer classes.
 */
public class BasisuGwtBufferUtil {

    public static DirectReadWriteByteBuffer createDirectByteBuffer(ArrayBuffer arrayBuffer) {
        return new DirectReadWriteByteBuffer(arrayBuffer);
    }
}
