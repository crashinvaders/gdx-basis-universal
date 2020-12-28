# GDX Basis Universal

Cross-platform support for Binomial's [Basis Universal](https://github.com/BinomialLLC/basis_universal) portable supercompressed GPU textures.

Now you can supply the same [compressed texture](https://en.wikipedia.org/wiki/Texture_compression) assets (`.basis`) for all the LibGDX backends and save sinigicant amount of RAM by using platforms' natively supported GPU compression.

<details>
    <summary>
        If you've never heard of Basis Universal project or unfamiliar with the "supercompressed texture" term, this is how it works...
    </summary>
    
#### The problem
When using traditional image formats (like PNG, JPG, TIFF, etc), they all get decoded to plain (uncompressed) RGB/RGBA representation before supplied to the OpenGL and loaded to the RAM. This is mostly fine, but once you get to the point when you need to use lots of simultaneously loaded huge textures you may easily run out of memory (especially on mobile devices). To give a better idea, a 4096x4096 RGBA32 (8 bits per channel) texture being loaded into the GPU will hold roughly 67.1MB of memory. Stack a few of those and you're pretty much screwed.
    
#### The solution(?)
To address this issue many GPU manufacturers introduced their texture compression formats that are available on their hardware.
Some may help you to chop down the memory footprint with the compression ratio of impressive 8 times, if you're agreed on a small trade-off in image quality (most of the texture compression formats are lossy).
    
The only downside is there is no one universal texture compression format that guarantees to work on every single platform/hardware. And if you're up for the game of supplying GPU compressed textures for your game, you have to pack your distributions with a whole lot of specific types of compressed textures per device hardware support for them (to mention, what in a way [Unity practices](https://docs.unity3d.com/Manual/class-TextureImporterOverride.html) for quite a while).
    
#### The compromise
To address this issue, [Binomial LLC](http://www.binomial.info/) founded their Basis Universal project.
The solution is to use one intermediate compressed texture format (supercompressed) and transcode it on runtime to one of the supported texture formats by the running platform.
The Basis transcoder is capable of transcoding a Basis texture to one of the dozen native GPU compressed formats and covers all the modern platforms that way.
There's a little overhead price for processing the texture data, but the transcoding operation is highly optimized and, to say, only should happen once upon asset loading.
    
The transcoder uses a number of transcoding tables to port data across different formats. Some of them pretty bulky and thus being dropped from compilation for specific platforms (e.g. there's no reason to support mobile-only formats like PVRTC1 on desktop). So this library maintains the selection logic as well, leaving you with a simple portable compressed texture format that will transparently work everywhere.
    
Basis Universal is backed by Google, open-source, and now available to everyone!
</details>
    
### Cool, how do I turn my images into Basis textures?
You can use [the command-line tool](https://github.com/BinomialLLC/basis_universal/releases) or build the encoder from [the sources](https://github.com/BinomialLLC/basis_universal) yourself.

As of now, there's no a web-based encoder or a desktop GUI tool to convert to the Basis format.
But don't be discouraged, there is a number of convenient options that will be available for that purpose soon.

## Platform limitations

Here's a list of the limitations you should be aware of when using this library (on top of regular LibGDX backend limitations).

- __[Android]__ Due to NDK specs, __Android 4.1 (API 16) is the minimum supported version__.
- __[GWT]__ WebAssembly is available pretty much on every modern browser ([compatibility chart](https://caniuse.com/wasm)). Just for reference, the support is enabled by default as of __Firefox 52__, __Chrome 57__, __Opera 44__, and __Edge 16__.

## Texture format notes

Basis Universal texture transcoder supports a bunch of very different GPU compressed texture formats.
Some of them impose very important limitations and cannot be used (cannot be transcoded to on runtime) unless all the requirements are met.

To have the widest possible native format support, it's highly recommended to encode intermediate Basis images that comply with __ALL__ of these specifics.

- __PVRTC1__ requires square textures with the power of two sides.
- __PVRTC1__ transparency is very poor and only suitable for pre-multiplied alpha (check that).
- __BC1_RGB__ & __BC3_RGBA__ require textures with sides to be multiple of four (superseded by PVRTC1 requirements).

> To round up, always use square images with the power of two dimensions for Basis texture assets.

## Using the library

Once added as Maven dependency to LibGDX project modules, the library is pretty easy to deal with, no extra integration calls are required in the code.

All the official LibGDX backends are fully supported.

### Connecting dependencies (Gradle)

The release and snapshot Maven artifacts are available on Maven Central repository
```gradle
buildscript {
    repositories {
        mavenCentral()
        maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' } // <-- optional, for snapshot versions
    }
}
```

And then just add these records to the dependency section of your `build.gradle` files. 

> Don't forget to set `gdxBasisuVersion` property with the correct library version 
(e.g. declaring `gdxBasisuVersion=0.1.0` in the project's root `settings.gradle` file).

#### Core module
```gradle
dependencies {
    api "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion"
    api "com.crashinvaders.basisu:basisu-gdx:$gdxBasisuVersion"
}
```

#### Desktop module ([LWJGL](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backend-lwjgl), [LWJGL3](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backend-lwjgl3), [Headless](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backend-headless) backends)
```gradle
dependencies {
    runtimeOnly "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-desktop"
}
```

#### Android module ([Android](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backend-android) backend)
```gradle
dependencies {
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-armeabi-v7a"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-arm64-v8a"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-x86"
    natives "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-x86_64"
}
```

#### iOS module ([RoboVM](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backend-robovm) backend)
```gradle
dependencies {
    runtimeOnly "com.crashinvaders.basisu:basisu-wrapper:$gdxBasisuVersion:natives-ios"
}
```

#### Web module ([GWT](https://github.com/libgdx/libgdx/tree/master/backends/gdx-backends-gwt) backend)

As usual, GWT backed requires a bit more dance around.
You need to declare an extra dependency and the sources for all the used jars.
```gradle
dependencies {
    implementation "com.crashinvaders.basisu:basisu-gdx-gwt"
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

The library provides transparent support for `Basis` format textures using `BasisuTextureData` class, which acts very similar to LibGDX's implementation of `ETC1TextureData`. The only difference is LibGDX doesn't know how to load `.basis` texture files out of the box, so you have to explicitly use the proper texture data class when creating a texture.

```java
Texture myTexture = new Texture(new BasisuTextureData(Gdx.files.internal("MyTexture.basis")));
```

From now on, it's safe to use the texture instance as usual and it's already should hold the data transcoded to the best suited native GPU compressed texture format for your platform.

If you're using `AssetManager` to load game textures, you can easily integrate with it as well using `BasisuTextureLoader` class.

```java
// Register the texture loader for the ".basis" file extension.
assetManager.setLoader(Texture.class, ".basis", new BasisuTextureLoader(assetManager.getFileHandleResolver()));
// ...
// Post your texture assets for loading as usual.
assetManager.load("MyTexture.basis", Texture.class);
// ...
// When the asset manager has finished loading, retrieve your texture as usual.
Texture myTexture = assetManager.get("MyTexture.basis", Texture.class);
```

## Native dependencies

The project is using [Basis Universal C/C++ code](https://github.com/BinomialLLC/basis_universal) and [JNI](https://en.wikipedia.org/wiki/Java_Native_Interface) wrappers to connect to LibGDX cross-platform code. [basisu-wrapper](basisu-wrapper) module provides a pure Java (no dependencies) abstraction layer over the native libs.

Read more about the module and the native library building notes on [the module's page](basisu-wrapper).
