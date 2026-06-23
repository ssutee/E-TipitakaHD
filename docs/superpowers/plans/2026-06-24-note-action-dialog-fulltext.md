# Full-note action dialog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the truncating note/keyword title in the Favorite & History action dialogs with a custom scrollable dialog that shows the full note text and keeps all action items pinned and visible.

**Architecture:** One reusable `ActionListDialog` (custom `androidx.compose.ui.window.Dialog`) with a scrollable, space-filling header region above a fixed action list. Both `FavoriteActionDialog` and `HistoryActionDialog` delegate to it. Supersedes the `maxLines=3` title truncation currently in the file.

**Tech Stack:** Jetpack Compose, Material3 (compose-bom 2025.03.00). Spec: `docs/superpowers/specs/2026-06-24-note-action-dialog-fulltext-design.md`.

---

## File Structure

- **Modify** `app/src/main/java/com/watnapp/etipitaka/plus/fragment/FavoriteHistoryScreen.kt`
  - Add imports.
  - Add private `ActionListDialog` composable.
  - Rewrite `FavoriteActionDialog` (keep `internal`) and `HistoryActionDialog` to delegate to it; remove the `maxLines` title blocks.
  - `DialogAction` unchanged.
- **Modify** `app/src/androidTest/java/com/watnapp/etipitaka/NoteActionDialogInstrumentedTest.kt`
  - Add an assertion that the note header is displayed (the existing all-actions-visible assertions are the core regression guard).

