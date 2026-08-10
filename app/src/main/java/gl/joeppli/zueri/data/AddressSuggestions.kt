package gl.joeppli.zueri.data

import android.content.Context
import android.content.pm.PackageManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Address autocomplete backed by the Places SDK.
 *
 * Every failure path — missing/dummy API key, Places API not enabled on the
 * project, no network — resolves to an empty suggestion list, so the address
 * fields keep working as plain text input exactly as they did before.
 */
object AddressSuggestions {

    /** Placeholder key shipped in the repo; never worth a network round trip. */
    private const val DUMMY_KEY_PREFIX = "AIzaSyDummyKey"

    /** Roughly the city of Zürich — biases results to the service area. */
    private val ZURICH_BOUNDS = RectangularBounds.newInstance(
        LatLng(47.3200, 8.4500), // south-west
        LatLng(47.4350, 8.6250)  // north-east
    )

    private var client: PlacesClient? = null
    private var initialized = false

    /** One token per typing session keeps autocomplete billed as a single session. */
    private var sessionToken: AutocompleteSessionToken? = null

    private fun clientOrNull(context: Context): PlacesClient? {
        if (initialized) return client
        initialized = true
        client = runCatching {
            val key = apiKey(context)
            if (key.isNullOrBlank() || key.startsWith(DUMMY_KEY_PREFIX)) return@runCatching null
            if (!Places.isInitialized()) {
                Places.initializeWithNewPlacesApiEnabled(context.applicationContext, key)
            }
            Places.createClient(context.applicationContext)
        }.getOrNull()
        return client
    }

    private fun apiKey(context: Context): String? = runCatching {
        @Suppress("DEPRECATION")
        val info = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        info.metaData?.getString("com.google.android.geo.API_KEY")
    }.getOrNull()

    /**
     * Suggests Swiss addresses for [query], biased to Zürich.
     * Returns an empty list when autocomplete is unavailable for any reason.
     */
    suspend fun suggest(context: Context, query: String): List<String> {
        if (query.length < 3) return emptyList()
        val places = clientOrNull(context) ?: return emptyList()
        val token = sessionToken ?: AutocompleteSessionToken.newInstance().also { sessionToken = it }

        return runCatching {
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("CH")
                .setLocationBias(ZURICH_BOUNDS)
                .setSessionToken(token)
                .build()
            places.findAutocompletePredictions(request).await()
                .autocompletePredictions
                .map { it.getFullText(null).toString() }
        }.getOrDefault(emptyList())
    }

    /**
     * Ends the current billing session. Call after the user picks a suggestion
     * so the next lookup starts a fresh session.
     */
    fun endSession() {
        sessionToken = null
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> if (cont.isActive) cont.resume(result) }
    addOnFailureListener { error -> if (cont.isActive) cont.resumeWithException(error) }
    addOnCanceledListener { cont.cancel() }
}
