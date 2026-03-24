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

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).

## Roadmap

- [x] hide cells only if puzzle has still exactly one solution
- [ ] improve selection buttons 
  - hide number selection button once all numbers are placed
  - display how many are left on selection number buttons
  - automatically select next selection number when finishing placing last one of a selection
- [ ] display "You win!" message when all numbers are placed
- [ ] create a start/home page
  - create game here
  - offer to continue previous game if there is one in progress
- [ ] add a hint button to show one of the remaining numbers 
  - and be able to show it step by step
- [ ] keep all puzzle history in memory
  - add a timer and best time tracking
  - count errors and display them
  - be able to replay it step by step
- [ ] add about page with features, how to play, maybe todo, license, author, etc.
- [ ] work on difficulty (with guaranteed unique solution, hard is too simple) 
  - maybe use some heuristics to make it more human-like
- [ ] manage horizontal display
- [ ] improve appearance
  - maybe add theme system, such as dark mode
  - background color
  - button color
  - have a brown theme for a more classic look
  - add some animations when placing numbers, winning, etc.?
  - [ ] editable mode font color should be darker ("wood" brown) and font bold maybe
- [ ] the clear button does nothing
