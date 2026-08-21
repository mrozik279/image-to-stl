// ============================================================================
// Daszek "Morph" - dwa pochyle daszki (profil "Lambda"), puste pod spodem,
//   zamkniete na koncach trojkatnymi sciankami czolowymi z nacieciem na szczycie.
// ----------------------------------------------------------------------------
// Konstrukcja (wg zdjec z Dysku Google, folder Morph):
//   - trojkatny profil, ktory od spodu jest WYDRAZONY (zostaja dwa daszki
//     lewy+prawy o grubosci "grubosc_scianki", spotykaja sie na kalenicy),
//     POD SPODEM PUSTE - bez dna, bez podstawy,
//   - na obu koncach pelna trojkatna ScIANKA CZOLOWA (szczyt) zamykajaca profil,
//   - w kazdym czole TROJKATNE OKNO: waski wierzcholek 3 mm pod szczytem,
//     rozszerzajace sie ku dolowi (wysoki trojkat o waskiej podstawie).
// Zbudowane jako: bryla trojkatna  MINUS  wnetrze (otwarte u dolu)  MINUS naciecia.
// Material: PETG, druk FDM. Jednostki: milimetry.
// Orientacja druku: podstawa na stole, kalenica do gory - bez podpor.
// Wersja jednoplikowa - lekka, dziala tez w OpenSCAD Playground.
// Data: 2026-08-21
// ============================================================================

/* [Co renderowac] */
czesc = "wszystko"; // [wszystko, daszek, plyta_druku]

/* [Wymiary glowne] */
// dlugosc calego daszka (wzdluz osi) [mm]
dlugosc = 164;              // [40:1:400]
// rozstaw dolnych krawedzi (szerokosc u podstawy) [mm]
szerokosc_podstawy = 24;    // [10:0.5:80]
// dlugosc pochylej scianki - jednego daszka / spadku [mm]
skrzydlo = 20;              // [8:0.5:60]
// grubosc paneli dachu [mm]
grubosc_scianki = 2.0;      // [1.2:0.1:5]

/* [Scianki czolowe (szczyty)] */
// czy zamykac konce trojkatna scianka czolowa
czolo_wl = true;            // [true, false]
// scianki na obu koncach? (false = tylko poczatek)
czolo_oba = true;           // [true, false]
// grubosc scianki czolowej [mm]
grubosc_czola = 2.0;        // [1.2:0.1:6]

/* [Naciecie - trojkatne okno w czole] */
// ile ponizej szczytu zaczyna sie okno (waski wierzcholek u gory) [mm]
odsuniecie_gora = 3;        // [0:0.5:15]
// szerokosc podstawy okna u dolu ("waska podstawa") [mm]
podstawa_naciecia = 9;      // [2:0.5:30]
// wysokosc dolnej krawedzi okna nad podstawa daszka [mm]
dol_naciecia = 2;           // [0:0.5:15]

/* [Kalibracja drukarki] */
// globalna korekta wymiaru (jesli czesci wychodza za male/za duze) [mm]
KOREKTA = 0.0;              // [-0.3:0.05:0.3]

/* [Jakosc] */
// maksymalny blad cieciwy dla lukow - mniej = gladziej, wolniej [mm]
BLAD_CIECIWY = 0.05;        // [0.02:0.01:0.2]

/* [Hidden] */
EPS = 0.01;                 // maly zapas na operacje boolowskie

