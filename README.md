# VideoVoid Pro

Phone-only Android video downloader UI using yt-dlp through youtubedl-android.

## Included
- Black-hole style home screen
- Heart download button with click animation
- Clipboard URL detection
- Source detection
- Best / 1080p / 720p / 480p / Audio quality choices
- Real yt-dlp progress displayed as a monotonic progress bar
- Downloads saved under `Download/VideoVoid`
- GitHub Actions workflow that builds a debug APK

## Important
This app relies on the current yt-dlp extractors, so no downloader can promise that every website will work forever. Private, DRM-protected, login-required, unavailable, or anti-bot protected media may fail. The app does not remove watermarks from source files and does not bypass DRM or access controls. Use it only for content you are allowed to download and in accordance with each service's terms.

## Build
The GitHub Actions workflow builds `app-debug.apk` and publishes it as an Actions artifact.
