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

This code can't be installed onto your phone directly from this chat session
— it needs to be compiled on a computer. Steps:

1. Install **Android Studio** (free, from developer.android.com/studio) on a
   Windows/Mac/Linux computer.
2. Clone this repository and open the project folder in Android Studio.
   Let it sync/download the first time (needs internet).
3. Connect your Galaxy S22 by USB, with **Developer options** and **USB
   debugging** turned on (Settings → About phone → tap "Build number" 7
   times, then Settings → Developer options → USB debugging).
4. Press the green "Run" button in Android Studio, select your phone, and it
   installs and launches the app.
5. Long-press your home screen → Widgets → find "To-Do" → drag it onto your
   home screen.

Alternatively, Android Studio can build an `.apk` file (Build → Build Bundle
/ APK → Build APK) that you can transfer to the phone and install manually
(you'll need to allow "install unknown apps" for whichever app you use to
open the file).

## Known limitations (v1)

- No cloud sync / backup — data lives only on this phone. If you lose the
  phone or uninstall the app, tasks are gone. (Fine for now per the "no
  existing data to preserve" starting point — worth revisiting once you rely
  on this daily.)
- No reordering/drag-and-drop yet, no due dates or reminders.
- The widget shows the whole list; there's no way yet to configure the
  widget to show only some tasks.
