##Build requirements

Some things besides JDK and Gradle to build the project. 

###Windows
- Install ant
    1. `choco install ant`
    2. Add ant's bin dir to the PATH (chocolatey creates an `ant.exe` shortcut file, but jnigen expects `ant.bat` to be on the PATH).
- Install MinGW-w64
    1. `choco install msys64`


##Notes

`basis_transcoder.h` requires `#include <stddef.h>` on Win32 for some reason.