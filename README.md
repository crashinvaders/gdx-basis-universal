##Build requirements

Some things besides JDK and Gradle to build the project. 

###Windows
- ant `choco install ant`
- MinGW-w64 `choco install msys64`


##Notes

`basis_transcoder.h` requires `#include <stddef.h>` on Win32 for some reason.