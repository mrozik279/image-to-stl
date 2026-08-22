// ============================================================================
// KORPUS "MORPH" v6 - glowica SZLIFIERKI NAROZNEJ (corner sander) do plyt
// gipsowo-kartonowych, montowana na dragu teleskopowym.
// ----------------------------------------------------------------------------
// Po przejrzeniu WSZYSTKICH 38 zdjec z folderu "Morph korpus" (Dysk Google) i
// weryfikacji przez Jacka. Kluczowe dowody:
//   n09: "93 degree wings" + "corner sander works"  -> kat rozwarcia = 93°
//   n10: "Morph to any Drywall"                     -> zastosowanie: gk
//   n12: widoczny rzep na wewn. powierzchni         -> mocowanie papieru sciern.
//   n01: caly system - drag + kulka + glowica       -> montaz na wysiegniku
//   n04: przekroj wewnetrzny - plaster miodu wewn.  -> dekoracja wygrawerowana
//   m01/n07: "BE HOLLOW INSIDE"                     -> wnetrze puste
//
// Geometria: dwa PROSTOKATNE SKRZYDLA polaczone kalenica pod katem 93°.
// Konce otwarte (nie ma scianek czolowych - inaczej niz w daszek_morph).
// Material: PETG. Druk FDM: kalenica u gory, spod skrzydel na stole - bez podpor.
// Wersja jednoplikowa - dziala tez w OpenSCAD Playground / na telefonie.
// Data: 2026-08-22
// ============================================================================

/* [Co renderowac] */
czesc = "wszystko"; // [wszystko, korpus, plyta_druku, podglad_gniazda]

/* [Wymiary glowne] */
// kat rozwarcia miedzy skrzydlami (93° dla wewn. narozn. gk) [stopnie]
kat_rozwarcia = 93; // [60:1:150]
// DLUGOSC MIERZONA NA WIERZCHOLKU (KALENICY) [mm]  <- Jacek: 160 mm
// (poniewaz brylla jest pryzmatem trojkatnym, kalenica i dolne krawedzie
// skrzydel maja te sama dlugosc, ale referencja z pomiaru jest zawsze
// wierzcholek/kalenica)
dlugosc = 160; // [60:1:250]
// szerokosc skrzydla od kalenicy do dolnej krawedzi [mm]
// (dostosowana do kuli 24 mm - mostek musi ja pomiescic w wnetrzu)
skrzydlo = 85; // [25:1:120]
// grubosc paneli skrzydel [mm]
grubosc_scianki = 3.0; // [1.6:0.1:6]

/* [Zaokraglenie koncow skrzydel] */
// czy zaokraglic konce skrzydel wzdluz dlugosci (widoczne na m05, n08)
// UWAGA: skomplikowana geometria, na OpenSCAD telefon renderuje sie wolno
zaokraglenie_koncow_wl = false; // [true, false]
// promien zaokraglenia dolnego kata konca skrzydla [mm]
zaokr_promien = 10; // [0:0.5:25]

/* [Rowki wzdluzne (usztywnienia widoczne na s01, m03)] */
rowki_wl = true; // [true, false]
// liczba rowkow wzdluz kazdego skrzydla
rowki_liczba = 2; // [0:1:6]
// szerokosc pojedynczego rowka [mm]
rowek_szer = 3.0; // [1:0.1:6]
// glebokosc wciecia rowka [mm]
rowek_glebokosc = 1.5; // [0.4:0.1:3]
// odsuniecie rowkow od kalenicy [mm]
rowek_od_kalenicy = 15; // [3:0.5:30]
// odstep miedzy rowkami wzdluz szerokosci [mm]
rowek_odstep = 18; // [4:0.5:30]
// margines rowka od koncow skrzydla wzdluz osi [mm]
rowek_margines = 15; // [0:1:40]

