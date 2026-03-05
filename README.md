# MediReminder (Android + Server Starter)

This repository now includes runnable starter code for your medicine reminder system:

- `app/` → Android app (Kotlin + Jetpack Compose)
- `server/` → Node.js Express API server
- `MEDICINE_APP_FULL_GUIDE.md` → full product + architecture guide

## Implemented in code

### Parent side
- Three period cards: Morning / Noon / Night.
- Tap a period to open medicines one-by-one.
- Previous/Next controls.
- Final action button: **"I have completed the medicine for the selected period"**.
- Period completion status reflected back on home screen.
- Period-based themes (morning/noon/night colors).

### Son/Daughter side
- Open admin mode.
- Add medicine.
- Delete medicine.
- Date-limited medicines with auto-hide logic in period queries.

### Server side
- Medicine CRUD basics.
- Period schedule assignment endpoint.
- Period-specific medicine query endpoint.
- Adherence completion logging endpoint.
- Daily adherence fetch endpoint.

## Run backend
```bash
cd server
npm install
npm start
```

## Run Android app
1. Open project in Android Studio.
2. Let Gradle sync.
3. Run app on emulator/device.

> Note: current Android starter stores data in-memory (for clarity). Next step is wiring Retrofit + Room and real auth.
