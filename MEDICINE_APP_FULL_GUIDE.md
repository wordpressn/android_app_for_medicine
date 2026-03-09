# Medicine Reminder Android App (Parents + Son/Daughter Admin) — Full Build Guide

## 1) Product Goal
Build one system with:
- **Parents app experience**: simple, large-text, period-based medicine flow (`Morning`, `Noon`, `Night`) with step-by-step medicine cards, previous/next controls, and a final completion button for each period.
- **Son/Daughter admin experience**: manage medicines, assign them by period, upload medicine images, set start/end day limits, and monitor adherence remotely.
- **Server-backed data**: all medicine plans, completion logs, and user data stored on backend so you can view it from your end.

---

## 2) User Roles

### A. Parent Role (Care Receiver)
- Login with simple account or PIN.
- See **3 period cards**:
  - Morning ☀️
  - Noon 🌤️
  - Night 🌙
- Tap a period → open medicine list for that period.
- View medicines **one by one**:
  - Medicine name (large)
  - Generic name (small)
  - Picture
  - Dose + notes
- Navigation buttons:
  - `Previous`
  - `Next`
- At end of list:
  - `I have completed the medicine for the selected period`
- Once completed, mark the period done and sync to server.

### B. Son/Daughter Role (Care Admin)
- Add/edit/delete medicine.
- Upload medicine image.
- Assign medicine to one or more periods (`Morning`, `Noon`, `Night`).
- Set medicine schedule dates (`start_date`, `end_date`).
- Auto-hide medicines after `end_date` on parent side.
- View dashboard with:
  - Today completion status by period
  - Missed doses
  - Adherence percentage

---

## 3) Parent Interface Design (Simple + Elder-Friendly)

## App home screen (parents)
- Header: greeting + today date
- Three large cards/buttons:
  1. Morning
  2. Noon
  3. Night
- Each card includes:
  - Icon
  - Status badge: `Pending` / `Completed`

### Period visual theme (same vibe as time)
- **Morning**: warm yellow/orange gradient, sun icon
- **Noon**: bright sky blue gradient
- **Night**: dark navy/purple gradient, moon icon

### Accessibility rules (important)
- Font size: minimum 18sp (names 24sp+)
- High contrast colors
- Large tap targets (48dp+)
- Voice prompt option (`TextToSpeech`)
- Minimal text, clear labels
- Offline cache + retry sync

---

## 4) Son/Daughter Interface Design

### Admin dashboard tabs
1. **Medicines**
2. **Schedules**
3. **Reports**
4. **Profile/Settings**

### Medicines screen
- List all medicines with image + status (active/expired)
- Add medicine form
- Edit/delete actions

### Schedule screen
- Pick medicine
- Assign period(s)
- Set dosage instructions
- Set start/end dates
- Optional day-specific rules (Mon-Sun)

### Reports screen
- Parent daily completion timeline
- Missed period alerts
- % adherence (7-day, 30-day)

---

## 5) Medical Fields (Basic + Essential)
Use these fields in your model/forms:

### Patient profile fields
- patient_id
- full_name
- age
- sex
- blood_group
- weight
- allergies
- chronic_conditions (diabetes, bp, etc.)
- emergency_contact_name
- emergency_contact_phone
- doctor_name
- doctor_phone

### Medicine fields
- medicine_id
- brand_name
- generic_name
- medicine_type (tablet/capsule/syrup/injection)
- strength (e.g., 500mg)
- dose_quantity (e.g., 1 tablet)
- route (oral, topical, etc.)
- before_food / after_food / with_food
- purpose (bp, sugar, pain)
- side_effect_note (short)
- image_url
- instructions
- start_date
- end_date
- active (computed from date)

### Schedule fields
- schedule_id
- patient_id
- medicine_id
- period (`MORNING`, `NOON`, `NIGHT`)
- time_of_day (e.g., 08:00)
- days_of_week (optional)
- is_active

### Adherence log fields
- log_id
- patient_id
- date
- period
- completed_at
- status (`TAKEN`, `SKIPPED`, `PENDING`)
- note
- synced_at

---

## 6) Recommended Tech Stack

### Android app
- **Kotlin**
- **Jetpack Compose** (UI)
- **MVVM + Clean Architecture**
- **Hilt** (dependency injection)
- **Room** (offline local cache)
- **WorkManager** (background sync/reminders)
- **Retrofit + OkHttp + Kotlinx Serialization** (API)

### Backend
- Option A: Node.js + Express + PostgreSQL
- Option B: Django + DRF + PostgreSQL
- Option C (fast MVP): Firebase Auth + Firestore + Cloud Functions + Storage

### Notifications
- Local notifications for medicine times
- Optional server push via Firebase Cloud Messaging

---

## 7) Database Schema (SQL-style)

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY,
  role VARCHAR(20) NOT NULL, -- PARENT / ADMIN
  name VARCHAR(120) NOT NULL,
  phone VARCHAR(20) UNIQUE,
  email VARCHAR(120) UNIQUE,
  password_hash TEXT NOT NULL,
  linked_parent_id UUID,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE patients (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  age INT,
  sex VARCHAR(16),
  blood_group VARCHAR(8),
  allergies TEXT,
  chronic_conditions TEXT,
  doctor_name VARCHAR(120),
  doctor_phone VARCHAR(20),
  emergency_contact_name VARCHAR(120),
  emergency_contact_phone VARCHAR(20)
);

