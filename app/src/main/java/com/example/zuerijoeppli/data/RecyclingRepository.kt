package com.example.zuerijoeppli.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.min
import kotlin.random.Random

data class UserProfile(
    val id: String = "user_123",
    val name: String = "Ueli Maurer",
    val email: String = "ueli@example.ch",
    val phone: String = "079 123 45 67",
    val homeAddress: String = "Langstrasse 120, 8004 Zürich",
    val invoiceAddress: String = "Langstrasse 120, 8004 Zürich",
    val invoiceSameAsHome: Boolean = true,
    val defaultPaymentMethod: String = "twint_demo"
)

data class CategoryBreakdown(
    var glass: Float = 45.2f,
    var cardboard: Float = 52.8f,
    var pet: Float = 12.0f,
    var bio: Float = 25.0f,
    var aluminum: Float = 7.5f
)

data class RecyclingStats(
    val totalKg: Float = 142.5f,
    val categories: CategoryBreakdown = CategoryBreakdown(),
    val karma: Int = 85,
    val co2Saved: Float = 198.4f,
    val streakWeeks: Int = 4,
    val neighborhoodTotalKg: Float = 4230f
)

data class PickupRequest(
    val address: String,
    val dateString: String,
    val timeSlot: String,
    val materials: List<String>,
    val price: Float,
    val isExpress: Boolean
)

object RecyclingRepository {
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _stats = MutableStateFlow(RecyclingStats())
    val stats: StateFlow<RecyclingStats> = _stats.asStateFlow()

    private val _lastPickup = MutableStateFlow<PickupRequest?>(null)
    val lastPickup: StateFlow<PickupRequest?> = _lastPickup.asStateFlow()

    fun updateProfile(name: String, phone: String, homeAddr: String, payment: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(
            name = name,
            phone = phone,
            homeAddress = homeAddr,
            invoiceAddress = if (current.invoiceSameAsHome) homeAddr else current.invoiceAddress,
            defaultPaymentMethod = payment
        )
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
    }
}
