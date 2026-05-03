# Technical Approach Document

## 1. Problem-Solving Approach: Image Overlay Logic
The core feature of the Greeting App relies on seamlessly merging user profile data (name and photo) onto a selected template image. This overlay logic was implemented using two distinct mechanisms to ensure parity between the UI preview and the final exported image:

*   **UI Preview (Jetpack Compose):** We utilized `Box` layouts to stack elements on the Z-axis. By combining `Modifier.offset` and `Modifier.align`, the profile photo was precisely positioned to overlap the intersection of the newly added black header bar and the template image. `AsyncImage` from Coil handled the asynchronous loading and caching of both the template and the user photo.
*   **Export Rendering (`GreetingBitmapRenderer`):** To generate a shareable image, we replicated the Compose layout using Android's native `Canvas` API. We calculate the total dimensions (header height + template height) and draw a solid black `#1A1A1A` background for the header. The text is drawn using `Paint` with specific bounds, and the profile photo is painted as a circular bitmap with a thick white stroke (simulating the UI border) precisely at the required coordinate offset.

## 2. Tech Stack
The application is built using modern Android development practices and libraries:

*   **Core Architecture:** Clean Architecture principles combined with the MVVM (Model-View-ViewModel) pattern.
*   **UI Framework:** Jetpack Compose (Material 3) for declarative and reactive user interfaces.
*   **Language:** Kotlin, utilizing Coroutines and StateFlow for asynchronous programming and state management.
*   **Dependency Injection:** Hilt / Dagger for managing dependencies across the application.
*   **Remote Data & Authentication:** Firebase (Firestore for template data, Firebase Auth for Email/Google authentication, Firebase Storage for profile images).
*   **Local Caching:** Room Database, integrated with Paging 3 for offline-first capabilities and efficient list loading.
*   **Image Loading:** Coil for fetching and caching remote images efficiently within Compose.

## 3. Challenges Faced & Overcome
Several technical hurdles were addressed during development:

*   **Data Model Deserialization (`isPremium` Not Recognized):** During the transition from Firebase documents to local models, we faced issues where the `isPremium` flag was not mapping correctly due to naming conventions and missing fields in DTOs. This required creating dedicated mapper files (`TemplateMapper.kt`) to ensure safe data extraction, utilizing Elvis operators (`?: false`) to handle nullability robustly.
*   **Paging State & Scroll Persistence:** When loading templates via Paging 3 in horizontal `LazyRow` components, we encountered state leakage where scrolling one category affected another. This was resolved by dynamically generating a unique `LazyListState` keyed to the category title using `remember(title) { LazyListState() }` alongside utilizing `key`s in the outer `LazyColumn`.
*   **Pagination Ordering Bug:** Appending new pages via the `RemoteMediator` initially reset the `orderInRoom` index to zero, causing templates to render out of order. We fixed this by dynamically calculating the current item count in the local database before appending new entities to maintain a consistent sequence.


## 4. Future Improvements & Scalability
To further enhance the application and prepare for a growing user base, the following improvements are planned:

*   **Advanced Customization:**
    *   **Move & Resize:** Implement gesture detectors (`detectTransformGestures`) allowing users to drag, scale, and rotate their profile photo and name on the template dynamically.
    *   **Dynamic Data Fields:** Add support for incorporating additional user details into the template, such as an Address, Location, or custom personal messages.
*   **Enhanced Engagement & AI Features:**
    *   **Push Notifications & Deep Linking:** Implement Firebase Cloud Messaging (FCM) to notify users of new seasonal templates (e.g., Diwali, New Year) and use deep links to navigate directly to the specific template.
    *   **AI-Generated Messages:** Integrate a generative AI API to automatically suggest personalized greeting text based on the occasion and recipient.


