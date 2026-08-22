// ============================================================================
// KORPUS "MORPH" v7 - glowica SZLIFIERKI NAROZNEJ (corner sander)
// do plyt gipsowo-kartonowych, montowana na dragu teleskopowym.
// ----------------------------------------------------------------------------
// Geometria: klin Λ o profilu trojkatnym.
//   - przednia scianka ZAMKNIETA (trojkatna plansza z napisem "morph" wypuklym)
//   - tylny koniec OTWARTY (zaokraglone konce skrzydel - opcjonalne)
//   - wnetrze PUSTE ("BE HOLLOW INSIDE")
//   - mostek lity w srodku dlugosci z gniazdem kulkowym OD DOLU
// Wymiary: Jacek podal dlugosc=160, wys=100, pol-podstawy=80, kula=24 mm
// Material: PETG. Druk: kalenica u gory, spod na stole - bez podpor.
// Wersja jednoplikowa (telefon / OpenSCAD Playground / desktop).
// Data: 2026-08-22
// ============================================================================

/* [Co renderowac] */
czesc = "wszystko"; // [wszystko, korpus, podglad_gniazda]

/* [Wymiary glowne] */
// DLUGOSC na kalenicy [mm]
dlugosc = 160; // [60:1:250]
// WYSOKOSC od podstawy skrzydel do kalenicy [mm]
wys_glowna = 100; // [30:1:200]
// POLOWA SZEROKOSCI podstawy (poziomy zasieg jednego skrzydla) [mm]
polowa_podstawy = 80; // [20:1:160]
// grubosc scianki skrzydel [mm]
grubosc_scianki = 3.0; // [1.6:0.1:6]
// grubosc przedniej scianki czolowej [mm]
grubosc_czola = 3.0; // [1.6:0.1:8]

/* [Zaokraglenie tylnych koncow skrzydel] */
// UWAGA: wolny render na telefonie - wlacz tylko na desktopie
zaokraglenie_koncow_wl = false; // [true, false]
zaokr_promien = 12; // [0:0.5:30]

/* [Rowki wzdluzne (usztywnienia)] */
rowki_wl = true; // [true, false]
rowki_liczba = 2; // [0:1:6]
rowek_szer = 3.0; // [1:0.1:6]
rowek_glebokosc = 1.5; // [0.4:0.1:3]
rowek_od_kalenicy = 18; // [3:0.5:40]
rowek_odstep = 20; // [4:0.5:40]
rowek_margines = 15; // [0:1:40]

/* [Plaster miodu (dekoracja zewn. skrzydel)] */
// UWAGA: ~600 heksagonow na skrzydlo - bardzo wolny render na telefonie
plaster_wl = false; // [true, false]
heks_rozstaw = 8; // [3:0.5:15]
heks_scianka = 1.0; // [0.4:0.1:2]
plaster_glebokosc = 1.0; // [0.4:0.1:3]
plaster_margines = 8; // [0:0.5:20]

/* [Napis na czolowej sciance] */
// UWAGA: text() wiesza OpenSCAD Playground - wlacz tylko na desktopie
napis_wl = false; // [true, false]
napis = "morph";
napis_wys = 14; // [4:0.5:24]
napis_glebokosc = 1.2; // [0.2:0.1:4]
// "" = font domyslny (dziala wszedzie, w tym telefon/Playground)
// "Liberation Sans:style=Bold" = desktop OpenSCAD
napis_font = "";

/* [Gniazdo kulkowe od spodu] */
// UWAGA: sfera wiesza OpenCSG preview w Playground - wlacz tylko na desktopie/do druku
gniazdo_wl = false; // [true, false]
kula_srednica = 24; // [6:0.5:40]
kula_luz = 0.4; // [0:0.05:1.5]
wpust_srednica = 12; // [3:0.5:25]
gniazdo_pozycja = 0.5; // [0.1:0.05:0.9]
drut_srednica = 1.5; // [0.5:0.1:3.0]
mostek_szer = 36; // [10:1:80]

