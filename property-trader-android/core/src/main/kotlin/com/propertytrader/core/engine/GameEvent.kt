package com.propertytrader.core.engine

sealed interface GameEvent {
    data class DiceRolled(val a: Int, val b: Int) : GameEvent
    data class PlayerMoved(val playerId: Int, val from: Int, val to: Int) : GameEvent
    data class PassedGo(val playerId: Int, val bonus: Int) : GameEvent
    data class PropertyPurchased(val playerId: Int, val spaceIndex: Int, val price: Int) : GameEvent
    data class RentPaid(val payerId: Int, val ownerId: Int, val amount: Int, val spaceIndex: Int) : GameEvent
    data class TaxPaid(val playerId: Int, val amount: Int) : GameEvent
    data class SentToJail(val playerId: Int) : GameEvent
    data class LeftJail(val playerId: Int, val paidBail: Boolean) : GameEvent
    data class PlayerBankrupt(val playerId: Int, val creditorId: Int?) : GameEvent
    data class GameOver(val winnerId: Int, val reason: String) : GameEvent
    data class TradeProposed(val fromPlayerId: Int, val toPlayerId: Int) : GameEvent
    data class TradeAccepted(val fromPlayerId: Int, val toPlayerId: Int) : GameEvent
    data class TradeDeclined(val fromPlayerId: Int, val toPlayerId: Int) : GameEvent
}
