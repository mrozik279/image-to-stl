package com.propertytrader.core.board

object BoardFactory {

    private fun houseCostFor(group: ColorGroup): Int = when (group) {
        ColorGroup.A, ColorGroup.B -> 50
        ColorGroup.C, ColorGroup.D -> 100
        ColorGroup.E, ColorGroup.F -> 150
        ColorGroup.G, ColorGroup.H -> 200
    }

    private fun rentLadder(baseRent: Int): List<Int> =
        listOf(baseRent * 5, baseRent * 15, baseRent * 30, baseRent * 50, baseRent * 70)

    private fun property(index: Int, name: String, group: ColorGroup, price: Int, baseRent: Int): PropertySpace =
        PropertySpace(
            index = index,
            name = name,
            group = group,
            price = price,
            baseRent = baseRent,
            houseCost = houseCostFor(group),
            rentWithHouses = rentLadder(baseRent),
        )

    fun classicBoard(): List<Space> = listOf(
        GoSpace(0),
        property(1, "Ulica Wierzbowa", ColorGroup.A, price = 60, baseRent = 4),
        EventSpace(2, "Wydarzenie"),
        property(3, "Ulica Klonowa", ColorGroup.A, price = 60, baseRent = 8),
        TaxSpace(4, "Podatek", amount = 200),
        TransitSpace(5, "Dworzec Polnocny", price = 200, rentByOwnedCount = listOf(25, 50, 100, 200)),
        property(6, "Ulica Cedrowa", ColorGroup.B, price = 100, baseRent = 6),
        EventSpace(7, "Wydarzenie"),
        property(8, "Ulica Bukowa", ColorGroup.B, price = 100, baseRent = 6),
        property(9, "Ulica Debowa", ColorGroup.B, price = 120, baseRent = 8),
        JailSpace(10),
        property(11, "Ulica Sosnowa", ColorGroup.C, price = 140, baseRent = 10),
        UtilitySpace(12, "Elektrownia Miejska", price = 150),
        property(13, "Ulica Swierkowa", ColorGroup.C, price = 140, baseRent = 10),
        property(14, "Ulica Jesionowa", ColorGroup.C, price = 160, baseRent = 12),
        TransitSpace(15, "Dworzec Wschodni", price = 200, rentByOwnedCount = listOf(25, 50, 100, 200)),
        property(16, "Ulica Topolowa", ColorGroup.D, price = 180, baseRent = 14),
        EventSpace(17, "Wydarzenie"),
        property(18, "Ulica Lipowa", ColorGroup.D, price = 180, baseRent = 14),
        property(19, "Ulica Jaworowa", ColorGroup.D, price = 200, baseRent = 16),
        FreeParkingSpace(20),
        property(21, "Ulica Kasztanowa", ColorGroup.E, price = 220, baseRent = 18),
        EventSpace(22, "Wydarzenie"),
        property(23, "Ulica Grabowa", ColorGroup.E, price = 220, baseRent = 18),
        property(24, "Ulica Brzozowa", ColorGroup.E, price = 240, baseRent = 20),
        TransitSpace(25, "Dworzec Poludniowy", price = 200, rentByOwnedCount = listOf(25, 50, 100, 200)),
        property(26, "Ulica Akacjowa", ColorGroup.F, price = 260, baseRent = 22),
        property(27, "Ulica Jarzebinowa", ColorGroup.F, price = 260, baseRent = 22),
        UtilitySpace(28, "Wodociagi Miejskie", price = 150),
        property(29, "Ulica Platanowa", ColorGroup.F, price = 280, baseRent = 24),
        GoToJailSpace(30),
        property(31, "Ulica Wiazowa", ColorGroup.G, price = 300, baseRent = 26),
        property(32, "Ulica Modrzewiowa", ColorGroup.G, price = 300, baseRent = 26),
        EventSpace(33, "Wydarzenie"),
        property(34, "Ulica Cyprysowa", ColorGroup.G, price = 320, baseRent = 28),
        TransitSpace(35, "Dworzec Zachodni", price = 200, rentByOwnedCount = listOf(25, 50, 100, 200)),
        EventSpace(36, "Wydarzenie"),
        property(37, "Ulica Magnoliowa", ColorGroup.H, price = 350, baseRent = 35),
        TaxSpace(38, "Podatek luksusowy", amount = 100),
        property(39, "Ulica Palisandrowa", ColorGroup.H, price = 400, baseRent = 50),
    )
}
