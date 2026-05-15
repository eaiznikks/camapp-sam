Build a complete Android Studio Kotlin project for a Samsung TV photo-sharing MVP.

Goal:
When the user captures a photo on the Android phone, the latest image should appear automatically on a Samsung TV browser over the same Wi-Fi network.

Very important:
- I only have Android Studio.
- Do NOT use Tizen Studio.
- Do NOT use Firebase, cloud storage, WebRTC, casting SDKs, or any external backend.
- Keep everything simple, beginner friendly, and compile-safe.
- This is a one-day proof of concept.
- I want code that is practical and handles basic edge cases sensibly.
- Do not give pseudocode. Give real code only.
- Avoid unnecessary abstraction.
- Make sure the project can compile with minimal fixes.

Architecture:
- Android app captures image using CameraX.
- Android app saves image as latest.jpg.
- Android app runs a local HTTP server on port 8080.
- Android app serves:
  - /latest.jpg -> latest image
  - /viewer -> fullscreen webpage for Samsung TV browser
- Samsung TV browser opens:
  - http://PHONE_IP:8080/viewer
- The webpage auto-refreshes the image every 1 second.

Android app requirements:
- Kotlin
- Compatible with Android Studio
- Single activity app
- Use CameraX for preview and image capture
- Show camera preview
- Add a capture button
- Save each captured image as latest.jpg and overwrite the previous one
- Show the local phone IP and port on screen
- Start the local HTTP server automatically after app launch
- Use NanoHTTPD as the embedded server
- Serve latest image over HTTP
- Serve an HTML webpage for the TV viewer
- Add cache busting using a timestamp query parameter
- Add all required Android permissions
- Allow cleartext HTTP traffic if required
- Include all imports and all Gradle dependencies
- Use stable dependency versions
- Prefer Jetpack Compose only if it is simpler and safer. Otherwise use XML.
- Include exact folder structure and exact file placement

Required endpoints:
1. GET /latest.jpg
   - Return the latest captured image
   - If no image exists yet, return a clear and safe placeholder response instead of crashing

2. GET /viewer
   - Return a fullscreen HTML page for the TV browser
   - The page should load the image from /latest.jpg and refresh it every second

Viewer page requirements:
- Black background
- Fullscreen image
- Auto refresh every 1 second
- Use cache busting like:
  latest.jpg?t=Date.now()
- Responsive for TV screen
- No external dependencies

Common sense edge cases the code must handle:
- Camera permission denied
- Storage or file write failure
- HTTP server fails to start
- Port 8080 already in use
- No image captured yet
- Empty or missing latest.jpg file
- Phone IP cannot be determined
- Wi-Fi disconnected
- Cleartext HTTP blocked on Android
- TV browser caching the image too aggressively
- Image capture failing because the camera is unavailable
- App reopening after process death or rotation
- Server thread cleanup when activity stops or closes
- Avoid crashing on null values or missing files

Implementation expectations:
- Use defensive coding
- Wrap risky operations in try/catch where appropriate
- Show user-friendly error text in the UI when something fails
- Log important failures clearly
- Keep the code minimal but reliable
- Do not assume perfect conditions
- Make sure functions return safe fallback values where possible
- Do not hard crash if the network or camera is unavailable

Generate complete Android project files including:
- build.gradle
- settings.gradle if needed
- AndroidManifest.xml
- MainActivity.kt
- CameraX setup code
- NanoHTTPD server class
- IP address helper
- viewer HTML generation
- all imports
- all dependencies
- layout XML or Compose code
- any helper classes needed

Also include:
- exact step-by-step setup instructions
- how to run the app
- how to test http://PHONE_IP:8080/latest.jpg from a laptop browser first
- how to open http://PHONE_IP:8080/viewer on Samsung TV browser
- how to troubleshoot:
  - same Wi-Fi problems
  - image not refreshing
  - no image yet
  - permission issues
  - network security issues
  - cache issues
  - server startup issues

Output format:
1. First give a very short explanation of the chosen approach.
2. Then give the exact folder structure.
3. Then give the full code for each file.
4. Then give the run steps.
5. Then give the troubleshooting checklist.

Important:
- Make it copy-paste ready.
- Make it compile-friendly.
- Make it robust enough for a demo with basic edge cases handled.
- If there are multiple ways to do something, choose the simplest way that is most likely to work fast.
