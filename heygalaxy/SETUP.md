# Hey Galaxy - Setup Guide

## Step 1: Get the source code on GitHub

You have two options:

### Option A: Download ZIP (simplest)
1. On your Mac, open the `heygalaxy` folder in Finder
2. Select all files (Cmd+A)
3. Right-click → **Compress "heygalaxy"** → creates `heygalaxy.zip`
4. Email the ZIP to yourself

### Option B: Use GitHub Desktop (for future updates)
1. Download [GitHub Desktop](https://desktop.github.com/) (free)
2. Install and sign in
3. File → Add Local Repository → select the `heygalaxy` folder

---

## Step 2: Create GitHub repository

1. Go to [github.com](https://github.com) → Sign in (or sign up free)
2. Click the **+** → **New repository**
3. Repository name: `hey-galaxy`
4. Leave it **Public**
5. Do NOT check "Add .gitignore" or "Add README"
6. Click **Create repository**

---

## Step 3: Upload files to GitHub

1. On your repo page, click **"Add file"** → **Upload files**
2. Drag the ZIP file or click to browse
3. Upload all files from the `heygalaxy` folder
4. Scroll down → click **Commit changes** (green button)

**Important:** The workflow file at `.github/workflows/build.yml` must be included. If drag-drop skips hidden folders (names starting with `.`), then [click here to create it manually](https://github.com/NEW-REPO-URL/actions/create/...) — OR just follow the upload steps carefully (GitHub web shows hidden folders too).

---

## Step 4: Trigger the build

1. Click the **Actions** tab
2. On the left, click **Build APK**
3. If nothing runs automatically, click the **Run workflow** dropdown → **Run workflow from main**
4. Wait 3-5 minutes
5. When the run shows a green ✓, scroll to **Artifacts**
6. Click **HeyGalaxy-debug** to download the APK

---

## Step 5: Install on Samsung S24 Ultra

1. Email the `HeyGalaxy-debug.apk` file to yourself on your phone
2. On your S24U, open **Settings** → **Apps** → **Special access** → **Install unknown apps**
3. Find your email app → toggle **Allow**
4. Open the email → tap the APK attachment → tap **Install**

---

## Step 6: Grant all permissions

1. Open **Hey Galaxy** app
2. Tap **"Check Permissions"**
3. Tap **"Grant Runtime Permissions"** → allow all 9 permissions
4. Tap **"Enable Accessibility Service"** → find Hey Galaxy → turn ON
5. Tap **"Enable Notification Access"** → find Hey Galaxy → turn ON
6. Tap **"Allow Display Over Other Apps"** → toggle ON

---

## Step 7: Start using Hey Galaxy

1. Make sure the app is open (you'll see a pulsing animation)
2. Say clearly: **"Hey Galaxy"**

After the app responds with "Yes?":
- **"Answer call"** → answers incoming call
- **"Decline call"** → declines incoming call
- **"Volume up"** → increases media volume
- **"Next song"** → skips to next track
- **"Turn on flashlight"** → turns on torch
- **"Set alarm for 7 AM"** → opens alarm
- **"Open WhatsApp"** → opens WhatsApp
- **"What time is it"** → speaks the time

---

## Troubleshooting

**If "Hey Galaxy" isn't detected:**
1. Make sure your earbuds are set as the microphone in Bluetooth settings
2. Speak clearly and wait 2 seconds between the wake word and command
3. Check the notification panel → ensure "Listening for Hey Galaxy" persists

**If WhatsApp message sending fails:**
1. Open WhatsApp manually first, then use the voice command
2. The accessibility service may need a few seconds to find the contact

**If the app stops listening:**
1. Make sure the notification "Hey Galaxy • Listening..." stays in your notification shade
2. If you force-close the app, reopen it and start the service again

---

## Files in this project

```
heygalaxy/
├── .github/workflows/
│   └── build.yml          ← GitHub Actions workflow (builds APK)
├── app/
│   ├── build.gradle.kts   ← App build config
│   ├── proguard-rules.pro ← Release optimization (optional)
│   └── src/main/
│       ├── AndroidManifest.xml   ← Permissions + all app components
│       ├── java/com/heygalaxy/app/
│       │   ├── handlers/         ← Call, Media, Flash, Alarm, Calendar, WhatsApp
│       │   ├── service/          ← Voice service, command router, receivers
│       │   └── ui/               ← Main screen, Permissions screen
│       └── res/                  ← Layouts, drawables, themes
├── gradle/wrapper/gradle-wrapper.properties  ← Gradle version config
├── gradlew                       ← Gradle launcher script
├── settings.gradle.kts           ← Project structure
├── build.gradle.kts              ← Root build config
└── gradle.properties             ← Optimization flags
```
