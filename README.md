<p align="center">
  <img src="assets/preview_3.png" alt="Droidbar Preview" width="100%" />
  <img src="assets/preview_2.png" alt="Droidbubble Preview" width="100%" />
</p>

# Droidtools

<p align="center">
  <a href="https://github.com/Velocity081/droidtools/releases">
    <img src="https://img.shields.io/github/v/release/Velocity081/droidtools?include_prereleases&style=flat-square&label=Release" alt="Release" />
  </a>
  <img src="https://img.shields.io/badge/Platform-Android%2013%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
  <a href="https://github.com/Velocity081/droidtools/releases/latest/download/Droidtools.apk">
    <img src="https://img.shields.io/badge/Download-APK-2ea44f?style=flat-square" alt="Download APK" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License" />
  </a>
</p>

---

A small collection of overlay tools for Android. Built natively with Kotlin, Jetpack Compose and Material 3.

Right now it has two things in it:

- **Droidbubble**: a dynamic island that wraps around your front camera.
- **Droidbar**: a little bar on the right edge of your screen for quick toggles and apps.

It's free, open source, has no ads, and doesn't track you in the background.

---

## Droidbubble

A dynamic island for Android. It sits quietly in your camera cutout and pops out when something happens.

- **Now playing**: album art, track title, seek bar and playback controls, with a blurred background that matches the artwork.
- **System events**: see charging wattage, ringer mode changes and other status stuff as it happens.
- **Notifications**: heads-up alerts you can expand, and reply to without opening the app.
- **Idle**: when nothing's going on, it tucks itself into the cutout and stays out of the way.
- **Make it yours**: tweak position, size, spring feel, font, and the order of the layout layers.

## Droidbar

A slim bar on the right edge of the screen. Tap it to open, tap again to close.

- **Quick toggles**: flashlight, ring/vibrate, Wi-Fi, mobile data and screenshot.
- **Apps**: jump to the apps you use most, or pin your own.
- Dark, with a bit of spring and haptic feedback so it feels good to use.

---

## What you need

- Android 13 or newer
- A few permissions, since this is an overlay app:
  - **Display over other apps**, to draw on top of everything
  - **Accessibility service**, for edge touches and actions like screenshots
  - **Notification access**, to read what's playing and show incoming alerts

Nothing leaves your phone.

---

## License

MIT. See [LICENSE](LICENSE).
