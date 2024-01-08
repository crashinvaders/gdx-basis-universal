package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

/**
 * {@link AssetManager} compliant loader for Basis Universal textures.
 * <p/>
 * Here's an example of how to set up the loader, so the {@link AssetManager} can support loading of ".basis" texture files: *
 * <br/>
 * <code>
 *     assetManager.setLoader(Texture.class, ".basis", new BasisuTextureLoader(assetManager.getFileHandleResolver()));
 * </code>
 * <br/>
 * And after that call to <code>assetManager.load("MyImage.basis", Texture.class);</code> will post the texture for loading.
 */
public class BasisuTextureLoader extends AsynchronousAssetLoader<Texture, TextureLoader.TextureParameter> {

    BasisuTextureData textureData;

    public BasisuTextureLoader(FileHandleResolver resolver) {
        super(resolver);
        // We need to make sure this one is first time called
        // on the main thread and not during async texture loading.
        BasisuGdxUtils.initSupportedGlTextureFormats();
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, TextureLoader.TextureParameter parameter) {
        BasisuTextureData data;
        if (parameter instanceof BasisuTextureParameter) {
            BasisuTextureParameter basisParameter = (BasisuTextureParameter) parameter;
            data = new BasisuTextureData(fileHandle, basisParameter.imageIndex);
            data.setUseMipMaps(basisParameter.useMipmaps);
            if (basisParameter.formatSelector != null) {
                data.setTextureFormatSelector(basisParameter.formatSelector);
            }
        } else {
            data = new BasisuTextureData(fileHandle);
        }
        data.prepare();
        textureData = data;
    }

    public Texture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, TextureLoader.TextureParameter parameter) {
        Texture texture = new Texture(this.textureData);
        this.textureData = null;

        if (parameter != null) {
            texture.setFilter(parameter.minFilter, parameter.magFilter);
            texture.setWrap(parameter.wrapU, parameter.wrapV);
        }

        return texture;
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, TextureLoader.TextureParameter parameter) {
        return null;
    }

    /**
     * Parameter class is an optional extension for the standard {@link com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter}.
     */
    public static class BasisuTextureParameter extends TextureLoader.TextureParameter {
        public int imageIndex = 0;
        public boolean useMipmaps = true;
        public BasisuTextureFormatSelector formatSelector = null;

        public BasisuTextureParameter() {
        }
    }
}