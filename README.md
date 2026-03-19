# Sudoku Minimal Template (Privacy-First)

This project is a minimal Android Sudoku template using Kotlin with a shared pure Kotlin game module.

## Design goals

- Android app that can be installed directly on your phone
- Minimal dependencies
- No tracking SDKs, no ads, no cloud/account requirements
- Shared game logic (`core-game`) ready for future iOS reuse

## Modules

- `app`: Android app module (Activity + XML UI)
- `core-game`: Pure Kotlin module with Sudoku generation logic

## Privacy defaults

- No network code
- No analytics/crash/ad SDK dependencies
- `AndroidManifest.xml` does not request internet permission

## Quick checks

Run unit tests for shared game logic:

```bash
./gradlew :core-game:test
```

Run the console preview of generated board:

```bash
./gradlew :core-game:runPreview
```

Build debug APK (requires Android SDK configured on your machine):

```bash
./gradlew :app:assembleDebug
```

Install on connected phone:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).


