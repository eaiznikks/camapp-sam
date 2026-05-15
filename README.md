# Samsung TV Photo Sharing MVP

A simple Android app that captures photos and streams them live to a Samsung TV browser on the same Wi-Fi network.

## How It Works

1. **Android Phone**: Runs a local HTTP server on port 8080
2. **Captures Photos**: Uses CameraX to capture images and save as `latest.jpg`
3. **Serves to TV**: Browser on Samsung TV accesses `http://PHONE_IP:8080/viewer`
4. **Auto-Refresh**: Webpage refreshes image every 1 second with cache busting

## Project Structure

```
camera-samsungapp/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    ├── src/main/
    │   ├── AndroidManifest.xml
    │   ├── java/com/example/camerasamsungapp/
    │   │   ├── MainActivity.kt
    │   │   ├── PhotoServer.kt
    │   │   ├── PhotoServerThread.kt
    │   │   ├── IpAddressHelper.kt
    │   │   └── HtmlViewerGenerator.kt
    │   └── res/
    │       ├── layout/
    │       │   └── activity_main.xml
    │       ├── values/
    │       │   ├── strings.xml
    │       │   └── colors.xml
    │       ├── xml/
    │       │   ├── network_security_config.xml
    │       │   ├── data_extraction_rules.xml
    │       │   └── backup_rules.xml
    │       ├── drawable/
    │       │   └── ic_launcher_foreground.xml
    │       └── mipmap-anydpi-v33/
    │           └── ic_launcher.xml
```

## Setup Instructions

### Prerequisites
- Android Studio (latest)
- Android phone with API 24+ (Android 7.0+)
- Samsung TV with Wi-Fi and web browser
- Both devices on the same Wi-Fi network

### Build & Run

1. **Open the project in Android Studio**
   - File → Open → Select `camera-samsungapp` folder

2. **Sync Gradle**
   - Wait for all dependencies to download
   - Should complete without errors

3. **Connect your Android device**
   - Enable USB Debugging on your phone
   - Or create an AVD emulator in Android Studio

4. **Run the app**
   - Click "Run" (green play button) or Shift+F10
   - Grant camera permissions when prompted
   - You should see:
     - Camera preview on screen
     - "Server: Running on port 8080 ✓" (green text)
     - Your phone's Wi-Fi IP address (e.g., `IP: 192.168.0.100:8080`)

### Test from Phone Browser

1. Open your phone's browser
2. Navigate to `http://localhost:8080/latest.jpg`
3. You should see a placeholder image (if no photo taken yet)

### Test from Laptop

1. Find your phone's IP from the app (e.g., `192.168.0.100`)
2. Open laptop browser
3. Test endpoints:
   - Image: `http://192.168.0.100:8080/latest.jpg` (or placeholder if no image yet)
   - Viewer: `http://192.168.0.100:8080/viewer` (fullscreen viewer page)

### Set Up on Samsung TV

1. **On your phone**: Tap "CAPTURE PHOTO" to capture an image
2. **On TV**: Open web browser
3. Navigate to `http://YOUR_PHONE_IP:8080/viewer`
   - Replace `YOUR_PHONE_IP` with your actual phone IP (e.g., `192.168.0.100`)
4. You should see:
   - Black fullscreen background
   - Your captured photo centered
   - Image auto-refreshes every 1 second
5. Each time you capture on your phone, the TV updates within 1 second

## Usage

1. Open the app on your Android phone
2. Point camera at subject
3. Tap **CAPTURE PHOTO** button
4. Image saves and is served to the HTTP server
5. TV browser auto-refreshes and shows new image

## Features Included

✅ CameraX camera preview  
✅ High-quality image capture  
✅ Local HTTP server (NanoHTTPD)  
✅ Auto-refresh with cache busting (cache-busting query params)  
✅ Shows phone IP address  
✅ Shows server status  
✅ Responsive for TV screen  
✅ Black background for TV display  
✅ Error handling and edge cases  
✅ Cleartext HTTP support  
✅ Permission handling (runtime permissions)  
✅ Defensive coding (null checks, try/catch)  

## Troubleshooting

### "Server: Failed to start server"

