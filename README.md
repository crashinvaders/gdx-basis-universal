# GDX Basis Universal

Cross-platform support for Binomial's [Basis Universal](https://github.com/BinomialLLC/basis_universal) portable super-compressed GPU textures.

It allows you to use the same [compressed texture](https://en.wikipedia.org/wiki/Texture_compression) assets (`.basis`) for all the LibGDX backends while saving tons of GPU RAM on runtime by using platforms' natively supported GPU compression.

_Work in progress. The first beta release is on the way..._

## Platform limitations

Here's a list of the limitations you should be aware of when using this library (on top of regular LibGDX backend limitations).

- __[Android]__ Due to NDK specs, __Android 4.1 (API 16) is the minimum supported version__.
- __[GWT]__ [WebAssembly is available](https://caniuse.com/wasm) pretty much on every modern browser. But just for reference, the support is enabled by default as of __Firefox 52, Chrome 57, Opera 44__, and __On Edge 16__.

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
