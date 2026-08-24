# CS360
SNHU CS360 Submission
Inventory Management Mobile Application

## Briefly summarize the requirements and goals of the app you developed. What user needs was this app designed to address?
This mobile application was developed to provide casual and small business operators with an efficient, reliable tool for tracking inventory in their garage or home. The primary user need addressed is reducing human error and search time in stock retrieval for orders. Users must be able to securely authenticate, view their stock quantities, log updates and not have to do so being tethered to a workstation or and overcomplicated app.

## What screens and features were necessary to support user needs and produce a user-centered UI for the app? How did your UI designs keep users in mind? Why were your designs successful?
To support frictionless user workflows, the interface was organized across three primary screens:

Authentication Screen: Simple, secure login and user registration that protects local store data while keeping credential entry fast and straightforward.

Inventory Dashboard: A clean, scrollable dynamic list (RecyclerView / GridLayout) showing current items, stock levels, and immediate visual status indicators for depleted stock.

Item Management Interface: Dedicated input dialogs and forms for CRUD operations that dynamically adjust above the soft keyboard (adjustResize), preventing input fields and action buttons from being obscured during typing.

The UI design was successful because it prioritized operational visibility: high-contrast typography, large touch targets for industrial and handheld environments, and consistent layout constraints ensured predictable responsiveness across varied Android device densities.

## How did you approach the process of coding your app? What techniques or strategies did you use? How could those techniques or strategies be applied in the future?
The development process followed an interwoven pipeline strategy rather than building isolated features in a vacuum:

Separation of Concerns: Isolated data persistence (SQLiteOpenHelper CRUD methods) from UI lifecycle controllers (Activity and adapter classes).

Least Privilege Permissions: Handled Android runtime permissions (such as SMS/notification alerts for low inventory thresholds) defensively, ensuring the core app remains functional even if permissions are declined.

Reusability in Future Projects: Establishing a clear contract between the data layer and view adapters established a modular template that can be ported directly into future native mobile architectures (such as migrating to Jetpack Room or integrating REST/GraphQL APIs).

## How did you test to ensure your code was functional? Why is this process important, and what did it reveal?
Testing involved a combination of unit-level SQLite CRUD verification, Android Studio Logcat monitoring, and manual emulation and hardware keyboard configurations:

Lifecycle & State Preservation: Verified that data persisted accurately across device rotations, app minimizes, and cold restarts.

Permission Fallbacks: Manually toggled runtime permissions off in device settings to verify that SMS failure paths degraded gracefully without application crashes.

Importance: This testing revealed edge-case collisions between dynamic UI components and soft input keyboards, enabling proactive layout adjustments before final APK compilation.

## Consider the full app design and development process from initial planning to finalization. Where did you have to innovate to overcome a challenge?
The primary technical challenge involved managing UI responsiveness simultaneously with persistent database transactions. Specifically, bridging persistent SQLite queries with dynamic card layouts when the soft input keyboard appeared required innovating layout hierarchy constraints. Overcoming this required fine-tuning window insets and view hierarchy weights so that active input fields remained completely visible and interactive during continuous database updates.

## In what specific component of your mobile app were you particularly successful in demonstrating your knowledge, skills, and experience?
I was particularly successful in architecting the end-to-end data persistence and UI synchronization layer. Structuring a clean relational SQLite schema, wiring asynchronous query responses directly into the dynamic list adapter, and handling atomic updates for low-stock triggers demonstrated a robust grasp of native Android lifecycle management, database integrity, and production-ready mobile architecture.









