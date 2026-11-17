# Android-DiariViatges-Storage

Aplicació Android (Jetpack Compose + MVVM) per registrar rutes o escapades, generar una
"postal / crònica" visual i gestionar la persistència tant a l'emmagatzematge intern
privat com a l'extern (exportable i compartible).

## Arquitectura MVVM aplicada

```
app/src/main/java/com/example/diariiviatges/
├── MainActivity.kt              # Activitat única: setContent { DiariViatgesTheme { AppNavigation() } }
├── model/
│   ├── RouteEntry.kt            # Model immutable (@Serializable) + enum Difficulty
│   └── TravelDiaryUiState.kt    # Estat únic de la UI: camps, errors, historial, ruta seleccionada
├── viewmodel/
│   └── TravelDiaryViewModel.kt  # MutableStateFlow privat + StateFlow públic (.asStateFlow())
│                                # validació en temps real amb .copy() i persistència en corrutines
├── data/
│   └── StorageRepository.kt     # E/S de fitxers (filesDir + Uri via SAF) amb withContext(Dispatchers.IO)
├── util/
│   └── Formatters.kt            # Crònica en format Markdown
├── navigation/
│   └── AppNavigation.kt         # NavHost: form → postal / history (ViewModel compartit)
├── ui/
│   ├── FormScreen.kt            # OutlinedTextField, FilterChip, DatePickerDialog, Switch
│   ├── PostalScreen.kt          # Targeta visual + exportació CreateDocument + compartir
│   ├── HistoryScreen.kt         # Llista de rutes desades
│   └── theme/                   # Material 3 (Color, Type, Theme)
└── res/                         # strings, themes, colors i icones vectorials
```

**Flux de dades:** la vista observa `uiState` amb `collectAsStateWithLifecycle()` i envia
esdeveniments al ViewModel (`onRouteNameChanged`, `onBudgetChanged`, ...). El ViewModel
actualitza l'estat de manera immutable amb `.copy()` i exposa només un `StateFlow` de lectura.
La capa de dades aïlla la E/S de disc, de manera que ni la UI ni el ViewModel coneixen
detalls de fitxers.

### Pas de dades entre pantalles

S'usa un **ViewModel compartit** (`viewModel()` dins d'`AppNavigation`): en prémer
"Generar Postal / Crònica", el ViewModel desa la ruta i posa `selectedEntry` a l'estat;
`PostalScreen` l'observa directament. Alternatives vàlides segons l'enunciat: arguments de
navegació codificant la ruta com a JSON (`Json.encodeToString(entry)` + `navArgument`) o
passar l'objecte amb `Parcelable`/`Serializable`.

## Comparativa tècnica: intern vs extern

| Característica | Emmagatzematge intern (`context.filesDir`) | Emmagatzematge extern (Storage Access Framework) |
|---|---|---|
| Privadesa | 100 % privat: només hi accedeix la teva app | Compartit: l'usuari i altres apps poden veure el fitxer |
| Ruta | `/data/data/<paquet>/files/diari_viatges.json` | Documents / Descàrregues (triat per l'usuari) |
| Permisos | Cap, no cal declarar res al Manifest | Cap: el selector del sistema retorna una `Uri` segura |
| Desinstal·lació | **Els fitxers s'esborren** amb l'app | **Els fitxers persisteixen** al dispositiu |
| Ús en aquest projecte | Històric privat de rutes (JSON) | Exportació de la crònica en `.txt`/`.md` i botó Compartir |

En poques paraules: l'intern és memòria privada de l'app (mor amb ella); l'extern amb SAF
és un fitxer propietat de l'usuari que sobreviu a la desinstal·lació. Per això l'historial
es desa a `filesDir` i la crònica exportada es deixa a Documents.

## Conceptes nous utilitzats (comentats al codi font)

- **StateFlow / MutableStateFlow + `asStateFlow()`**: estat reactiu immutable.
- **`.copy()`**: actualitzacions immutables de les data class.
- **`OutlinedTextField` + `isError` / `supportingText`**: validació visual en temps real.
- **`FilterChip`**: selector d'opció única per a la dificultat.
- **`DatePickerDialog` (Material 3)**: selecció de data amb `rememberDatePickerState`.
- **`Switch`**: ruta en solitari.
- **Navigation Compose** (`NavHost`, `composable`, `navigate`): navegació entre pantalles.
- **kotlinx.serialization** (`@Serializable`, `Json`): històric en JSON.
- **`ActivityResultContracts.CreateDocument("text/plain")`**: Storage Access Framework.
- **`ContentResolver.openOutputStream(uri)?.use { }`**: escriptura segura a la Uri.
- **`withContext(Dispatchers.IO)`**: E/S fora del fil principal (UI thread).
- **`Intent.ACTION_SEND` + `Intent.createChooser`**: compartir la crònica (opcional).

## Captures de pantalla (TODO)

- [ ] `docs/form_screen.png` — formulari amb validació
- [ ] `docs/postal_screen.png` — postal / crònica generada
- [ ] `docs/history_screen.png` — historial de rutes

## Posada en marxa

1. Obre la carpeta `Android-DiariViatges-Storage` amb Android Studio (Hedgehog o superior).
2. Android Studio generarà el `gradle/wrapper/gradle-wrapper.jar` i descarregarà Gradle 8.9.
3. Sincronitza Gradle i executa l'app en un emulator o dispositiu (minSdk 24).
4. Per inspeccionar l'historial: *Device Explorer* → `data` → `data` →
   `com.example.diariiviatges` → `files` → `diari_viatges.json`.

## Tasks pendents (TODO) per l'alumne

- [ ] Afegir les captures de pantalla i enllaçar-les al README.
- [ ] Revisar i personalitzar els missatges d'error i els colors del tema.
- [ ] Fer commits descriptius per funcionalitat i publicar el repo `Android-DiariViatges-Storage` a GitHub.
- [ ] Enllaçar el repositori a la plataforma virtual abans de la data límit.
