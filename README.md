## Build requirements

Some things besides JDK and Gradle to build the project. 

### Windows
> All further steps consider you have [Chocolatey](https://chocolatey.org/) installed on the system.

- Install ant
    1. `choco install ant`
    2. Add ant's bin dir to the PATH (chocolatey creates an `ant.exe` shortcut file, but jnigen expects `ant.bat` to be on the PATH).
- Install MinGW-w64
    1. `choco install msys64`

### Linux
- To build for Linux ARM32 target, `g++-arm-linux-gnueabihf` [package](https://packages.debian.org/stretch/g++-arm-linux-gnueabihf) (or similar) should be installed. 
- To build for Linux ARM64 target, `g++-aarch64-linux-gnu` [package](https://packages.debian.org/stretch/g++-aarch64-linux-gnu) (or similar) should be installed. 

## Notes

- `basis_transcoder.h` requires `#include <stddef.h>` on Win32 for some reason.
- WebAssembly support is enabled by default as of Firefox 52, Chrome 57 and Opera 44. On Edge 15 you can enable it via “Experimental JavaScript Features” flag.


## Recommendations

- PVRTC1 requires square textures with power of two sides.
- PVRTC1 transparency is very poor and only suitable for pre-multiplied alpha (check that).
- BC1_RGB & BC3_RGBA require textures with sides to be multiple of four (superseded by PVRTC1 requirements).