/* [Plaster miodu (dekoracja na zewn. powierzchni skrzydel)] */
// UWAGA: ~600 heksagonow na skrzydlo - na OpenSCAD telefon renderuje sie
// bardzo wolno (kilka minut) lub moze zabraknac pamieci. Wlacz TYLKO gdy
// jestes gotowy na dlugi render, albo zwieksz heks_rozstaw do 12-15.
plaster_wl = false; // [true, false]
// rozstaw pod klucz heksagonu [mm]
heks_rozstaw = 8; // [3:0.5:15]
// grubosc scianki miedzy heksagonami [mm]
heks_scianka = 1.0; // [0.4:0.1:2]
// glebokosc wciecia [mm]
plaster_glebokosc = 1.0; // [0.4:0.1:3]
// margines pola plastra od krawedzi skrzydla [mm]
plaster_margines = 8; // [0:0.5:20]

/* [Grawer "morph" na kalenicy] */
napis_wl = true; // [true, false]
napis = "morph";
// wysokosc czcionki [mm]
napis_wys = 12; // [3:0.5:20]
// glebokosc grawerki [mm]
napis_glebokosc = 1.0; // [0.2:0.1:3]
// czcionka (OpenSCAD desktop: "Liberation Sans:style=Bold"
//           OpenSCAD telefon/Playground: zostaw puste "")
napis_font = "Liberation Sans:style=Bold";

/* [Gniazdo kulkowe od spodu (mostek w srodku dlugosci, montaz na dragu)] */
gniazdo_wl = true; // [true, false]
// srednica kuli montazowej [mm]  <- Jacek: 24 mm
kula_srednica = 24; // [6:0.5:35]
// luz montazowy na kuli [mm]
kula_luz = 0.4; // [0:0.05:1.5]
// srednica otworu wpustowego dla szyjki draga (mniejsza od kuli!) [mm]
// (typowo 40-55 % srednicy kuli)
wpust_srednica = 12; // [3:0.5:25]
// polozenie srodka gniazda wzdluz dlugosci (0=przod, 1=tyl)
gniazdo_pozycja = 0.5; // [0.1:0.05:0.9]
// srednica otworu na drut spr. [mm]
drut_srednica = 1.5; // [0.5:0.1:3.0]
// szerokosc mostka (bryly wypelniajacej wnetrze wokol gniazda) wzdluz dlugosci [mm]
// (min. kula_srednica + 8 mm, aby kula pomiescila sie z zapasem)
mostek_szer = 36; // [10:1:80]

/* [Kalibracja drukarki] */
KOREKTA = 0.0; // [-0.3:0.05:0.3]

/* [Jakosc] */
BLAD_CIECIWY = 0.05; // [0.02:0.01:0.2]

/* [Hidden] */
EPS = 0.01;
function fn_walec(r) = max(24, ceil(360 / (2*acos(1 - min(0.99, BLAD_CIECIWY/max(0.1,r))))));
function fn_kula(r)  = max(32, ceil(360 / (2*acos(1 - min(0.99, BLAD_CIECIWY/max(0.1,r))))));

// wartosci pochodne
polkat = kat_rozwarcia / 2;                     // pol kata rozwarcia
sk = skrzydlo;
t = grubosc_scianki;
b_polowa = sk * sin(polkat);                    // polowa szerokosci u podstawy
wys = sk * cos(polkat);                         // wysokosc kalenicy nad podstawa
// wnetrze: mniejszy trojkat, wnetrzny szczyt nizej o t/sin(polkat)
h_in = max(0.5, wys - t / sin(polkat));
dno_in = -1;
x_dol_in = (b_polowa) * (h_in - dno_in) / wys;

echo(str("Klin Morph v6: dl=", dlugosc, "  kat=", kat_rozwarcia,
         "  sk=", sk, "  b=", 2*b_polowa, "  h=", wys, "  panel=", t));

// ============================================================================
// PROFILE 2D
// ============================================================================

module trojkat_zewn_2d() {
    polygon([[-b_polowa, 0], [b_polowa, 0], [0, wys]]);
}

module trojkat_wewn_2d() {
    polygon([[0, h_in], [x_dol_in, dno_in], [-x_dol_in, dno_in]]);
}

