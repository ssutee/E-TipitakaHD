# Fastlane — Google Play release automation

Build a signed release AAB and upload it to Google Play with one command.

```bash
fastlane android release                 # build + upload to production
fastlane android release track:internal  # build + upload to internal testing
fastlane android release rollout:0.1     # staged 10% production rollout
fastlane android upload                  # upload the already-built app-release.aab
fastlane android validate                # check the service-account credentials
```

Signing comes from `keystore.properties` (already wired in `app/build.gradle`).
Play credentials come from `fastlane/play-service-account.json` (gitignored).

---

## One-time setup: create the service-account JSON

You only do this once. It produces the `fastlane/play-service-account.json`
key that lets fastlane talk to the Play Developer API.

### 1. Enable the API + create the service account (Google Cloud Console)

1. Go to <https://console.cloud.google.com/> and pick (or create) a project.
2. **APIs & Services → Library** → search **"Google Play Android Developer API"** → **Enable**.
3. **APIs & Services → Credentials → Create credentials → Service account**.
   - Name: e.g. `play-publisher`.
   - Skip the optional role/grant steps → **Done**.
4. Open the new service account → **Keys** tab → **Add key → Create new key → JSON** → **Create**.
   - A `.json` file downloads. This is your key.
5. Move it into the repo at the gitignored path:

   ```bash
   mv ~/Downloads/<project>-<hash>.json \
      "/Volumes/SeagateBackup/Works/watnapahpong/E-TipitakaHD/fastlane/play-service-account.json"
   ```

   Note the service-account email (looks like
   `play-publisher@<project>.iam.gserviceaccount.com`) — you need it next.

### 2. Grant the service account access (Play Console)

1. Go to <https://play.google.com/console/> → **Users and permissions** (account level, the left rail of the *all-apps* view, not inside one app).
2. **Invite new users** → paste the service-account email.
3. **App permissions** → add **E-Tipitaka** (`com.watnapp.etipitaka.plus`).
4. **Account permissions** → grant at least:
   - **Releases: Manage production releases** (for the `production` track)
   - **Releases: Manage testing track releases** (for `internal` / `beta`)
   Or simpler for a personal account: **Admin (all permissions)**.
5. **Invite user** / **Send invitation**.

It can take a few minutes to a few hours for the grant to propagate.

### 3. Verify

```bash
fastlane android validate
```

Expect `Successfully established connection to Google Play Store`.

---

## Usage

### Build + upload (default flow)

```bash
fastlane android release
```

Runs `./gradlew clean bundleRelease`, then uploads the fresh signed AAB to the
**production** track as a completed rollout. versionCode/versionName are read
from the AAB — bump them in `app/build.gradle` before releasing.

### Upload an AAB you already built

```bash
fastlane android upload
```

Skips the build, uploads `app/build/outputs/bundle/release/app-release.aab`.
Override with `aab:path/to/file.aab`.

### Staged rollout

```bash
fastlane android release rollout:0.1   # 10% of users
```

`release_status` becomes `inProgress`. Increase the fraction (or complete the
rollout) later from the Play Console, or re-run with a higher `rollout:`.

### Other tracks

```bash
fastlane android release track:internal
fastlane android release track:beta
```

---

## Gotchas

- **First production release must be manual.** Google requires the *very first*
  release of an app on the production track to go through the Play Console UI.
  After that, the API (and therefore fastlane) can publish. This app already
  has prior production releases, so this is only relevant for a brand-new app.
- **Service-account JSON is secret.** It is gitignored
  (`fastlane/play-service-account.json`). Anyone with it can publish to your
  Play account. If leaked, delete the key in Google Cloud Console and create a
  new one.
- **Permission propagation lag.** A freshly invited service account may return
  `403` for a while. Wait and retry `fastlane android validate`.
- **`completed` rollout to production** publishes to all users after Google
  review. Use `rollout:` for a staged release if you want a safety valve.
- **UTF-8 locale warning** from fastlane is harmless. To silence it, add to your
  shell profile:
  ```bash
  export LC_ALL=en_US.UTF-8
  export LANG=en_US.UTF-8
  ```