**Cause 1: Port 8080 already in use**
- Solution: Close other apps using port 8080 or restart your phone
- Or: Check if previous app instance is still running (kill it from Settings → Apps)

**Cause 2: Cleartext HTTP blocked**
- Solution: Already handled in code via `network_security_config.xml`
- Check Android logs: `adb logcat | grep PhotoServer`

### "IP: Not connected to Wi-Fi"

- Check that phone is on the same Wi-Fi network as TV
- Ensure Wi-Fi is enabled and connected (check Wi-Fi icon in status bar)
- Try disconnecting and reconnecting to Wi-Fi
- Restart the app

### Image not refreshing on TV

**Cause 1: Image cache in TV browser**
- Solution: Hard refresh the page (Ctrl+Shift+R or Cmd+Shift+R)
- Or: Reload by navigating back and forward in browser history

**Cause 2: No new image captured**
- Solution: Tap "CAPTURE PHOTO" on your phone
- Check status text on phone for "Photo captured!"

**Cause 3: Network connectivity issue**
- Solution: Verify both devices are on same Wi-Fi (not different 2.4GHz/5GHz bands)
- Test: Ping phone IP from laptop: `ping 192.168.0.100`

### "Camera permission denied"

- Grant permission when prompt appears
- If already denied: Settings → Apps → CameraSamsungApp → Permissions → Camera → Allow

### No image on first load

- This is normal! Placeholder image shows until first photo is captured
- Tap "CAPTURE PHOTO" on phone to capture first image
- TV will show it within 1 second

### "Tap Capture to take a photo" but button does nothing

**Cause 1: Camera not initialized**
- Wait a few seconds for camera to initialize
- Check logs: `adb logcat | grep MainActivity`

**Cause 2: Camera already in use**
- Close other camera apps (camera, video, social media)
- Restart app

**Cause 3: Camera permission not granted**
- See "Camera permission denied" section above

### TV browser shows "ERR_INVALID_URL" or can't connect

- Verify phone IP is correct (shown in app status)
- Check both devices on same Wi-Fi (use Wi-Fi settings to confirm network)
- Try using full URL with protocol: `http://192.168.0.100:8080/viewer`
- Not: `https://` (only plain HTTP)
- Test connectivity: From laptop, try: `curl http://192.168.0.100:8080/latest.jpg`

### "Camera preview is frozen" or blank

- Restart the app
- Or restart your phone and reopen app
- Check that camera isn't blocked by permissions or access

### Image looks stretched or wrong aspect ratio

- This is TV browser behavior, not app issue
- Some older TV browsers may not handle max-width/max-height properly
- The viewer page uses CSS `object-fit: contain` to preserve aspect ratio

### App crashes on startup

- Check Android version is API 24+ (Android 7.0+)
- Check all dependencies are downloaded (check Gradle sync errors)
- Check permissions are granted
- View logs: `adb logcat` and search for "FATAL" or "Exception"

## Dependencies

- **CameraX** (camera-core, camera-camera2, camera-lifecycle): Modern camera API
- **NanoHTTPD**: Lightweight embedded HTTP server
- **AndroidX**: Android Framework libraries
- **Kotlin**: Programming language

All versions pinned to stable releases for compatibility.

## Architecture

- **Single Activity**: Simple, single-screen UI
- **CameraX**: Modern camera implementation (recommended by Google)
- **NanoHTTPD**: Self-contained HTTP server (no external backend needed)
- **Async Server Thread**: Server runs in background, doesn't block UI
- **Image Cache**: Latest image stored in app's cache directory
- **HTML Viewer**: Pure JavaScript, no external dependencies

## Security Notes

- Server is on local Wi-Fi only (not exposed to internet)
- Port 8080 is standard HTTP, not HTTPS (by design for simplicity)
- Image overwrite behavior: New photos replace old ones (no image history)
- cleartext HTTP is explicitly allowed for local Wi-Fi (network_security_config.xml)

## Future Enhancements (Out of Scope)

- HTTPS support
- Authentication
- Multiple image history/slideshow
- Tizen app for native Samsung TV support
- Cloud backup
- Editing/filters
- Video recording

## License

Open source, for personal use.

## Contact

For issues or questions, check the troubleshooting guide above.
