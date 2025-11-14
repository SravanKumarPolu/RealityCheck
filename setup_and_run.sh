#!/bin/bash
# RealityCheck Project Setup and Run Script

echo "🚀 RealityCheck - Setting up and running project..."
echo ""

# Check for Java
if command -v java &> /dev/null; then
    echo "✅ Java found: $(java -version 2>&1 | head -1)"
    export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
else
    echo "❌ Java not found"
    echo ""
    echo "📥 To install Java, run one of:"
    echo "   Option 1: Download Android Studio (includes JDK):"
    echo "   https://developer.android.com/studio"
    echo ""
    echo "   Option 2: Install JDK 17 via Homebrew:"
    echo "   brew install openjdk@17"
    echo "   export JAVA_HOME=/opt/homebrew/opt/openjdk@17"
    echo ""
    exit 1
fi

# Check for Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo "✅ ANDROID_HOME set: $ANDROID_HOME"
elif [ -d "$HOME/Library/Android/sdk" ]; then
    export ANDROID_HOME="$HOME/Library/Android/sdk"
    echo "✅ Android SDK found: $ANDROID_HOME"
elif [ -d "$HOME/Android/Sdk" ]; then
    export ANDROID_HOME="$HOME/Android/Sdk"
    echo "✅ Android SDK found: $ANDROID_HOME"
else
    echo "❌ Android SDK not found"
    echo ""
    echo "📥 To install Android SDK:"
    echo "   1. Install Android Studio from: https://developer.android.com/studio"
    echo "   2. Open Android Studio → SDK Manager → Install SDK"
    echo "   3. Or set ANDROID_HOME environment variable"
    echo ""
    exit 1
fi

# Try to run Gradle
echo ""
echo "🔨 Building project..."
cd "$(dirname "$0")"
./gradlew assembleDebug --no-daemon 2>&1 | tail -20

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 To install and run:"
    echo "   ./gradlew installDebug"
    echo ""
    echo "   Make sure you have an Android device/emulator connected:"
    echo "   adb devices"
else
    echo ""
    echo "❌ Build failed. See errors above."
fi
