# Material Kolor Demo

<img width="700px" src="../art/demo.png" />

## Before running!

- check your system with [KDoctor](https://github.com/Kotlin/kdoctor)
- install JDK 8 on your machine
- install
  the [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile)

## Running

Open the project in Android Studio and let it sync the project. In the configuration dropdown should
be the following:

- androidApp
- iosApp

You can select a configuration and run it, or follow the steps below.

### Android

To run the application on android device/emulator:

- open project in Android Studio and run imported android run configuration

To build the application bundle:

- run `./gradlew :composeApp:assembleDebug`
- find `.apk` file in `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### iOS

To run the application on iPhone device/simulator:

- Open `iosApp/iosApp.xcworkspace` in Xcode and run standard configuration
- Or
  use [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile)
  for Android Studio
