# 🚀 Quick Start - Run RealityCheck

## Prerequisites Missing

To run this Android project, you need:

### 1. Java/JDK 17+
**Install via one of these methods:**

**Option A: Android Studio (Recommended - includes everything)**
```bash
# Download and install from:
open https://developer.android.com/studio

# After installation:
open -a "Android Studio" /Users/sravanpolu/Projects/RealityCheck
```

**Option B: JDK via Homebrew**
```bash
# Fix Homebrew permissions first:
sudo chown -R $(whoami) /usr/local/Homebrew

# Then install JDK:
brew install openjdk@17

# Set JAVA_HOME:
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

### 2. Android SDK

**If you installed Android Studio:**
- Open Android Studio
- Go to: Settings/Preferences → Appearance & Behavior → System Settings → Android SDK
- Install Android SDK Platform 34
- Set ANDROID_HOME:
  ```bash
  export ANDROID_HOME=$HOME/Library/Android/sdk
  export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
  ```

## Running the Project

### Method 1: Android Studio (Easiest)
1. Open Android Studio
2. File → Open → Select `/Users/sravanpolu/Projects/RealityCheck`
3. Wait for Gradle sync
4. Click Run ▶️ button
5. Select an emulator or connected device

### Method 2: Command Line
```bash
cd /Users/sravanpolu/Projects/RealityCheck

# Make sure Java and Android SDK are set up first
export JAVA_HOME=/path/to/jdk
export ANDROID_HOME=$HOME/Library/Android/sdk

# Build the project
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Check connected devices
adb devices
```

## Current Project Status

✅ Gradle wrapper configured
✅ Project structure complete
✅ All source files ready
❌ Java/JDK not installed
❌ Android SDK not installed

## Next Steps

1. **Install Android Studio** (recommended - includes everything):
   - Visit: https://developer.android.com/studio
   - Download and install
   - Open the project in Android Studio
   - Click Run ▶️

2. **Or install tools separately:**
   - Install JDK 17+
   - Install Android SDK
   - Run `./setup_and_run.sh`

The project is ready - you just need the development tools installed!