// --- wartosci pochodne (obliczane) --------------------------------------
b   = szerokosc_podstawy + KOREKTA;   // podstawa
sk  = skrzydlo;                       // dlugosc spadku
t   = grubosc_scianki;                // grubosc panelu
gc  = grubosc_czola;                  // grubosc czola
// wysokosc trojkata (spadek^2 = wysokosc^2 + (b/2)^2)
wys = sqrt(max(0.01, sk*sk - (b/2)*(b/2)));
// wewnetrzny szczyt (wierzcholek wnetrza) - z przeskalowania wzgledem okregu wpisanego
r_in    = b * wys / (b + 2 * sk);
skala   = (r_in - t) / r_in;
szczyt_w = r_in + skala * (wys - r_in);          // wysokosc wierzcholka wnetrza
// wnetrze siega ponizej podstawy (otwarte dno, bez plyty)
dno_w   = -1;                                     // dno wnetrza 1 mm pod podstawa
polowa_w = (szczyt_w - dno_w) * (b / 2) / wys;    // polowa szerokosci wnetrza u dolu

echo(str("Daszek: dlugosc=", dlugosc, " podstawa=", b, " skrzydlo=", sk,
         " wysokosc=", wys, " panel=", t, " czolo=", gc));

// ============================================================================
// MODULY
// ============================================================================

// Pelny trojkat (2D) - przekroj bryly zewnetrznej i scianki czolowej
module trojkat_pelny() {
    polygon([[-b/2, 0], [b/2, 0], [0, wys]]);
}

// Wnetrze (2D) - trojkat otwarty u dolu; po odjeciu zostaja dwa daszki bez dna
module profil_wnetrze() {
    polygon([[0, szczyt_w], [polowa_w, dno_w], [-polowa_w, dno_w]]);
}

// Bryla zewnetrzna na calej dlugosci, kalenica do gory, dlugosc wzdluz Y
module bryla_pelna() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = dlugosc)
                trojkat_pelny();
}

// Wydrazenie wnetrza w zakresie y0..y1 (poza czolami)
module wydrazenie(y0, y1) {
    translate([0, y1, 0])
        rotate([90, 0, 0])
            linear_extrude(height = y1 - y0)
                profil_wnetrze();
}

// Trojkatne OKNO w czole: waski wierzcholek u gory (3 mm pod szczytem),
// rozszerza sie ku dolowi. Wycinane na wylot przez scianke czolowa.
// y0 = poczatek wzdluz osi, dl = dlugosc otworu wzdluz osi
module naciecie_okno(y0, dl) {
    pb = podstawa_naciecia;
    translate([0, y0 + dl, 0])
        rotate([90, 0, 0])
            linear_extrude(height = dl)
                polygon([
                    [ 0,     wys - odsuniecie_gora],  // waski wierzcholek u gory
                    [-pb/2,  dol_naciecia],           // dol - lewy
                    [ pb/2,  dol_naciecia]            // dol - prawy
                ]);
}

// Gotowy daszek
module daszek() {
    // gdzie zaczyna/konczy sie wnetrze (czola zostawiaja lita bryle na koncach)
    y0 = czolo_wl              ? gc            : -EPS;
    y1 = (czolo_wl && czolo_oba) ? dlugosc - gc : dlugosc + EPS;
    difference() {
        bryla_pelna();
        wydrazenie(y0, y1);
        // trojkatne okno wycinane na wylot przez czolo
        if (czolo_wl)               naciecie_okno(-EPS, gc + 2 * EPS);
        if (czolo_wl && czolo_oba)  naciecie_okno(dlugosc - gc - EPS, gc + 2 * EPS);
    }
}

// Ulozenie na stole druku (czesc juz stoi podstawa na Z=0)
module plyta_druku() {
    daszek();
}

// ============================================================================
// WYWOLANIE
// ============================================================================
if (czesc == "wszystko" || czesc == "daszek") daszek();
else if (czesc == "plyta_druku") plyta_druku();

// ============================================================================
// ===== BIBLIOTEKA (wklejona, nie edytuj) =====
// Funkcje jakosci siatki - dobieraja $fn z bledu cieciwy, nigdy sztywne $fn.
// ============================================================================

// liczba segmentow walca/luku o promieniu r przy zadanym bledzie cieciwy
function fn_walec(r) =
    (r <= BLAD_CIECIWY)
        ? 24
        : max(24, ceil(180 / acos(1 - BLAD_CIECIWY / r)));
