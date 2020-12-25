## Build requirements

Some things besides JDK and Gradle to build the project. 

### Windows
- Install __Ant__
    1. `choco install ant`
    2. Add ant's bin dir to the `PATH` (chocolatey creates an `ant.exe` shortcut file, but jnigen expects `ant.bat` to be on the PATH).
- To build for Win64 target
    1. `choco install msys2`
- To build for Win32 target
    1. Go to [MinGW32](https://sourceforge.net/projects/mingw/files/Installer/) distribution page and get the installer from there.
    2. Make sure that `g++.exe`, `gcc.exe`, `strip.exe` and `ar.exe` are discoverable from the `PATH`.


## Notes

- `basis_transcoder.h` requires `#include <stddef.h>` on Win32 for some reason.
- WebAssembly support is enabled by default as of Firefox 52, Chrome 57 and Opera 44. On Edge 15 you can enable it via “Experimental JavaScript Features” flag.


## Recomendations

- PVRTC1 requires square textures with power of two sides.
- PVRTC1 transparency is very poor and only suitable for pre-multiplied alpha (check that).
- BC1_RGB & BC3_RGBA require textures with sides to be multiple of four (superseded by PVRTC1 requirements).
