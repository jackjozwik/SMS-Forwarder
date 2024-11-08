# Android SMS Email Forwarder

A simple Android application that forwards incoming SMS messages to an email address. Perfect for IoT projects, backup purposes, or monitoring SMS messages remotely.

## Features

- Forwards incoming SMS to a specified email address
- Places SMS content in email subject line for easy parsing
- Runs as a background service with notification
- Persists through app closure and phone restarts
- Simple interface for email configuration
- Secure credential storage

## Setup

### Prerequisites

- Android Studio
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Gmail account with App Password configured

### Building the Project

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build the project

### Required Permissions

```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

## Usage

1. Launch the app
2. Enter your email credentials:
   - Destination Email: Where you want to receive forwards
   - Sender Email: Gmail account used to send forwards
   - App Password: Gmail app-specific password
3. Save settings
4. Toggle the service on

The app will now forward all incoming SMS messages to your specified email.

### Email Format

- Subject: `SMS from {sender}: {truncated_message}`
- Body: Full message with sender details

## Testing

You can test the app using the Android Emulator by sending test SMS messages:

```bash
# Using ADB to send test SMS
adb emu sms send <phone_number> "Your test message"

# Example
adb emu sms send 555555 "Use verification code 322749 for Microsoft authentication"
```

## Building Release APK

To create an installable APK:

1. In Android Studio: Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Find the APK in: `app/build/outputs/apk/debug/app-debug.apk`
3. Transfer to phone and install

Note: Enable "Install from Unknown Sources" on your device to install the APK.

## ⚠️ Security Notice - Proof of Concept

This application is a **proof of concept** and has several security considerations that make it unsuitable for production use without modifications:

- Credentials are stored in SharedPreferences which is not a secure storage solution
- Email credentials are stored in plaintext
- No encryption is implemented for stored data
- No authentication is required to access the app settings
- Messages are transmitted without end-to-end encryption

**Recommended Security Improvements for Production Use:**
- Use Android Keystore for secure credential storage
- Implement encryption for stored credentials
- Add local authentication (biometric/pin) before accessing settings
- Add message encryption
- Implement secure error logging
- Add rate limiting and abuse prevention

Use this application for educational purposes or as a base for developing a more secure solution.

## Known Issues

- Some Android manufacturers may have additional battery optimization settings that need to be disabled
- Email sending is dependent on network connectivity

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaMail for Android
- Android Foreground Service implementation