// heksagon pointy-top o rozstawie pod klucz w
module heks_2d(w, scianka) {
    d_op = (w - scianka) * 2/sqrt(3);
    rotate(90) circle(d = d_op, $fn = 6);
}

// siatka heksagonalna
module heks_siatka(dl, wys_pola, w, scianka) {
    krok_x = w;
    krok_y = w * sqrt(3)/2;
    n_x = ceil(dl / krok_x) + 1;
    n_y = ceil(wys_pola / krok_y) + 1;
    intersection() {
        translate([dl/2, wys_pola/2, 0]) square([dl, wys_pola], center = true);
        union() {
            for (iy = [0 : n_y])
                for (ix = [0 : n_x]) {
                    x = ix * krok_x + (iy % 2 == 0 ? 0 : krok_x/2);
                    y = iy * krok_y;
                    translate([x, y, 0]) heks_2d(w, scianka);
                }
        }
    }
}

// ============================================================================
// BRYLY 3D
// ============================================================================

module bryla_zewn() {
    translate([0, dlugosc, 0])
        rotate([90, 0, 0])
            linear_extrude(height = dlugosc)
                trojkat_zewn_2d();
}

// Wydrazenie wnetrza tylko w zakresie y0..y1 (moze byc wywolane dwukrotnie:
// przed i za mostkiem, zeby mostek gniazda pozostal lity).
module wydrazenie(y0, y1) {
    if (y1 > y0)
        translate([0, y1, 0])
            rotate([90, 0, 0])
                linear_extrude(height = y1 - y0)
                    trojkat_wewn_2d();
}

// zaokraglenie koncow skrzydel (odetnij dolne rogi przy y=0 i y=dlugosc)
module zaokraglenie_koncow_neg() {
    if (zaokraglenie_koncow_wl && zaokr_promien > 0) {
        r = zaokr_promien;
        // dla obu skrzydel, po obu koncach - klinaki naroznikow
        for (znak_y = [0, 1])
            for (znak_x = [-1, 1]) {
                y_konc = znak_y == 0 ? 0 : dlugosc;
                znak = znak_y == 0 ? -1 : 1;   // kierunek wciecia w Y
                // wciecie dolnego naroznika: cube minus cylinder w rogu
                translate([znak_x * (b_polowa - r), y_konc + znak * r/2, -EPS])
                    difference() {
                        translate([-r*znak_x, -r*znak, 0])
                            cube([2*r, r + EPS, r + 2]);
                        translate([r - znak_x*r*2, -znak*r, 0])
                            cylinder(h = r + 3, r = r, $fn = fn_walec(r));
                    }
            }
    }
}

// pojedynczy rowek na skrzydle (znak_bok = +1 prawe, -1 lewe)
// r_od_kalenicy = odsuniecie srodka rowka od kalenicy wzdluz skrzydla
module rowek(znak_bok, r_od_kalenicy) {
    dl_rowka = max(0, dlugosc - 2 * rowek_margines);
    if (dl_rowka > 0) {
        // punkt na powierzchni skrzydla w odl. r_od_kalenicy od kalenicy
        px = znak_bok * r_od_kalenicy * sin(polkat);
        pz = wys - r_od_kalenicy * cos(polkat);
        // rowek jako podluzny cuboid na wylot
        translate([px, dlugosc/2, pz])
            rotate([0, -znak_bok * polkat, 0])
                cube([2 * rowek_glebokosc + 2*EPS,
                      dl_rowka,
                      rowek_szer],
                     center = true);
    }
}

module rowki_wszystkie() {
    if (rowki_wl && rowki_liczba > 0) {
        for (i = [0 : rowki_liczba - 1]) {
            r_dist = rowek_od_kalenicy + i * rowek_odstep;
            if (r_dist < sk - 3) {
                rowek(+1, r_dist);
                rowek(-1, r_dist);
            }
        }
    }
}

