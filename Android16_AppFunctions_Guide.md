# Android 16 AppFunctions Implementation Guide & Blockers

This document serves as a knowledge base for implementing Android 16 (API 36) AppFunctions using the Jetpack `androidx.appfunctions` library. It details the required architecture, the role of AppSearch, and the specific limitations encountered in the `1.0.0-alpha08` version of the library.

## Architectural Overview

AppFunctions allow apps to expose specific, granular capabilities as "tools" that can be discovered and executed by AI agents (like Gemini) or other authorized system apps. It is effectively the mobile equivalent of the Model Context Protocol (MCP).

### Key Components

1.  **Dependencies:**
    *   `androidx.appfunctions:appfunctions`
    *   `androidx.appfunctions:appfunctions-service`
    *   `androidx.appfunctions:appfunctions-compiler` (KSP)

2.  **Target SDK:**
    *   The app must compile and target Android 16 (API 36).
    *   Requires a Java 17+ build environment and an updated Android Gradle Plugin (AGP 8.9.0+).

3.  **AppSearch & Assets (`app_functions_v2.xml`):**
    *   The Android OS uses the **AppSearch** framework to index exposed functions.
    *   The OS explicitly looks for an `app_functions_v2.xml` (and an associated `.xsd` schema) file inside the app's `assets/` directory.
    *   Without these XML assets, the AppSearch indexer silently fails to register the app's capabilities, leading to an `IllegalArgumentException: App function not found` when attempting to execute a function via ADB or Gemini.

4.  **Manifest & Service Declaration:**
    *   Do **NOT** use a custom `AppFunctionService` subclass in your manifest unless absolutely necessary for legacy fallback.
    *   The `alpha08` library automatically injects `androidx.appfunctions.service.PlatformAppFunctionService` into the merged manifest.
    *   This built-in service expects to read the specific `app_functions_v2.xml` asset to bridge the gap between AppSearch and the actual Kotlin function bindings.

## The `1.0.0-alpha08` Blocker

During implementation, a critical blocker was identified within the KSP compiler for `1.0.0-alpha08`:

*   **Missing XML Generation:** While the KSP compiler successfully generates the Kotlin reflection bindings (e.g., `AppFunctionInventory`), it **fails to automatically generate the required `app_functions_v2.xml` asset** in the correct output directory for the Android resource linker.
*   **Failed Workarounds:** 
    *   Attempting to pass KSP arguments like `arg("androidx.appfunctions.generateAppFunctionXml", "true")` or `arg("androidx.appfunctions.metadata_xml_path", "app_functions_v2.xml")` did not resolve the asset generation issue.
    *   Manually creating the `assets/app_functions_v2.xml` file bypasses the `FileNotFoundException` in the OS, but the strict internal validation of the v2 schema by the Android 16 system causes it to silently drop the manually crafted XML if it doesn't match the exact undocumented XSD structure expected by the system.

## Action Plan for Future Implementation

When revisiting this feature in the future, follow these steps to check if the blockers have been resolved:

1.  **Update Jetpack Libraries:** Check for newer versions of `androidx.appfunctions` (e.g., `alpha09`, `beta01`, or stable).
2.  **Verify KSP Output:** After updating, run a build (`./gradlew assembleDebug`) and verify that the KSP compiler correctly generates `app_functions_v2.xml` and `app_functions_schema.xsd` within the build tree and packages them into the final APK's `assets/` directory.
3.  **Test Execution via ADB:**
    *   List functions (to verify AppSearch indexing):
        ```bash
        adb shell cmd app_function list-app-functions --user 0
        ```
    *   Execute a function (to verify execution routing):
        ```bash
        adb shell cmd app_function execute-app-function --package com.github.muellerma.coffee.debug --function "com.github.muellerma.coffee.CoffeeAppFunctions#getStatus" --parameters "{}" --user 0
        ```
4.  **Clean up Manifest:** Ensure the custom `CoffeeAppFunctionService` is removed from `AndroidManifest.xml` and rely purely on the library's `PlatformAppFunctionService`.

Once the KSP compiler correctly generates the XML assets, the connection between Gemini, AppSearch, and the app's functions should work seamlessly.
