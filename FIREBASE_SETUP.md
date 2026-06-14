# Firebase setup — going from placeholder to live auth

The app is wired for **real** Firebase Auth (Email/Password, Google, Phone) and
Firestore profile sync. Until the steps below are done it builds and runs against
a **placeholder** `app/google-services.json`, so the real sign-in buttons will
fail and the **demo paths** (Google demo accounts, phone demo code `123456`) are
the way into the app.

To activate live auth:

## 1. Create the Firebase project
1. Firebase console → **Add project** (e.g. `joeppli-zueri`).
2. **Add app → Android**, package name **`gl.joeppli.zueri`**.
3. Download the generated **`google-services.json`** and replace
   `app/google-services.json` with it. (Its API key is not a secret; it is safe
   to commit so CI keeps building.)

## 2. Enable the sign-in providers
Firebase console → **Authentication → Sign-in method**:
- **Email/Password** — enable.
- **Google** — enable.
- **Phone** — enable. Add **test phone numbers** for development, and make sure
  **Play Integrity** / reCAPTCHA is configured (Project settings → App Check /
  the Phone provider section).

## 3. Google Sign-In extras
Real Google sign-in needs an OAuth **Web client ID** and your app's signing
fingerprints:
1. Project settings → **Your apps → SHA certificate fingerprints** → add the
   **SHA-1** (and SHA-256) for both debug and release. Get them with:
   ```
   ./gradlew signingReport
   ```
2. Set the Web client ID in `AuthManager.kt`:
   ```kotlin
   private const val WEB_CLIENT_ID = "<your-web-client-id>.apps.googleusercontent.com"
   ```
   It is the OAuth 2.0 **Web client** (auto-created by Firebase) — find it under
   Google Cloud console → APIs & Services → Credentials, or as
   `client_type: 3` in `google-services.json`. (Alternatively, switch the
   constant to `context.getString(R.string.default_web_client_id)`, which the
   google-services plugin generates once the real file is in place.)

## 4. Firestore
1. Firebase console → **Firestore Database → Create database**.
2. The app stores each user's durable profile at `users/{uid}`. Suggested
   security rules (a signed-in user can read/write only their own doc):
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
     }
   }
   ```

That's it — no further code changes are required. The demo paths remain as a
labeled fallback.
