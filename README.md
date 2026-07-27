# Panchayat Grievance Register and Resolution Tracker

SIH 2026 — Internal Practical Assessment (Easy level)

A register where every grievance is recorded against a department and a
status, stays visible until it is closed, and is shown to the panchayat
clerk ordered by how long it has been waiting.

## What's in this repository

```
panchayat-grievance-tracker/
├── data/               Task 1 - the seeded dataset + the script that made it
├── backend/             Task 2/3 - Spring Boot (Java) REST API + MySQL
├── ml/                  Task 4/5 - Python model + Flask prediction service
├── frontend/             Task 2/3 - React screens
├── presentation/         Task 6 - source files for presentation.pdf
├── presentation.pdf      Task 6 - the required submission deck
└── README.md
```

## What each field means

See the header comment in `data/generate_dataset.py` for the full field
dictionary (grievance_id, complainant, ward, category, department,
description, date_raised, status, resolved_date, days_to_resolve, delayed).

**How the "days waiting" figure is calculated** (shown in the register):
- Still Open / In Progress: `today's date − date_raised`
- Resolved: the stored `days_to_resolve` (`resolved_date − date_raised`)

The register is always sorted so Open cases come first, then In Progress,
then Resolved — and within each group, the oldest `date_raised` first. That
is what puts the longest-waiting complaint at the top.

## Before you start

Install these once:
1. **Java 17+** and **Maven** — for the backend
2. **Node.js 18+** — for the frontend
3. **Python 3.10+** — for the ML service
4. **MySQL 8** (or skip this — see "Quick start without installing MySQL" below)

## Step 1: Generate the dataset (Task 1)

```
cd data
python generate_dataset.py
```

This writes `grievances.csv` (100 rows) into the `data/` folder. The
backend loads this automatically the first time it starts.

## Step 2: Backend setup (Tasks 2/3)

**Option A — with MySQL installed:**
```
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS grievance_db;"
```
Then, if your MySQL password isn't `root`, edit
`backend/src/main/resources/application.properties` and update
`spring.datasource.password`.

```
cd backend
mvn spring-boot:run
```

**Option B — Quick start without installing MySQL (uses a file-based H2 database instead):**
```
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Everything else works identically — only the database engine changes.

The backend starts on **http://localhost:8080** and seeds itself from
`data/grievances.csv` on first run.

## Step 3: ML prediction service (Tasks 4/5)

In a new terminal:
```
cd ml
pip install -r requirements.txt
python train_model.py
python app.py
```
This trains the delay-risk classifier from the resolved rows in the dataset
and starts the prediction API on **http://localhost:5001**.
The backend calls this automatically whenever a new grievance is recorded —
if this service isn't running, the app still works, it just won't show a
delay-risk prediction for new grievances.

## Step 4: Frontend (Tasks 2/3)

In a new terminal:
```
cd frontend
npm install
npm run dev
```
Open **http://localhost:5173** in your browser.

## Running everything at once (after Step 1)

Three terminals, in this order: backend, then `ml/app.py`, then frontend.

## What to check when testing (Task 5)

- Record a new grievance from the form → it appears at the top of the list
  as an Open case with 0 days waiting.
- Search for "Selvam" → both `Selvam R` and `Selvam. R` (the awkward
  near-duplicate names from Task 1) should appear separately.
- Filter by a status → the "Showing N grievances" count updates.
- Stop the `ml/app.py` service and record a new grievance → the app still
  saves it, just without a delay-risk prediction ("Uncertain").
- G0096 in the seeded data has a blank department — confirm the list shows
  "Not assigned" instead of breaking.

## Uploading to GitHub

```
cd panchayat-grievance-tracker
git init
git add .
git commit -m "Panchayat Grievance Register and Resolution Tracker"
git branch -M main
git remote add origin <your-empty-GitHub-repo-URL>
git push -u origin main
```

## Demo Video

[Watch the demo video here](https://drive.google.com/file/d/1LMgqt7wM277aCtAO90E6XpPiuC_SPbcA/view?usp=sharing)

## Level 2 Demo Video

[Watch the Level 2 (on-spot changes) demo video](https://drive.google.com/file/d/1jAvNgj5deYI_2OOC9vQcwbn4QcbRtjam/view?usp=sharing)

