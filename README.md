# GDX Basis Universal

The library provides support for Binomial's [Basis Universal](https://github.com/BinomialLLC/basis_universal) portable super-compressed GPU textures.

It allows you to use the same [compressed texture](https://en.wikipedia.org/wiki/Texture_compression) assets (`.basis`) for all the LibGDX backends while saving tons of GPU RAM on runtime by using platforms' nativelly supported GPU compression.

_Work in progress. The first beta release is on the way..._

## Library limitations

Here's a list of the limitations you should be aware of when using this library.

- __[Android]__ Due to NDK specs, __the minimum supported Android SDK version code is 16__.
- __[GWT]__ WebAssembly support is enabled by default as of __Firefox 52, Chrome 57__ and __Opera 44__. On Edge 15 you can enable it via “Experimental JavaScript Features” flag.


## Texture format notes

- PVRTC1 requires square textures with power of two sides.
- PVRTC1 transparency is very poor and only suitable for pre-multiplied alpha (check that).
- BC1_RGB & BC3_RGBA require textures with sides to be multiple of four (superseded by PVRTC1 requirements).

## Native build requirements

Some things besides JDK and Gradle to build the natives libs.

First and foremost, make sure you have all the sub-repos cloned (including Basis Universal).
From the project's root, use this command to recursively pull everything: 
```
git submodule update --init --recursive
```

### Linux
- To build for Linux ARM32 target, `g++-arm-linux-gnueabihf` [package](https://packages.debian.org/stretch/g++-arm-linux-gnueabihf) (or similar) should be installed. 
- To build for Linux ARM64 target, `g++-aarch64-linux-gnu` [package](https://packages.debian.org/stretch/g++-aarch64-linux-gnu) (or similar) should be installed. 

### Windows
> All further steps consider you have [Chocolatey](https://chocolatey.org/) installed on the system.
- Install __Ant__
    1. `choco install ant`
    2. Add ant's bin dir to the `PATH` (chocolatey creates an `ant.exe` shortcut file, but jnigen expects `ant.bat` to be on the PATH).
- To build for Win64 target
    1. `choco install msys2`
- To build for Win32 target
    1. Go to [MinGW32](https://sourceforge.net/projects/mingw/files/Installer/) distribution page and get the installer from there.
    2. Make sure that `g++.exe`, `gcc.exe`, `strip.exe` and `ar.exe` are discoverable from the `PATH`.

### Android
> The build was only tested with NDK 21.3.6528147 and may not work with any other version. It's recommended you install and use the same one.
- Install the NDK (using IntelliJ Idea/Android Studio's Android SDK manager or from [the official web page](https://developer.android.com/ndk/downloads)).
- Create an environment variable `NDK_HOME` pointing to the installed NDK location.
