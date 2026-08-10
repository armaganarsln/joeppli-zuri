package gl.joeppli.zueri.data

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Real authentication backed by Firebase Auth.
 *
 * Kept deliberately separate from [RecyclingRepository] (which stays pure,
 * local-only state) so JVM unit tests never touch Firebase: the [FirebaseAuth]
 * instance is created lazily on the first real sign-in call, not at class load.
 *
 * On success each method writes the authenticated user into
 * [RecyclingRepository.setAuthenticatedUser], which the rest of the app already
 * observes via the userProfile flow.
 *
 * Until the real google-services.json is dropped in, calls run against the
 * placeholder project and surface as [AuthError]s — the demo login paths remain
 * the way to get into the app.
 */
object AuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // OAuth Web client ID from the Firebase project. Placeholder until the real
    // google-services.json + console setup land; real Google sign-in needs this
    // value set and an SHA-1 fingerprint registered for the app.
    private const val WEB_CLIENT_ID = "REPLACE_WITH_FIREBASE_WEB_CLIENT_ID.apps.googleusercontent.com"

    enum class AuthError { EMAIL_IN_USE, INVALID_CREDENTIALS, WEAK_PASSWORD, INVALID_EMAIL, INVALID_CODE, NETWORK, NO_GOOGLE_ACCOUNT, CANCELLED, UNKNOWN }

    sealed interface AuthResult {
        object Success : AuthResult
        data class Failure(val error: AuthError) : AuthResult
    }

    /** Creates a new email/password account, sets the display name, and signs in. */
    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult = runAuth {
        val created = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = requireNotNull(created.user)
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
        ).await()
        finishSignIn(user.uid, name.trim(), user.email ?: email.trim(), phone = "", authType = "EMAIL")
    }

    /** Signs in to an existing email/password account. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult = runAuth {
        val signedIn = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = requireNotNull(signedIn.user)
        finishSignIn(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@").orEmpty(),
            email = user.email ?: email.trim(),
            phone = "",
            authType = "EMAIL"
        )
    }

    /**
     * Signs in with Google via Credential Manager: shows the account picker,
     * exchanges the returned Google ID token for a Firebase credential, and
     * signs in. [context] must be the Activity so the picker can be shown.
     */
    suspend fun signInWithGoogle(context: Context): AuthResult = runAuth {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        check(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Unexpected credential type: ${credential.type}" }
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val signedIn = auth.signInWithCredential(firebaseCredential).await()
        val user = requireNotNull(signedIn.user)
        finishSignIn(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@").orEmpty(),
            email = user.email.orEmpty(),
            phone = "",
            authType = "GOOGLE"
        )
    }

    // Holds the verification id between sendPhoneCode() and verifyPhoneCode().
    @Volatile
    private var phoneVerificationId: String? = null

    sealed interface PhoneSendResult {
        /** SMS dispatched; collect the code and call [verifyPhoneCode]. */
        object CodeSent : PhoneSendResult
        /** Instant verification signed the user in already; no code needed. */
        object AutoVerified : PhoneSendResult
        data class Failure(val error: AuthError) : PhoneSendResult
    }

    /**
     * Starts Firebase phone verification: triggers an SMS to [phoneE164] and,
     * on some devices, auto-retrieves the code. [activity] is required for the
     * reCAPTCHA / Play Integrity check.
     */
    suspend fun sendPhoneCode(activity: Activity, phoneE164: String): PhoneSendResult =
        suspendCancellableCoroutine { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Instant / auto-retrieval: sign in straight away.
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (!cont.isActive) return@addOnCompleteListener
                        if (task.isSuccessful) {
                            task.result.user?.let { applyPhoneUser(it, phoneE164) }
                            cont.resume(PhoneSendResult.AutoVerified)
                        } else {
                            cont.resume(PhoneSendResult.Failure(mapError(task.exception ?: IllegalStateException())))
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resume(PhoneSendResult.Failure(mapError(e)))
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    phoneVerificationId = verificationId
                    if (cont.isActive) cont.resume(PhoneSendResult.CodeSent)
                }
            }
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneE164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    /** Verifies the SMS [code] the user typed against the sent verification. */
    suspend fun verifyPhoneCode(code: String, phoneE164: String): AuthResult = runAuth {
        val verificationId = phoneVerificationId ?: error("No phone verification in progress")
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        val signedIn = auth.signInWithCredential(credential).await()
        val user = requireNotNull(signedIn.user)
        finishSignIn(user.uid, user.displayName.orEmpty(), user.email.orEmpty(), user.phoneNumber ?: phoneE164, "PHONE")
        phoneVerificationId = null
    }

    private fun applyPhoneUser(user: FirebaseUser, phoneE164: String) {
        RecyclingRepository.setAuthenticatedUser(
            uid = user.uid,
            name = user.displayName.orEmpty(),
            email = user.email.orEmpty(),
            phone = user.phoneNumber ?: phoneE164,
            authType = "PHONE"
        )
    }

    /**
     * After a successful Firebase sign-in: load the user's profile document from
     * Firestore (the cloud is the source of truth on login) or create it for a
     * new account. Falls back to a local-only profile if Firestore is
     * unreachable, so sign-in is never blocked by a network hiccup.
     */
    private suspend fun finishSignIn(uid: String, name: String, email: String, phone: String, authType: String) {
        val docRef = firestore.collection("users").document(uid)
        val snap = runCatching { docRef.get().await() }.getOrNull()
        if (snap != null && snap.exists()) {
            RecyclingRepository.applyRemoteProfile(
                uid = uid,
                name = snap.getString("name").takeUnless { it.isNullOrBlank() } ?: name,
                email = snap.getString("email").takeUnless { it.isNullOrBlank() } ?: email,
                phone = snap.getString("phone").takeUnless { it.isNullOrBlank() } ?: phone,
                homeAddress = snap.getString("homeAddress").orEmpty(),
                authType = authType
            )
        } else {
            RecyclingRepository.setAuthenticatedUser(uid, name, email, phone, authType)
            runCatching {
                docRef.set(profileMap(RecyclingRepository.userProfile.value), SetOptions.merge()).await()
            }
        }
    }

    /** Pushes the durable profile fields to the signed-in user's doc (no-op for demo sessions). */
    fun pushProfileToCloud() {
        val uid = runCatching { auth.currentUser?.uid }.getOrNull() ?: return
        scope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .set(profileMap(RecyclingRepository.userProfile.value), SetOptions.merge()).await()
            }
        }
    }

    private fun profileMap(p: UserProfile): Map<String, Any> = mapOf(
        "name" to p.name,
        "email" to p.email,
        "phone" to p.phone,
        "homeAddress" to p.homeAddress
    )

    /** Signs out of Firebase and clears the saved credential selection. */
    suspend fun signOut(context: Context) {
        runCatching { auth.signOut() }
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private suspend fun runAuth(block: suspend () -> Unit): AuthResult = try {
        block()
        AuthResult.Success
    } catch (e: Exception) {
        AuthResult.Failure(mapError(e))
    }

    private fun mapError(e: Throwable): AuthError = when (e) {
        is FirebaseNetworkException -> AuthError.NETWORK
        // Subtypes first: both extend GetCredentialException.
        is GetCredentialCancellationException -> AuthError.CANCELLED
        is NoCredentialException -> AuthError.NO_GOOGLE_ACCOUNT
        is GetCredentialException -> AuthError.UNKNOWN
        is FirebaseAuthException -> when (e.errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EMAIL_IN_USE
            "ERROR_WEAK_PASSWORD" -> AuthError.WEAK_PASSWORD
            "ERROR_INVALID_EMAIL" -> AuthError.INVALID_EMAIL
            "ERROR_INVALID_VERIFICATION_CODE",
            "ERROR_SESSION_EXPIRED" -> AuthError.INVALID_CODE
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_USER_NOT_FOUND",
            "ERROR_USER_DISABLED" -> AuthError.INVALID_CREDENTIALS
            else -> AuthError.UNKNOWN
        }
        else -> AuthError.UNKNOWN
    }
}

/** Bridges a Play-services [Task] into a coroutine without an extra dependency. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val e = task.exception
        if (e != null) cont.resumeWithException(e) else cont.resume(task.result)
    }
}