/* [Kalibracja drukarki] */
KOREKTA = 0.0; // [-0.3:0.05:0.3]

/* [Jakosc] */
BLAD_CIECIWY = 0.05; // [0.02:0.01:0.2]

/* [Hidden] */
EPS = 0.01;
function fn_walec(r) = max(24, ceil(360/(2*acos(1-min(0.99,BLAD_CIECIWY/max(0.1,r))))));
function fn_kula(r)  = max(32, ceil(360/(2*acos(1-min(0.99,BLAD_CIECIWY/max(0.1,r))))));

// wartosci pochodne
wys       = wys_glowna;
b_polowa  = polowa_podstawy;
polkat    = atan2(b_polowa, wys);          // kat od pionu do skrzydla
t         = grubosc_scianki;
sk        = sqrt(wys*wys + b_polowa*b_polowa); // dlugosc skrzydla wzdluz powierzchni
h_in      = max(0.5, wys - t / sin(polkat));
dno_in    = -1;
x_dol_in  = b_polowa * (h_in - dno_in) / wys;

echo(str("Klin Morph v7: dl=",dlugosc,"  wys=",wys,"  pol_podst=",b_polowa,
         "  kat=",round(2*polkat*10)/10,"  sk=",round(sk*10)/10,"  t=",t));

// ============================================================================
// PROFILE 2D
// ============================================================================

module trojkat_zewn_2d() {
    polygon([[-b_polowa, 0], [b_polowa, 0], [0, wys]]);
}

module trojkat_wewn_2d() {
    polygon([[0, h_in], [x_dol_in, dno_in], [-x_dol_in, dno_in]]);
}

module heks_2d(w, scianka) {
    d_op = (w - scianka) * 2/sqrt(3);
    rotate(90) circle(d = d_op, $fn = 6);
}

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

// Hollowing: usuwa wnetrze w zakresie y0..y1
// Wywoływane dwukrotnie (przed i za mostkiem) albo raz (bez gniazda).
// Zaczynam od grubosc_czola, zeby sciana czolowa (y=0..grubosc_czola) zostala lita.
module wydrazenie(y0, y1) {
    if (y1 > y0)
        translate([0, y1, 0])
            rotate([90, 0, 0])
                linear_extrude(height = y1 - y0)
                    trojkat_wewn_2d();
}

// zaokraglenie TYLNYCH koncow skrzydel (tylko y=dlugosc, przod jest zamkniety)
module zaokraglenie_koncow_neg() {
    if (zaokraglenie_koncow_wl && zaokr_promien > 0) {
        r = zaokr_promien;
        for (znak_x = [-1, 1]) {
            translate([znak_x * (b_polowa - r), dlugosc - r, -EPS])
                difference() {
                    translate([-r*znak_x, 0, 0])
                        cube([2*r, r + EPS, r + 2]);
                    translate([r - znak_x*r*2, 0, 0])
                        cylinder(h = r + 3, r = r, $fn = fn_walec(r));
                }
        }
    }
}

module rowek(znak_bok, r_od_kalenicy) {
    dl_rowka = max(0, dlugosc - grubosc_czola - 2*rowek_margines);
    if (dl_rowka > 0) {
        px = znak_bok * r_od_kalenicy * sin(polkat);
        pz = wys - r_od_kalenicy * cos(polkat);
        // rowek wzdluzny od sciany czolowej + margines do tylnego konca
        translate([px, grubosc_czola + rowek_margines + dl_rowka/2, pz])
            rotate([0, -znak_bok * polkat, 0])
                cube([2*rowek_glebokosc + 2*EPS, dl_rowka, rowek_szer], center = true);
    }
}

module rowki_wszystkie() {
    if (rowki_wl && rowki_liczba > 0) {
        for (i = [0 : rowki_liczba - 1]) {
            r_dist = rowek_od_kalenicy + i * rowek_odstep;
            if (r_dist < sk - 4) {
                rowek(+1, r_dist);
                rowek(-1, r_dist);
            }
        }
    }
}

