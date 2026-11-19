# Phone Cleaner AI Cleaner Pro: Permission Explanation for Google Play

Use this text in Play Console (App content, Permissions declaration forms, or when replying to a policy review). Replace the bracketed placeholders.

## Summary

The app requests no runtime permission except Notifications. It does NOT request `READ_MEDIA_*`, contacts, location, camera, microphone, SMS or call log. Two special-access permissions (All files access and Modify system settings) are used, and each is user-initiated, opened through the official Android Settings page, and can be revoked at any time.

| Permission | Feature | Type |
|---|---|---|
| `MANAGE_EXTERNAL_STORAGE` | Junk Cleaner, Duplicates, AI Cleanup, WhatsApp Cleaner, Files | Special app access ("All files access"), Play declaration form required |
| `WRITE_SETTINGS` | Battery Manager | Special app access ("Modify system settings") |
| `KILL_BACKGROUND_PROCESSES` | Process Manager | Normal permission |
| `REQUEST_DELETE_PACKAGES` | Apps Manager | Normal permission, system confirms each uninstall |
| `POST_NOTIFICATIONS` | Notifications | Runtime |

---

## 1. WRITE_SETTINGS (Modify system settings)

**Core feature:** Battery Manager. It is a guided power-saving flow that lowers the settings that drain the battery most.

**What the app changes (and nothing else):**
- Screen brightness (`Settings.System.SCREEN_BRIGHTNESS`, `SCREEN_BRIGHTNESS_MODE`)
- Screen timeout (`Settings.System.SCREEN_OFF_TIMEOUT`)
- Haptic feedback and vibrate-when-ringing (`HAPTIC_FEEDBACK_ENABLED`, `vibrate_when_ringing`)


**Why it is required:** Android provides no alternative API for an app to change these system-wide values. Changing only the app window's brightness would not save battery once the user leaves the app.

**User control and transparency:**
- The permission is never requested at install or app start. It is requested only when the user taps Start in Battery Manager.
- Before opening Settings, the app shows its own prompt that names the exact settings that will be changed.
- The user grants the permission on the system "Modify system settings" page (`ACTION_MANAGE_WRITE_SETTINGS`) and can revoke it there at any time.
- If the user declines, the app does not apply these changes and nothing else is affected.

**Short text for the declaration form:**
> The app's Battery Manager feature lowers screen brightness, shortens screen timeout and turns off haptic feedback to extend battery life, at the user's request. These are system-wide settings that can be changed only with Modify System Settings access. The user grants it explicitly on the system settings page after an in-app explanation, and can revoke it at any time. No other system setting is modified and no data is collected.

---


## 2. KILL_BACKGROUND_PROCESSES

**Core feature:** Process Manager ("Task Killer"). On user tap, it calls `ActivityManager.killBackgroundProcesses` for cached background apps to free memory. It does not use Accessibility, does not kill foreground or system apps, and runs only on user request.

## 3. REQUEST_DELETE_PACKAGES

**Core feature:** Apps Manager. The user selects apps they want to remove, and the app launches the system uninstall dialog for each. The user confirms every uninstall in the system dialog. The app never uninstalls silently.

## 4. MANAGE_EXTERNAL_STORAGE (All files access)

**Core feature:** The app is a file cleaner and file manager. Its main purpose is to find and remove files across the whole device storage: Junk Cleaner (temp files, thumbnails, trash, obsolete APKs, large files), Duplicates, AI Cleanup (old screenshots, large videos, old downloads), WhatsApp Cleaner (WhatsApp media) and the Files manager (browse, open, share, delete, create folders).

**Why it is required:** These features need to read and delete files created by many other apps in shared storage, including documents, APKs and downloads that the media-only permissions and the Storage Access Framework cannot cover. A single folder grant is not enough because junk and duplicates are spread across the storage.

**User control and transparency:**
- It is requested only when the user opens one of these features, after an in-app explanation.
- The user grants it on the system "All files access" page (`ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`) and can revoke it there at any time.
- Files are processed only on the device and are never uploaded. Nothing is deleted without the user selecting and confirming it.

**Short text for the declaration form:**
> Phone Cleaner is a storage cleaning and file management app. Its core functionality is to scan the device storage and let the user review and delete junk files, duplicate photos and videos, large and obsolete files, WhatsApp media, and to browse and manage files. This requires All files access because the files belong to many other apps and are spread across shared storage, so media permissions or a single folder grant are not enough. The permission is requested only when the user opens these features, after an in-app explanation. All processing happens on the device, files are never uploaded, and nothing is deleted without the user's selection and confirmation.

`READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` are declared with `maxSdkVersion="29"` and apply only to Android 10 and lower, where All files access does not exist. The app does not use `READ_MEDIA_*` permissions.

## 5. POST_NOTIFICATIONS

Used only for the app's own cleaning and charge reminders. It is a runtime permission the user can decline.

---

## Suggested supporting material for review

1. **Short screen recording (30 to 60 seconds):** open Battery Manager, tap Start, the in-app explanation appears, the system "Modify system settings" page, return, the progress screens, the result.
2. **Privacy Policy:** state that the app does not collect or transmit file contents, and that the special accesses are used only for the features above.
3. **Store listing:** mention that Battery Manager lowers brightness, screen timeout, vibration and volume at the user's request.

## Notes for the team

- `WRITE_SETTINGS` is a special app access, not a Play "restricted" permission with a dedicated declaration form. Reviewers may still ask about them, and the text and video above answer that.
- Google does not guarantee approval. Keep the in-app prompts, the policy text and the actual behaviour consistent, because reviewers compare them.
