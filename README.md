# Züri Jöppli – Recycling-Roboter für Zürich

Native Android app (Kotlin + Jetpack Compose, Material 3) for ordering the autonomous
**Jöppli** recycling robot to your doorstep in Zurich: pick a spot, pick a time,
sort your materials, pay with TWINT (demo) and watch the robot drive from the
ERZ Werkhof Hardau to your door — with a recycling guide, AI vision scanner demo
and Züri-Karma gamification on top.

## Develop

1. Open the project in **Android Studio** (or Antigravity).
2. Let Gradle sync (AGP 9.0.1, Kotlin 2.3.20, Gradle 9.1, Compose BOM 2026.03).
3. Run the `app` configuration on an emulator or device (minSdk 24).

Command line:

```
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run unit tests
./gradlew lintDebug            # run Android lint
```

`versionCode` is derived automatically from the git commit count — no manual
bumping needed for releases.

## Google Maps & Places key

The live tracker map and the address autocomplete both read a single key. Put it
in `local.properties` (git-ignored — never commit it):

```
MAPS_API_KEY=AIza...
```

The build also accepts a `MAPS_API_KEY` environment variable, and falls back to a
dummy placeholder so builds and CI stay green without a key. With the dummy key
the map tiles stay blank and address autocomplete silently returns no
suggestions — the address fields still work as plain text.

On the key's Google Cloud project, enable **Maps SDK for Android** (map tiles)
and **Places API (New)** (address autocomplete). Restrict the key to the package
`gl.joeppli.zueri` plus your signing SHA-1.

Firebase setup for real login is documented separately in
[`FIREBASE_SETUP.md`](FIREBASE_SETUP.md).

## CI

Every push and PR to `main` runs [`.github/workflows/android.yml`](.github/workflows/android.yml):
assemble debug, unit tests, lint. The debug APK is attached to each run as the
`zueri-joeppli-debug-apk` artifact.

## Play Store publishing

The project uses the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)
plugin. Store listing texts live in `app/src/main/play/` (de-DE default + en-US);
screenshots and graphics can be added under
`app/src/main/play/listings/<locale>/graphics/` later.

One-time setup (all of these files are git-ignored):

1. Put your upload keystore at the repo root, e.g. `joeppli-upload-key.jks`.
2. Create `keystore.properties` at the repo root:

   ```properties
   storeFile=joeppli-upload-key.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```

3. Drop the Play Console service-account key at `app/play-service-key.json`.

Then publish to the Closed Testing (Alpha) track:

```
./gradlew publishReleaseBundle
```

Note: release builds are minified (R8) and must be signed — they intentionally
fail without the keystore. The app id is `gl.joeppli.zueri`; Play locks it
permanently on first upload.

## App structure

- `ui/MainAppLayout.kt` — scaffold, bottom bar with the recycling FAB
- `ui/screens/` — Home, Dashboard (Züri-Karma), Order wizard + live tracker, Guide (+ AI scanner demo), Profile
- `theme/` — Material 3 color schemes (light/dark), Poppins type scale
- `data/RecyclingRepository.kt` — in-memory demo data (profile, stats, pickups)
