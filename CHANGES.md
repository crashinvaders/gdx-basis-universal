### 1.1.0
- Updated to libGDX 1.14.0
- Mipmap support for KTX2/Basis textures. See #5 
- Android natives are 16KB page size aligned and is compatible with [the latest Android requirements](https://developer.android.com/guide/practices/page-sizes). See #6

### 1.0.2
- Fixed: failed to load natives on old Android devices (5.1.1 and down). See #2

### 1.0.1
- Updated to libGDX 1.12.1
- Possibly fixed: failed to load natives on old Android devices (5.1.1 and down). See #2

### 1.0.0
- Added: support for KTX2 textures.
- Added: support for decoding ZSTD compressed UASTC textures.
- Improved: better default ETC1S/UASTC transcoder selector.
- Fixed: Basis transcoded texture buffers weren't disposed properly (memleak).

### 0.2.0
- Updated to libGDX 1.12.0
- Updated to Basis Universal 1.16.4
- Fixed: texture atlases with Basis pages fail to load through `AssetManager`. See #1
- Removed: support for Linux x86 and Windows x86 targets (the lib no longer provides native dependencies for these platforms).
- Added: support for macOS arm64 target.

### 0.1.0
- Initial release.