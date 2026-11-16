# Phone Cleaner AI Cleaner Pro: release notes and checklist

## Permissions and why (for the Play Console declarations)

| Permission | Feature | Notes |
|---|---|---|
| `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` (`READ_EXTERNAL_STORAGE` up to API 32) | Duplicates, Files (Images, Video, Audio) | Asked only when the feature opens |
| `MANAGE_EXTERNAL_STORAGE` | Junk Cleaner, WA Cleaner, AI Cleanup, Files (Document, Apk, Download, folder browser) | File manager and cleaner core function; needs the All files access declaration |
| `WRITE_EXTERNAL_STORAGE` (max API 28) | Photo Compressor save on Android 9 and below | Not requested on newer versions |
| `REQUEST_DELETE_PACKAGES` | Apps Manager uninstall (system confirmation) | Never silent |
| `PACKAGE_USAGE_STATS` | Process Manager "Recently used apps" | Optional, user grants in Settings |
| `WRITE_SETTINGS` | Battery Manager brightness and screen timeout | Optional, user grants in Settings |
| `KILL_BACKGROUND_PROCESSES` | Process Manager memory request | Normal permission |
| `POST_NOTIFICATIONS`, `INTERNET`, `ACCESS_NETWORK_STATE` | Notifications, ads, billing, analytics | |

Removed on purpose: `QUERY_ALL_PACKAGES` (Apps Manager now uses a launcher `<queries>` entry, so it lists apps that have a launcher icon), and no permission is used for Photo Compressor (system photo picker).

## Still to do before upload
- Replace placeholders: `google-services.json`, AdMob app ID and ad unit IDs (test IDs today), billing product IDs, AppsFlyer dev key, `Constants.SUPPORT_EMAIL`, privacy and terms URLs.
- Create the release keystore (JKS) and sign the bundle.
- Billing: keep any debug price log inside `tryCatch`; call `setupConnection` for the billing helper.
- Verify Remote Config values, FCM notification icon (`ic_notification`) and colour (`color_app`).
- Check the "What's new" text, update `versionCode` and `versionName`.
- Translations for new strings (English only today).
