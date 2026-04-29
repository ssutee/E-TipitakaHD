# Jetpack Compose Migration Plan

This plan migrates the app to Jetpack Compose gradually while keeping the app runnable and preserving existing behavior after every phase.

## Current State

- The app is a hybrid Java/Kotlin Android app.
- Most screens still use XML layouts, ViewBinding, fragments, adapters, and AppCompat.
- The reader is high risk because it combines `ViewPager2`, `PageFragment`, `MyWebView`, database-backed content, search highlights, and scroll controls.
- Compose should be introduced through interop first, then used to replace complete screens once the foundation is stable.

## Ground Rules

- Create a new branch before each phase.
- Keep each phase small enough to build, run, review, and merge independently.
- Preserve existing behavior unless a behavior change is explicitly approved.
- Keep ViewBinding/XML enabled until the related screen has fully moved to Compose.
- Run `./gradlew assembleDebug` after every phase.
- Run `./gradlew testDebugUnitTest` for non-trivial logic changes.
- Run `./gradlew connectedDebugAndroidTest` when data, navigation, or model behavior changes.
- Run the app in the emulator for every UI phase.
- Merge to `master` only after manual approval.

## Phase 7: Compose Foundation

Branch: `phase-7-compose-foundation`

Goal: Enable Compose without changing app behavior.

Tasks:

- Add the Compose compiler Gradle plugin for the current Kotlin version.
- Enable `buildFeatures.compose = true`.
- Add Compose dependencies through the Compose BOM.
- Add core Compose libraries:
  - `androidx.compose.ui:ui`
  - `androidx.compose.ui:ui-tooling-preview`
  - `androidx.compose.material3:material3`
  - `androidx.activity:activity-compose`
  - `androidx.compose.ui:ui-test-junit4`
- Keep ViewBinding enabled.
- Add a minimal internal composable/theme file that is not wired into production UI yet.

Verification:

- `./gradlew assembleDebug`
- `./gradlew testDebugUnitTest`
- Run the app in emulator and confirm no visible behavior changed.

## Phase 8: Compose Theme Bridge

Branch: `phase-8-compose-theme`

Goal: Create reusable Compose styling that matches the current app.

Tasks:

- Add `ETipitakaTheme`.
- Map current XML colors, text sizes, and spacing into Compose tokens.
- Add helper composables for common text and surface styling.
- Keep the current AppCompat host theme.

Verification:

- `./gradlew assembleDebug`
- Run the app in emulator.
- Confirm no production screen has unintended visual changes.

## Phase 9: Small Dialogs And Utility UI

Branch: `phase-9-compose-dialogs`

Goal: Convert low-risk UI pieces first.

Candidates:

- Text entry dialog content.
- Font settings dialog content.
- Simple loading, empty, or error views.
- Confirmation dialog content where callbacks already exist.

Approach:

- Keep existing `DialogFragment` containers.
- Use `ComposeView` for dialog content.
- Keep existing strings, callbacks, and validation behavior.

Verification:

- `./gradlew assembleDebug`
- Open each converted dialog in emulator.
- Confirm callbacks, cancel behavior, and state restoration still work.

## Phase 10: List Rows In Compose

Branch: `phase-10-compose-list-rows`

Goal: Replace repeated row layouts without replacing entire screens.

Candidates:

- Book title row.
- File explorer row.
- Favorite row.
- History row.
- Dictionary/search result rows.

Approach:

- Use Compose row content inside existing adapters where practical.
- Keep existing adapters, item click handlers, and data loading.
- Avoid changing sorting, filtering, or cursor behavior.

Verification:

- `./gradlew assembleDebug`
- Run the app in emulator.
- Check menu book list, history, favorites, search results, and file explorer where applicable.

## Phase 11: Side Menu Fragment

Branch: `phase-11-compose-menu`

Goal: Convert the side menu UI while keeping existing navigation behavior.

Tasks:

- Replace the menu tab/radio UI with Compose.
- Keep current menu sections:
  - Book list
  - Search
  - Favorites
  - History
- Keep `SlidingMenu` during this phase.
- Keep existing fragment or navigation callbacks until behavior is proven stable.

Verification:

- `./gradlew assembleDebug`
- Run the app in emulator.
- Open and close the side menu.
- Switch all menu sections.
- Open a book from the menu.
- Search and navigate to a result.

## Phase 12: Reader Chrome

Branch: `phase-12-compose-reader-chrome`

Goal: Convert the reader shell, not the page content.

Convert:

- Subtitle/header area.
- Seekbar overlay.
- Compare and return bottom controls.

Keep:

- `ViewPager2`
- `PageFragment`
- `MyWebView`
- Existing page rendering and scrolling behavior.

Reason:

The reader content is the highest-risk part of the app. Keeping WebView pages unchanged while modernizing the surrounding controls reduces regression risk.

Verification:

- `./gradlew assembleDebug`
- Run the app in emulator.
- Swipe pages.
- Use the seekbar.
- Scroll up and down to show/hide controls.
- Test compare and return.
- Confirm the last page is restored after app restart.

## Phase 13: Main Activity Host

Branch: `phase-13-compose-main-host`

Goal: Move the main activity host layout toward Compose.

Tasks:

- Replace `activity_main.xml` with a Compose host where possible.
- Keep fragment interop for reader/menu components that are not migrated yet.
- Preserve action bar/menu behavior unless explicitly changed.

Verification:

- `./gradlew assembleDebug`
- `./gradlew connectedDebugAndroidTest`
- Run full emulator smoke test.

## Phase 14: Full Screen Conversions

Goal: Convert complete screens one at a time.

Recommended order:

1. File explorer.
2. Dictionary screens.
3. Search screen.
4. Favorites and history.
5. Book list.
6. Comparison screen.
7. Reader.

Each screen should get its own branch, commit, emulator run, and approval before merging.

## Long-Term Cleanup After Compose Adoption

Once most screens are converted:

- Remove unused XML layouts.
- Remove unused adapters.
- Remove old custom views that Compose replaces.
- Revisit `SlidingMenu` and replace it with a Compose drawer or navigation structure.
- Move remaining Java UI classes to Kotlin.
- Add focused Compose UI tests for converted screens.

## Next Step

Start with `phase-7-compose-foundation`.

This phase should not change visible app behavior. It only prepares Gradle, dependencies, and the first shared Compose files so later UI phases can be small and safe.
