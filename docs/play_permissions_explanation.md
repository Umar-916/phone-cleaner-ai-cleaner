# Phone Cleaner AI Cleaner Pro: Permission Explanation for Google Play

Use this text in Play Console (App content, Permissions declaration forms, or when replying to a policy review). Replace the bracketed placeholders.

## Summary

The app requests no runtime permission except Notifications. It does NOT request `MANAGE_EXTERNAL_STORAGE`, `READ_MEDIA_*`, contacts, location, camera, microphone, SMS or call log. One special-access permission (Modify system settings) is used by the Battery Manager, and one normal permission by the Apps Manager. Each one is user-initiated, opened through the official Android Settings page, and can be revoked at any time.

| Permission | Feature | Type |
|---|---|---|
| `WRITE_SETTINGS` | Battery Manager | Special app access ("Modify system settings") |
| `KILL_BACKGROUND_PROCESSES` | Process Manager | Normal permission |
| `REQUEST_DELETE_PACKAGES` | Apps Manager | Normal permission, system confirms each uninstall |
| `POST_NOTIFICATIONS` | Notifications | Runtime |

---

## 1. WRITE_SETTINGS (Modify system settings)

**Core feature:** Battery Manager. It is a guided power-saving flow that lowers the settings that drain the battery most.

**What the app changes (and nothing else):**
- Screen brightness (`Settings.System.SCREEN_BRIGHTNESS`, `SCREEN_BRIGHTNESS_MODE`)


**Why it is required:** Android provides no alternative API for an app to change these system-wide values. Changing only the app window's brightness would not save battery once the user leaves the app.

**User control and transparency:**
- The permission is never requested at install or app start. It is requested only when the user taps Start in Battery Manager.
- Before opening Settings, the app shows its own prompt that names the exact settings that will be changed.
- The user grants the permission on the system "Modify system settings" page (`ACTION_MANAGE_WRITE_SETTINGS`) and can revoke it there at any time.
- If the user declines, the app does not apply these changes and nothing else is affected.

**Short text for the declaration form:**
> The app's Battery Manager feature lowers the screen brightness to extend battery life, at the user's request. These are system-wide settings that can be changed only with Modify System Settings access. The user grants it explicitly on the system settings page after an in-app explanation, and can revoke it at any time. No other system setting is modified and no data is collected.

---


## 2. KILL_BACKGROUND_PROCESSES

**Core feature:** Process Manager ("Task Killer"). On user tap, it calls `ActivityManager.killBackgroundProcesses` for cached background apps to free memory. It does not use Accessibility, does not kill foreground or system apps, and runs only on user request.

## 3. REQUEST_DELETE_PACKAGES

**Core feature:** Apps Manager. The user selects apps they want to remove, and the app launches the system uninstall dialog for each. The user confirms every uninstall in the system dialog. The app never uninstalls silently.

## 4. Storage access (no broad storage permission)

- No `MANAGE_EXTERNAL_STORAGE` and no `READ_MEDIA_*` permissions.
- Duplicates, AI Cleanup, WhatsApp Cleaner and Files use the Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`). The user picks a folder, such as DCIM or the WhatsApp media folder. The app can access only that folder, and the user can revoke it.
- `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` are declared with `maxSdkVersion="29"`. They apply only to Android 10 and lower and are inactive on Android 11 and newer.
- Files never leave the device.

## 5. POST_NOTIFICATIONS

Used only for the app's own cleaning and charge reminders. It is a runtime permission the user can decline.

---

## Suggested supporting material for review

1. **Short screen recording (30 to 60 seconds):** open Battery Manager, tap Start, the in-app explanation appears, the system "Modify system settings" page, return, the progress screens, the result.
2. **Privacy Policy:** state that the app does not collect or transmit file contents, and that the special accesses are used only for the features above.
3. **Store listing:** mention that Battery Manager lowers screen brightness and volume at the user's request.

## Notes for the team

- `WRITE_SETTINGS` is a special app access, not a Play "restricted" permission with a dedicated declaration form. Reviewers may still ask about them, and the text and video above answer that.
- Google does not guarantee approval. Keep the in-app prompts, the policy text and the actual behaviour consistent, because reviewers compare them.
