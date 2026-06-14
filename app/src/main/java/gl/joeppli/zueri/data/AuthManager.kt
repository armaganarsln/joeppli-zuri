package gl.joeppli.zueri.data

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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

    enum class AuthError { EMAIL_IN_USE, INVALID_CREDENTIALS, WEAK_PASSWORD, INVALID_EMAIL, NETWORK, UNKNOWN }

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

    private suspend fun runAuth(block: suspend () -> Unit): AuthResult = try {
        block()
        AuthResult.Success
    } catch (e: Exception) {
        AuthResult.Failure(mapError(e))
    }

    private fun mapError(e: Throwable): AuthError = when {
        e is FirebaseNetworkException -> AuthError.NETWORK
        e is FirebaseAuthException -> when (e.errorCode) {
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
