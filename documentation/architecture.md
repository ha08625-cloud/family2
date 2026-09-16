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

1. Menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
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

**Build APK(s)** builds whichever build variant is selected, and the default
is **debug**. That's what you want: a debug APK is automatically signed with
Android Studio's built-in debug key, so it installs on the phone as-is.

If you switch the Build Variant to **release**, the APK it produces is
*unsigned* and the phone will refuse to install it ("App not installed" /
"package appears to be invalid"). A release build needs a signing key
created via Build → Generate Signed Bundle / APK. There's no benefit here —
debug builds run at full speed for an app like this. Stick with debug unless
you ever want to publish to the Play Store.

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

### Automatic builds on GitHub (no computer needed)

Every push to GitHub triggers the workflow in
`.github/workflows/build-apk.yml`, which compiles the project on GitHub's
own machines and attaches the finished APK to the run. This means:

- Compile errors show up on GitHub within a couple of minutes of a push, so
  they can be found and fixed without anyone opening Android Studio.
- A ready-to-install APK can be downloaded from GitHub directly onto the
  phone — Android Studio is no longer needed just to produce a build.

To get the APK: on github.com open the repository → **Actions** tab → click
the most recent **Build APK** run → scroll to **Artifacts** at the bottom →
download `family2-debug-apk-<number>`. It arrives as a .zip; unzip it to get
`app-debug.apk`, then install that as described above. A green tick next to
the run means it built; a red cross means it didn't compile, and clicking
the run shows the error.

The workflow builds the *debug* APK (see the debug vs release note above),
uses Java 17 to match the project's settings, and needs no configuration or
secrets. Artifacts are kept by GitHub for 90 days.

### The fast loop: Claude Code on the laptop, phone over Wi-Fi

The loop above (Android Studio -> APK -> upload -> download -> install) takes
a few minutes per change. This one takes seconds, and lets Claude Code build
the app, install it on the phone and read the phone's crash logs by itself.

It needs Claude Code running **on the Windows laptop**, not in a browser,
because the phone is attached to the laptop. Set up once, then use daily.

#### One-time setup

**1. Turn on wireless debugging on the phone**

- Settings -> About phone -> Software information -> tap **Build number**
  seven times. It will say "Developer mode has been enabled".
- Settings -> Developer options -> turn on **Wireless debugging**.
- The phone and the laptop must be on the *same* Wi-Fi network. A guest
  network usually will not work, because it blocks devices from seeing each
  other.

**2. Find adb on the laptop**

`adb` is the tool that talks to the phone. Android Studio already installed
it, at:

```
%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe
```

Add that folder to the PATH so `adb` works from any terminal: press Start,
type "environment variables", open **Edit the system environment variables**
-> **Environment Variables** -> under *User variables* select **Path** ->
**Edit** -> **New** -> paste the folder path (without `\adb.exe`) -> OK.
Close and reopen any terminal, then check it worked:

```
adb version
```

**3. Pair the phone with the laptop (once)**

On the phone: Developer options -> Wireless debugging -> **Pair device with
pairing code**. A box appears showing a 6-digit code and an address like
`192.168.1.42:37183`.

In a terminal on the laptop, using *that* address:

```
adb pair 192.168.1.42:37183
```

It asks for the pairing code; type the 6 digits from the phone.

**4. Connect (needed again after each reboot)**

Important gotcha: the pairing screen and the main Wireless debugging screen
show **two different port numbers**. Pairing is done. Now go back to the main
**Wireless debugging** screen and read the address shown there — same IP,
different port, e.g. `192.168.1.42:41235` — and run:

```
adb connect 192.168.1.42:41235
```

Check the phone is visible:

```
adb devices
```

It should list one device as `device` (not `unauthorized` or `offline`).

**5. Install Claude Code on the laptop**

Install Node.js 18 or newer from nodejs.org, then in a terminal:

```
npm install -g @anthropic-ai/claude-code
```

Then `cd` into the cloned project folder and run `claude`. Current install
options are at https://code.claude.com/docs.

Run it in **PowerShell or Git Bash, not WSL**. Inside WSL, Linux and Windows
are effectively different machines, and reaching the Windows `adb.exe` from
there is awkward.

#### Using it day to day

With the phone connected, one command builds, installs and launches:

```
.\gradlew.bat installDebug
adb shell am start -n com.family2.todo/.MainActivity
```

Claude Code can run both itself. When something crashes, it can read the
reason straight off the phone:

```
adb logcat -b crash -d
```

or watch the app's own log lines live with:

```
adb logcat --pid=$(adb shell pidof -s com.family2.todo)
```

So the loop becomes: describe the change -> Claude edits, builds, installs ->
look at the phone. No APK copying, and Claude sees its own errors.

#### Things that will trip you up

- **After the phone reboots or rejoins Wi-Fi, the port changes.** Pairing
  survives, but you must re-read the address on the Wireless debugging screen
  and run `adb connect` again. This is the single most common annoyance.
- **`adb devices` shows nothing** — usually the two devices are on different
  Wi-Fi networks, or the laptop is on a VPN.
- **The widget does not change after reinstalling.** Android caches widget
  layouts. Remove the widget from the home screen and add it again.
- **`installDebug` still needs the Android SDK**, which Android Studio
  provides. Gradle finds it via `local.properties`, which Android Studio
  wrote when it first opened the project. That file is machine-specific and
  deliberately not committed to git.
- Terminal builds use the same debug signing key as Android Studio on that
  machine (`%USERPROFILE%\.android\debug.keystore`), so installs update the
  existing app rather than being rejected. Building on a *different* computer
  still hits the key-mismatch problem described above.
- **GitHub Actions still matters.** It is the safety net that catches
  anything broken before it reaches the phone, and it is the only way a
  browser-based Claude Code session can check whether code compiles.

## Known limitations (v1)

- No cloud sync / backup — data lives only on this phone. If you lose the
  phone or uninstall the app, tasks are gone. (Fine for now per the "no
  existing data to preserve" starting point — worth revisiting once you rely
  on this daily.)
- No reordering/drag-and-drop yet, no due dates or reminders.
- The widget shows the whole list; there's no way yet to configure the
  widget to show only some tasks.
