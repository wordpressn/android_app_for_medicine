# MediReminder (Android + Web PWA + Server)

This repository includes working starter code for a complete medicine reminder system:

- `app/` → Android app (Kotlin + Jetpack Compose)
- `web/` → Web app with Progressive Web App (PWA) features
- `server/` → Node.js Express API + static hosting for `web/`
- `MEDICINE_APP_FULL_GUIDE.md` → detailed product/architecture guide

## What is implemented

### Parent experience
- Morning / Noon / Night period cards
- Open a selected period and see medicines one-by-one
- Previous / Next navigation
- Final completion action:
  - **"I have completed the medicine for the selected period"**
- Daily completion status
- Time-based visual vibe (morning/noon/night themes)

### Son/Daughter admin experience
- Add medicine
- Delete medicine
- Set start date + end date
- Assign one medicine to multiple periods
- Expired medicines auto-hide from period view

### PWA features
- Web app manifest (`manifest.webmanifest`)
- Service worker (`sw.js`) with basic offline caching
- Installable app behavior on supported devices/browsers

## Run the web app + API server
```bash
cd server
npm install
npm start
```

Open: `http://localhost:8080`

## Run Android app
1. Open project in Android Studio.
2. Let Gradle sync.
3. Run on emulator/device.
