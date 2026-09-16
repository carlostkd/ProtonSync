# Proton Sync

**Proton Sync** is an open-source Android app that syncs your Proton Contacts and Proton Calendar into Android's native contacts & calendar providers. This lets you use your Proton data everywhere on your phone — the dialer, lock screen, smartwatch, and any calendar or contacts app. No Google account required.

- **Fully open source** (GPL-3.0)  audit the code, contribute, or fork it.
- **End-to-end encrypted**  all decryption happens locally on your device. Decrypted data never leaves your phone unencrypted.
- **Local-only storage**  synced contacts/events are written only to the Android system providers.

## Features

- Two-way sync of Proton Contacts to the Android Contacts provider
- One-way sync of Proton Calendar events to the Android Calendar provider
- Full E2E decryption on-device: user keys, address keys, and calendar keys are unlocked locally
- Local-only storage: no cloud backup of decrypted data
- Built-in sync logs for debugging and transparency

## Requirements

- Android 10 (API 29) or newer
- A active  Proton account  

## Installation

### Option 1: Install the APK (recommended for most users)

1. Download the latest release APK from the [Releases](https://github.com/carlostkd/ProtonSync/releases) page.
2. Transfer the APK to your Android device.
3. Open the APK file on your device.
4. If prompted, allow installation from your file manager/browser.
5. Launch the app and sign in with your Proton credentials.

### Option 2: Compile from source

If you prefer to build the app yourself:

#### Prerequisites

- Android Studio (latest stable version) OR
- JDK 17+ and Android SDK command-line tools
- Git

#### Build steps

```bash
# Clone the repository
git clone https://github.com/carlostkd/ProtonSync.git
cd ProtonSync/ProtonSyncApp

# Build debug APK (for testing)
./gradlew assembleDebug

# Build release APK (for distribution)
./gradlew assembleRelease
```

The compiled APKs will be in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk` (needs signing)

## Usage

1. **Sign in** with your Proton account credentials.
2. Toggle **Contacts** and/or **Calendar** sync switches.
3. Tap **Sync Now** to start syncing.
4. Check **Sync Logs** for detailed sync activity.
5. Access **About** for app info and source code link.

## Privacy & Security

- All cryptographic operations happen on-device
- Your Proton credentials are stored only in Android's secure storage
- Decrypted contact/calendar data goes only to the Android system providers
- No third-party analytics or telemetry
- Fully auditable open-source codebase

## Disclaimer

This is **NOT** an official Proton app. It is an independent community project and is not affiliated with, endorsed by, or supported by Proton AG. Use it at your own risk.

## Support & Donation

If you find this app useful and want to support its continued development, consider donating:

[💝 Donate via Stripe](https://donate.stripe.com/8wM6pe9DD99xgAofYZ)

Your support helps keep the project alive and enables future features!

## License

GPL-3.0 — see [LICENSE](LICENSE) for details.

