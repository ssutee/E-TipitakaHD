# Full-note action dialog — Design

**Date:** 2026-06-24
**Status:** Approved (design)

## Problem

The Favorite (note) and History (keywords) action dialogs put the
user-entered header text into a Material3 `AlertDialog` title, with the
action items (`open note` / `edit note` / `delete` / `mark` / `sorting`) in
the dialog body. The title slot does not scroll, so a long note consumed the
dialog height and clipped the action list — only the first action stayed
visible.

The shipped (unreleased, 2.5.6) hotfix bounded the title with
`maxLines = 3` + ellipsis. That keeps the actions visible but **truncates the
note** — the user cannot read the full note from the action dialog.

## Goal

Show the **entire note text** AND **all action items** in the action dialog,
regardless of note length.

Chosen approach (user decision): a **custom full-screen-style dialog** where
the note **fills the available vertical space and scrolls**, with the action
list **pinned at the bottom**, always visible.

## Scope

In scope:

- A reusable `ActionListDialog` composable (custom `Dialog`) in
  `FavoriteHistoryScreen.kt`.
- Rewrite `FavoriteActionDialog` and `HistoryActionDialog` to use it.
- Remove the `maxLines = 3` title truncation (superseded). Folds into the
  unreleased 2.5.6 build — no version bump.

Out of scope:

- New string resources (none needed).
- Model changes (`Favorite` / `History` untouched).
- Bottom-sheet component (rejected approach B).
- Any other screen.

## Existing context

- File: `app/src/main/java/com/watnapp/etipitaka/plus/fragment/FavoriteHistoryScreen.kt`.
- Material3 from compose-bom `2025.03.00`. `Surface`, `Divider`, `Text`,
  `TextButton`, `MaterialTheme` already used in the codebase. No
  `ModalBottomSheet` or `verticalScroll` exists yet (this introduces the
  first `verticalScroll`).
- `DialogAction(text, onClick)` (full-width `TextButton`) already exists and
  is reused unchanged.
- `FavoriteActionDialog` is currently `internal` (test seam); keep it
  `internal`.

## Design

### `ActionListDialog` (new, private)

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
                    Divider()
                }
                Column(modifier = Modifier.fillMaxWidth()) { actions() }
            }
        }
    }
}
```

**Layout behaviour:**

- `Surface` is capped at 90% of screen height; `Column` lays out the header
  (weighted) then the divider + actions (fixed).
- `weight(1f, fill = false)` on the header: it grows to the space remaining
  after the fixed actions, but only as far as its content needs (`fill =
  false`). So a short note → small dialog (no wasted space); a long note →
  header expands until the dialog hits the 90% cap, then the header's
  `verticalScroll` takes over and the note scrolls while the action list
  stays pinned and fully visible.
- Blank header → header + divider omitted (actions-only dialog).

### `FavoriteActionDialog` (rewritten)

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

### `HistoryActionDialog` (rewritten)

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

### Imports to add

`androidx.compose.foundation.layout.fillMaxWidth`,
`androidx.compose.foundation.layout.heightIn`,
`androidx.compose.foundation.rememberScrollState`,
`androidx.compose.foundation.verticalScroll`,
`androidx.compose.material3.Divider`,
`androidx.compose.material3.Surface`,
`androidx.compose.ui.platform.LocalConfiguration`,
`androidx.compose.ui.unit.dp` (if not already present),
`androidx.compose.ui.window.Dialog`,
`androidx.compose.ui.window.DialogProperties`.
(`Column`, `padding`, `weight`, `Modifier`, `Text`, `MaterialTheme`,
`stringResource` already imported.)

## Error handling / edge cases

| Case | Behaviour |
|------|-----------|
| Empty/blank note or keywords | Header + divider omitted; actions-only dialog. |
| Very long note | Header scrolls inside its region; actions stay pinned and fully visible; dialog capped at 90% screen height. |
| Short note | Dialog wraps to content; no empty space. |
| Tap outside / back | `onDismissRequest` → `onDismiss` (same as before). |

## Testing

`NoteActionDialogInstrumentedTest` (instrumented Compose UI test) already
renders `FavoriteActionDialog` with a 200-line note and asserts all five
actions are displayed — that assertion still encodes the requirement and must
pass under the new layout. Add one assertion that the note header is
displayed (first line on screen). Runs on API ≤ 33 emulators (the Compose
test lib's input setup is incompatible with API 34+ — `InputManager.getInstance`
reflection; documented, use a lower-API AVD such as the API 28 `Medium_Phone`).

## Files touched

- `app/.../fragment/FavoriteHistoryScreen.kt` — add `ActionListDialog`;
  rewrite `FavoriteActionDialog` + `HistoryActionDialog`; remove the
  `maxLines` title truncation; add imports.
- `app/src/androidTest/.../NoteActionDialogInstrumentedTest.kt` — add the
  header-displayed assertion.
