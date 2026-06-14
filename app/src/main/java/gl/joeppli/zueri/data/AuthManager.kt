package gl.joeppli.zueri.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
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

    // OAuth Web client ID from the Firebase project. Placeholder until the real
    // google-services.json + console setup land; real Google sign-in needs this
    // value set and an SHA-1 fingerprint registered for the app.
    private const val WEB_CLIENT_ID = "REPLACE_WITH_FIREBASE_WEB_CLIENT_ID.apps.googleusercontent.com"

    enum class AuthError { EMAIL_IN_USE, INVALID_CREDENTIALS, WEAK_PASSWORD, INVALID_EMAIL, NETWORK, NO_GOOGLE_ACCOUNT, CANCELLED, UNKNOWN }

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
        RecyclingRepository.setAuthenticatedUser(
            uid = user.uid,
            name = name.trim(),
            email = user.email ?: email.trim(),
            authType = "EMAIL"
        )
    }

    /** Signs in to an existing email/password account. */
    suspend fun signInWithEmail(email: String, password: String): AuthResult = runAuth {
        val signedIn = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = requireNotNull(signedIn.user)
        RecyclingRepository.setAuthenticatedUser(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@").orEmpty(),
            email = user.email ?: email.trim(),
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
        RecyclingRepository.setAuthenticatedUser(
            uid = user.uid,
            name = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@").orEmpty(),
            email = user.email.orEmpty(),
            authType = "GOOGLE"
        )
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
