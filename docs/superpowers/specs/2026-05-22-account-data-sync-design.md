# Account Data Sync — Design

**Date:** 2026-05-22
**App:** E-TipitakaHD (Android, `com.watnapp.etipitaka.plus`)
**Status:** Approved

## Goal

Let users log into the E-Tipitaka backend server and upload/download their
personal data export file (favorites + search history) to it, tied to a user
account. This ports the "Account" feature of the iOS app (E-Tipitaka-Plus) —
specifically its login + own-backups flows.

## Scope

In scope:

- Login / logout against the backend.
- Upload the current user data export file to the server.
- List the user's own backups stored on the server (grouped by platform).
- Download a server backup and restore it into the local database.
- Delete a server backup.

Out of scope (explicitly deferred):

- "Others data" / sharing — browsing other users' shared favorites/history
  (the iOS `OthersData*` screens).
- Raw per-feature SQLite sync (the iOS `/sync_data/` path that syncs ~10
  individual `.sqlite` files).
- In-app account registration. Account creation is web-only.
- Auto-sync. Upload/download is manual, user-triggered.

## Background

- The backend (`data-etipitaka/`) is a Django + DRF + PostgreSQL service,
  already deployed. It exposes token auth and a `UserData` file-storage model.
- The Android app currently exports/imports user data as a JSON file via the
  Storage Access Framework. Export payload:
  `{"favorite_table": [...], "history_table": [...]}`.
- The Android app has no auth and no real HTTP client (only `HttpURLConnection`
  in `FileDownloader` for static DB downloads).
- The iOS app's `/upload/` + `/user_data_list/` + `/user_data/{id}/` endpoints
  are the export-file family this feature targets (the iOS
  `UserDataManagement` + `ServerData` screens).
- `FavoriteDaoHelper.restoreJSONArray` / `HistoryDaoHelper.restoreJSONArray`
  already merge by natural key — each checks `contains(...)` and inserts only
  rows not already present. The chosen "merge by key" restore behavior is
  therefore satisfied by reusing the existing import path; no new dedup logic
  is required.

## Backend protocol

Base URL: `https://data.etipitaka.com`
Auth header on all authenticated requests: `Authorization: Token <token>`

| Endpoint | Method | Request | Response |
|----------|--------|---------|----------|
| `/rest-auth/login/` | POST | form-encoded `username`, `password` | 200 `{"key": "<token>"}`; 400 `{"non_field_errors": [...]}` |
| `/rest-auth/logout/` | POST | token header | 200 |
| `/upload/` | POST | multipart: `file` (the export JSON), `title` | 200/201 |
| `/user_data_list/` | GET | token header | `{"items": "<json-encoded-string>"}` |
| `/user_data/{pk}/` | GET | token header | binary backup file (the JSON export) |
| `/user_data/{pk}/` | DELETE | token header | 204 |

Notes:

- `/user_data_list/` returns `items` as a **JSON-encoded string** nested inside
  the JSON object — it must be parsed twice. Each decoded item carries
  `pk`, `file` (server path), `platform`, `created_at`, `deleted`.
- The upload filename is `edata-YYYY-MM-DD.js`. The backend infers `platform`
  from the file extension; `.js` maps to `android`.
- `created_at` time format: `yyyy-MM-dd'T'HH:mm:ss.SSSZ`.

## Architecture

New package: `com.watnapp.etipitaka.plus.account`

| Unit | Responsibility | Depends on |
|------|----------------|------------|
| `EtipitakaApiClient.kt` | All HTTP calls (login, logout, upload, list, download, delete). Returns a sealed `ApiResult<T>`. | OkHttp, `SessionManager` (for token) |
| `SessionManager.kt` | Persist/read/clear `username` + `token`. | `SharedPreferences` (`account_preferences`) |
| `ServerBackup.kt` | Data class for one listed server backup (`pk`, `name`, `platform`, `createdAt`). | — |
| `AccountViewModel.kt` | UI state machine + actions. | `EtipitakaApiClient`, `SessionManager`, `UserDataExporter`, `UserDataImporter` |
| `AccountScreen.kt` | Compose UI. | `AccountViewModel` |
| `AccountActivity.kt` | Hosts `ComposeView`, edge-to-edge. | `AccountScreen` |

