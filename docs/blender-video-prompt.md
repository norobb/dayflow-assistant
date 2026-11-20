# Produktions-Prompt: „Dayflow — Product Intro" (60s Blender-Renderfilm)

> **Für die ausführende KI:** Du steuerst eine laufende Blender-Instanz über das Blender-MCP
> (Tools: `get_addon_status`, `get_scene_info`, `execute_blender_code`, `look`, `save_blend_file`).
> Du modellierst, texturierst, animierst, beleuchtest und renderst den kompletten Film selbst.
> Alles unten ist verbindlich. arbeite Szene für Szene ab, prüfe Zwischenergebnisse mit `look`,
> und rendern am Ende die volle Sequenz.

---

## 1. Projekt-Briefing

**Produkt:** *dayflow* — eine App, die chaotische Eingaben (Chat-Nachrichten, Screenshots, PDFs,
Sprachnotizen) automatisch in strukturierte Kalendereinträge, Aufgaben und Erinnerungen verwandelt.

**Kernbotschaft des Films:** „Aus Unordnung wird ein Tag, den du verstehst."
Der Film zeigt visuell: fragmentierte Informationen fliegen wild durch den Raum → werden von einer
zentralen Instanz eingesammelt → ordnen sich zu einem ruhigen, klaren Tagesablauf.

**Ton:** Warm, editorial, ruhig, hochwertig — wie ein Werbefilm eines Design-Studios.
**NICHT:** techy-neon, sci-fi, blau-lila Gradients, Cyberpunk. Der Film soll aussehen wie die App:
Papier, Leinen, Weinrot, Tinte.

**Technische Basisdaten:**

| Parameter | Wert |
|---|---|
| Länge | exakt 60 Sekunden |
| Framerate | 24 fps → 1440 Frames |
| Auflösung | 1920 × 1080 (16:9) |
| Render Engine | Cycles (GPU, falls verfügbar; sonst Eevee Next) |
| Output | `/tmp/opencode/dayflow_intro/` als PNG-Sequenz + finaler Zusammenchnitt als MP4 (H.264) via VSE |
| Blend-Datei | `dayflow_intro_video.blend` im Projektordner, nach jedem fertigen Shot speichern |

---

## 2. Verbindliche Farbwelt (aus der echten App extrahiert)

Diese Palette ist nicht verhandelbar. Jedes Material im Film nutzt exakt diese Hex-Werte
(in Blender als Linear-sRGB-Werte eingeben; Blender konvertiert selbst):

| Hex | Name | Verwendung im Film |
|---|---|---|
| `#F5EFE6` | Warm Linen | Welthintergrund, Boden, „Papier"-Stimmung der leeren Fläche |
| `#FAF6F0` | Soft Cream | Kartenoberflächen, Phone-Screen-Grundfarbe |
| `#FFFFFF` | Card White | Aufpoppende UI-Karten, weiße Paper-Sheets |
| `#641C24` | Deep Burgundy | **Hauptfarbe.** Logo, Buttons, Akzentkanten, das „Herz" der Maschine |
| `#7E242F` | Burgundy Light | Hover-/Glanz-Stufe, sekundäre Akzente, Kantenlicht-Tint |
| `#F0E4E6` | Burgundy Tint | Weiche Hinterlegung von Insight-Karten, Schatten-Tint auf Papier |
| `#1E1B19` | Ink Charcoal | 3D-Typo „dayflow", Phone-Bezel, Linien, feine Details |
| `#6B635B` | Warm Taupe | Sekundärtext, gedämpfte Beschriftungen |
| `#D8CFC2` | Warm Border | Kartenränder, Risslinien, Grid-Linien |
| `#2E5C38` | Forest Green | Häkchen/Success-States, „erledigt"-Momente (sparsam!) |
| `#8C5E28` | Warm Amber | „COMING SOON"-Badges, kleine Statusmarker (sparsam!) |

