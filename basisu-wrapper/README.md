# Basis Universal wrapper
Provides a pure Java (no dependencies) abstraction layer over the native libs.

The module uses [Basis Universal C/C++ code](https://github.com/BinomialLLC/basis_universal) 
and [JNI](https://en.wikipedia.org/wiki/Java_Native_Interface) wrappers to connect to LibGDX cross-platform code.

To manage native build configuration for all the platforms (except for the web, [read notes below](#Web)) we use [jnigen](https://github.com/libgdx/gdx-jnigen) Gradle plugin.
See `//region jnigen configuration` section of [`build.gradle`](build.gradle#L28) file for configuration details.

To streamline the native library assembly process, there are a bunch of `assemble-natives-<platform>.[sh|bat]` scripts are available in the project's root dir.
Be aware that some of the ".sh" scripts are OS dependant (read comments in the scripts).

### Native build requirements

> This section isn't complete yet, there might be other important steps missing. 
Please use this information as general notes and not as actual build steps.

Some things besides JDK and Gradle to build the native libs.

First and foremost, make sure you have all the sub-repos cloned (including Basis Universal).
From the project's root, use this command to recursively pull everything: 
```
git submodule update --init --recursive
```

#### Linux
> All references are for Debian packages, but their other package system counterparts should be available pretty much everywhere.
- Install `ant` [package](https://packages.debian.org/stretch/ant).
- Install `g++-arm-linux-gnueabihf` [package](https://packages.debian.org/stretch/g++-arm-linux-gnueabihf) (or similar) to build for Linux ARM32 target. 
- Install `g++-aarch64-linux-gnu` [package](https://packages.debian.org/stretch/g++-aarch64-linux-gnu) (or similar) to build for Linux ARM64 target. 

You can also build Windows natives from under Linux. 
Read [the official LibGDX wiki](https://github.com/libgdx/libgdx/wiki/jnigen#linux) for details.

#### Windows
> All further steps consider you have [Chocolatey](https://chocolatey.org/) installed on the system.
- Install __Ant__
    1. `choco install ant`
    2. Add ant's bin dir to the `PATH` (_Chocolatey__ creates an `ant.exe` shortcut file, but __jnigen__ expects `ant.bat` to be on the `PATH`).
- To build for Win64 target
    1. `choco install msys2`
- To build for Win32 target
    1. Install __MinGW32__. The installers are available from their [distribution page](https://sourceforge.net/projects/mingw/files/Installer/).
    2. Make sure that `g++.exe`, `gcc.exe`, `strip.exe`, and `ar.exe` are discoverable from the `PATH`.

#### macOS
> To be added soon

#### iOS
> To be added soon

#### Android
> The build was only tested with NDK 21.3.6528147 and may not work with any other version. It's recommended you install and use the same one.
- Install the NDK (using IntelliJ Idea/Android Studio's Android SDK manager or from [the official web page](https://developer.android.com/ndk/downloads)).
- Create an environment variable `NDK_HOME` pointing to the installed NDK location.


#### Web
We use [Emscripten](https://emscripten.org/) to compile C/C++ code to WASM/JS and then connect it through [JSNI](http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html) to the LibGDX GWT backend.

It's important to note here that we don't use __jnigen__ for configuring [the Ant build script for web natives](jni/build-web.xml), __jnigen__ doesn't support this platform.
And thus it's all hand-written and needs to be kept in sync with the main configuration in `build.gradle`.

> The build was only tested with Emscripten SDK 2.0.9.

To be able to assemble natives for this platform you need to:
- Install the [Emscripten SDK](https://emscripten.org/docs/getting_started/downloads.html)
- Make sure the Emscripten compiler executables (__emcc__ and __em++__) are available on the system `PATH`. Usually, they are located at `${EMSDK}/upstream/emscripten`.