module plaster_na_skrzydle(znak_bok) {
    d_pola = max(0, dlugosc - grubosc_czola - 2*plaster_margines);
    w_pola = max(0, sk - 2*plaster_margines);
    if (plaster_wl && d_pola > 0 && w_pola > 0) {
        cx = znak_bok * (sk/2) * sin(polkat);
        cy = grubosc_czola + plaster_margines + d_pola/2;
        cz = wys - (sk/2) * cos(polkat);
        translate([cx, cy, cz])
            rotate([0, -znak_bok * polkat, 0])
                translate([-plaster_glebokosc + EPS, -d_pola/2, -w_pola/2])
                    rotate([0, 90, 0])
                        linear_extrude(height = plaster_glebokosc + EPS)
                            heks_siatka(d_pola, w_pola, heks_rozstaw, heks_scianka);
    }
}

// Napis "morph" WYPUKLY na przedniej sciance czolowej
// Litery wysuwaja sie w -Y (przed czolo modelu)
module napis_na_czole() {
    if (napis_wl && napis != "") {
        // pozycja: srodek X, ~30% wysokosci od podstawy skrzydel
        // zakres Y: od -(napis_glebokosc) do +1 (1mm wnika w czolo dla polaczenia)
        translate([0, 1, wys * 0.30])
            rotate([90, 0, 0])
                linear_extrude(height = napis_glebokosc + 1)
                    if (napis_font != "")
                        text(napis, size = napis_wys, font = napis_font,
                             halign = "center", valign = "center");
                    else
                        text(napis, size = napis_wys,
                             halign = "center", valign = "center");
    }
}

// srodek kuli w Z
function gz_srodek() = (h_in + dno_in) / 2 + 3;

module gniazdo_kulkowe_neg() {
    if (gniazdo_wl) {
        r  = kula_srednica/2 + kula_luz/2;
        cx = 0;
        cy = gniazdo_pozycja * dlugosc;
        cz = gz_srodek();
        // sfera
        translate([cx, cy, cz])
            sphere(r = r, $fn = fn_kula(r));
        // wpust szyjki draga od dolu
        translate([cx, cy, dno_in - 5])
            cylinder(h = cz - dno_in + 5, d = wpust_srednica,
                     $fn = fn_walec(wpust_srednica/2));
        // kanal poprzeczny na drut spr.
        if (drut_srednica > 0)
            translate([-b_polowa - 5, cy, cz])
                rotate([0, 90, 0])
                    cylinder(h = 2*b_polowa + 10, d = drut_srednica,
                             $fn = fn_walec(drut_srednica/2));
    }
}

// ============================================================================
// KORPUS
// ============================================================================

module korpus() {
    y_c  = gniazdo_pozycja * dlugosc;
    y_m0 = y_c - mostek_szer/2;
    y_m1 = y_c + mostek_szer/2;
    union() {
        difference() {
            bryla_zewn();
            // wydrazenie zaczyna sie OD grubosc_czola, zeby sciana czolowa zostala lita
            if (gniazdo_wl) {
                wydrazenie(grubosc_czola, y_m0);
                wydrazenie(y_m1, dlugosc + 2);
            } else {
                wydrazenie(grubosc_czola, dlugosc + 2);
            }
            zaokraglenie_koncow_neg();
            rowki_wszystkie();
            plaster_na_skrzydle(+1);
            plaster_na_skrzydle(-1);
            gniazdo_kulkowe_neg();
        }
        // napis wypukly doklejony do czola (na zewnatrz bryly)
        napis_na_czole();
    }
}

module podglad_gniazda() {
    intersection() {
        korpus();
        translate([-40, gniazdo_pozycja * dlugosc - 28, dno_in])
            cube([80, 56, wys + 5]);
    }
    %translate([0, gniazdo_pozycja * dlugosc, gz_srodek()])
        sphere(d = kula_srednica, $fn = fn_kula(kula_srednica/2));
}

