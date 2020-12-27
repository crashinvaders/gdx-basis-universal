package com.crashinvaders.basisu.gdx;

import com.crashinvaders.basisu.wrapper.BasisuFileInfo;
import com.crashinvaders.basisu.wrapper.BasisuImageInfo;
import com.crashinvaders.basisu.wrapper.BasisuTextureFormat;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

import static com.crashinvaders.basisu.gdx.BasisuGdxUtils.*;
import static com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat.*;

/**
 * An abstract texture resolver that should decide which native
 * GPU texture compression format transcode a Basis texture to.
 * <br/>
 * The selection criteria may be different (e.g. the platform's GPU supported formats, supported subset of Basis Univeral native library transcoders, etc).
 * <br/>
 * The default selector is implemented by {@link Default}, and you may provide your own implementations based off it.
 * <p/>
 * <b>Useful Links</b>
 * <br/>
 * <a href="https://github.com/BinomialLLC/basis_universal#how-to-use-the-system">Comprehensive texture format comparison</a> from Unity documentation.
 * <br/>
 * <a href="https://github.com/BinomialLLC/basis_universal#how-to-use-the-system">General guide</a> on selecting the texture format based on supported extension.
 * <p/>
 */
public interface BasisuTextureFormatSelector {
    /**
     * Resolves the texture format that intermediate Basis Universal texture (ETC1S)
     * will be transcoded to based on the OpenGL compressed texture support.
     */
    BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuFileInfo fileInfo, BasisuImageInfo imageInfo);

    /**
     * The default texture format selector.
     * It's meant to be universal and to suit all the official LibGDX backends.
     * <br/>
     * The priority and comments are based on the Basis Universal
     * <a href="https://github.com/BinomialLLC/basis_universal#how-to-use-the-system">official GitHub repo documentation</a>.
     */
    class Default implements BasisuTextureFormatSelector {
        @Override
        public BasisuTranscoderTextureFormat resolveTextureFormat(BasisuData data, BasisuFileInfo fileInfo, BasisuImageInfo imageInfo) {
            //TODO Implement selectors for R and RG formats.

            BasisuTextureFormat btf = fileInfo.getTextureFormat();

            if (imageInfo.hasAlphaFlag()) {
                // The color block will be ETC1S, and the alpha block is EAC.
                // Conversion from ETC1S->EAC is very fast and nearly lossless.
                if (isBasisuFormatSupported(ETC2_RGBA, btf)) {
                    return ETC2_RGBA;
                }
                // Transcoding to BC7 mode 5 is very fast.
                if (isBasisuFormatSupported(BC7_RGBA, btf)) {
                    return BC7_RGBA;
                }
                // Conversion is nearly lossless and very fast.
                if (isBasisuFormatSupported(BC3_RGBA, btf) &&
                        isMultipleOfFour(imageInfo.getWidth(), imageInfo.getHeight())) {
                    return BC3_RGBA;
                }
                // Quality is very similar to BC1/BC3.
                if (isBasisuFormatSupported(ATC_RGBA, btf)) {
                    return ATC_RGBA;
                }
                if (isBasisuFormatSupported(ASTC_4x4_RGBA, btf)) {
                    return ASTC_4x4_RGBA;
                }
                // PVRTC1 transcoder requires that the ETC1S texture's dimensions both be equal and a power of two.
                if (isBasisuFormatSupported(PVRTC1_4_RGBA, btf) &&
                        isSquareAndPowerOfTwo(imageInfo.getWidth(), imageInfo.getHeight())) {
                    return PVRTC1_4_RGBA;
                }
                // This format is slower and much more complex than PVRTC2 RGB.
                // It will only work well with textures using premultiplied alpha.
                // The alpha channel should be relatively simple (like opacity maps).
                if (isBasisuFormatSupported(PVRTC2_4_RGBA, btf)) {
                    return PVRTC2_4_RGBA;
                }
                return RGBA32;

            } else {
                // ETC1 - The system's internal texture format is ETC1S, so outputting ETC1 texture data is a no-op.
                if (isBasisuFormatSupported(ETC1_RGB, btf)) {
                    return ETC1_RGB;
                }
                // Conversion to BC1 is very fast.
                // Conversion loses approx. .3-.5 dB Y PSNR relative to the source ETC1S data.
                if (isBasisuFormatSupported(BC1_RGB, btf) &&
                        isMultipleOfFour(imageInfo.getWidth(), imageInfo.getHeight())) {
                    return BC1_RGB;
                }
                // Conversion is nearly lossless and very fast.
                if (isBasisuFormatSupported(ATC_RGB, btf)) {
                    return ATC_RGB;
                }
                // Fast and almost as high quality as BC1.
                if (isBasisuFormatSupported(PVRTC2_4_RGB, btf)) {
                    return PVRTC2_4_RGB;
                }
//                // Fast and almost as high quality as BC1.
//                if (isBasisuFormatSupported(FXT1_RGB))basisTexFormat {
//                    return FXT1_RGB;
//                }
                // Nearly lowest quality of any texture format
                // This conversion loses the most quality - several Y dB PSNR.
                // PVRTC1 transcoder requires that the ETC1S texture's dimensions both be equal and a power of two.
                if (isBasisuFormatSupported(PVRTC1_4_RGB, btf) &&
                        isSquareAndPowerOfTwo(imageInfo.getWidth(), imageInfo.getHeight())) {
                    return PVRTC1_4_RGB;
                }
                return RGB565;
            }
        }
    }
}