**Material-Regeln:**
- Papier/Karten: leicht raue Principled-BSDF-Oberfläche, Roughness 0.6–0.8, Base Color exakt wie oben, minimale Subsurface nicht nötig.
- Burgundy-Akzente: Roughness 0.35–0.45, leichte Specular, wirkt wie lackiertes Papier/Siegelwachs.
- „dayflow"-Logotype: dunkles Charcoal `#1E1B19`, Roughness 0.4, kein Metall, kein Gloss-Overkill.
- Kein emissive außer: Toast-Benachrichtigungen dürfen minimal selbstleuchtend sein (Strength ≤ 2).
- Welt: flaches, warmes Leinen; Fog/Volume nur hauchzart (Tiefe lesen lassen, nie neblig wirken).

---

## 3. Licht-Setup (einmal bauen, für alle Shots wiederverwenden)

1. **Key Light:** Sun, warmweiß (Farbe ~ `#FFF4E4`), Strength 3–4, Winkel leicht von oben-links.
2. **Fill:** Large Area Light vor der Szene, Strength 1, neutral warm, weiche Schatten.
3. **Rim:** kleines Area Light hinten-rechts, Farbe `#7E242F` getönt (sehr subtil), Strength 2 — gibt den Karten eine warme Kante.
4. **Ambient:** World Background `#F5EFE6` mit Strength 0.4–0.6.
5. Soft Shadows an, Schattenfarbe nie grau-kalt, sondern warm-taupig (macht der Cycles-Indirect automatisch, wenn die World warm ist).

---

## 4. Shot-List — alle 7 Shots in Sekunden

