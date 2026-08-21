// ============================================================================
// Daszek "Morph" - trojkatny, pusty w srodku, z nacieciami na kalenicy
// ----------------------------------------------------------------------------
// Co to jest: dlugi profil w ksztalcie daszka (trojkat rownoramienny),
//   pusty w srodku (kanal otwarty na obu koncach), z dwoma pochylymi
//   sciankami ("skrzydlami") i polokraglym nacieciem na szczycie (kalenicy)
//   przy kazdym koncu. Odtworzone ze zdjec z Dysku Google (folder Morph).
// Material: PETG, druk FDM. Jednostki: milimetry.
// Orientacja druku: tak jak model stoi - podstawa na stole, kalenica do gory
//   (druk bez podpor - kazda warstwa wezsza od poprzedniej).
// Wersja jednoplikowa (biblioteka wklejona na dole) - dziala tez na telefonie.
// Data: 2026-08-21
// ============================================================================

/* [Co renderowac] */
czesc = "wszystko"; // [wszystko, daszek, plyta_druku]

/* [Wymiary glowne] */
// dlugosc calego daszka (wzdluz osi) [mm]
dlugosc = 164;              // [40:1:400]
// szerokosc u podstawy trojkata (rozstaw dolnych narozy) [mm]
szerokosc_podstawy = 24;    // [10:0.5:80]
// dlugosc pochylej scianki - "skrzydla" / spadku daszka [mm]
skrzydlo = 20;              // [8:0.5:60]
// grubosc scianek trojkata (dwa spadki + dno) [mm]
grubosc_scianki = 2.0;      // [1.2:0.1:5]

/* [Stopki podstawy] */
// szerokosc plaskiej stopki wystajacej na boki u podstawy (0 = brak) [mm]
kolnierz_podstawy = 3;      // [0:0.5:20]

/* [Naciecia na kalenicy] */
// czy wycinac naciecia na szczycie
naciecie_wl = true;         // [true, false]
// szerokosc naciecia (srednica polokragla) [mm]
naciecie_szerokosc = 6;     // [2:0.5:30]
// glebokosc naciecia liczona od szczytu w dol [mm]
naciecie_glebokosc = 5;     // [1:0.5:30]
// odsuniecie srodka naciecia od konca daszka [mm]
naciecie_odsuniecie = 12;   // [0:1:100]

/* [Wykonczenie] */
// fazka na dolnej krawedzi (przeciw "sloniowej stopie" - elephant foot) [mm]
faza_dol = 0.6;             // [0:0.1:2]

/* [Kalibracja drukarki] */
// globalna korekta wymiaru (jesli czesci wychodza za male/za duze) [mm]
KOREKTA = 0.0;              // [-0.3:0.05:0.3]

/* [Jakosc] */
// maksymalny blad cieciwy dla luków - mniej = gladziej, wolniej [mm]
BLAD_CIECIWY = 0.05;        // [0.02:0.01:0.2]

/* [Hidden] */
EPS = 0.01;                 // maly zapas na operacje boolowskie

// --- wartosci pochodne (obliczane) --------------------------------------
b   = szerokosc_podstawy + KOREKTA;                 // podstawa
sk  = skrzydlo;                                      // dlugosc spadku
t   = grubosc_scianki;                               // grubosc scianki
kol = kolnierz_podstawy;                             // stopka
// wysokosc trojkata z twierdzenia Pitagorasa (spadek^2 = wysokosc^2 + (b/2)^2)
wys = sqrt(max(0.01, sk*sk - (b/2)*(b/2)));

echo(str("Daszek: dlugosc=", dlugosc, " podstawa=", b,
         " skrzydlo=", sk, " wysokosc=", wys, " scianka=", t));

// ============================================================================
// MODULY
// ============================================================================

// Przekroj pelny (2D): X = szerokosc, Y = wysokosc; podstawa na Y=0
module profil_pelny() {
    union() {
        polygon([[-b/2, 0], [b/2, 0], [0, wys]]);
        if (kol > 0) {
            // stopki zachodza na scianke o "t" (zgrzew, nie styk punktowy)
            translate([ b/2 - t, 0]) square([kol + t, t]);       // stopka prawa
            translate([-b/2 - kol, 0]) square([kol + t, t]);     // stopka lewa
        }
    }
}

// Przekroj wnetrza (2D): trojkat wciety o grubosc scianki -> zostaja sciany t
module profil_wnetrze() {
    offset(delta = -t) polygon([[-b/2, 0], [b/2, 0], [0, wys]]);
}

// Bryla pelna: przekroj wyciagniety na dlugosc, ustawiony kalenica do gory,
// dlugosc wzdluz osi Y (0..dlugosc)
module korpus_pelny() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = dlugosc)
                profil_pelny();
}

// Pusty kanal - wyciagniety odrobine dluzej, zeby konce byly otwarte
module korpus_wnetrze() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            translate([0, 0, -EPS])
                linear_extrude(height = dlugosc + 2 * EPS)
                    profil_wnetrze();
}

// Jedno naciecie: polokragly rowek wciety w kalenice z gory, w poprzek szerokosci
module jedno_naciecie() {
    rr  = min(naciecie_szerokosc / 2, naciecie_glebokosc); // promien dna
    z0  = wys - naciecie_glebokosc + rr;                   // srodek luku dna
    hx  = b / 2 + kol + 2;                                 // zasieg w poprzek (X)
    union() {
        // zaokraglone dno rowka (walec lezacy wzdluz X)
        translate([0, 0, z0])
            rotate([0, 90, 0])
                cylinder(h = 2 * hx, r = rr, center = true, $fn = fn_walec(rr));
        // proste scianki rowka az ponad kalenice
        translate([-hx, -rr, z0])
            cube([2 * hx, 2 * rr, (wys - z0) + 2]);
    }
}

// Naciecia przy obu koncach kalenicy
module naciecia() {
    for (y = [naciecie_odsuniecie, dlugosc - naciecie_odsuniecie])
        translate([0, y, 0]) jedno_naciecie();
}

// Fazka 45' na dolnych zewnetrznych krawedziach (relief pod pierwsza warstwe)
module faza_dolna() {
    oh = b / 2 + (kol > 0 ? kol : 0);   // zewnetrzna krawedz podstawy
    s  = faza_dol * sqrt(2);
    for (sx = [-1, 1])
        translate([sx * oh, dlugosc / 2, 0])
            rotate([0, 45, 0])
                cube([s, dlugosc + 2, s], center = true);
}

// Gotowy daszek
module daszek() {
    difference() {
        korpus_pelny();
        korpus_wnetrze();
        if (naciecie_wl) naciecia();
        if (faza_dol > 0) faza_dolna();
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

// liczba segmentow kuli o promieniu r
function fn_kula(r) =
    max(32, fn_walec(r));
