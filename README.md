# WordWonder Kids 🌟

*Play. Discover. Speak. Shine!*

An offline Android app to help a 4-year-old learn new English words and say them clearly.
It has no backend, no accounts, no ads and **no INTERNET permission**. Everything stays on the phone.

## What's inside

| Activity | How it works |
|---|---|
| 📚 **Learn Words** | A big picture plus the word. The app says the word and a short sentence ("The dog says woof woof."). Tap to hear it again, 🐢 to hear it slowly, swipe or use the arrows to move. |
| 🎤 **Say It!** | The app says "Can you say… dog?" and then listens. Saying it right earns ⭐⭐⭐. A close try earns ⭐, and the app repeats the word and listens again. After 3 tries it says "Good trying!" and moves on. It never says "wrong". |
| 🔍 **Find It!** | "Find the elephant!" She taps one of 3 pictures. If she taps the wrong one, the app names what she tapped ("That's the lion") and asks again, so the mistake teaches a word too. |
| 🧒 **Her buddy** | Add her name and up to 4 photos (Grown-ups page). The app crops around her face on the phone and turns it into an animated buddy that talks, listens and jumps when she gets it right. With no photo, a smiley mascot is used. |
| 🏆 **My Stickers** | Every 10 ⭐ unlocks a dress-up sticker of her (👑 Princess, 🦄 Unicorn, 🚀 Astronaut…), 12 in total. |
| 🎉 **Fun extras** | Confetti, sound effects, bouncy buttons, and praise with her name ("Great job, Jhanvi!"). |
| 🔒 **Grown-ups** | Behind a times-table question. Shows learned, practising and new words, a "needs practice" list, per-word counts, a slower-voice switch and a progress reset. |

There are 75 words in 8 groups: animals, fruits, colours, body, vehicles (including auto 🛺), food, home and nature.
Pictures are emoji, so the app needs no image files. Say It! and Find It! show the words she knows least first.

## Build it

### Option A: Android Studio (easiest)
1. Install [Android Studio](https://developer.android.com/studio) (Koala or newer).
2. **File → Open** and pick the `WordBuddy` folder. Wait for the Gradle sync to finish.
3. Plug in the phone with USB debugging on (Settings → About phone → tap *Build number* 7 times → Developer options → USB debugging).
4. Press ▶ **Run**.

### Option B: command line
```bash
./gradlew assembleRelease          # needs JDK 17 + Android SDK (ANDROID_HOME set)
adb install app/build/outputs/apk/release/app-release.apk
```

### Option C: GitHub Actions (no Android SDK on your laptop)
Push this folder to a GitHub repo. `.github/workflows/build-apk.yml` runs the unit tests, builds the APK
and uploads it as the **word-buddy-apk** artifact. Download it to the phone and open it to install
(allow "Install unknown apps" for your browser or file manager).

> The release APK is signed with the debug key so it installs on your own devices.
> Create a proper keystore before publishing to the Play Store.

## Phone setup (one time)
- **Voice:** Settings → System → Languages → Text-to-speech → *Speech Services by Google*. Install the
  **English (India)** or **English (US)** voice so speech works offline. The app uses en-IN if it's installed and falls back to en-US.
- **Listening:** the Say It! game uses the phone's built-in speech recogniser. It asks for
  on-device recognition first. To keep it fully offline, download the English offline speech pack
  (Google app → Settings → Voice → Offline speech recognition; the menu varies by phone).
  If the phone has no recogniser at all, Say It! switches to a 👍 button for a grown-up to tap.
- Allow the **microphone** the first time Say It! asks.

## Customising
- **Add or change words:** edit `app/src/main/java/com/bhumaji/wordbuddy/data/WordBank.kt`.
  Each word is one line: id, emoji, sentence, and optional sound-alikes (`alsoAccept`) the recogniser
  tends to hear instead (for example "eye" → "i").
- **Voice speed:** in `speech/Speaker.kt` (0.9 normal, 0.7 slower setting, 0.5 for 🐢).
- **How strict Say It! is:** in `speech/Match.kt`. Whole-word or plural matches count as great, and anything ≥ 60% similar counts as close.

## Project layout
```
app/src/main/java/com/bhumaji/wordbuddy/
  MainActivity.kt
  data/WordBank.kt        words, emoji, sentences, categories
  data/ProgressStore.kt   stars + per-word stats (SharedPreferences)
  speech/Speaker.kt       text-to-speech
  speech/Listener.kt      speech recognition (prefers on-device)
  speech/Match.kt         lenient "did she say it?" check (+ unit tests)
  ui/                     Home, Learn, Say, Find, Grown-ups screens (Jetpack Compose)
```
Stack: Kotlin 2.0, Jetpack Compose (Material 3), AGP 8.5, minSdk 26 (Android 8+), targetSdk 34.

## Releases & versions
- Every push to `main` builds a test APK (Actions → *Build APK* → Artifacts).
- To publish a version, change the `VERSION` file on `main` (e.g. `3.1.0` → `3.2.0`) and add a
  `## v3.2.0` section to `CHANGELOG.md`. The **Release** workflow builds a signed APK and publishes
  GitHub Release `v3.2.0` with `WordBuddy-v3.2.0.apk` attached. Pushing a tag `v3.2.0` works too.
  The version sets the app version: `3.2.0` → versionName `3.2.0`, versionCode `30200`. Always go up.
- All releases are signed with the same key (repo secrets `SIGNING_KEYSTORE_BASE64` + `SIGNING_PASSWORD`),
  so a new version installs **over** the old one and keeps her stars, photos and progress.
  Keep a backup of the keystore. If it's lost, future versions can't update the installed app.
