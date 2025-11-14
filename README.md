# GDX Basis Universal

![Maven Central](https://img.shields.io/maven-central/v/com.crashinvaders.basisu/basisu-gdx?label=Maven%20Central)
[![libGDX](https://img.shields.io/badge/libGDX-1.14.0-blue.svg)](https://libgdx.com/)
[![Basis Universal](https://img.shields.io/badge/Basis%20Universal-1.16.4-blue.svg)](https://github.com/BinomialLLC/basis_universal)

Cross-platform support for Binomial's [Basis Universal](https://github.com/BinomialLLC/basis_universal) supercompressed GPU textures.

Use the same intermediate [compressed texture](https://en.wikipedia.org/wiki/Texture_compression) assets (`.ktx2`/`.basis`) for all the [libGDX](https://github.com/libGDX/libGDX) backends and save on video memory leveraging the platforms' natively supported GPU compression.

<details>
    <summary>
        If you've never heard of the Basis Universal project or are unfamiliar with the "supercompressed texture" term, this is how it works...
    </summary>

#### The problem

When using traditional image formats (like PNG, JPG, TIFF, etc), they all get decoded to plain (uncompressed) RGB/RGBA representation before being supplied to a rendering API (OpenGL) and loaded to RAM. This is mostly fine, but once you get to the point when you need to use lots of simultaneously loaded huge textures you may easily run out of memory (especially on mobile devices). To give a better idea, a 4096x4096 RGBA32 (8 bits per channel) texture, being loaded into the GPU, holds roughly 64MB of memory. Stack a few of those and you're pretty much screwed.

#### The solution(?)

To address this issue many GPU manufacturers implement hardware support for specific texture compression formats.
Some may help you to chop down the memory footprint with the compression ratio of an impressive 8 times if you've agreed on a small trade-off in image quality (most of the texture compression formats are lossy).

The only downside is there is no one universal texture compression format that guarantees to work on every single platform/hardware. And if you're up for the game of supplying GPU compressed textures for your game, you have to pack your distributions with a lot of specific types of compressed textures per device hardware support for them (to mention, what in a way [Unity practices](https://docs.unity3d.com/Manual/class-TextureImporterOverride.html) for quite a while).

#### The compromise

To address this issue, [Binomial LLC](http://www.binomial.info/) founded their Basis Universal project.
The solution is to use one intermediate compressed texture format (super-compressed) and transcode it on runtime to one of the supported texture formats by the running platform.
The Basis transcoder is capable of transcoding a Basis texture to one of the dozen native GPU compressed formats and covers all the modern platforms that way.
There's a little overhead price for processing the texture data, but the transcoding operation is highly optimized and, to say, only should happen once upon asset loading.

The transcoder uses a number of transcoding tables to port data across different formats. Some of them are pretty bulky and thus being dropped from compilation for specific platforms (e.g. there's no reason to support mobile-only formats like PVRTC1 on desktop). So this library maintains the selection logic as well, leaving you with a simple portable compressed texture format that will transparently work everywhere.

Basis Universal is backed by Google, open-source, and now available to everyone!

</details>

### Cool, how do I turn my images into Basis textures?

[GDX Texture Packer GUI](https://github.com/crashinvaders/gdx-texture-packer-gui) has full support for encoding atlases as Basis Universal textures. Also, it provides a CLI interface to turn any PNG/JPG image into a KTX2/Basis texture.

You can also use the official [command-line tool](https://github.com/BinomialLLC/basis_universal/releases) or build the encoder from the [sources](https://github.com/BinomialLLC/basis_universal) yourself.

There could be some other options (even potentially encoding in the browser), but I'm not aware of ATM, and it's worth googling.

> Please read the ["Texture format notes"](#texture-format-notes) and ["Feature support notes"](#basis-universal-feature-support-notes) sections before encoding your textures.

## Using the library

Once added as a Maven dependency to libGDX project modules, the library is pretty easy to deal with, no global initialization calls are required in the code.

All the official libGDX backends are fully supported.

### Connecting dependencies (Gradle)

The release and snapshot Maven artifacts are available on the Maven Central repository

```gradle
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://central.sonatype.com/repository/maven-snapshots/' } // <-- optional, for snapshot versions
    }
}
```

And then just add these records to the dependency section of your `build.gradle` files. 

> Don't forget to set `gdxBasisuVersion` property with the correct library version 
> (e.g. declaring `gdxBasisuVersion=1.1.0` in the project's root `settings.gradle` file).

#### Core module

```gradle
dependencies {
    api "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion"
    api "com.crashinvaders.basisu:basisu-gdx:$gdxBasisuVersion"
}
```

#### Desktop module ([LWJGL](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backend-lwjgl), [LWJGL3](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backend-lwjgl3), [Headless](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backend-headless) backends)
```gradle
dependencies {
    runtimeOnly "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-desktop"
}
```

#### Android module ([Android](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backend-android) backend)
```gradle
dependencies {
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-armeabi-v7a"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-arm64-v8a"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-x86"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-x86_64"
}
```

#### iOS module ([RoboVM](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backend-robovm-metalangle) backend)

> It's highly recommended to use `robovm-metalangle` libGDX backend in favor of classic `robovm`,
> as this is the only way to unlock access to more compressed texture formats on Apple devices.

```gradle
dependencies {
    implementation "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-ios"

    // This is highly recomended, otherwise you're stuck with only PVRTC1 textures.
    implementation "com.badlogicgames.gdx:gdx-backend-robovm-metalangle:$gdxVersion"
}
```

#### Web module ([GWT](https://github.com/libGDX/libGDX/tree/master/backends/gdx-backends-gwt) backend)

As usual, the GWT module requires a bit more dance around.
You need to declare an extra dependency and the sources for all the used jars.

```gradle
dependencies {
    implementation "com.crashinvaders.basisu:basisu-gdx-gwt:$gdxBasisuVersion"
    implementation "com.crashinvaders.basisu:basisu-gdx-gwt:$gdxBasisuVersion:sources"
    implementation "com.crashinvaders.basisu:basisu-gdx:$gdxBasisuVersion:sources"
    implementation "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:sources"
    implementation "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-web"
}
```

Don't forget to add a GWT module entry to your `GdxDefinition.gwt.xml` file.

```xml
<module>
    <inherits name='com.crashinvaders.basisu.BasisuGdxGwt'/>
</module>
```

### Example code

The library provides transparent support for `KTX2`/`Basis` format textures using `Ktx2TextureData`/`BasisuTextureData` classes respectively. Each acts very similar to libGDX's implementation of `ETC1TextureData`. The only difference is that libGDX doesn't know how to load `.ktx2`/`.basis` texture files out of the box, so you have to explicitly use the proper texture data class when creating a texture.

```java
Texture myTexture = new Texture(new Ktx2TextureData(Gdx.files.internal("MyTexture.ktx2")));
```

From now on, it's safe to use the texture instance as usual, and it already should hold the data transcoded to the best suited native GPU compressed texture format for your platform.

#### Asset manager integration

If you're using `AssetManager` to load game assets, you can easily integrate with it as well using `BasisuTextureLoader` class.

```java
// Register the texture loader for the ".ktx2" file extension.
assetManager.setLoader(Texture.class, ".ktx2", new Ktx2TextureLoader(assetManager.getFileHandleResolver()));

// Post your texture assets for loading as usual.
assetManager.load("MyTexture.ktx2", Texture.class);
// You can also use ktx2-based atlases.
// The Basis textures will be automatically resolved and loaded.
assetManager.load("MyAtlas.atlas", TextureAtlas.class);

// When the asset manager has finished loading, retrieve the assets as usual.
Texture myTexture = assetManager.get("MyTexture.ktx2", Texture.class);
TextureAtlas myAtlas = assetManager.get("MyAtlas.atlas", TextureAtlas.class);
```

> You can use [`gdx-texture-packer-gui`](https://github.com/crashinvaders/gdx-texture-packer-gui) to create Basis based texture atlases.

## Platform limitations

Here's the list of the limitations you should be aware of when using this library (on top of regular libGDX backend limitations).

- __[Android]__ Due to NDK specs, __Android 5.0 (API 21) is the minimum supported version__.
- __[GWT]__ WebAssembly is available pretty much on every modern browser ([compatibility chart](https://caniuse.com/wasm)). Just for reference, the support is enabled by default as of __Firefox 52__, __Chrome 57__, __Opera 44__, and __Edge 16__.
- __[iOS]__ Due to OpenGL deprecation, working with compressed textures is troublesome. Please read [this section](#ios-module-robovm-backend). 

## Basis Universal feature support notes

Most of the essential Basis transcoder features are exposed and implemented for Java (including file validation, transcoding to all the necessary formats, and KTX2/Basis file/image information lookup methods).

Transcoding from both intermediate formats (__ETC1S__ low-medium quality and __UASTC__ high quality) works as intended.

Mipmaps are supported, but not implemented on the libGDX integration side.
The feature will be enabled in future releases.

### Texture channel layout types

There are four possible texture channel layout types:

1. __RGBA__ - three-color component images with full alpha channel support
2. __RGB__ - fully opaque three-color component images
3. __RG__ or __XY__ - luminance and alpha
4. __R__ - luminance or alpha

The __RGBA__ and __RGB__ types are the general case.
For these, there is the most variety of transcoder texture formats supported.

The rest two are considered niche types, and the transcoder has fewer options.

Please be aware, that at the moment the [default texture format selector](#texture-format-resolution-strategy) doesn't recognize __RG__ and __R__ texture types, and they will be treated as __RGBA__ and __RGB__ respectively. If you need support for them, provide a custom selector implementation.

### Basis texture types

Basis supports five different texture types:

1. __Regular 2D__ - an arbitrary array of 2D RGB or RGBA images with optional mipmaps, array size = # images, each image may have a different resolution and # of mipmap levels.
2. __Regular 2D array__ - an array of 2D RGB or RGBA images with optional mipmaps, array size = # images, each image has the same resolution and mipmap levels.
3. __Cubemap array__ - an array of cubemap levels, total # of images must be divisible by 6, in X+, X-, Y+, Y-, Z+, Z- order, with optional mipmaps.
4. __Video frames__ - an array of 2D video frames, with optional mipmaps, # frames = # images, each image has the same resolution and # of mipmap levels.
5. __volume__ - a 3D texture with optional mipmaps, Z dimension = # images, each image has the same resolution and # of mipmap levels.

Out of which support only for __Regular 2D__ is implemented through __BasisuTextureData__ for libGDX textures.

> I'm not very familiar with 3D related formats like __Cubemap array__ and skipped them for now.
> The Basis data for those is fully available from `basisu-wrapper`, only the libGDX support is missing.
> If you're in demand for those or may assist with the implementation, please come forward and open an issue with the details.

### KTX2 vs Basis files

Simply put, when in doubt, go with `.ktx2` files in favor of `.basis`.

The long story. Those are simply two different texture file containers. Both are capable of containing the same Basis textures, but a little differently in terms of inner layout (like `mp4` and `mpv` in the world of video files). Historically `.basis` was the only container, made specifically for the needs of the Basis Universal library.
Later on, Basis Universal was standardized by [The Khronos Group](https://www.khronos.org/assets/uploads/developers/presentations/3D_Formats_Wayfair_KTX2_SIGGRAPH_Aug21.pdf) and the new `.ktx2` container came along.

### Debugging/diagnostics

If you're writing your own transcoder format selector or just wish to know which of the textures are supported on the specific platform, 
here's a little snippet that logs out the Basis Universal support report:

```
BasisuNativeLibLoader.loadIfNeeded(); // Make sure the Basis Universal natives are loaded.
Gdx.app.log(TAG, BasisuGdxUtils.reportAvailableTranscoderFormats(BasisuTextureFormat.ETC1S));
Gdx.app.log(TAG, BasisuGdxUtils.reportAvailableTranscoderFormats(BasisuTextureFormat.UASTC4x4));
```

Besides that, the library prints a bunch of useful information (like texture transcoding operations) to debug log.
Check it on your device.

## Texture format notes

Basis Universal texture transcoder supports a bunch of very different GPU-compressed texture formats.
Some of them impose very important limitations and cannot be used (cannot be transcoded to on runtime) unless all the requirements are met.

To have the widest possible native format support, it's highly recommended to encode intermediate Basis images that comply with __ALL__ of these specifics.

- __PVRTC1__ requires square textures with the power of two sides.
- __BC1_RGB__ and __BC3_RGBA__ require textures with sides to be multiple of four (superseded by PVRTC1 requirements).

> To round up, always use square images with the power of two dimensions for Basis texture assets.

## Texture format resolution strategy

Basis textures can be easily transcoded to many other texture formats. This is great, but another challenge here is to transcode to the format that is most appropriate for the current runtime platform.

Here are all the criteria we should respect in making such a decision (the most important ones at the top):

- Intermediate texture channel layout (RGBA/RGB/RG/R).
- OpenGL/hardware support for the texture format.
- Basis transcoder support for the texture format (to save up on the native library size, some of the transcoder formats are disabled per platform).
- Whether the intermediate image meets the target format's requirements (see [Texture format notes](#texture-format-notes) section).
- The target format quality loss and/or transcoding speed.

The [default texture format selector](basisu-gdx/src/com/crashinvaders/basisu/gdx/BasisuTextureFormatSelector.java#L42) logic is implemented based on these. That way it should always pick the best available option. In case there are none of the texture formats are passing the check, the selector falls back to the uncompressed texture formats (RGBA8888/RGB888). Which are regular libGDX texture formats and have guaranteed support on all the platforms.

If you require a different selection strategy, you can always create a custom implementation for `BasisuTextureFormatSelector` and use it selectively with `BasisuTextureData#setTextureFormatSelector()`/`Ktx2TextureData#setTextureFormatSelector()` methods or set it to be used as the default selector by updating the `BasisuGdxUtils#defaultFormatSelector` static field.

> Please be aware, that at the moment the default texture format selector doesn't recognize __RG__ and __R__ texture types and they will be treated as __RGBA__ and __RGB__ respectively. If you need support for them, provide a custom selector implementation.

## Native dependencies

The project uses [Basis Universal C/C++ code](https://github.com/BinomialLLC/basis_universal) and [JNI](https://en.wikipedia.org/wiki/Java_Native_Interface) wrappers to connect to libGDX cross-platform code. [basisu-wrapper](basisu-wrapper) module provides a pure Java (no dependencies) abstraction layer over the native libs.

Read more about the module and the native library building notes on [the module's page](basisu-wrapper).
