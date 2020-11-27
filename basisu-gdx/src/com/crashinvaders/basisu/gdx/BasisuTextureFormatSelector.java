package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

import static com.crashinvaders.basisu.gdx.BasisuGdxUtils.isGlExtensionSupported;

/**
 * <b>Useful Links</b>
 * <br/>
 * <a href="https://github.com/BinomialLLC/basis_universal#how-to-use-the-system">Comprehensive texture format comparison</a> from Unity documentation.
 * <br/>
 * <a href="https://github.com/BinomialLLC/basis_universal#how-to-use-the-system">General guide</a> on selecting the texture format based on supported extension.
 * <p/>
 */
public interface BasisuTextureFormatSelector {
    BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo);

    class Default implements BasisuTextureFormatSelector {
        public static BasisuTextureFormatSelector desktop = new Desktop();
        public static BasisuTextureFormatSelector android = new Android();
        public static BasisuTextureFormatSelector iOS = new IOS();
        public static BasisuTextureFormatSelector webGl = new WebGl();

        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo) {
            Application.ApplicationType type = Gdx.app.getType();
            switch (type) {
                case Desktop:
                    return desktop.resolveTextureFormat(data, imageInfo);
                case Android:
                    return android.resolveTextureFormat(data, imageInfo);
                case iOS:
                    return iOS.resolveTextureFormat(data, imageInfo);
                case WebGL:
                    return webGl.resolveTextureFormat(data, imageInfo);
                case HeadlessDesktop:
                case Applet:
                default:
                    throw new BasisuGdxException("Unsupported LibGDX platform type: " + type);
            }
        }
    }

    class Desktop implements BasisuTextureFormatSelector {
//        public static BasisuTextureFormatSelector windows = new Windows();
//        public static BasisuTextureFormatSelector macOS = new MacOS();
//        public static BasisuTextureFormatSelector android = new Android();

        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo) {

            if (imageInfo.hasAlphaFlag()) {
                if (isGlExtensionSupported("GL_ARB_texture_compression_bptc")) {
                    return BasisuTranscoderTextureFormat.BC7_RGBA;
                }
                if (isGlExtensionSupported("GL_EXT_texture_compression_s3tc")) {
                    return BasisuTranscoderTextureFormat.BC3_RGBA;
                }
                if (isGlExtensionSupported("GL_ARB_ES3_compatibility")) {
                    return BasisuTranscoderTextureFormat.ETC2_RGBA;
                }
                return BasisuTranscoderTextureFormat.RGBA32;
            } else {
                if (isGlExtensionSupported("GL_EXT_texture_compression_s3tc")) {
                    return BasisuTranscoderTextureFormat.BC1_RGB;
                }
                //TODO Check if ETC1 is supported and use it.
//                return BasisuTranscoderTextureFormat.ETC1_RGB;

                return BasisuTranscoderTextureFormat.RGB565;
            }
        }
    }

//    class Linux implements BasisuTextureFormatSelector {
//
//        @Override
//        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data) {
//            return null;
//        }
//    }
//
//    class Windows implements BasisuTextureFormatSelector {
//
//        @Override
//        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data) {
//            return null;
//        }
//    }
//
//    class MacOS implements BasisuTextureFormatSelector {
//
//        @Override
//        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data) {
//            return null;
//        }
//    }

    class Android implements BasisuTextureFormatSelector {

        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo) {
            throw new BasisuGdxException(new UnsupportedOperationException("Not implemented yet."));
        }
    }

    class WebGl implements BasisuTextureFormatSelector {

        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo) {
            throw new BasisuGdxException(new UnsupportedOperationException("Not implemented yet."));
        }
    }

    class IOS implements BasisuTextureFormatSelector {

        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuImageInfo imageInfo) {
            throw new BasisuGdxException(new UnsupportedOperationException("Not implemented yet."));
        }
    }
}