`ApiResult<T>` sealed type: `Success(value)`, `AuthError`, `NetworkError`,
`ServerError(message)`.

`AccountViewModel` state: `LoggedOut` → `LoggingIn` → `LoggedIn(username,
backups, busy)`. A 401 from any authenticated call clears the session and
returns to `LoggedOut`.

### Shared-code refactor (minimal, in service of the feature)

- Extract `buildExportJson()` from `MainActivity.exportData(Uri)` into a shared
  helper (`UserDataExporter`) returning the export JSON string. Used by both
  the existing SAF export and the new upload.
- Extract a UI-free `UserDataImporter.importAndroidJson(context, json)` that
  performs the DAO restore (the body of `MainActivity.importAndroidData`,
  minus toasts/threading). `MainActivity.importAndroidData` delegates to it;
  the download flow calls it directly.

### Entry point

New overflow menu item "บัญชีผู้ใช้" (`Constants.MENU_ITEM_ACCOUNT`), opens
`AccountActivity`. Placed alongside the existing top-level overflow items
(Import/Export data, Version).

## UI

`AccountScreen`, two states:

- **Logged out:** username + password fields, "Login" button, inline error
  text on failure, and a "Create account" link that opens the backend signup
  web page (`https://data.etipitaka.com/signup/`) in a browser via an
  `ACTION_VIEW` intent.
- **Logged in:** shows the username, a "Logout" button, an "Upload current
  data" button, and the list of server backups grouped by platform
  (iOS / Android / PC). Each backup row shows its date and offers Download and
  Delete actions.

Compose + Material3, consistent with `VersionDialogFragment`.

## Data flow

- **Login:** `POST /rest-auth/login/` → on success store `token` + `username`
  via `SessionManager`, transition to `LoggedIn`, refresh backup list.
- **Logout:** `POST /rest-auth/logout/` → clear `SessionManager` → `LoggedOut`.
  Clear local session even if the network call fails.
- **Upload:** `UserDataExporter.buildExportJson()` → multipart `POST /upload/`
  with filename `edata-YYYY-MM-DD.js` → refresh list.
- **List:** `GET /user_data_list/` → parse the double-encoded `items` →
  `List<ServerBackup>`.
- **Download:** `GET /user_data/{pk}/` → `UserDataImporter.importAndroidJson`
  (merges by key into the local DB) → success toast.
- **Delete:** `DELETE /user_data/{pk}/` → refresh list.

Network calls run on a coroutine IO dispatcher (the app already uses
Coroutines + Koin).

## Error handling

- Network failure / server error → inline message in the screen + a toast.
- 401 on an authenticated call → clear session, return to the login state.
- Login 400 → "login failed" inline error.
- Upload/download/delete failure → toast, list state left unchanged.

## Security / credential storage

`SessionManager` stores `username` + `token` in a dedicated
`account_preferences` `SharedPreferences` file, in plain text. This matches the
iOS app, which keeps the token in plain `UserDefaults`. The token is a
revocable, low-value DRF sync token. Upgrading to `EncryptedSharedPreferences`
is a possible later hardening step but is not in scope here.

## Testing

- Unit test `EtipitakaApiClient` response parsing — in particular the
  double-encoded `items` string from `/user_data_list/`, and the login
  success/failure shapes.
- Unit test `SessionManager` store / read / clear.

Test density kept light, consistent with the existing repo.

## New dependency

- `com.squareup.okhttp3:okhttp`

## Open risks

- The `/upload/` endpoint is described as "legacy" in the backend; the iOS
  export-file flow still uses it and the `UserData` model is the correct shape
  for listing/deleting backups. If the backend later removes `/upload/`, the
  upload call would need to move to `/sync_data/`.
