# StudBud
Hello! Welcome to StudBud! This is an application developed as a final project for NUS High's CS4131 module (last run), but there are plans to continue developing this application in the future!
This app was fully made by me! Click [here](https://github.com/Keanu365/StudBud/releases/download/v1.0.0-alpha/studbud-v1.0.0-alpha.apk) to download the APK file for this project!
All features:
* Study Timer! Based off of the Pomodoro method, for each session, set the length of each study period, the length of breaks between each study period, and the number of study periods! You may choose to study an assignment on your list, or simply have a general study session.
* Groups. These are "classes" native to StudBud. After you start a group, you can share the group join code with friends so that they can join your group!
* Assignments list! All assignments posted by your group will be shown here! Clicking on each assignment will show more details, where you can start a study session with the assignment.
* Studs. Every minute that you study for will earn you 1 stud! If you are in a StudBud group, there will be a group leaderboard ranked by all-time studs! You can always check the number of studs you have through the...
* Profile Page! View and edit your profile picture, and view your statistics!
* More features coming soon!

## Special Thanks
This project uses [Supabase](https://supabase.com) as its primary database and [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging) to send push notifications! It also uses the [Ktor Client](https://ktor.io) and [kotlinx-serialization](https://github.com/kotlin/kotlinx.serialization) to handle Supabase operations, among other things!

Thank you to the following GitHub repositories for helping me out with my project and its features!
* [ConnectivityKMP](https://github.com/KhubaibKhan4/ConnectivityKMP)
* [ImagePickerKMP](https://github.com/ismoy/ImagePickerKMP)
* [KMPNotifier](https://github.com/mirzemehdi/KMPNotifier)
* [SupabaseKT](https://github.com/supabase-community/supabase-kt)
* [Zoomable](https://github.com/usuiat/Zoomable)

## KOTLIN MULTIPLATFORM
This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
