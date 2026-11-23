# Phone Cleaner AI Cleaner Pro: Permission Explanation for Google Play

Package: `com.phonecleaner.aicleaner`

Use this document for Play Console (App content, Permissions declaration form, All files access declaration, policy review replies). Every claim below was checked against the source code and the manifest. Where text is meant to be pasted into a form, it is in a quote block.

---

## 1. Summary

Phone Cleaner is an on-device storage, memory and battery utility. It does not collect, upload or share file contents or personal data. The app declares only the permissions below. Each sensitive one is requested at the moment the user opens the feature that needs it, after an in-app explanation, through the official Android system settings page, and the user can revoke it at any time.

| Permission | Sensitivity | Feature that needs it | Play Console action |
|---|---|---|---|
| `MANAGE_EXTERNAL_STORAGE` | High risk, special app access ("All files access") | Junk Cleaner, Duplicates, AI Cleanup, WhatsApp Cleaner, Files manager | **Permissions Declaration Form + demo video required** |
| `WRITE_SETTINGS` | Sensitive, special app access ("Modify system settings") | Battery Manager | Explain in App content / review reply (no dedicated form) |
| `REQUEST_DELETE_PACKAGES` | Normal, but reviewed for abuse | Apps Manager (uninstall) | Explain if asked (no dedicated form) |
| `KILL_BACKGROUND_PROCESSES` | Normal | Process Manager, Battery Manager | None |
| `POST_NOTIFICATIONS` | Runtime (Android 13+) | Reminders | None |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Normal | Ads, analytics, crash reporting, push messages | None |
| `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` | `maxSdkVersion="29"` | Android 10 and lower only | None |

