# E-TipitakaHD

E-TipitakaHD is an Android app for reading and searching Buddhist Tipitaka texts. It includes Thai and Pali scripture content, dictionary data, bookmarks/history, comparison reading, and reader controls for navigating volumes and pages.

The app is an Android Gradle project using Java, Kotlin, ViewBinding, and Jetpack Compose during an ongoing UI migration.

## Requirements

- Android Studio or Android SDK command-line tools
- JDK 17
- Android SDK with API 36 installed
- A device or emulator for running the app

If you build from the command line, make sure `local.properties` points to your Android SDK:

```properties
sdk.dir=/path/to/Android/sdk
```

## Run In Android Studio

1. Open this repository in Android Studio.
2. Let Gradle sync finish.
3. Select the `app` run configuration.
4. Choose an Android device or emulator.
5. Click Run.

## Run From Command Line

Build the debug APK:

```bash
./gradlew assembleDebug
```

Install it on a connected device or emulator:

```bash
./gradlew installDebug
```

Run instrumentation tests on a connected device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

Run the project verification task:

```bash
./gradlew verifyDebug
```

## Project Docs

- Compose migration plan: [docs/COMPOSE_MIGRATION_PLAN.md](docs/COMPOSE_MIGRATION_PLAN.md)