**Testing note:** the instrumented test is a regression guard that the new layout keeps all actions visible and shows the note header; it stays green across this change (this is a UX refactor of already-tested behavior, not a new bug contract). The scroll-to-read-the-whole-note behavior is verified by manual QA (Compose scroll-state assertions are brittle, and the project's posture is manual QA for visual layout). The Compose UI test must run on an **API ≤ 33** emulator — the test lib's input setup throws `NoSuchMethodException: InputManager.getInstance` on API 34+. Use the API 28 `Medium_Phone` AVD.

---

## Task 1: Custom scrollable ActionListDialog + rewire both dialogs

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/fragment/FavoriteHistoryScreen.kt`

- [ ] **Step 1: Add imports**

In the import block (top of file), add exactly these lines. The file uses explicit per-symbol imports (no wildcards). `Column`, `fillMaxWidth`, `heightIn`, `padding`, `MaterialTheme`, `Text`, `TextButton`, `Modifier`, `dp`, `stringResource`, `Composable` are already imported — do NOT duplicate them.

```kotlin
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
```

All eight are NEW to this file and all are required (`ColumnScope` for the `actions` lambda receiver; the rest for the dialog/scroll/surface/divider).

- [ ] **Step 2: Add the `ActionListDialog` composable**

Insert this new composable immediately BEFORE the `DialogAction` composable (currently around line 355, `@Composable private fun DialogAction`):

```kotlin
@Composable
private fun ActionListDialog(
    headerText: String,
    onDismiss: () -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = maxHeight),
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                if (headerText.isNotBlank()) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                    HorizontalDivider()
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    actions()
                }
            }
        }
    }
}
```

- [ ] **Step 3: Rewrite `FavoriteActionDialog`**

Replace the entire current `FavoriteActionDialog` body (the `AlertDialog(...)` block with the `maxLines` title, currently lines ~298–323) so the function reads exactly:

```kotlin
@Composable
internal fun FavoriteActionDialog(
    favorite: Favorite,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMark: () -> Unit,
    onSort: () -> Unit,
) {
    ActionListDialog(headerText = favorite.getNote().orEmpty(), onDismiss = onDismiss) {
        DialogAction(text = stringResource(R.string.open_note), onClick = onOpen)
        DialogAction(text = stringResource(R.string.edit_note), onClick = onEdit)
        DialogAction(text = stringResource(R.string.delete), onClick = onDelete)
        DialogAction(text = stringResource(R.string.mark), onClick = onMark)
        DialogAction(text = stringResource(R.string.sorting), onClick = onSort)
    }
}
```

Keep the `internal` modifier (test seam). The signature (favorite + 6 lambdas) is unchanged, so the call site at the top of the file needs no edit.

- [ ] **Step 4: Rewrite `HistoryActionDialog`**

Replace the entire current `HistoryActionDialog` body (the `AlertDialog(...)` block with the `maxLines` keyword title, currently lines ~325–353) so the function reads exactly:

```kotlin
@Composable
private fun HistoryActionDialog(
    history: History,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMark: () -> Unit,
    onSort: () -> Unit,
) {
    ActionListDialog(headerText = history.getKeywords().orEmpty(), onDismiss = onDismiss) {
        DialogAction(text = stringResource(R.string.delete), onClick = onDelete)
        DialogAction(text = stringResource(R.string.mark), onClick = onMark)
        DialogAction(text = stringResource(R.string.sorting), onClick = onSort)
    }
}
```

- [ ] **Step 5: Remove the now-unused `AlertDialog` import if unused**

After Steps 3–4 there are no more `AlertDialog(` usages in the file. Remove `import androidx.compose.material3.AlertDialog` (line 16). Verify `TextOverflow` is still used (it is — `FavoriteRow` uses `overflow = TextOverflow.Ellipsis`); keep `import androidx.compose.ui.text.style.TextOverflow`. To confirm what's now unused:

Run:
```bash
grep -n "AlertDialog\|TextOverflow" app/src/main/java/com/watnapp/etipitaka/plus/fragment/FavoriteHistoryScreen.kt
```
Expected: no `AlertDialog(` call sites remain (only possibly the import line you are deleting); `TextOverflow` still referenced in `FavoriteRow`.

- [ ] **Step 6: Compile**

Run:
```bash
./gradlew compileDebugKotlin
```
Expected: BUILD SUCCESSFUL. Fix any unresolved-reference errors by adding the missing import from Step 1 (most likely `ColumnScope`, `Surface`, `HorizontalDivider`, `Dialog`, or `DialogProperties`).

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/fragment/FavoriteHistoryScreen.kt
git commit -m "feat(notes): show full note + all actions in a scrollable dialog

Replace the truncating AlertDialog title with a reusable custom
ActionListDialog: the note/keyword header fills the available height and
scrolls, while the action list stays pinned at the bottom and always
visible. Used by both the Favorite and History action dialogs.
Supersedes the maxLines title truncation.

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Task 2: Strengthen + run the regression test

**Files:**
- Modify: `app/src/androidTest/java/com/watnapp/etipitaka/NoteActionDialogInstrumentedTest.kt`

- [ ] **Step 1: Add a header-visible assertion**

Open the test. After the five `assertIsDisplayed()` action assertions in `longNote_allActionsRemainVisible`, add an assertion that the note header (its first line) is on screen. Insert before the closing brace of the test method:

```kotlin
    // The note header itself must be visible (full text shown, not hidden).
    composeRule.onNodeWithText("บันทึกบรรทัดที่ 1", substring = true).assertIsDisplayed()
```

The note built in the test is `(1..200).joinToString("\n") { "บันทึกบรรทัดที่ $it" }`, so its first line is `บันทึกบรรทัดที่ 1`. (`onNodeWithText(..., substring = true)` matches the single header `Text` node, whose first visible line is that string.)

- [ ] **Step 2: Boot an API ≤ 33 emulator (if not running)**

The Compose test lib cannot run on API 34+ (`InputManager.getInstance` reflection). Use the API 28 AVD:
```bash
/Users/sutee/Library/Android/sdk/emulator/emulator -avd Medium_Phone -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect &
ADB=/Users/sutee/Library/Android/sdk/platform-tools/adb
# wait until an API 28 device reports boot_completed=1, then note its serial
```

- [ ] **Step 3: Run the test (expect PASS — regression guard)**

With `ANDROID_SERIAL` set to the API 28 emulator:
```bash
ANDROID_SERIAL=<api28-serial> ./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.watnapp.etipitaka.NoteActionDialogInstrumentedTest
```
Expected: BUILD SUCCESSFUL; `longNote_allActionsRemainVisible` passes (all five actions + the note header are displayed under the new scrollable layout).

- [ ] **Step 4: Run the full instrumented suite (no regressions)**

```bash
ANDROID_SERIAL=<api28-serial> ./gradlew connectedDebugAndroidTest
```
Expected: BUILD SUCCESSFUL; 0 failures / 0 errors (the offline `CompareDownloadInstrumentedTest` self-skips).

- [ ] **Step 5: Commit**

```bash
git add app/src/androidTest/java/com/watnapp/etipitaka/NoteActionDialogInstrumentedTest.kt
git commit -m "test(notes): assert the full note header is visible in the action dialog

Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>"
```

---

## Manual QA (post-implementation, on device)

1. **Long note** — open a favorite with a very long note → action dialog: the note fills most of the screen and **scrolls** to reveal the whole text; "เปิดบันทึก / แก้ไขบันทึก / ลบ / สำคัญ / เรียงลำดับ" stay pinned at the bottom and all visible.
2. **Short note** — favorite with a 1-line note → small dialog, no large empty area, all actions visible.
3. **Empty note** — favorite with no note → actions-only dialog (no header/divider).
4. **History dialog** — long search keyword → keyword scrolls, delete/mark/sorting pinned.
5. **Dismiss** — tap outside / back → dialog closes.

---

## Self-review notes

- **Spec coverage:** custom Dialog with scrollable header + pinned actions (Task 1, `ActionListDialog`) ✓; note fills available space then scrolls (`weight(1f, fill = false)` + `verticalScroll`) ✓; blank header omits header+divider ✓; reused by both dialogs (Tasks 1.3/1.4) ✓; reuse `DialogAction` ✓; removes maxLines truncation (Task 1.3/1.4 rewrite) ✓; no new strings / no model change ✓; test keeps all-actions-visible green + header assertion (Task 2) ✓; API ≤ 33 test caveat documented ✓.
- **Type consistency:** `ActionListDialog(headerText: String, onDismiss: () -> Unit, actions: @Composable ColumnScope.() -> Unit)` defined in Task 1.2 and called identically in 1.3 and 1.4. `FavoriteActionDialog` stays `internal` (matches the test import). `DialogAction` signature unchanged.
- **Placeholders:** none — all steps carry full code and exact commands.
