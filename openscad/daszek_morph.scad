// ============================================================================
// Daszek "Morph" - dwa pochyle daszki (profil "Lambda"), puste pod spodem,
//   zamkniete na koncach trojkatnymi sciankami czolowymi z nacieciem na szczycie.
// ----------------------------------------------------------------------------
// Konstrukcja (wg zdjec z Dysku Google, folder Morph):
//   - dwa panele dachu (lewy + prawy) o grubosci "grubosc_scianki", spotykaja
//     sie na kalenicy; POD SPODEM PUSTE (bez dna, bez podstawy),
//   - na obu koncach pelna trojkatna ScIANKA CZOLOWA (szczyt) zamykajaca profil,
//   - w szczycie kazdego czola polokragle NACIECIE (jak na zdjeciu konca czesci).
// Material: PETG, druk FDM. Jednostki: milimetry.
// Orientacja druku: podstawa na stole, kalenica do gory - bez podpor.
// Wersja jednoplikowa (biblioteka na dole) - dziala tez w OpenSCAD Playground.
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

/* [Naciecie na szczycie czola] */
// szerokosc naciecia (srednica polokragla) [mm]
naciecie_szerokosc = 5;     // [2:0.5:30]
// glebokosc naciecia liczona od szczytu w dol [mm]
naciecie_glebokosc = 4;     // [1:0.5:30]
// jak gleboko naciecie wchodzi w kalenice za czolem [mm]
naciecie_zasieg = 3;        // [0:0.5:30]

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
// jednostkowy wektor "do wnetrza" dla prawego panelu (prostopadly do spadku)
nx = -wys / sk;
ny = -(b/2) / sk;

echo(str("Daszek: dlugosc=", dlugosc, " podstawa=", b,
         " skrzydlo=", sk, " wysokosc=", wys, " panel=", t, " czolo=", gc));

// ============================================================================
// MODULY
// ============================================================================

// Prawy panel dachu (2D): pas o grubosci t wzdluz prawego spadku
module bar_prawy() {
    polygon([
        [b/2,            0],              // dolny naroznik zewnetrzny
        [0,              wys],            // kalenica (zewnatrz)
        [t*nx,           wys + t*ny],     // kalenica (wewnatrz)
        [b/2 + t*nx,     t*ny]            // dolny naroznik wewnetrzny
    ]);
}

// Profil dachu (2D): lewy + prawy panel, przyciete plasko u dolu (y>=0)
module profil_daszek() {
    intersection() {
        union() {
            bar_prawy();
            mirror([1, 0, 0]) bar_prawy();
        }
        translate([-1000, 0]) square([2000, 1000]);   // odetnij ponizej podstawy
    }
}

// Pelny trojkat (2D) - ksztalt scianki czolowej
module trojkat_pelny() {
    polygon([[-b/2, 0], [b/2, 0], [0, wys]]);
}

// Dach na calej dlugosci, kalenica do gory, dlugosc wzdluz Y (0..dlugosc)
module korpus_daszek() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = dlugosc)
                profil_daszek();
}

// Scianka czolowa przy poczatku (y: 0..gc)
module czolo_y0() {
    translate([0, gc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = gc)
                trojkat_pelny();
}

// Scianka czolowa przy koncu (y: dlugosc-gc..dlugosc)
module czolo_y1() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = gc)
                trojkat_pelny();
}

// Naciecie w szczycie: polokragly rowek od gory, przez czolo i kawalek kalenicy.
// y0 = poczatek wzdluz osi, dl = dlugosc rowka wzdluz osi
module naciecie_szczyt(y0, dl) {
    rr = min(naciecie_szerokosc / 2, naciecie_glebokosc);   // promien dna
    z0 = wys - naciecie_glebokosc + rr;                     // srodek luku dna
    union() {
        // zaokraglone dno (walec lezacy wzdluz Y)
        translate([0, y0, z0])
            rotate([-90, 0, 0])
                cylinder(h = dl, r = rr, $fn = fn_walec(rr));
        // proste scianki rowka az ponad kalenice
        translate([-rr, y0, z0])
            cube([2 * rr, dl, (wys - z0) + 2]);
    }
}

// Gotowy daszek
module daszek() {
    dl0 = gc + naciecie_zasieg + EPS;      // dlugosc naciecia od konca
    difference() {
        union() {
            korpus_daszek();
            if (czolo_wl) czolo_y0();
            if (czolo_wl && czolo_oba) czolo_y1();
        }
        if (czolo_wl) {
            naciecie_szczyt(-EPS, dl0);                       // naciecie na poczatku
            if (czolo_oba)
                naciecie_szczyt(dlugosc - dl0 + EPS, dl0);    // naciecie na koncu
        }
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
