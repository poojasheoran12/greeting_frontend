# Greeting App

A modern, offline-first Android application that allows users to create, customize, and share personalized greeting cards for various occasions (Birthdays, Anniversaries, Festivals, etc.). Users can browse templates, overlay their profile photo and name onto the cards, and share the final generated images seamlessly.

## 📹 App Demo
Check out the video recording of the app in action: [Watch Demo on Google Drive](https://drive.google.com/file/d/13vEyil1lhCzKDoUhKwsTFMBh5t3ZiH6m/view?usp=drivesdk)

> **⚠️ Note:** The demo was recorded on an extremely slow network connection, so template images may appear to load slowly. On a normal network, loading is significantly faster thanks to the app's offline-first Room + Paging 3 cache.

## 🚀 Features
*   **User Authentication:** Secure email/password and Google Sign-in utilizing Firebase Authentication.
*   **Template Gallery:** Browse categorized greeting templates (e.g., Diwali, Birthdays) with smooth horizontal scrolling.
*   **Offline-First:** Templates are cached locally using Room and Paging 3's `RemoteMediator` for a seamless experience even without an internet connection.
*   **Live Preview & Image Generation:** Instantly preview how your name and profile picture look on a selected template. The app uses Android's native `Canvas` API to render and export high-quality, shareable Bitmaps.
*   **Profile Management:** Set and edit your display name and upload a profile picture (stored in Firebase Storage).
*   **Premium Content:** Distinct UI elements to identify and upsell premium greeting templates.

## 🛠 Tech Stack
*   **Architecture:** Clean Architecture with MVVM (Model-View-ViewModel)
*   **UI:** Jetpack Compose (Material 3)
*   **Asynchronous Programming:** Kotlin Coroutines & Flow (StateFlow, SharedFlow)
*   **Dependency Injection:** Dagger Hilt
*   **Remote Data:** Firebase Firestore
*   **Authentication:** Firebase Auth
*   **Storage:** Firebase Storage
*   **Local Database:** Room Database
*   **Pagination:** Paging 3
*   **Image Loading:** Coil

## ⚙️ Setup Instructions

Follow these steps to build and run the project locally:

### 1. Prerequisites
*   Android Studio (Jellyfish or newer recommended).
*   Java Development Kit (JDK) 17.
*   A Firebase Account.

### 2. Firebase Configuration
To run this app, you must connect it to your own Firebase project:
1.  Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2.  Add an Android app to the project with the package name: `com.example.greeting`.
3.  Download the `google-services.json` file.
4.  Place the `google-services.json` file in the `app/` directory of your project (`AndroidStudioProjects/Greeting/app/`).

### 3. Enable Firebase Services
In your Firebase Console, ensure the following services are enabled and configured:
*   **Authentication:** Enable the **Email/Password** and **Google** sign-in providers.
*   **Firestore Database:** Create a database. Start in Test Mode or configure proper security rules. You will need a `templates` collection and a `users` collection.
*   **Storage:** Enable Firebase Storage to allow users to upload profile pictures.

### 4. Build and Run
1.  Clone the repository and open it in Android Studio.
2.  Allow Gradle to sync and download all dependencies.
3.  Connect an Android device or start an emulator.
4.  Click the **Run** button (`Shift + F10`) to build and launch the app.

## 📁 Project Structure (Clean Architecture)
*   **`data/`**: Handles data retrieval, mappers, DTOs, Room DAOs, and Firebase implementations.
*   **`domain/`**: Contains the core business logic, domain models, and repository interfaces.
*   **`presentation/`**: Contains the UI logic, Jetpack Compose screens, ViewModels, and navigation graphs.
*   **`core/`**: Utility classes, such as the `GreetingBitmapRenderer` for canvas drawing and file sharing.
*   **`di/`**: Hilt modules for dependency injection.
