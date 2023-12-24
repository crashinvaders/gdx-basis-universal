package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

/**
 * {@link AssetManager} compliant loader for KTX2 textures.
 * <p/>
 * Here's an example of how to setup the loader, so the {@link AssetManager} can support loading of ".basis" texture files: *
 * <br/>
 * <code>
 *     assetManager.setLoader(Texture.class, ".ktx2", new Ktx2TextureLoader(assetManager.getFileHandleResolver()));
 * </code>
 * <br/>
 * And after that call to <code>assetManager.load("MyImage.ktx2", Texture.class);</code> will post the texture for loading.
 */
public class Ktx2TextureLoader extends AsynchronousAssetLoader<Texture, TextureLoader.TextureParameter> {

    Ktx2TextureData textureData;

    public Ktx2TextureLoader(FileHandleResolver resolver) {
        super(resolver);
        // We need to make sure this one is first time called
        // on the main thread and not during async texture loading.
        BasisuGdxUtils.initSupportedGlTextureFormats();
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, TextureLoader.TextureParameter parameter) {
        Ktx2TextureData data;
        if (parameter instanceof Ktx2TextureParameter) {
            Ktx2TextureParameter basisParameter = (Ktx2TextureParameter) parameter;
            data = new Ktx2TextureData(fileHandle);
            data.setUseMipMaps(basisParameter.useMipmaps);
            if (basisParameter.formatSelector != null) {
                data.setTextureFormatSelector(basisParameter.formatSelector);
            }
        } else {
            data = new Ktx2TextureData(fileHandle);
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
     * Parameter class is an optional extension for the standard {@link TextureLoader.TextureParameter}.
     */
    public static class Ktx2TextureParameter extends TextureLoader.TextureParameter {
        // public int layerIndex = 0; // Not yet supported.
        public boolean useMipmaps = true;
        public BasisuTextureFormatSelector formatSelector = null;

        public Ktx2TextureParameter() {
        }
    }
}