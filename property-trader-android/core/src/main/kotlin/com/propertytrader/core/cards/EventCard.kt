package com.propertytrader.core.cards

sealed interface EventCardEffect {
    data class CollectMoney(val amount: Int) : EventCardEffect
    data class PayMoney(val amount: Int) : EventCardEffect
    data class MoveTo(val spaceIndex: Int) : EventCardEffect
    data object GoToJail : EventCardEffect
    data object GetOutOfJailFree : EventCardEffect
    data object ExtraRoll : EventCardEffect
}

data class EventCard(val description: String, val effect: EventCardEffect)

object EventDeck {
    fun classicDeck(): List<EventCard> = listOf(
        EventCard("Zwrot podatku: odbierz 50.", EventCardEffect.CollectMoney(50)),
        EventCard("Wygrywasz na loterii: odbierz 100.", EventCardEffect.CollectMoney(100)),
        EventCard("Mandat za przekroczenie predkosci: zaplac 40.", EventCardEffect.PayMoney(40)),
        EventCard("Naprawa dachu: zaplac 75.", EventCardEffect.PayMoney(75)),
        EventCard("Idz na Start i odbierz premie.", EventCardEffect.MoveTo(0)),
        EventCard("Kontrola drogowa: idz do wiezienia.", EventCardEffect.GoToJail),
        EventCard("Karta \"Wyjscie z wiezienia za darmo\" - zachowaj ja.", EventCardEffect.GetOutOfJailFree),
        EventCard("Dodatkowy rzut kosmi w tej turze.", EventCardEffect.ExtraRoll),
        EventCard("Zwrot za nadplate: odbierz 25.", EventCardEffect.CollectMoney(25)),
        EventCard("Oplata administracyjna: zaplac 20.", EventCardEffect.PayMoney(20)),
    )
}
