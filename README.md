# DevHatch

A tiny, single-purpose Android app that flips two `Settings.Global` flags and
deep-links into the wireless-debugging screen — surfaced as a home-screen
widget (and a status/fallback screen). It removes the 7-tap "re-enable
Developer options" ritual needed after turning dev options off so Indian
banking/UPI apps behave.

Full rationale, decisions, and roads-not-taken are in [`docs/design.md`](docs/design.md).

## What it does

| Flag | `Settings.Global` key | Toggled by |
|---|---|---|
| Developer options | `development_settings_enabled` | "Dev options" cell / button |
| Wireless debugging | `adb_wifi_enabled` | "ADB Wi-Fi" cell / button |

- **Bank-safe off** — one action sets **both** flags to `0`, leaving the device
  in the exact state manual toggling would.
- **Pair** — ensures wireless debugging is on, then jumps to the
  wireless-debugging/pairing screen (best-effort component, falling back to the
  Developer options screen).

## Trust surface

- Declares **only** `WRITE_SECURE_SETTINGS`. **No `INTERNET`** — it physically
  cannot phone home.
- No third-party runtime dependencies beyond AndroidX `core-ktx`.
- No Compose, no accessibility service, no overlays (the categories banking
  apps hunt for). Plain Views.

## Build

Requires the Android SDK (platform 35) and JDK 17. From the project root:

```sh
./gradlew :app:assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

For a signed release build, follow the keystore steps in
`~/Personal/MVault/References/android-scripts-helper.sh` (creates
`keystore.properties`), then `./gradlew :app:assembleRelease`.

## Install & one-time grant

Install the app, then grant the permission once over adb (needs USB debugging
on the device). `WRITE_SECURE_SETTINGS` is signature-level, so there is no
runtime prompt:

```sh
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.mahesh.devhatch android.permission.WRITE_SECURE_SETTINGS
# multiple devices: adb -s <serial> shell pm grant com.mahesh.devhatch android.permission.WRITE_SECURE_SETTINGS
```

The grant **survives reboots and app updates**; it is lost only on
uninstall + reinstall. The app's status screen displays this exact command
(with a Copy button) so you are never stuck after a reinstall.

Then add the **DevHatch** widget from your launcher's widget picker.

## Decisions baked in (see `docs/design.md` §10)

- Package: `com.mahesh.devhatch`
- `minSdk 26` (adaptive icons; avoids legacy icon bitmaps), `targetSdk 35`
- Status screen in plain Views (lighter on a 4 GB device)
- Quick Settings tiles: **deferred** (widget is the priority)
- Pairing deep link: the dedicated Wireless Debugging screen is not launchable
  by a third-party app on My UX (its host `SubSettings` is not exported), so
  "Pair" opens **Developer options highlighting the Wireless debugging row**
  (one tap from the sub-screen) via the `:settings:fragment_args_key` extra.
  It routes through an invisible `PairingActivity` trampoline to avoid
  background-activity-launch limits. See `docs/design.md` §10 for the full
  on-device investigation.

## Layout

```
app/src/main/
├── AndroidManifest.xml              only WRITE_SECURE_SETTINGS
├── java/com/mahesh/devhatch/
│   ├── SettingsWriter.kt            read/write the two flags + permission guard
│   ├── ToggleReceiver.kt            handles widget toggle taps, writes flags
│   ├── PairingLauncher.kt           opens Dev options highlighting wireless debug
│   ├── PairingActivity.kt           invisible trampoline for the "Pair" tap
│   ├── DevHatchWidget.kt            AppWidgetProvider, live-state rendering
│   └── MainActivity.kt              status + manual fallback + grant command
└── res/
    ├── layout/activity_main.xml, widget_devhatch.xml
    ├── xml/devhatch_widget_info.xml
    └── drawable, mipmap, values
```
