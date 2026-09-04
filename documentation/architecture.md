# Architecture

## What this is

A native Android to-do list app for the Galaxy S22, with:
- Tasks that can be broken down into subtasks (a task can have subtasks, which
  can themselves have subtasks — an unlimited tree).
- A home screen widget that shows the list and lets you check tasks off
  without opening the app.

## Why native Android (not a web app or cross-platform framework)

A home-screen widget is an OS-level feature. On Android it only exists for
apps built with the Android SDK (Kotlin/Java) — a website or a plain
cross-platform app cannot place a widget on the home screen. That's why this
is a real Android Studio project rather than something simpler.

## Tech stack

- **Kotlin** — the language.
- **Jetpack Compose** — builds the in-app screen (the task list, add/edit,
  checkboxes).
- **Room** — a local SQLite database stored on the phone. All data lives on
  the device only; nothing is synced anywhere.
- **Jetpack Glance** — the framework for building the home screen widget UI
  (widgets can't use regular Compose views, Glance is Android's Compose-style
  toolkit specifically for widgets/notifications).

## Data model

One table, `tasks`:

| column     | meaning                                              |
|------------|-------------------------------------------------------|
| id         | unique id                                              |
| title      | task text                                              |
| isDone     | checked or not                                         |
| parentId   | null for a top-level task, otherwise the parent task's id |
| position   | ordering                                                |
| createdAt  | timestamp                                               |

Subtasks are just tasks whose `parentId` points at another task, so the same
table represents both top-level tasks and any depth of subtask without a
separate "subtask" concept.

## App structure

```
app/src/main/java/com/family2/todo/
  data/            Task entity, Room DAO/database, TaskRepository
  ui/              TodoViewModel, TodoScreen (Compose UI)
  widget/          TodoWidget (Glance), TodoWidgetReceiver
  MainActivity.kt
  TodoApp.kt       Application class, wires up the database + repository
```

The widget and the app share the same Room database file on the phone, so
checking a task off in the widget updates the app (and vice versa) — the
widget calls `TodoWidget().updateAll()` after every change, and the app does
the same after edits made in-app.

## Building and installing it

This code can't be installed onto your phone from a chat session — it has to
be compiled on a computer with Android Studio.

### One-time setup

1. Install **Android Studio** (free, from developer.android.com/studio).
2. Clone this repository and open the *project folder* in Android Studio
   (File → Open, pick the folder containing `settings.gradle.kts` — not a
   file inside it).
3. Wait for the first Gradle sync to finish. It downloads several hundred MB
   of Android SDK and libraries and can take 10–20 minutes on a first run.
   If Android Studio offers to install a missing SDK component or accept
   SDK licences, say yes.

### Building an APK (no USB cable needed)

1. Menu: **Build → Generate App Bundles or APKs → Generate APKs**.
   (In older Android Studio versions this menu reads **Build Bundle(s) /
   APK(s) → Build APK(s)** — same thing.) Don't pick *Generate Signed App
   Bundle or APK...* just below it; that's the release path and it will ask
   you to create a signing key you don't need.
2. When it finishes, a notification appears in the bottom-right: "APK(s)
   generated successfully" with a **locate** link. That opens the folder:

   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

   (You can also just navigate there in your file manager.)

3. Get that file onto the phone by any route you like — upload it to Google
   Drive / Dropbox and download it on the phone, email it to yourself, or
   send it over Bluetooth.
4. On the phone, open the downloaded file. Android will say the app you're
   opening it with (Files, Chrome, Drive) isn't allowed to install unknown
   apps — tap **Settings**, turn the permission on for that app, go back,
   and tap **Install**.
5. Long-press the home screen → **Widgets** → find "To-Do" → drag it onto
   the home screen.

### Debug vs release APKs — important

**Generate APKs** builds whichever build variant is selected, and the default
is **debug**. That's what you want: a debug APK is automatically signed with
Android Studio's built-in debug key, so it installs on the phone as-is.

If you switch the Build Variant to **release**, the APK it produces is
*unsigned* and the phone will refuse to install it ("App not installed" /
"package appears to be invalid"). A release build needs a signing key
created via Build → Generate Signed App Bundle or APK. There's no benefit
here — debug builds run at full speed for an app like this. Stick with debug
unless you ever want to publish to the Play Store.

Practical consequences of the debug key: the APK is signed with a key
Android Studio generated on that specific computer, so always rebuild from
the same machine. If you later build on a different computer, its debug key
differs and the phone will refuse to *update* the app — you'd have to
uninstall first, which deletes the tasks (there's no backup yet).

### If you do get a cable later

Turn on Developer options and USB debugging on the phone (Settings → About
phone → tap "Build number" seven times, then Settings → Developer options →
USB debugging), plug it in, and press the green **Run** button. That builds,
installs and launches in one step — much faster to iterate with than
copying APKs around.

### Building from a terminal (optional)

The project includes the Gradle wrapper, so you don't need Gradle installed:

```
./gradlew assembleDebug          # Mac/Linux
gradlew.bat assembleDebug        # Windows
```

The APK lands in the same `app/build/outputs/apk/debug/` folder. You still
need the Android SDK installed (Android Studio provides it); if Gradle can't
find it, create a file `local.properties` in the project root containing
`sdk.dir=` followed by your SDK path.

## Known limitations (v1)

- No cloud sync / backup — data lives only on this phone. If you lose the
  phone or uninstall the app, tasks are gone. (Fine for now per the "no
  existing data to preserve" starting point — worth revisiting once you rely
  on this daily.)
- No reordering/drag-and-drop yet, no due dates or reminders.
- The widget shows the whole list; there's no way yet to configure the
  widget to show only some tasks.
