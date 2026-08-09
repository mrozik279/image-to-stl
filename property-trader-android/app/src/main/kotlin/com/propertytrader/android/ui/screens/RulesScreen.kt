package com.propertytrader.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val RULE_SECTIONS = listOf(
    "Cel gry" to
        "Zostań ostatnim graczem, który nie zbankrutował, albo (jeśli ustawiono limit rund) " +
        "miej najwyższy majątek (gotówka + wartość nieruchomości i zabudowy) po ostatniej rundzie.",
    "Tura" to
        "Rzuć kostki, przesuń pionek. Puste pole do kupienia - decyzja kup/pomiń. Pole zajęte przez " +
        "przeciwnika - automatyczna zapłata czynszu. Dublet daje dodatkowy rzut (trzy dublety z rzędu - " +
        "od razu do więzienia, bez ruchu).",
    "Czynsz" to
        "Podstawowy czynsz, podwójny za pełen komplet koloru bez zabudowy, albo stawka za dane " +
        "domy/hotel jeśli zbudowane. Stacje: czynsz rośnie z liczbą posiadanych stacji. Zakłady: " +
        "suma oczek na kostkach razy mnożnik (4 albo 10).",
    "Więzienie" to
        "Zapłac kaucję, spróbuj wyrzucić dublet (3 próby, potem wymuszona kaucja), albo użyj " +
        "karty \"Wyjście z więzienia za darmo\", jeśli ją masz.",
    "Budowanie" to
        "Gdy posiadasz cały komplet kolorów, możesz budować domy (do 4) i hotel na każdej " +
        "nieruchomości w tej grupie - ekran \"Buduj\". Każdy poziom ma wyższy czynsz.",
    "Karty Szansa" to
        "Pole \"?\" losuje kartę z talii: gotówka, przejście na Start, więzienie, karta \"wyjście z " +
        "więzienia\" albo dodatkowy rzut.",
    "Darmowy Parking" to
        "Podatki i kaucje trafiają do wspólnej puli. Kto wyląduje na Darmowym Parkingu, zgarnia całą pulę.",
    "Handel" to
        "W dowolnym momencie swojej otwartej tury możesz zaproponować wymianę gotówki i " +
        "nieruchomości innemu graczowi - ekran \"Handel\". Druga strona akceptuje lub odrzuca.",
    "Bankructwo" to
        "Jeśli nie stać Cię na zapłatę, odpadasz z gry. Twoje nieruchomości trafiają do " +
        "wierzyciela (dług wobec gracza) albo wracają do puli (dług wobec banku).",
)

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Regulamin", style = MaterialTheme.typography.headlineMedium)

        RULE_SECTIONS.forEach { (title, body) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Wroc")
        }
    }
}
