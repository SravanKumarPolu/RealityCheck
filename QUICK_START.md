# 🚀 Quick Start - Run RealityCheck

## Prerequisites

You need the following tools to run this Android project:

1. **Android Studio** (recommended - includes everything)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK, Gradle, and JDK
   - Or check if already installed: `/Applications/Android Studio.app`

2. **JDK 17 or higher**
   - Android Studio includes JDK, or install separately
   - Via Homebrew: `brew install openjdk@17`
   - Set JAVA_HOME: `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`

3. **Android SDK**
   - Usually installed with Android Studio
   - Or install via command line tools
   - Set ANDROID_HOME: `export ANDROID_HOME=$HOME/Library/Android/sdk`
   - Add to PATH: `export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools`

## Quick Start (Using Android Studio)

1. **Open the project:**
   ```bash
   # Open Android Studio
   # File → Open → Select /Users/sravanpolu/Projects/RealityCheck
   ```

2. **Let Gradle sync:**
   - Android Studio will automatically download dependencies
   - Wait for "Gradle Sync" to complete

3. **Set up an emulator or device:**
   - **Emulator**: Tools → Device Manager → Create Virtual Device
   - **Physical Device**: Enable Developer Options & USB Debugging

4. **Run the app:**
   - Click the green "Run" button (▶️)
   - Or press `Shift + F10` (Windows/Linux) or `Control + R` (Mac)

## Alternative: Command Line

### Build and Install

```bash
cd /Users/sravanpolu/Projects/RealityCheck

# Make gradlew executable (if needed)
chmod +x ./gradlew

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Or run directly
./gradlew installDebug && adb shell am start -n com.realitycheck.app/.MainActivity
```

### Check for Connected Devices

```bash
# Check if device/emulator is connected
adb devices

# If no devices, start an emulator:
emulator -list-avds
emulator -avd <AVD_NAME>
```

## Troubleshooting

### Issue: "Gradle not found"
- **Solution**: Install Android Studio (includes Gradle) or install Gradle separately
- On macOS: `brew install gradle`

### Issue: "SDK not found"
- **Solution**: 
  1. Install Android Studio (easiest)
  2. Or install SDK via command line tools
  3. Set `ANDROID_HOME` environment variable

### Issue: "Java not found"
- **Solution**: 
  1. Install JDK 17+ (`brew install openjdk@17`)
  2. Set `JAVA_HOME` environment variable
  3. Or use Android Studio's bundled JDK

### Issue: "No devices found"
- **Solution**: 
  1. Start an Android emulator from Android Studio
  2. Or connect a physical device with USB debugging enabled

## Project Configuration

- **Package**: `com.realitycheck.app`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Gradle Version**: 8.2
- **Kotlin Version**: 1.9.20

## Recommended Approach

**For development, use Android Studio** - it handles:
- ✅ Gradle wrapper generation
- ✅ Android SDK setup
- ✅ Emulator management
- ✅ Debugging tools
- ✅ Code completion and IntelliSense

## Current Project Status

✅ Gradle wrapper configured  
✅ Project structure complete  
✅ All source files ready  
✅ Ready to build and run