### SHOT 1 — „Chaos" (0:00–0:08 · Frames 1–192)
- **Setting:** Große, leere Leinen-Fläche (`#F5EFE6`), leicht unebene Papier-Textur (Displacement sehr dezent).
- **Aktion:** 12–16 kleine Objekte purzeln/fallen aus dem Off ins Bild und liegen chaotisch verstreut:
  - 4 weiße Papierblätter („Chat-Nachricht": dünne extrudierte Sprechblasen-Platten),
  - 2 Screenshots (weiße Rechtecke mit dunklem Balken oben, simulierte UI-Blöcke aus dünnen Boxen),
  - 1 gefaltetes PDF (Buch-Objekt, 3–4 Seiten),
  - 2 „Sprachnotizen"-Wellenformen (dünne vertikale Stäbchen-Batterie, amber `#8C5E28` getönt),
  - diverse Krümel/Zettelreste.
- Sie landen mit leichtem Bounce (Keyframe: Position + Scale-Squash, F-Curve-Ease „Back" beim Aufprall).
- **Kamera:** langsamer Dolly-in von weit oben diagonal (35°–20° Neigung), leichte Rotation 5°.
- **Stimmung:** schön, aber unruhig — viele Härchen/Überlappungen, Schatten kreuz und quer.

### SHOT 2 — „Das Symbol erscheint" (0:08–0:16 · Frames 193–384)
- **Aktion:** In der Bildmitte formt sich aus einer sanften Welle im Papier das **Dayflow-Symbol**:
  ein abstrakter, geschlossener Ribbon-Loop (Torus-artig, aber elliptisch asymmetrisch, wie eine
  liegende Schleife) — Hauptfarbe Burgundy `#641C24`, Kanten dunkel `#1E1B19` (beveled edge),
  mittig ein kleiner Knoten (Sphere r≈0.05 relativ).
- Der Loop „sammelt" das Chaos: alle Papierobjekte aus Shot 1 werden per Keyframe-Nachlauf
  (Follow-Curve, gestaffelt 2–4 Frames Versatz) sanft in den Loop hineingezogen und verschwinden
  dort (Scale → 0 mit Ease-in). 
- **Kamera:** orbited 30° um den Loop, Höhe senkt sich von 15° auf Augenhöhe.
- **Sound-Logik (nur visuell rhythmisch umsetzen):** Jedes eingesammelte Objekt → winziger Scale-Pulse am Loop.

### SHOT 3 — „Struktur entsteht" (0:16–0:26 · Frames 385–624)
- **Aktion:** Aus dem Loop schießen 3 geordnete Objekte heraus und reihen sich in ein sauberes Raster:
  1. **Kalenderkarte** (weiß `#FFFFFF`, rounded corners 0.04, dünner Border `#D8CFC2`, oben ein Burgundy-Streifen als Zeit-Header, darauf 2 flache „Event"-Blöcke),
  2. **Task-Karte** (weiß, mit 3 Zeilen + Forest-Green `#2E5C38` Häkchen auf Karte 1),
  3. **Reminder-Karte** (weiß, mit einem Amber `#8C5E28` Glocken-Icon aus einfachen Primitiven).
- Die Karten landen exakt fluchtend (Grid, gleiche Abstände, gleiche Ausrichtung). Der Rest der Fläche bleibt leer — Negative Space ist Stil.
- **Kamera:** langsamer Seiten-Slide (lateral, 4 Einheiten), Parallelprojektion-artige Frontal-Schräge (leicht isometisch wirkend, aber perspektivisch).

### SHOT 4 — „Das Phone" (0:26–0:38 · Frames 625–912)
- **Aktion:** Die 3 Karten fliegen in ein Phone-Modell:
  - Phone: abgerundetes Box-Body (Bevel ~0.5), Farbe Charcoal `#1E1B19`,钛-bezel-Variante mit leicht warmem Grau `#3D3734` Kante (wie in der App), Screen als leicht emissive weiße Fläche `#FAF6F0`.
  - Auf dem Screen erscheinen nacheinander (alle 2 s) 3 Toast-Benachrichtigungen: weiße rounded Platten mit Burgundy-Icon, gleiten von oben ein, bleiben 1.5 s, sliden weiter nach unten in einen Stapel.
  - Die 3 großen Karten von Shot 3 schrumpfen (Scale 1 → 0.35) und gleiten **in** den Screen hinein (Position-Z animiert hinter die Screen-Ebene).
- **Kamera:** halbe Umrundung (90°) von leicht unten nach leicht oben, Phone bleibt bildmittig.
- **Details:** Seitliche Hardware-Buttons am Bezel (dünne Boxen), Punch-Hole-Camera (kleine schwarze Kugel oben Mitte), Home-Pill (dünne dunkle Kapsel unten).

### SHOT 5 — „Der Tag" (0:38–0:50 · Frames 913–1200)
- **Aktion:** Kamera „taucht" ins Phone-Screen ein (Dolly durch den Screen hindurch, 1 s, leichter FOV-Kick),
  landet in einer **Timeline-Welt**: eine vertikale Zeitachse (dünne Linie `#D8CFC2`) mit 5 Event-Karten,
  die nacheinander (alle ~1.5 s) mit Sanfter Ease-Out von links einfaden und einrasten:
  - Jede Karte: weiß, Border `#D8CFC2`, linke Kante Burgundy `#641C24`, Zeit-Label (flache dunkle Platte `#1E1B19`), Ort-Label (Taupe `#6B635B`).
  - Eine Karte bekommt ein Häkchen (Forest Green `#2E5C38`): der Haken zeichnet sich als kleine
    Extrusion nach, die Karte kurz grün-tinted (`#2E5C38` bei 20 % Opacity) und wieder weiß.
- Oberhalb schwebt eine **Insight-Karte** in Burgundy-Tint `#F0E4E6` mit burgundy Text-Balken —
  pulsiert sanft (Scale 1.0 → 1.02, 3 s-Zyklus, wie `.animate-gentle-pulse` in der App).
- **Kamera:** vertikaler Fahrstuhl-Fahrt nach unten entlang der Timeline (sanft, 10 s), leichtes Pendeln ±2°.

### SHOT 6 — „Today, understood." (0:50–0:56 · Frames 1201–1344)
- **Aktion:** Alles zoomt heraus zu einer ruhigen Komposition: Phone schwebt links, die 3 Karten
  schweben rechts im Grid, oben großer 3D-Text:
  - Zeile 1: **„Today,"** (Charcoal `#1E1B19`, extrudiert 0.02, sans-serif-ähnlich, fettnah — Bfont reicht,
    aber mit Extrude & Bevel 0.005),
  - Zeile 2: **„understood."** (Burgundy `#641C24`, gleiche Behandlung, leicht versetzt nach rechts).
- Text baut sich per Scale-Y-Reveal auf (wie ein Vorhang, 0.8 s pro Zeile).
- Darunter klein (flache Platte, Taupe): „from chaos to clarity".
- **Kamera:** langsamer Pullback + 5° Orbit, alles atmet (Idle-Bobbing ±0.01).

### SHOT 7 — „Logo & Outro" (0:56–1:00 · Frames 1345–1440)
- **Aktion:** Karten und Phone blenden/fallen weg, nur das Burgundy-Symbol (aus Shot 2) bleibt,
  zentriert, darunter Wortmarke **„dayflow"** (lowercase, Charcoal, extrudiert, Extrude 0.015).
- Letzte 1 s: sanftes Überblenden in reines Leinen `#F5EFE6`, Symbol skaliert minimal hoch (1.0 → 1.05)
  mit Ease-In-Out — Freeze-Frame-Gefühl.
- **Kamera:** voller Stillstand, einzige Bewegung ist der Symbol-Puls.

---

## 5. Animationstechnik (verbindlich)

- **Easing:** Niemals Linear. Standard: „Ease In Out — Sine" für Kamera, „Ease Out — Back" (Overshoot ~1.1)
  für Objekte, die „ankommen", „Ease In — Cubic" für Objekte, die „eingesammelt" werden.
- **Bewegungsmenge:** Maximal 2 gleichzeitige aktionen pro Shot (Objekt + Kamera). Ruhigkeit > Effektfeuerwerk.
- **Idle:** Alles, was „fertig" ist, bekommt subtiles Idle-Bobbing (±0.005–0.01 auf Z, 3–4 s-Zyklus, phasenversetzt).
- **Keyframe-Hygiene:** Jede Keyframe-Interpolation explizit setzen (kein „Bezier-Auto" mit Overshoot-Überraschungen).
- Kamera immer mit leichtem Handheld-Gefühl? **NEIN** — dieser Film ist statisch-edel. Null Wobble.

---

## 6. Render-Pipeline

1. **Vorbereitung:** Cycles, GPU (OptiX/HIP — prüfe `get_addon_status`; bei AMD: HIP), 128 Samples mit OpenImageDenoise reichen für Preview, Final 256–512.
2. **Test-Render:** Von jedem Shot EIN Frame (Mitte) in 1080p rendern, mit `look` visuell prüfen (Farben stimmig? Belichtung? Komposition?), dann Shot freigeben.
3. **Final:** Pro Shot als PNG-Sequenz rendern (`/tmp/opencode/dayflow_intro/shot01/frame####.png` etc., 4-stellig).
4. **Zusammenbau:** Neues VSE-Scene, alle Shot-Sequenzen in chronologischer Reihenfolge auf Video-Ebene,
   Crossfades NUR wo in der Shot-Liste angegeben (sonst Hard Cuts), Output-Format FFMPEG/H.264,
   Crf ~18, 24 fps, `dayflow_intro.mp4`.
5. **Deliverables:**
   - `dayflow_intro_video.blend`
   - `/tmp/opencode/dayflow_intro/` (PNG-Sequenzen)
   - `/home/norobb/Projects/dayflow-assistant/dayflow_intro.mp4` (1080p, 60 s)

---

## 7. Arbeitsweise (wie du vorgehst)

1. `get_addon_status` + `get_scene_info` → Blender-Version, GPU, freie Szene bestätigen.
2. Baue zuerst das **Shared Setup** (Welt, Lichter, Kamera-Rig, Materialbibliothek mit exakten Hex-Farben, ground plane) und speichere.
3. **Shot für Shot:** Szene aufbauen → Keyframes setzen → 1 Testframe rendern → `look` → korrigieren → speichern.
4. Nach jedem 2. Shot: Gesamteindruck mit `look` prüfen (Farbkonsistenz über Shots!).
5. Rendering läuft lange: Shot-Renderings einzeln starten, nicht alles in einem Script-Aufruf.
6. Wenn etwas nicht in 2 Korrekturversuchen gut aussieht: vereinfachen (weniger Detail schlägt kaputtes Detail). Der Stil ist reduziert — das ist erlaubt und gewollt.

---

## 8. Härte-No-Gos (Design-Bruch = Neubau)

- ❌ Neon, Glow-Overkill, Bloom auf Text, Chrom/Spiegel-Metall
- ❌ Blau/Lila in irgendeiner Form
- ❌ Übermäßig viele Partikel/Confetti
- ❌ Schnelle Cuts (schnellster Cut ist 2 s)
- ❌ Graue, kalte Schatten oder kaltes Licht
- ✅ Warm, papierig, ruhig, burgundy-akzentuiert, viel Leerraum
