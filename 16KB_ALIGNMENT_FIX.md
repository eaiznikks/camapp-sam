# 16 KB Page Size Alignment Fix

## Overview
Android devices are increasingly moving to 16 KB page size support. The warning about "APK and ELF alignment checks failed" indicates that some native libraries in your app's dependencies are not optimized for this newer page size.

## The Warning
The app contains native libraries (like `libimage_processing_util_jni.so`) that are not 16 KB aligned. While the app will still run in "pagesize compatible mode," it's recommended to rebuild for full compatibility.

## Solution Applied
The following changes were made to `app/build.gradle.kts`:

### 1. Added NDK Version Configuration
```kotlin
ndkVersion = "26.1.10909125"
```
This ensures the Android NDK (Native Development Kit) version used supports 16 KB alignment.

### 2. Added ABI Filters
```kotlin
ndk {
    abiFilters.add("arm64-v8a")
}
```
This ensures the app targets ARM64-v8a architecture, which properly supports 16 KB alignment.

### 3. Added Packaging Options
```kotlin
packagingOptions {
    // Preserve 16 KB alignment for native libraries
    pickFirst("lib/arm64-v8a/libimage_processing_util_jni.so")
}
```
This configuration helps preserve 16 KB alignment for native libraries during packaging.

## What This Means for You

### Current Status
- ✅ App runs successfully in "pagesize compatible mode"
- ⚠️ Will show compatibility warning on first installation

### After Rebuild
- Rebuild the APK with these changes using: `./gradlew clean buildRelease`
- The warning should be eliminated
- Better compatibility with upcoming Android devices using 16 KB page sizes
- Improved performance on 16 KB page size devices

## Best Practices Going Forward

1. **Keep NDK Updated**: The NDK version should be updated periodically to ensure latest optimizations
2. **Rebuild Dependencies**: If your dependencies have newer versions, update them to versions with 16 KB support
3. **Test on 16 KB Devices**: If possible, test on devices with 16 KB page size support

## Technical Details

16 KB page size support is important because:
- Traditional Android devices use 4 KB page sizes
- Newer devices (especially newer Snapdragon SoCs) use 16 KB page sizes
- Proper alignment ensures:
  - Better memory efficiency
  - Reduced fragmentation
  - Improved security
  - Faster load times

## References
- [Android 16 KB Page Size Support](https://developer.android.com/16kb-page-size)
- [Android NDK Documentation](https://developer.android.com/ndk)
