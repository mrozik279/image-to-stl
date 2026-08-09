package com.propertytrader.core.board

enum class ColorGroup { A, B, C, D, E, F, G, H }

sealed interface Space {
    val index: Int
    val name: String
}

data class GoSpace(override val index: Int, override val name: String = "Start") : Space

data class JailSpace(override val index: Int, override val name: String = "Wiezienie") : Space

data class GoToJailSpace(override val index: Int, override val name: String = "Idz do wiezienia") : Space

data class FreeParkingSpace(override val index: Int, override val name: String = "Darmowy Parking") : Space

data class EventSpace(override val index: Int, override val name: String) : Space

data class TaxSpace(override val index: Int, override val name: String, val amount: Int) : Space

data class PropertySpace(
    override val index: Int,
    override val name: String,
    val group: ColorGroup,
    val price: Int,
    val baseRent: Int,
    val fullGroupRentMultiplier: Int = 2,
    val houseCost: Int,
    val rentWithHouses: List<Int>, // size 5: 1, 2, 3, 4 houses, then a hotel
) : Space

data class TransitSpace(
    override val index: Int,
    override val name: String,
    val price: Int,
    val rentByOwnedCount: List<Int>,
) : Space

data class UtilitySpace(
    override val index: Int,
    override val name: String,
    val price: Int,
    val diceMultiplierOneOwned: Int = 4,
    val diceMultiplierBothOwned: Int = 10,
) : Space