// ============================================================================
// WYWOLANIE
// ============================================================================
if      (czesc == "wszystko")        korpus();
else if (czesc == "korpus")          korpus();
else if (czesc == "podglad_gniazda") podglad_gniazda();

// ============================================================================
// CO KRECIC SUWAKAMI
// ============================================================================
// * wys_glowna      - wysokosc od dolnej krawedzi skrzydel do kalenicy.
//                     Zmierz suwmiarka na gotowym lub ustal proporcje.
//                     Jacek: 100 mm.
// * polowa_podstawy - jak szeroko jedno skrzydlo siega poziomo od osi.
//                     Jacek: 80 mm. Lacznie obie = 160 mm.
// * dlugosc         - dlugosc wzdluz kalenicy. Jacek: 160 mm.
// * kula_srednica   - ZMIERZ kulke na dragu suwmiarka. Jacek: 24 mm.
// * kula_luz        - +0.1 jesli za ciasno, -0.1 jesli lataje.
// * grubosc_czola   - grubosc przedniej zamknietej scianki.
// * rowki_liczba    - usztywnienia wzdluzne (2 = jak na zdjeciach).
// * plaster_wl      - wzor plastra miodu na zewnatrz. WYLACZ na telefonie!
// * napis_wyl       - napis "morph" na czole. Font domyslny = dziala wszedzie.
//
// ============================================================================
// WYMIARY v7 (przy domyslnych parametrach)
// ============================================================================
//   dlugosc              160 mm
//   wys_glowna           100 mm  (Jacek)
//   polowa_podstawy       80 mm  (Jacek)
//   kat_rozwarcia      ~77.3°   (derived: 2*atan(80/100))
//   skrzydlo_pow       ~128 mm  (sqrt(100²+80²), dlugosc skrzydla po pow.)
//   kula_srednica         24 mm  (Jacek)
//   mostek_szer           36 mm  (kula 24 + 12 mm zapas)
//   gz_srodek             ~50 mm od podstawy
//
// PRZED DRUKIEM ZMIERZ:
//   - kule na dragu (kula_srednica)
//   - szyjke draga nad kula (wpust_srednica)
//   - grubosc drutu spr. (drut_srednica)
//
// PROBA KALIBRACYJNA:
//   1. czesc="podglad_gniazda" - drukuj, sprawdz opor kulki
//   2. Kalibruj kula_luz (+/-0.1)
//   3. Pelny wydruk: PETG, kalenica gora, bez podpor, warstwa 0.2, dysza 0.4
//
// ============================================================================
// HISTORIA WERSJI
// ============================================================================
// v1: klin Λ + rzedy "tarki" -> BLAD: zle id. jako tarka do gk
// v2: katownik L             -> BLAD: zle interpret. widok slicera
// v3: prostopadloscian       -> BLAD: zle interpret. m01 "BE HOLLOW INSIDE"
// v4: klin Λ z czolowymi     -> BLAD: za wask i zly kat
// v5: klin 93° bez czol      -> BLAD: gniazdo w kalenicy zamiast od spodu
// v6: gniazdo w mostku       -> BLAD: czolo otwarte, napis na kalenicy,
//                               kat 93° niespojny z wymiarami Jacka
// v7 (TA WERSJA):
//   - czolo ZAMKNIETE (grubosc_czola mm lita sciana przy y=0)
//   - napis "morph" WYPUKLY na czole (jak na zdjeciach z paczki)
//   - kat wynikajacy z wys=100 + pol-pod=80: ~77° (nie 93°)
//   - parametry glowne = wys_glowna + polowa_podstawy (latwiejsze do pomiaru)
//   - plaster_wl=false domyslnie (telefon)
//   - napis_font="" domyslnie (telefon/Playground)
// ============================================================================