// plaster miodu wciety w zewn. powierzchnie skrzydla (znak_bok = +1 prawe, -1 lewe)
module plaster_na_skrzydle(znak_bok) {
    // pole plastra: pas na powierzchni skrzydla, od kalenicy do dolnej krawedzi
    // z odsunieciem plaster_margines po obu stronach
    d_pola = max(0, dlugosc - 2 * plaster_margines);
    w_pola = max(0, sk - 2 * plaster_margines);
    if (d_pola > 0 && w_pola > 0) {
        // umiescic pole na powierzchni skrzydla:
        // - srodek pola: na osi skrzydla, w polowie dlugosci
        cx = znak_bok * (sk/2) * sin(polkat);
        cy = dlugosc / 2;
        cz = wys - (sk/2) * cos(polkat);
        translate([cx, cy, cz])
            rotate([0, -znak_bok * polkat, 0])
                translate([-plaster_glebokosc + EPS, -d_pola/2, -w_pola/2])
                    rotate([0, 90, 0])
                        linear_extrude(height = plaster_glebokosc + EPS)
                            heks_siatka(d_pola, w_pola, heks_rozstaw, heks_scianka);
    }
}

// napis wygrawerowany na powierzchni kalenicy (od gory - druk 3D pokazuje litery od gory)
// UWAGA: na OpenSCAD telefon/Playground - ustaw napis_font = "" (pusty string)
module napis_na_kalenicy() {
    if (napis_wl && napis != "") {
        translate([0, dlugosc/2, wys - napis_glebokosc + EPS])
            linear_extrude(height = napis_glebokosc + 0.3)
                if (napis_font != "")
                    text(napis, size = napis_wys,
                         font = napis_font,
                         halign = "center", valign = "center");
                else
                    text(napis, size = napis_wys,
                         halign = "center", valign = "center");
    }
}

// srodek kuli - na srodku dlugosci, na osi symetrii, wysokosc w Z tak zeby kula
// byla wewnatrz mostka (nizej niz wewn. kalenica, aby wpust szedl na wylot przez
// dolna krawedz mostka).
function gz_srodek() = (h_in + dno_in) / 2 + 3;   // tuz nad srodkiem wnetrza

// mostek - bryla LITA wypelniajaca wnetrze klina w srodku dlugosci
// (dodawana pozytywnie do korpusu; gniazdo wywiercone w tej bryle)
module mostek_gniazda_bryla() {
    y_c = gniazdo_pozycja * dlugosc;
    translate([0, y_c + mostek_szer/2, 0])
        rotate([90, 0, 0])
            linear_extrude(height = mostek_szer)
                trojkat_wewn_2d();
}

// gniazdo kulkowe od SPODU, wywiercone w mostku
// - kula ma srodek w gz_srodek(), sfera + wpust dla szyjki draga od dolu +
//   kanal poprzeczny na drut spr.
module gniazdo_kulkowe_neg() {
    if (gniazdo_wl) {
        r = kula_srednica/2 + kula_luz/2;
        cx = 0;
        cy = gniazdo_pozycja * dlugosc;
        cz = gz_srodek();
        // 1) sfera na kule
        translate([cx, cy, cz])
            sphere(r = r, $fn = fn_kula(r));
        // 2) wpust dla SZYJKI DRAGA (mniejszy od kuli - kula nie wypadnie) w dol
        translate([cx, cy, dno_in - 5])
            cylinder(h = cz - dno_in + 5,
                     d = wpust_srednica,
                     $fn = fn_walec(wpust_srednica/2));
        // 3) kanal poprzeczny na drut spr. - na wylot przez oba skrzydla
        if (drut_srednica > 0) {
            translate([-b_polowa - 5, cy, cz])
                rotate([0, 90, 0])
                    cylinder(h = 2*b_polowa + 10, d = drut_srednica,
                             $fn = fn_walec(drut_srednica/2));
        }
    }
}

// ============================================================================
// KORPUS
// ============================================================================

