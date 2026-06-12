package gl.joeppli.zueri.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.min
import kotlin.random.Random

@Serializable
data class UserProfile(
    val id: String = "user_123",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val homeAddress: String = "",
    val invoiceAddress: String = "",
    val invoiceSameAsHome: Boolean = true,
    val defaultPaymentMethod: String = "twint_demo",
    val isLoggedIn: Boolean = false,
    val authType: String? = null
)

@Serializable
data class CategoryBreakdown(
    var glass: Float = 45.2f,
    var cardboard: Float = 52.8f,
    var pet: Float = 12.0f,
    var bio: Float = 25.0f,
    var aluminum: Float = 7.5f
)

@Serializable
data class RecyclingStats(
    val totalKg: Float = 142.5f,
    val categories: CategoryBreakdown = CategoryBreakdown(),
    val karma: Int = 85,
    val co2Saved: Float = 198.4f,
    val streakWeeks: Int = 4,
    val neighborhoodTotalKg: Float = 4230f
)

@Serializable
data class PickupRequest(
    val address: String,
    val dateString: String,
    val timeSlot: String,
    val materials: List<String>,
    val price: Float,
    val isExpress: Boolean
)

private val Context.joeppliDataStore: DataStore<Preferences> by preferencesDataStore(name = "joeppli_state")

object RecyclingRepository {
    private val LANGUAGE = stringPreferencesKey("language")
    private val PROFILE = stringPreferencesKey("profile")
    private val STATS = stringPreferencesKey("stats")
    private val LAST_PICKUP = stringPreferencesKey("last_pickup")
    private val THEME = stringPreferencesKey("theme")

    private val json = Json { ignoreUnknownKeys = true }
    private var store: DataStore<Preferences>? = null
    private val persistScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _userLanguage = MutableStateFlow("de")
    val userLanguage: StateFlow<String> = _userLanguage.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _stats = MutableStateFlow(RecyclingStats())
    val stats: StateFlow<RecyclingStats> = _stats.asStateFlow()

    private val _lastPickup = MutableStateFlow<PickupRequest?>(null)
    val lastPickup: StateFlow<PickupRequest?> = _lastPickup.asStateFlow()

    private val _theme = MutableStateFlow("green")
    val theme: StateFlow<String> = _theme.asStateFlow()

    /**
     * Hydrates state from disk. Call once from Application.onCreate, before
     * any Activity draws. The blocking read is a single small file; doing it
     * synchronously avoids flashing the logged-out auth screen on every cold
     * start.
     */
    fun initialize(context: Context) {
        if (store != null) return
        val ds = context.applicationContext.joeppliDataStore
        store = ds
        val prefs = runBlocking { ds.data.first() }
        prefs[LANGUAGE]?.let { _userLanguage.value = it }
        prefs[PROFILE]?.let { decode<UserProfile>(it)?.let { p -> _userProfile.value = p } }
        prefs[STATS]?.let { decode<RecyclingStats>(it)?.let { s -> _stats.value = s } }
        prefs[LAST_PICKUP]?.let { decode<PickupRequest>(it)?.let { p -> _lastPickup.value = p } }
        prefs[THEME]?.let { _theme.value = it }
    }

    private inline fun <reified T> decode(raw: String): T? = try {
        json.decodeFromString<T>(raw)
    } catch (e: Exception) {
        null // stale or corrupt entry: fall back to defaults
    }

    private fun persist() {
        val ds = store ?: return // not initialized (e.g. unit tests): in-memory only
        val language = _userLanguage.value
        val profile = json.encodeToString(_userProfile.value)
        val stats = json.encodeToString(_stats.value)
        val lastPickup = _lastPickup.value?.let { json.encodeToString(it) }
        val themeSetting = _theme.value
        persistScope.launch {
            ds.edit { prefs ->
                prefs[LANGUAGE] = language
                prefs[PROFILE] = profile
                prefs[STATS] = stats
                prefs[THEME] = themeSetting
                if (lastPickup != null) prefs[LAST_PICKUP] = lastPickup else prefs.remove(LAST_PICKUP)
            }
        }
    }

    fun loginWithGoogle(name: String, email: String) {
        _userProfile.value = UserProfile(
            name = name,
            email = email,
            isLoggedIn = true,
            authType = "GOOGLE"
        )
        persist()
    }

    fun loginWithEmail(name: String, email: String) {
        _userProfile.value = UserProfile(
            name = name,
            email = email,
            isLoggedIn = true,
            authType = "EMAIL"
        )
        persist()
    }

    fun loginWithPhone(phone: String) {
        _userProfile.value = UserProfile(
            phone = phone,
            isLoggedIn = true,
            authType = "PHONE"
        )
        persist()
    }

    fun registerAddress(homeAddr: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(
            homeAddress = homeAddr,
            invoiceAddress = homeAddr
        )
        persist()
    }

    fun logout() {
        _userProfile.value = UserProfile()
        persist()
    }

    fun setLanguage(lang: String) {
        _userLanguage.value = lang
        persist()
    }

    fun setTheme(newTheme: String) {
        _theme.value = newTheme
        persist()
    }

    fun updateProfile(name: String, phone: String, homeAddr: String, payment: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(
            name = name,
            phone = phone,
            homeAddress = homeAddr,
            invoiceAddress = if (current.invoiceSameAsHome) homeAddr else current.invoiceAddress,
            defaultPaymentMethod = payment
        )
        persist()
    }

    fun addPickup(address: String, dateString: String, timeSlot: String, materials: List<String>, price: Float, isExpress: Boolean) {
        val request = PickupRequest(address, dateString, timeSlot, materials, price, isExpress)
        _lastPickup.value = request

        // Update statistics
        val currentStats = _stats.value
        val addedGlass = if (materials.contains("glass") || materials.contains("Altglas")) Random.nextFloat() * 5f + 2f else 0f
        val addedCardboard = if (materials.contains("cardboard") || materials.contains("Papier/Karton")) Random.nextFloat() * 5f + 2f else 0f
        val addedPet = if (materials.contains("pet") || materials.contains("Plastik/Sonder")) Random.nextFloat() * 4f + 1f else 0f
        val addedBio = if (materials.contains("bio") || materials.contains("Biogut/Kompost")) Random.nextFloat() * 5f + 2f else 0f
        val addedAlu = if (materials.contains("aluminum") || materials.contains("Alu/Metall")) Random.nextFloat() * 3f + 1f else 0f

        val sumAdded = addedGlass + addedCardboard + addedPet + addedBio + addedAlu
        val newKarma = min(100, currentStats.karma + materials.size * 4)

        currentStats.categories.glass += addedGlass
        currentStats.categories.cardboard += addedCardboard
        currentStats.categories.pet += addedPet
        currentStats.categories.bio += addedBio
        currentStats.categories.aluminum += addedAlu

        _stats.value = currentStats.copy(
            totalKg = currentStats.totalKg + sumAdded,
            karma = newKarma,
            co2Saved = currentStats.co2Saved + sumAdded * 1.4f,
            neighborhoodTotalKg = currentStats.neighborhoodTotalKg + sumAdded + Random.nextFloat() * 10f
        )
        persist()
    }
}
