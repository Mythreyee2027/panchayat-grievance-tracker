"""
Task 1: Generate the seeded grievance dataset.

Run:  python generate_dataset.py
Output: grievances.csv (about 100 rows) in this same folder.

FIELD MEANINGS
---------------
grievance_id   : unique id, format G0001, G0002 ...
complainant     : name of the person who raised the grievance
ward            : ward number the grievance belongs to, 1-15
category        : type of problem, one of:
                   Water Supply, Road/Culvert, Street Light, Drainage,
                   Sanitation, Electricity, Public Property
department      : the panchayat department responsible for the category
                   (Water Dept, Public Works, Electrical Dept,
                    Sanitation Dept, General Admin)
description     : free-text description of the problem (kept short)
date_raised     : the date the grievance was recorded, YYYY-MM-DD
status          : current state -> Open, In Progress, Resolved
resolved_date   : date it was closed (blank if still Open/In Progress)
                   -> used only to compute the outcome column, and only
                   filled in for rows that are Resolved.
days_to_resolve : (Resolved rows only) resolved_date - date_raised, in days
delayed         : OUTCOME COLUMN used later for the ML model in Task 4.
                   1 if a Resolved case took longer than 7 days to close,
                   0 if it was closed within 7 days.
                   Blank for grievances that are still Open/In Progress
                   (we don't know the outcome yet for those).

AWKWARD CASES INCLUDED ON PURPOSE (for Task 1 + Task 3 testing)
-----------------------------------------------------------------
1. One row has a missing 'department' value (blank) - tests how the
   screen/search handles a missing field.
2. Two rows use very similar complainant names ("Selvam R" and
   "Selvam. R") - tests that search doesn't silently merge/confuse them.
3. One row is a "junk" record with a description unrelated to any real
   civic issue ("Test entry - ignore", category "Other") - tests that
   listing/search/filter still behave sensibly with unrelated data.
"""

import csv
import random
from datetime import date, timedelta

random.seed(42)  # fixed seed so the dataset is reproducible

FIRST_NAMES = ["Muthu", "Kavitha", "Selvam", "Priya", "Rajesh", "Lakshmi",
               "Suresh", "Anitha", "Karthik", "Meena", "Ganesan", "Divya",
               "Vijay", "Saranya", "Manoj", "Revathi", "Prakash", "Deepa",
               "Arun", "Bhavani"]
LAST_INITIALS = ["R", "K", "S", "M", "P", "N", "V", "T"]

WARDS = list(range(1, 16))

CATEGORY_DEPT = {
    "Water Supply": "Water Dept",
    "Road/Culvert": "Public Works",
    "Street Light": "Electrical Dept",
    "Drainage": "Public Works",
    "Sanitation": "Sanitation Dept",
    "Electricity": "Electrical Dept",
    "Public Property": "General Admin",
}
CATEGORIES = list(CATEGORY_DEPT.keys())

DESCRIPTIONS = {
    "Water Supply": ["No water supply for 3 days", "Pipe leaking near street",
                      "Contaminated water in tap", "Low water pressure"],
    "Road/Culvert": ["Pothole causing accidents", "Culvert collapsed after rain",
                       "Road washed out near bridge", "Broken road divider"],
    "Street Light": ["Street light not working", "Pole damaged and sparking",
                       "Light flickers all night", "No light on main road"],
    "Drainage": ["Drain blocked, water stagnant", "Sewage overflow on street",
                  "Drain cover missing, unsafe", "Storm water not draining"],
    "Sanitation": ["Garbage not collected for a week", "Public toilet unclean",
                    "Dead animal not removed", "Waste dumped near school"],
    "Electricity": ["Frequent power cuts", "Transformer making noise",
                      "Exposed wiring near houses", "Meter reading incorrect"],
    "Public Property": ["Community hall roof leaking", "Park equipment broken",
                          "Bus shelter damaged", "Compound wall collapsed"],
}

STATUSES = ["Open", "In Progress", "Resolved"]
# weight towards more open/in-progress so the "oldest first" ordering matters
STATUS_WEIGHTS = [0.40, 0.25, 0.35]

start = date(2025, 10, 1)
today = date(2026, 7, 25)


def random_date(d1, d2):
    delta = (d2 - d1).days
    return d1 + timedelta(days=random.randint(0, delta))


rows = []
for i in range(1, 96):  # 95 generated rows, 5 hand-added awkward/edge rows below
    gid = f"G{i:04d}"
    name = f"{random.choice(FIRST_NAMES)} {random.choice(LAST_INITIALS)}"
    ward = random.choice(WARDS)
    category = random.choice(CATEGORIES)
    department = CATEGORY_DEPT[category]
    description = random.choice(DESCRIPTIONS[category])
    date_raised = random_date(start, today - timedelta(days=1))
    status = random.choices(STATUSES, weights=STATUS_WEIGHTS)[0]

    resolved_date = ""
    days_to_resolve = ""
    delayed = ""
    if status == "Resolved":
        # resolved between 1 and 20 days after raised, but not after today
        max_gap = min(20, (today - date_raised).days)
        max_gap = max(max_gap, 1)
        gap = random.randint(1, max_gap)
        rdate = date_raised + timedelta(days=gap)
        resolved_date = rdate.isoformat()
        days_to_resolve = gap
        delayed = 1 if gap > 7 else 0

    rows.append([gid, name, ward, category, department, description,
                 date_raised.isoformat(), status, resolved_date,
                 days_to_resolve, delayed])

# ---- Awkward / edge cases (appended on purpose, ids continue from 96) ----

# 1. Missing department value
rows.append(["G0096", "Ramesh K", 4, "Road/Culvert", "",
             "Road damaged after heavy rain", "2026-06-02", "Open", "", "", ""])

# 2 & 3. Two very similar complainant names
d = random_date(start, today - timedelta(days=10))
rows.append(["G0097", "Selvam R", 7, "Water Supply", "Water Dept",
             "No water supply for 3 days", d.isoformat(), "In Progress", "", "", ""])
d2 = d + timedelta(days=2)
rows.append(["G0098", "Selvam. R", 7, "Water Supply", "Water Dept",
             "Low water pressure in the mornings", d2.isoformat(), "Open", "", "", ""])

# 4. Unrelated / junk record
rows.append(["G0099", "Test User", 1, "Other", "General Admin",
             "Test entry - ignore, not a real complaint", "2026-01-15",
             "Resolved", "2026-01-16", 1, 0])

# 5. An old, long-open case (useful to sanity-check "oldest first" ordering)
rows.append(["G0100", "Ganesan V", 12, "Drainage", "Public Works",
             "Drain has been blocked since last monsoon", "2025-10-05",
             "Open", "", "", ""])

header = ["grievance_id", "complainant", "ward", "category", "department",
          "description", "date_raised", "status", "resolved_date",
          "days_to_resolve", "delayed"]

with open("grievances.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(header)
    writer.writerows(rows)

print(f"Wrote {len(rows)} rows to grievances.csv")