module korpus() {
    y_c = gniazdo_pozycja * dlugosc;
    y_m0 = y_c - mostek_szer/2;
    y_m1 = y_c + mostek_szer/2;
    difference() {
        // klin zewnetrzny (mostek juz wliczony bo wydrazamy poza jego zakresem)
        bryla_zewn();
        // wydrazenie wnetrza PRZED i ZA mostkiem (obszar mostka pozostaje lity)
        if (gniazdo_wl) {
            wydrazenie(-2, y_m0);
            wydrazenie(y_m1, dlugosc + 2);
        } else {
            wydrazenie(-2, dlugosc + 2);
        }
        zaokraglenie_koncow_neg();
        rowki_wszystkie();
        plaster_na_skrzydle(+1);
        plaster_na_skrzydle(-1);
        napis_na_kalenicy();
        gniazdo_kulkowe_neg();
    }
}

module podglad_gniazda() {
    intersection() {
        korpus();
        translate([-30, gniazdo_pozycja * dlugosc - 25, dno_in])
            cube([60, 50, wys + 5]);
    }
    %translate([0, gniazdo_pozycja * dlugosc, gz_srodek()])
        sphere(d = kula_srednica, $fn = fn_kula(kula_srednica/2));
}

module plyta_druku() {
    korpus();
}

// ============================================================================
// WYWOLANIE
// ============================================================================
if      (czesc == "wszystko")        korpus();
else if (czesc == "korpus")          korpus();
else if (czesc == "plyta_druku")     plyta_druku();
else if (czesc == "podglad_gniazda") podglad_gniazda();

