package com.crashinvaders.basisu.wrapper;

import java.util.HashSet;

import static com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat.*;

/**
 * Keeps track of {@link BasisuTranscoderTextureFormat} compatibility per target platform/OS.
 * <p/>
 * Unity docs have a nice reference page - https://docs.unity3d.com/Manual/class-TextureImporterOverride.html
 */
//TODO Use basist::basis_is_format_supported() native function to make a list of supported transcoder formats.
public enum BasisuPlatform {
    MAC_OS,
    WINDOWS,
    LINUX,
    ANDROID,
    IOS,
    WEB;

    private final HashSet<BasisuTranscoderTextureFormat> unsupported = new HashSet<>();

    public boolean isCompatible(BasisuTranscoderTextureFormat format) {
        return !unsupported.contains(format);
    }

    private static void desktopUnsupported(BasisuTranscoderTextureFormat format) {
        MAC_OS.unsupported.add(format);
        WINDOWS.unsupported.add(format);
        LINUX.unsupported.add(format);
    }

    // This format exception table also should be in sync with the Basisu Universal native macro compilation flags.
    // Make sure jnigen platform's "cppflags" properties (basisu-wrapper/build.gradle) match these values.
    static {
        // Android
        // ASTC, ETC1, ETC2, DXT, ATC, PVRTC1 and PVRTC2
        ANDROID.unsupported.add(BC7_RGBA);

        // iOS
        // ASTC, ETC1, ETC2, PVRTC1
        IOS.unsupported.add(BC1_RGB);
        IOS.unsupported.add(BC3_RGBA);
        IOS.unsupported.add(BC4_R);
        IOS.unsupported.add(BC5_RG);
        IOS.unsupported.add(BC7_RGBA);
        IOS.unsupported.add(ATC_RGB);
        IOS.unsupported.add(ATC_RGBA);
        IOS.unsupported.add(PVRTC2_4_RGB);
        IOS.unsupported.add(PVRTC2_4_RGBA);

        // Web (WebGL)
        // https://developer.mozilla.org/en-US/docs/Web/API/WebGL_API/Compressed_texture_formats
        // ASTC, ETC1, ETC2, PVRTC1, DXT
        WEB.unsupported.add(PVRTC2_4_RGB);
        WEB.unsupported.add(PVRTC2_4_RGBA);
        WEB.unsupported.add(ATC_RGB);
        WEB.unsupported.add(ATC_RGBA);
        WEB.unsupported.add(BC7_RGBA);

        // Desktop
        // Essentially supports everything (or it's more accurate to say ANYTHING), so we need them all.
        // Except for a few mobile-first GPU specific formats.
        desktopUnsupported(PVRTC1_4_RGB);
        desktopUnsupported(PVRTC1_4_RGBA);
    }
}
