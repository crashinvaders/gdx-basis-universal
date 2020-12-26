package com.crashinvaders.basisu.gdx;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.basisu.wrapper.BasisuTranscoderTextureFormat;

/**
 * {@link AssetManager} compliant loader for Basis Universal textures.
 * <p/>
 * Here's an example of how to setup the loader, so the {@link AssetManager} can support loading of ".basis" texture files: *
 * <br/>
 * <code>
 *     assetManager.setLoader(Texture.class, ".basis", new BasisuTextureLoader(assetManager.getFileHandleResolver()));
 * </code>
 * <br/>
 * And after that call to <code>assetManager.load("MyImage.basis", Texture.class);</code> will post the texture for loading.
 */
public class BasisuTextureLoader extends AsynchronousAssetLoader<Texture, BasisuTextureLoader.BasisuTextureParameter> {

    BasisuTextureData textureData;

    public BasisuTextureLoader(FileHandleResolver resolver) {
        super(resolver);
        // We need to make sure this one is first time called
        // on the main thread and not during async texture loading.
        BasisuGdxUtils.initSupportedGlTextureFormats();
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle fileHandle, BasisuTextureParameter parameter) {
        BasisuTextureData data;

        if (parameter != null) {
            data = new BasisuTextureData(fileHandle, parameter.imageIndex, parameter.mipmapLevel);
            if (parameter.formatSelector != null) {
                textureData.setTextureFormatSelector(parameter.formatSelector);
            }
        } else {
            data = new BasisuTextureData(fileHandle);
        }
        data.prepare();
        textureData = data;
    }

    public Texture loadSync(AssetManager manager, String fileName, FileHandle fileHandle, BasisuTextureParameter parameter) {
        Texture texture = new Texture(this.textureData);
        this.textureData = null;

        if (parameter != null) {
            texture.setFilter(parameter.minFilter, parameter.magFilter);
            texture.setWrap(parameter.wrapU, parameter.wrapV);
        }

        return texture;
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle fileHandle, BasisuTextureParameter parameter) {
        return null;
    }

    public static class BasisuTextureParameter extends AssetLoaderParameters<Texture> {
        public int imageIndex = 0;
        public int mipmapLevel = 0;
        public BasisuTextureFormatSelector formatSelector = null;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;

        public BasisuTextureParameter() {
        }

        /**
         * Shortcut method to lock the transcoder to the specific texture format.
         * Use it carefully as there's no single texture format to be supported by all the platforms
         * (that's kinda the whole purpose of Basis Universal dynamic format selector...)
         */
        public void setTextureFormat(BasisuTranscoderTextureFormat format) {
            this.formatSelector = (data, imageInfo) -> format;
        }
    }
}