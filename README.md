# GymTracker - Android Fitness App

Een Android fitness tracking applicatie gebouwd met Jetpack Compose en Room.

## Functionaliteit

De app biedt workout management met volledige CRUD-operaties op custom workouts. Gebruikers kunnen exercises browsen via de ExerciseDB API en deze toevoegen aan hun workouts. Tijdens het uitvoeren van een workout wordt de tijd bijgehouden en kan de gebruiker sets, reps en gewicht aanpassen per exercise.

## Core Features

- **Volledige CRUD**: Create, Read, Update, Delete voor workouts
- **API Integratie**: Browse exercises via ExerciseDB API (Volley)
- **Real-time Search**: Filter workouts en exercises
- **Workout Execution**: Live timer en voortgang tracking tijdens workouts
- **Data Persistentie**: Room database voor permanente opslag
- **Notifications**: Reminders voor workouts

## Extra Features

1. **Authenticatie Systeem**
   - Custom login flow en validatie
   - Persistent login state met `SharedPreferences`
2. **Splash Screen & Onboarding**
   - Splash screen experience
   - Auto-navigation naar juiste scherm (login vs main)
3. **Responsive Design**
   - Layouts voor variërende schermgroottes via Jetpack Compose
4. **Advanced Search & Filtering**
   - Zoekfunctionaliteit verwerkt in ViewModels
5. **Architectuur en State Management**
   - Real-time updates via ViewModel `StateFlow`'s
   - Clean layer separation via Repositories

## Technische Implementatie

- **Jetpack Compose** voor een volledig declaratieve user interface
- **Room** voor lokale data persistentie (SQLite abstractie)
- **Kotlin Coroutines** voor asynchrone network/database task executie
- **REST API** integratie met ExerciseDB via **Volley**
- **Navigation Compose** voor in-app navigatie

## Opdracht Vereisten

De app voldoet aan gestelde basis eisen voor het project:

- ✅ Meerdere schermen met navigatie (`NavGraph` en diverse composables)
- ✅ Webservice integratie (ExerciseDB API met Volley)
- ✅ Compose lijsten (`LazyColumn`) met data van de API
- ✅ Volledige CRUD-functionaliteit met Room database
- ✅ Multithreading via Kotlin Coroutines
- ✅ Lokale opslag met Room en `SharedPreferences` (`PreferencesManager`)
- ✅ Error handling en state management in ViewModels

## Architectuur

De app volgt het aanbevolen **MVVM (Model-View-ViewModel)** patroon.

### Android Frameworks & Libraries
- **Jetpack Compose** - Voor de declaratieve user interface (Material 3)
- **Room** - Voor database ORM
- **Volley** - Voor HTTP / network requests
- **Coroutines** - Voor achtergrondtaken en asynchroon programmeren

### Schermen
De UI is onderverdeeld in de volgende Compose schermen:
- `SplashScreen` en `LoginScreen`
- `WorkoutsScreen` en `AddWorkoutScreen` voor management
- `WorkoutDetailScreen` voor weergave van een specifieke workout
- `ExercisesScreen` en `ExerciseDetailScreen` om via de API oefeningen in te zien
- `SettingsScreen` voor configuraties

### Data Models & Lagen
- **Database**: `GymDatabase` met `WorkoutDao`
- **Models**: `Exercise.kt` en `Workout.kt`
- **Repository**: `WorkoutRepository` om data logica te abstraheren van de UI
- **ViewModels**: `WorkoutViewModel` en `ExerciseViewModel` voor state logic

## Installatie

### Vereisten
- Minimum SDK: API 26 (Android 8.0)
- Target SDK: API 35 (Android 15)
- Android Studio IDE (recente versie aanbevolen)
- Java 11

### API Setup
De app maakt gebruik van de ExerciseDB API via RapidAPI. 
Indien een specifieke key nodig is:
1. Registreer op RapidAPI ExerciseDB
2. Vervang de API key in `network/ExerciseApiService.kt`

### Build
1. Open het project in Android Studio
2. Wacht tot Gradle sync ("Build") is voltooid
3. Selecteer een emulator (API 26+) of een fysiek device
4. Klik op Run (Shift+F10)

## Code Structuur
```text
app/src/main/java/com/avans/gymtracker/
├── GymTrackerApplication.kt      # App entry point voor global state setup
├── MainActivity.kt               # Entry Activity (Compose host)
├── data/
│   ├── database/                 # Room DAOs en Database
│   ├── model/                    # Data Classes (Entities)
│   └── repository/               # Data Repositories
├── network/                      # Volley ExerciseApiService
├── ui/
│   ├── navigation/               # Jetpack Navigation component (NavGraph)
│   ├── screens/                  # Compose UI Screens
│   └── theme/                    # Material 3 kleuren en typography
├── utils/                        # PreferencesManager
└── viewmodel/                    # MVVM ViewModels
```

## Studenten Informatie
- **Naam**: Koray Yilmaz - Daan Hoeksema
- **Vak**: Mobile Development / Android

---
**API**: ExerciseDB via RapidAPI