CREATE TABLE medicines (
  id UUID PRIMARY KEY,
  patient_id UUID REFERENCES patients(id),
  brand_name VARCHAR(120) NOT NULL,
  generic_name VARCHAR(120),
  medicine_type VARCHAR(30),
  strength VARCHAR(50),
  dose_quantity VARCHAR(50),
  route VARCHAR(20),
  food_timing VARCHAR(20), -- BEFORE_FOOD / AFTER_FOOD / WITH_FOOD
  purpose VARCHAR(255),
  side_effect_note VARCHAR(255),
  image_url TEXT,
  instructions TEXT,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE schedules (
  id UUID PRIMARY KEY,
  patient_id UUID REFERENCES patients(id),
  medicine_id UUID REFERENCES medicines(id),
  period VARCHAR(10) NOT NULL, -- MORNING / NOON / NIGHT
  time_of_day TIME,
  days_of_week VARCHAR(32), -- e.g. MON,TUE,WED
  is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE adherence_logs (
  id UUID PRIMARY KEY,
  patient_id UUID REFERENCES patients(id),
  date DATE NOT NULL,
  period VARCHAR(10) NOT NULL,
  status VARCHAR(10) NOT NULL, -- TAKEN/SKIPPED/PENDING
  completed_at TIMESTAMP,
  note TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(patient_id, date, period)
);
```

---

## 8) API Design (Example REST Endpoints)

### Auth
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`

### Medicines
- `GET /patients/{id}/medicines?active=true`
- `POST /patients/{id}/medicines`
- `PUT /medicines/{medicineId}`
- `DELETE /medicines/{medicineId}`

### Schedules
- `GET /patients/{id}/periods/{period}/medicines?date=YYYY-MM-DD`
- `POST /patients/{id}/schedules`
- `PUT /schedules/{id}`
- `DELETE /schedules/{id}`

### Adherence
- `POST /patients/{id}/adherence/complete-period`
- `GET /patients/{id}/adherence/daily?date=YYYY-MM-DD`
- `GET /patients/{id}/adherence/report?from=YYYY-MM-DD&to=YYYY-MM-DD`

### Media
- `POST /upload/medicine-image`

---

## 9) Parent App Flow (exact requested behavior)

1. Parent opens app.
2. Home shows 3 period cards: Morning / Noon / Night.
3. Parent taps one period.
4. App fetches period medicines where:
   - `start_date <= today <= end_date`
   - linked to selected period
5. Show one medicine card at a time:
   - Name (big)
   - Generic name (small)
   - image
   - dose + food timing
6. `Previous` / `Next` to navigate.
7. On last item, show button:
   - **"I have completed the medicine for the selected period"**
8. On button click:
   - Create adherence log status `TAKEN`
   - Update period badge to completed
   - Sync with server

---

## 10) Auto-hide Medicine After End Date

At query time, always filter by active date window:

```sql
WHERE CURRENT_DATE BETWEEN start_date AND end_date
```

On Android side, also locally filter before rendering in case of stale cache.

---

## 11) Android Project Structure

```text
app/
  data/
    local/ (Room db, dao, entities)
    remote/ (Retrofit api, dto)
    repository/
  domain/
    model/
    usecase/
  ui/
    parent/
      home/
      period_detail/
    admin/
      medicines/
      schedules/
      reports/
    common/
  worker/
    SyncWorker.kt
  notifications/
```

---

## 12) Suggested Implementation Plan (Step-by-step)

1. **Setup project** with Kotlin + Compose + Hilt + Room + Retrofit.
2. **Create backend** auth + patient + medicine + schedule + adherence endpoints.
3. **Build parent home screen** with 3 period cards and themed backgrounds.
4. **Build period detail screen** with one-by-one medicine card and prev/next controls.
5. Add completion CTA and save adherence log.
6. Build **admin medicine CRUD** screen with image upload.
7. Build schedule assignment UI per period.
8. Add date range validation (start <= end).
9. Add local notifications per medicine time.
10. Add dashboard/report APIs and admin report UI.
11. Add offline-first sync using Room + WorkManager.
12. Add accessibility improvements (large fonts, TTS, language options).
13. QA testing with real medicine scenarios.
14. Release internal beta first.

---

## 13) Important Safety + Compliance Notes
- This app is a **reminder/support tool**, not a diagnostic tool.
- Add disclaimer in app.
- Never suggest dosage changes automatically.
- Protect personal data:
  - HTTPS only
  - encrypted tokens
  - secure storage for auth token
  - role-based access control

---

## 14) MVP Features Checklist

### Parent side
- [ ] Login
- [ ] 3 period cards
- [ ] Period-specific medicine carousel
- [ ] Previous/Next navigation
- [ ] Complete period button
- [ ] Completion sync

### Son/Daughter side
- [ ] Add medicine
- [ ] Edit medicine
- [ ] Delete medicine
- [ ] Upload medicine image
- [ ] Assign periods
- [ ] Set start/end dates
- [ ] View completion report

### Core
- [ ] Auto-hide expired medicines
- [ ] Local notifications
- [ ] Offline cache
- [ ] Secure auth

---

## 15) What to Build First (practical suggestion)
If you want fastest real-world result:
1. Build backend quickly with Firebase or Supabase.
2. Build parent flow first (because it is critical).
3. Add admin CRUD second.
4. Add reports and polishing last.

This gives you a working medicine support app early, then you can enhance it safely.
