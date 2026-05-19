# AutoRenew Watch

AutoRenew Watch is a privacy-first, lightweight Android application that automatically detects subscriptions, recurring payments, EMIs, utility bills, and auto-debits by parsing incoming and historical SMS alerts *locally* on the device.

## Core Features
* **Privacy-First:** 100% offline-first execution. Zero server uploads for SMS data.
* **Performance:** Optimized background processing using `WorkManager` for bulk historical SMS scanning and `BroadcastReceiver` for real-time parsing.
* **Indian Financial Ecosystem:** Custom regex parsers specifically tailored for standard Indian banks (HDFC, SBI, ICICI, etc.) and popular merchants (Netflix, Spotify, LIC, SIPs).
* **Modern Stack:** Built entirely with Kotlin, Jetpack Compose (Material 3), MVVM Architecture, Hilt, and Room Database.

## Project Structure
This is a modern Android multi-module project managed via Gradle Kotlin DSL and Version Catalogs (`libs.versions.toml`).
- `:app`: The base application module linking everything together via Hilt.
- `:core:database`: Contains the Room DB schema for tracking subscriptions, transaction patterns, and user settings offline securely using AndroidX Security Crypto.
- `:feature:sms-scanner`: Houses the regex parsing logic and background Android services responsible for extracting transaction details from raw SMS texts.

## How to Run This App

The recommended way to run and build this project is via **Android Studio**.

### 1. Open the Project
1. Launch **Android Studio**.
2. Select **Open** (or **File > Open** if another project is already active).
3. Navigate to the `AutoRenewWatch` project directory and select it.
4. Allow Android Studio to complete the initial **Gradle Sync**. This will download necessary dependencies like Hilt, Room, and Compose.

### 2. Set Up a Device
- **Physical Device:** Connect your Android device via USB. Make sure **Developer Options** and **USB Debugging** are enabled on your phone.
- **Emulator:** Alternatively, open the **Device Manager** in Android Studio and create a Virtual Device running Android 8.0 (API 26) or higher.

### 3. Build & Run
1. Ensure the **`app`** module is selected in the run configuration dropdown at the top of Android Studio.
2. Ensure your target device/emulator is selected next to it.
3. Click the **Run** button (green play icon) or press `Control + R`.
4. The project will compile and install onto the device.

*(Note: To build from the terminal directly, you can run `./gradlew installDebug` in the root directory once Android Studio has generated the Gradle wrapper files).*
