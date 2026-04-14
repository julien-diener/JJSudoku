# Sudoku Minimal Template (Privacy-First)

This project is a minimal Android Sudoku template using Kotlin with a shared pure Kotlin game module.

## Design goals

- Android app that can be installed directly on your phone
- Minimal dependencies
- No tracking SDKs, no ads, no cloud/account requirements
- Shared game logic (`core-game`) ready for future iOS reuse

## Setup / Prerequisites

To build and run this project on your machine, you need:

### Required

1. **Java Development Kit (JDK) 17 or later**
   - Download from https://www.oracle.com/java/technologies/downloads/
   - Or use a package manager: `sudo apt install openjdk-17-jdk` (Linux), `brew install openjdk@17` (macOS)

2. **Android SDK**
   - Download Android Studio from https://developer.android.com/studio
   - Or install just the SDK tools
   - Required API level: 26 (minSdk)

3. **Git**
   - Clone this repository: `git clone https://github.com/julien-diener/JJSudoku.git`

### Configuration

After cloning, create `local.properties` in the project root:

```properties
sdk.dir=/path/to/your/Android/Sdk
```

Replace `/path/to/your/Android/Sdk` with your actual Android SDK location:
- **Linux/macOS**: typically `~/Android/Sdk`
- **Windows**: typically `C:\Users\<YourUser>\AppData\Local\Android\Sdk`

### Optional

- **Android Emulator** (to run the app without a physical phone)
  - Set up via Android Studio AVD Manager
  - Or use a connected Android phone with USB debugging enabled

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

**This project is licensed under GNU General Public License v3.0 (GPL-3.0).**

See [`LICENSE`](LICENSE) and [`COPYING`](COPYING) for full license text.

### Why GPL-3.0?

This project incorporates `hodoku-core`, which is derived from HoDoKu (GPL-3.0-or-later by Bernhard Hobiger).
GPL-3.0 is a **copyleft license** that requires any derivative work to also be GPL-3.0.

**Key implications:**
- You may freely use, modify, and distribute this software
- Any derivative work **must** also be GPL-3.0
- Source code must be provided to end users
- No warranty is provided (see COPYING for full terms)

## Roadmap

- [x] hide cells only if puzzle has still exactly one solution
- [x] improve selection buttons 
  - hide number selection button once all numbers are placed
  - display how many are left on selection number buttons
  - automatically select next selection number when finishing placing last one of a selection
- [x] display "You win!" message when all numbers are placed
- [x] create a start/home page
  - create game here
  - offer to continue previous game if there is one in progress
- [x] when finished, home should not show continue game. Maybe clear storage?
- [ ] add a hint button to show one of the remaining numbers 
  - and be able to show it step by step
- [x] add a timer, also keep in memory start and end time
- [x] count errors and display them
- [ ] keep all puzzle history in memory
  - [ ] store time of start, end, time taken, Sudoku seed(?), difficulty
  - [ ] best time tracking
- [ ] add about page with features, how to play, maybe todo, license, author, etc.
- [x] work on difficulty (with guaranteed unique solution, hard is too simple) 
- [ ] manage horizontal display
- [x] improve appearance
- [x] remove clear button
- [ ] plug in github automatic build, with test coverage etc... if possible (and free)
- [ ] add to google play store
- [ ] convert hodoku core to kotlin
- training
  - [x] order techniques by difficulty level
  - [ ] show not implemented training techniques
  - [ ] for single, indicate which number should be placed
  - [ ] for chain and coloring, indicate which candidates should be removed
  - [x] use game fragment for training ?

## Publishing to Google Play Store

### 1. Create a Developer Account
- Go to https://play.google.com/console and sign in with your Google account
- Pay the one-time **$25 registration fee**

### 2. Create a Signing Keystore (once — keep it safe forever, do NOT commit it)

```bash
keytool -genkey -v -keystore jjsudoku.jks -keyalg RSA -keysize 2048 -validity 10000 -alias jjsudoku
```

Add `jjsudoku.jks` to `.gitignore` immediately.

### 3. Configure Release Signing in `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../jjsudoku.jks")   // path relative to app/
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "jjsudoku"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}
```

Using environment variables avoids storing passwords in source control.

### 4. Build the Release AAB

```bash
KEYSTORE_PASSWORD=yourpassword KEY_PASSWORD=yourpassword \
  ./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 5. Prepare Store Assets (required)
- **App icon**: 512×512 PNG
- **Screenshots**: at least 2 phone screenshots
- **Short description**: max 80 characters
- **Full description**
- **Feature graphic**: 1024×500 PNG (recommended)

### 6. Create & Submit the App in Play Console
1. **Create app** → set title, language, free, game type
2. **Content rating** questionnaire → will be rated *Everyone*
3. **Data safety** form → no data collected, no network (straightforward for this app)
4. Go to **Production** (or **Internal Testing** first, recommended)
5. Upload the `.aab`, fill release notes, and **Submit for review**

> First review typically takes **a few days**.

### Release Checklist
- [ ] Keystore created and backed up securely
- [ ] `jjsudoku.jks` added to `.gitignore`
- [ ] `versionCode` incremented in `app/build.gradle.kts` for each new release
- [ ] `versionName` updated (currently `0.1.0`)
- [ ] App icon designed (currently uses default Android icon)
- [ ] Screenshots taken
- [ ] Store listing text written
- [ ] Data safety form completed