**Permissions the app does NOT use:** `READ_MEDIA_IMAGES/VIDEO/AUDIO`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`, `FOREGROUND_SERVICE` (explicitly removed in the manifest), Accessibility Service, Usage Access, contacts, location, camera, microphone, SMS and call log.

---

## 2. MANAGE_EXTERNAL_STORAGE (All files access)

This is the only permission that needs the Play Console **Permissions Declaration Form**.

### 2.1 Core functionality

Phone Cleaner is a storage cleaning and file management app. Finding and removing unwanted files across the device is its main purpose, which falls under the permitted use cases "file managers" and "device cleaning / storage management" for All files access.

| Feature | What it does with files |
|---|---|
| Junk Cleaner | Scans shared storage for temporary files, thumbnails caches, residual and ad-junk files, trash and obsolete APKs. The user reviews the list and cleans the selected items. |
| Duplicates | Finds duplicate files by content so the user can keep one copy and remove the rest. |
| AI Cleanup | Groups large videos, large files, audio and old screenshots to free space quickly. |
| WhatsApp Cleaner | Lists WhatsApp and WhatsApp Business media (images, video, audio, documents, received and sent) so the user can delete what they no longer need. |
| Files manager | Browse folders, open, share, create folders and delete files. |

### 2.2 Why this permission is required

- The files to clean are created by **many other apps** and live in shared storage folders that media-only permissions never cover: documents, APKs, downloads, app cache leftovers, `Android/media/<package>` folders (WhatsApp) and trash folders.
- MediaStore returns only photos, videos and audio, and cannot delete or list non-media files created by other apps.
- The Storage Access Framework (one folder grant) cannot cover the whole storage. It also blocks `Android/` and the `Download` root on Android 11+, so a junk cleaner or file manager built only on it cannot do its job.
- Junk and duplicates are spread across the entire storage, so the app has to walk it as a whole.

### 2.3 User control and transparency

- Requested only when the user opens one of the features above. Never at install or app start.
- An in-app screen first explains what will be scanned and why.
- Granted on the official system page (`Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`). The user can revoke it there at any time.
- If the user declines, the features that need it stay unavailable and the rest of the app keeps working.

### 2.4 Data handling

- All scanning and processing happens on the device.
- File names, paths and contents are not uploaded, shared or sold. No file contents are sent to any server.
- Nothing is deleted without the user selecting the items and confirming.

### 2.5 Text for the declaration form

> Phone Cleaner is a storage cleaning and file management app. Its core functionality is to scan the device's shared storage and let the user review and delete junk and temporary files, duplicate files, large and obsolete files and WhatsApp media, and to browse and manage files. This requires All files access because the files belong to many other apps and are spread across shared storage, so media permissions (which do not cover documents, APKs, downloads or app folders) and a single Storage Access Framework folder grant (which cannot cover the whole storage and is blocked for Android and Download folders on Android 11+) are not enough. The permission is requested only when the user opens these features, after an in-app explanation, and is granted on the system "All files access" page where it can be revoked at any time. All processing happens on the device, file data is never uploaded or shared, and nothing is deleted without the user's selection and confirmation.

### 2.6 Notes on related storage permissions

`READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` are declared with `android:maxSdkVersion="29"`. They apply only to Android 10 and lower, where All files access does not exist.

---

## 3. WRITE_SETTINGS (Modify system settings)

### 3.1 Core functionality

Battery Manager is a guided power-saving flow. It is started by the user tapping Continue, and then applies five steps one after another with visible progress.

### 3.2 Exactly what the app changes

| Step | System values written | Notes |
|---|---|---|
| Screen timeout | `Settings.System.SCREEN_OFF_TIMEOUT` set to 5 seconds | |
| Sound | Media, notification and system stream volumes lowered to 0 via `AudioManager`; `SOUND_EFFECTS_ENABLED`, `lockscreen_sounds_enabled` and `DTMF_TONE_WHEN_DIALING` set to off | Volume itself does not need `WRITE_SETTINGS`; the sound effect keys do |
| Brightness | `SCREEN_BRIGHTNESS_MODE` set to manual; `SCREEN_BRIGHTNESS` and `screen_brightness_float` set to a low level; `ACCELEROMETER_ROTATION` (auto-rotate) set to off | |
| Vibration | `HAPTIC_FEEDBACK_ENABLED` and `vibrate_when_ringing` set to off | |
| Background apps | `ActivityManager.killBackgroundProcesses` for cached background apps | Uses `KILL_BACKGROUND_PROCESSES` |

No other system setting is read or modified.

### 3.3 Why this permission is required

These are system-wide values. Android provides no other API to change screen timeout, haptic feedback, sound effects or global brightness for the device. Changing only the app window's brightness would not save any battery once the user leaves the app.

### 3.4 User control and transparency

- Never requested at install or app start. It is requested only when the user taps Continue in Battery Manager.
- The app first shows its own prompt, then opens the system "Modify system settings" page (`Settings.ACTION_MANAGE_WRITE_SETTINGS`). The user can revoke access there at any time.
- If the user declines, no setting is changed.
- No data is collected by this feature.

### 3.5 Text for the declaration / review reply

> The app's Battery Manager feature, started explicitly by the user, lowers screen brightness, shortens screen timeout, turns off haptic feedback and interface sounds, and disables auto-rotate to extend battery life. These are system-wide settings that can only be changed with Modify System Settings access. The user grants it on the system settings page after an in-app explanation and can revoke it at any time. No other system setting is modified and no data is collected.

> **Consistency warning (action for the team):** the in-app prompt string `battery_perm_b1` currently says "Only brightness, screen timeout and vibration are changed". The feature also turns off interface sounds (and lowers volume) and disables auto-rotate. Reviewers compare the in-app wording with the declared behaviour. Update `battery_perm_b1` (and its translations) to name all of them, or remove those steps, before submitting.

---

## 4. REQUEST_DELETE_PACKAGES

**Feature:** Apps Manager. The user selects apps to remove and the app opens the system uninstall dialog (`Intent.ACTION_DELETE`) for each one.

- The user confirms every uninstall in the system dialog.
- The app never uninstalls silently. The uninstall itself is always performed and confirmed by the Android system dialog.
- The app list is read with `PackageManager` filtered through the manifest `<queries>` launcher intent. The app does **not** declare `QUERY_ALL_PACKAGES`.

> The Apps Manager feature lets the user remove apps they no longer need to free storage. It launches the standard Android uninstall confirmation for each selected app. The user confirms every uninstall, and the app never removes anything silently.

---

## 5. KILL_BACKGROUND_PROCESSES

**Feature:** Process Manager ("Task Killer") and the background-apps step of Battery Manager. On user action it calls `ActivityManager.killBackgroundProcesses` for the packages of cached user apps to free memory.

- It does not use Accessibility or any hidden mechanism.
- It does not touch foreground or system apps.
- It runs only on user request.
- Store listing wording: describe it as freeing memory by stopping background apps at the user's request. Do not promise guaranteed speed or battery gains.

---

## 6. POST_NOTIFICATIONS

The app asks for it on the Home screen so it can show notifications (delivered through Firebase Cloud Messaging). It is a runtime permission on Android 13+ and the user can decline it without losing any feature.

> Team check: confirm what the notifications actually contain (tips, offers, reminders) and describe exactly that in the Play form and Data safety section. The app has no local notification code, so the content comes from the push service.

---

## 7. INTERNET and ACCESS_NETWORK_STATE

Used for advertising, analytics, crash reporting and push messaging, and to check connectivity. File contents and the user's personal files are never transmitted. Keep the Data safety form and the Privacy Policy consistent with the SDKs listed in the app.

---

## 8. Supporting material for the review

### 8.1 Demo video (required for All files access, 30 to 90 seconds)

**Before recording**
- Use a real device with the final release build, English UI, a fresh install (or revoke both special accesses first: Settings > Apps > Phone Cleaner > "All files access" off and "Modify system settings" off), so the permission prompts are shown.
- Put some junk, duplicates, a large video and an app you do not need on the phone, so every screen has content.
- Use the phone's built-in screen recorder, portrait, with touches visible if possible.
- Upload to YouTube as **Unlisted** (public also works), no ads, and paste the link into the declaration form. Keep it under 2 minutes, with English captions or voice-over naming each permission as it appears. Do not use sped-up or edited footage that hides the permission screens.

**Scene 1: All files access (about 60 to 75 seconds)**
1. Show the Home screen, then tap **Clean** (Junk Cleaner).
2. Pause on the in-app explanation screen so the text is readable.
3. Tap Allow. The system **All files access** page opens. Show the app name in the list, toggle it **on**, press back.
4. Show the scan running, then the result list (Residuals, Obsolete files, Ad Junk, Trash). Pause 2 seconds.
5. Select a few items, tap Clean, and show the confirmation and the finished screen. This proves nothing is deleted without the user's action.
6. Go to the **Files** tab, open Internal storage, open a couple of folders (Download, Documents). Caption: "File manager browsing files from other apps".
7. Go back and open **Delete Duplicates**, let it scan, show the result groups, do not need to delete.
8. Open **AI Cleanup** and then **View All** on Videos, so large and old files are shown.
9. Open **WA Cleaner** (WhatsApp must be installed with some media) and show the categories.

**Scene 2: Modify system settings (about 30 to 40 seconds)**
1. Go to Home and open **Battery Manager**. Pause on the list of five steps.
2. Tap **Continue**. Show the in-app permission prompt, tap Allow.
3. The system **Modify system settings** page opens. Toggle the app **on**, press back.
4. Show the steps running one by one (timeout, sound, brightness, vibration, background apps) and the final completed screen.
5. Optional but helpful: swipe down the quick settings to show the brightness actually dropped.

**Scene 3: Uninstall and background apps (about 20 to 30 seconds)**
1. Open **Apps Manager**, select one app, tap Uninstall and show the **system uninstall dialog**. Cancel it, or confirm on a test app.
2. Open **Process Manager** and tap **Task Killer**, showing it runs only after the user taps the button.

**Captions to add on screen**
- "All files access: used to find and delete junk, duplicates and large files across storage, at the user's request"
- "Modify system settings: Battery Manager changes brightness, timeout, sounds and vibration at the user's request"
- "Uninstall confirmed by the system dialog"

**Common reasons for rejection to avoid**
- The video does not show the permission being requested and granted.
- The video shows a feature that is not in the release build, or the permission is requested at app start instead of when the feature is opened.
- The in-app wording does not match what the app really does (see the consistency warning in section 3.5).
- The declared use case is not the app's core purpose. Keep the form text focused on storage cleaning and file management.

### 8.2 Privacy Policy must state

- The app does not collect or transmit file contents.
- All Files access is used only for scanning, browsing and deleting files at the user's request.
- Modify system settings is used only by Battery Manager at the user's request.
- Which third-party SDKs (ads, analytics, crash reporting, messaging) receive which data.

### 8.3 Store listing

- Say clearly that the app scans storage and deletes files only after the user confirms.
- Say that Battery Manager changes brightness, screen timeout, vibration, sounds and auto-rotate at the user's request.
- Avoid claims such as "boosts performance by X%" or "doubles battery life".

---

## 9. Notes for the team

- Only `MANAGE_EXTERNAL_STORAGE` has a dedicated Play declaration form. `WRITE_SETTINGS` and `REQUEST_DELETE_PACKAGES` have none, but reviewers can still ask about them, so keep the text above ready.
- Google does not guarantee approval. If All files access is rejected, the fallback is the Storage Access Framework, which cannot cover the whole storage and would remove the file manager and parts of the junk and duplicate features.
- Keep the in-app prompts, this text, the video and the real behaviour identical. Reviewers compare them.
- Re-check the current wording of Google Play's "All files access" and "Permissions and APIs that access sensitive information" policies before each submission, because they change.
