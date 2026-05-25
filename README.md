# Samsung TV Photo Sharing MVP

Android Studio Kotlin proof-of-concept: capture a photo on an Android phone and show the latest photo on a Samsung TV browser over the same Wi-Fi.

## MVP behavior

1. Android phone runs the app.
2. App starts a local HTTP server on port `8080`.
3. App shows the TV URL: `http://PHONE_IP:8080/viewer`.
4. User taps **Capture and Send to TV**.
5. App saves the newest capture as `latest.jpg` in app cache.
6. Samsung TV browser displays `/viewer` and auto-refreshes `/latest.jpg?t=Date.now()` every second.

No cloud, no Firebase, no WebRTC, no Tizen Studio, no Samsung casting SDK, no account login.

## Corporate-safe dependency policy

This repo is intentionally simple for office/corporate networks:

- Clone over **HTTPS**, not SSH.
- No private Git dependencies.
- No Git submodules.
- No external backend.
- No custom package registries.
- Dependencies come only from standard Gradle repositories:
  - `google()`
  - `mavenCentral()`
  - `gradlePluginPortal()` for Android/Kotlin plugins
- Main open-source/runtime dependencies:
  - AndroidX CameraX
  - NanoHTTPD
  - AndroidX/AppCompat/Material/ConstraintLayout

Recommended clone command:

```bash
git clone https://github.com/aiznikks/camapp-sam.git
```

Avoid this in office networks if SSH is blocked:

```bash
git clone git@github.com:aiznikks/camapp-sam.git
```

## Requirements

- Android Studio with JDK 17 support
- Android SDK Platform 35 installed
- Android phone, API 24+
- Samsung TV browser
- Phone and TV on the same Wi-Fi/LAN

## Key files

```text
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/example/camerasamsungapp/MainActivity.kt
app/src/main/java/com/example/camerasamsungapp/PhotoServer.kt
app/src/main/java/com/example/camerasamsungapp/PhotoServerThread.kt
app/src/main/java/com/example/camerasamsungapp/IpAddressHelper.kt
app/src/main/java/com/example/camerasamsungapp/ViewerHtml.kt
app/src/main/res/layout/activity_main.xml
app/src/main/res/values/strings.xml
app/src/main/res/xml/network_security_config.xml
app/src/test/java/com/example/camerasamsungapp/ViewerHtmlTest.kt
```

## Run from Android Studio

1. Open Android Studio.
2. Choose **File → Open** and select this repo folder.
3. Let Gradle sync.
4. If Android Studio asks to install SDK 35/build tools, accept.
5. Connect an Android phone with USB debugging enabled.
6. Press **Run**.
7. Grant camera permission.
8. Confirm the app shows:
   - server running on port `8080`
   - TV browser URL
   - camera preview

## Test before using Samsung TV

Always test from a laptop/phone browser first.

Replace `PHONE_IP` with the IP shown in the app:

```text
http://PHONE_IP:8080/status
http://PHONE_IP:8080/viewer
http://PHONE_IP:8080/latest.jpg
```

Expected:

- `/status` returns JSON with `server: ok`.
- `/viewer` opens a black fullscreen viewer page.
- `/latest.jpg` returns a transparent placeholder before first capture, then the latest photo.

## Use on Samsung TV

1. Keep phone app open.
2. Keep phone and TV on the same Wi-Fi.
3. Open Samsung TV browser.
4. Open:

```text
http://PHONE_IP:8080/viewer
```

5. Capture a photo on the phone.
6. TV should update within 1 second.

## Troubleshooting

### Gradle clone/sync fails with SSH error

Use HTTPS clone only:

```bash
git clone https://github.com/aiznikks/camapp-sam.git
```

This project does not require SSH for dependencies.

### Gradle cannot download dependencies

Corporate firewall/proxy may block Gradle/Maven downloads. Ask IT to allow:

- `https://services.gradle.org`
- `https://plugins.gradle.org`
- `https://dl.google.com/dl/android/maven2/`
- `https://repo.maven.apache.org/maven2/`

### Phone URL does not open from laptop/TV

Check:

- Phone and TV are on the same Wi-Fi.
- Phone is not on mobile hotspot/client isolation network.
- VPN is off on phone/laptop.
- Try `/status` first.
- Use `http://`, not `https://`.

### TV page opens but image does not update

- Capture a fresh photo.
- Reload the TV browser page.
- Confirm `/latest.jpg` works from laptop.
- The viewer uses `/latest.jpg?t=Date.now()` to avoid TV browser cache.

### Server failed to start

- Port `8080` may already be in use.
- Force stop the app and reopen it.
- Restart the phone if needed.

### Camera does not work

- Grant camera permission.
- Close other camera apps.
- Try a real phone, not emulator, for the final demo.

## Design choices

- Saves only one latest image: `latest.jpg`.
- Does not save to phone gallery.
- Uses app cache to avoid Android storage permission issues.
- Uses plain HTTP because Samsung TV browser only needs local LAN access.
- Uses relative image URL `/latest.jpg`, not `localhost`, because on TV `localhost` means the TV itself.

## License

Use internally as a proof-of-concept. Add your company-approved license before wider distribution.
