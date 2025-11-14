# Basis Universal wrapper
Provides a pure Java (no dependencies) abstraction layer over the native libs.

The module uses [Basis Universal C/C++ code](https://github.com/BinomialLLC/basis_universal) 
and [JNI](https://en.wikipedia.org/wiki/Java_Native_Interface) wrappers to connect to libGDX cross-platform code.

To manage native build configuration for all the platforms (except for the web, [read notes below](#Web)) we use [jnigen](https://github.com/libGDX/gdx-jnigen) Gradle plugin.
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
Install required packages onto the x64 based system. Example for Debian/Ubuntu:
```shell
sudo apt install ant gcc-arm-linux-gnueabihf g++-arm-linux-gnueabihf gcc-aarch64-linux-gnu g++-aarch64-linux-gnu
```
> The dependencies are for Debian/Ubuntu packages, but their other package system counterparts should be available pretty much everywhere.
> `gcc-arm-linux-gnueabihf` (or similar) is required to build for Linux ARM32 target. 
> `gcc-aarch64-linux-gnu` (or similar) is required to build for Linux ARM64 target. 

You can also build Windows natives from under Linux. Install the mingw64 package.
```shell
sudo apt install g++-mingw-w64-x86-64
```

#### Windows
> All further steps consider you have [Chocolatey](https://chocolatey.org/) installed on the system.
- Install __Ant__
    1. `choco install ant`
    2. Add ant's bin dir to the `PATH` (_Chocolatey_ creates an `ant.exe` shortcut file, but __jnigen__ expects `ant.bat` to be on the `PATH`).
- To build for Win64 target
    1. Download and unpack [MinGW64](https://github.com/niXman/mingw-builds-binaries/releases/download/13.1.0-rt_v11-rev1/x86_64-13.1.0-release-posix-seh-msvcrt-rt_v11-rev1.7z) or the latest release. It's important to download `posix` version of MinGW distribution.
    2. Make sure `g++.exe`, `gcc.exe`, `strip.exe`, and `ar.exe` are discoverable from the `PATH`

#### macOS & iOS
Nothing special is required, you only need the latest XCode (install from App Store) along with JDK and Ant.
```shell
brew install temurin ant
```

#### Android
> The build is configured and tested for NDK 29.0.14206865 and may not work with any other version. 
> It's recommended that you install and use this exact NDK release.
- Install the NDK (using `sdkmanager` or IntelliJ Idea/Android Studio's Android SDK manager or from [the official web page](https://developer.android.com/ndk/downloads)).
- Create an environment variable `NDK_HOME` pointing to the installed NDK location.

> Follow the steps from [this answer](https://stackoverflow.com/a/62914315/3802890) to install `sdkmanager` on a Debian based Linux system.
```shell
sdkmanager --install "ndk;29.0.14206865" 
export NDK_HOME='/usr/lib/android-sdk/ndk/29.0.14206865'
```

#### Web
We use [Emscripten](https://emscripten.org/) to compile C/C++ code to WASM/JS and then connect it through [JSNI](http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJSNI.html) to the libGDX GWT backend.

It's important to note, that we don't use __jnigen__ to configure [the Ant build script for web natives](jni/build-web.xml), __jnigen__ doesn't support this platform.
And thus it's all handwritten and needs to be kept in sync with jnigen configuration for other platforms in `build.gradle`.

> The build is configured and tested with Emscripten SDK 3.1.46.

To be able to assemble natives for this platform you need to:
- Install the [Emscripten SDK](https://emscripten.org/docs/getting_started/downloads.html#installation-instructions-using-the-emsdk-recommended)
- Make sure the Emscripten compiler executables (__emcc__ and __em++__) are available on the system `PATH`. Usually, they are located at `${EMSDK}/upstream/emscripten`.
