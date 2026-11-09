# Word Buddy 🌟

An offline Android app to help a 4-year-old learn new English words and say them clearly.
It has no backend, no accounts, no ads and **no INTERNET permission**. Everything stays on the phone.

## What's inside

| Activity | How it works |
|---|---|
| 📚 **Learn Words** | A big picture plus the word. The app says the word and a short sentence ("The dog says woof woof."). Tap to hear it again, 🐢 to hear it slowly, swipe or use the arrows to move. |
| 🎤 **Say It!** | The app says "Can you say… dog?" and then listens. Saying it right earns ⭐⭐⭐. A close try earns ⭐, and the app repeats the word and listens again. After 3 tries it says "Good trying!" and moves on. It never says "wrong". |
| 🔍 **Find It!** | "Find the elephant!" She taps one of 3 pictures. If she taps the wrong one, the app names what she tapped ("That's the lion") and asks again, so the mistake teaches a word too. |
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