// ============================================================================
// CO KRECIC SUWAKAMI
// ============================================================================
// * kat_rozwarcia    - kat miedzy skrzydlami. 93° dla wewn. narozn. gk (troche
//                      wieksze niz 90° dla ciasnego przylegania). Zmien pod
//                      inny narozniki: zewn. 87°, otwarty 120°.
// * dlugosc          - jak dluga glowica wzdluz kalenicy. Domyslnie 150 mm.
// * skrzydlo         - dlugosc jednego skrzydla od kalenicy do dolnej krawedzi.
//                      Wiekszy = wieksza powierzchnia scierna.
// * grubosc_scianki  - 3 mm dla PETG jest solidne. Nie schodz ponizej 2.
// * kula_srednica    - ZMIERZ kulke na dragu teleskopowym. Standardy 8/10/12 mm.
// * kula_luz         - +0.1 jesli za ciasno, -0.1 jesli lataje.
// * rowki_liczba     - ile rowkow usztywnien na kazdym skrzydle (widoczne na
//                      zdjeciach oryginalu - 2 rowki na skrzydlo).
// * plaster_wl       - wzor plastra miodu na zewn. powierzchni jako dekoracja.
//                      Wylacz jesli chcesz gladka powierzchnie.
// * KOREKTA          - +0.1 gdy wychodzi za male, -0.1 za duze.
//
// ============================================================================
// SYSTEM MORPH (co jest w ekosystemie, dla kontekstu)
// ============================================================================
// TEN plik = GLOWICA szlifierska (korpus_morph.scad).
// Do pelnego zestawu potrzebne:
//   - drag teleskopowy (kupiony gotowy lub drewniany kij)
//   - metalowy uchwyt kulowy Ø kula_srednica na koncu draga (kupiony gotowy)
//   - drut spr. Ø drut_srednica (0.8-1.0 mm) - z pinu, spinacz biurowy, itp.
//   - papier scierny wycinany do rozmiaru skrzydla, przyklejany rzepem
//   - RZEP samoprzylepny (paski velcro), doklejany na wewn. pow. skrzydel
//
// Instrukcja montazu drutu spr.: wsun kulke do gniazda -> przelozyc drut przez
// otwor poprzeczny nad kulka -> drut trzyma kulke w gniezdzie i pozwala
// swobodne obroty glowicy wokol kulki.
//
// ============================================================================
// WYMIARY (Jacek: dlugosc 160 mm mierzona NA WIERZCHOLKU, kula 24)
// ============================================================================
// Wymiary v6 dostosowane pod:
//   dlugosc              160 mm  (na wierzcholku/kalenicy - podane)
//   kula_srednica         24 mm  (podane)
//   kat_rozwarcia         93°    (z n09 - "93 degree wings")
//   skrzydlo              85 mm  (dopasowane pod kule 24mm, aby zmiescila sie
//                                w mostku z zapasem)
//   grubosc_scianki       3 mm   (PETG, standard)
//
// Wartosci pochodne (przy tych parametrach):
//   szerokosc_podstawy  ~123 mm  (2 * 85 * sin 46.5°)
//   wysokosc_kalenicy    ~59 mm  (85 * cos 46.5°)
//   wysokosc_wnetrza     ~55 mm  (od dna_in=-1 do h_in=54.4)
//   srodek_kuli_w_Z      ~30 mm  (od podstawy)
//   szerokosc_mostka
//     wzdluz Y (dlugosci) 36 mm  (kula 24 + 12 mm zapasu)
//     wzdluz X na Z=30 mm 52 mm  (kula 24 mieści się z zapasem 14 mm)
//     wzdluz X na Z=42 mm 27 mm  (gorna krawedz kuli - kula ma tam prom. 0)
//
// PRZED DRUKIEM ZMIERZ:
//   - kule na dragu (24 mm zgodnie z Jackiem, ale sprawdz suwmiarka)
//   - szyjke draga tuz nad kula (przewiduje 12 mm, moze byc 10-14)
//   - grubosc drutu spr. (przewiduje 1.5 mm, moze byc 1.0-2.0)
//
// Niezaimplementowane cechy:
//   - "lopatkowe" zaokraglenie koncow skrzydel (m05, n08) - jest szkielet
//     "zaokraglenie_koncow" ale wymaga dopracowania geometrii
//   - RZEP samoprzylepny jest osobnym elementem - naklejasz go po druku, nie
//     modelujesz w pliku (chyba ze chcesz go zaznaczyc jako obszar przygotowany
//     do przyklejenia - w tej wersji nie zaznaczony)
//   - dokladny font "morph" moze sie roznic od oryginalu (uzyty Liberation Sans
//     Bold, brak kropki po napisie - w oryginale bywa "morph." z kropka)
//
// PROBKA KALIBRACYJNA:
//   1. Wydrukuj czesc = "podglad_gniazda" - sprawdz opor wsuwania kulki.
//   2. Wydrukuj maly fragment glowicy (dlugosc=30, plaster_wl=false) - sprawdz
//      kat 93°, grubosc scianki.
//   3. Po korekcie druku pelnej glowicy przymocuj RZEP samoprzylepny na wewn.
//      powierzchniach - dopiero to zakończy narzedzie.
//
// ============================================================================
// HISTORIA WERSJI
// ============================================================================
// v1 (blad): daszek Λ + rzedy "tarki"       -> tarka gk, wg Jacka zle
// v2 (blad): katownik L z pionowymi scianami -> zle interpret. widoku slicera
// v3 (blad): prostopadloscienny blok         -> zle interpret. m01
// v4 (blizej): klin Λ z gniazdem, oknem czolowym
//              -> BLAD: klin miał czolowe scianki i waski kat (nie 93°)
// v5 (blad): klin bez czol z katem 93°, ale gniazdo umieszczone w KALENICY
//            -> BLAD: wg Jacka gniazdo jest OD SPODU (od strony otwartego
//            wnetrza klina) w srodku dlugosci, w mostku wypelniajacym wnetrze.
//            Zdjecia s14/s15 pokazywaly PRZEKROJ przez mostek, nie kalenice.
// v6b: napis_font jako parametr (desktop: Liberation Sans Bold, telefon: "")
// v6 (TA WERSJA): gniazdo w mostku (bryla lita) na srodku dlugosci wnetrza
//                 klina. Kula wchodzi OD DOLU (od otwartego dna) przez wpust
//                 dla SZYJKI DRAGA (mniejszy od kuli). Drut spr. poprzecznie
//                 na wylot. Wydrazenie wnetrza na dwoch obszarach - przed i
//                 za mostkiem, mostek pozostaje lity.
// ============================================================================
