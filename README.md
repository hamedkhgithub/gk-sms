# GeniusKala SMS Alert v3

Independent SMS alert companion. It does NOT become the default SMS application and
does NOT cancel or modify notifications from Google Messages, Samsung Messages, etc.

## Features
- Multiple keywords/phrases
- Android ringtone/notification sound picker
- Vibration toggle
- Repeat alerts (0-5) with configurable interval
- Test alert button
- Android notification-channel settings shortcut
- GitHub Actions APK build

## Behavior
When Android delivers SMS_RECEIVED, the app joins multipart SMS bodies and checks the
message text against configured keywords. A match creates this app's own notification.

## Android caveat
RECEIVE_SMS is a sensitive/restricted permission for distribution-policy purposes.
Sideloaded/testing behavior can also vary by Android/OEM security settings. This project
does not attempt to suppress the phone's normal SMS notification.

## Build
Upload extracted contents to repository root, open Actions, run
"Build GeniusKala SMS Alert APK", then download the artifact.
