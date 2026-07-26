"""
Task 4: Train the delay-risk classifier.

WHAT IT PREDICTS
------------------
Target: 'delayed' -> will this grievance take MORE than 7 days to resolve (1)
or be resolved WITHIN 7 days (0)? This is genuinely uncertain at the moment a
grievance is raised - unlike a rule such as "category = Drainage -> delayed",
which you could just write as an if-statement.

INPUTS USED (only things known the moment the grievance is raised)
----------------------------------------------------------------------
- ward
- category
- department
- day_of_week the grievance was raised (Monday=1 .. Sunday=7)
- description_length (number of characters)

INPUTS DELIBERATELY *NOT* USED
----------------------------------
status, resolved_date, days_to_resolve - these only exist AFTER the case is
resolved, i.e. after the answer is already known. Using them would make the
model look excellent on paper and useless in real use, because none of that
data exists yet when a new grievance comes in and a prediction is needed.

TRAINING DATA
----------------
Only rows with status = Resolved have a known outcome ('delayed' is not
blank for those), so training uses that subset of the Task 1 dataset.

Run:  python train_model.py
Output: model.joblib in this folder (used by app.py)
"""

import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import accuracy_score, classification_report
import joblib

RANDOM_SEED = 42  # fixed seed, per Task 4 instructions

df = pd.read_csv("../data/grievances.csv")

# Only resolved cases have a known outcome
df = df[df["status"] == "Resolved"].copy()

# Recreate day_of_week from date_raised (1=Monday .. 7=Sunday) - available at creation time
df["date_raised"] = pd.to_datetime(df["date_raised"])
df["day_of_week"] = df["date_raised"].dt.dayofweek + 1

# description_length - available at creation time
df["description"] = df["description"].fillna("")
df["description_length"] = df["description"].str.len()

# department can be missing (Task 1's awkward case) - fill with a placeholder category
df["department"] = df["department"].fillna("Unknown")

FEATURES = ["ward", "category", "department", "day_of_week", "description_length"]
TARGET = "delayed"

df = df.dropna(subset=[TARGET])  # keep only rows with a known outcome
df[TARGET] = df[TARGET].astype(int)

X = df[FEATURES]
y = df[TARGET]

print(f"Training on {len(df)} resolved grievances "
      f"({y.sum()} delayed, {len(y) - y.sum()} on time)")

X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.25, random_state=RANDOM_SEED, stratify=y if y.nunique() > 1 else None
)

categorical = ["category", "department"]
numeric = ["ward", "day_of_week", "description_length"]

preprocessor = ColumnTransformer(transformers=[
    ("cat", OneHotEncoder(handle_unknown="ignore"), categorical),
], remainder="passthrough")  # numeric columns pass through unchanged

model = Pipeline(steps=[
    ("preprocess", preprocessor),
    ("classifier", RandomForestClassifier(
        n_estimators=100, max_depth=4, random_state=RANDOM_SEED
    )),
])

model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print(f"\nTest accuracy: {accuracy_score(y_test, y_pred):.2f}")
print(classification_report(y_test, y_pred, zero_division=0))

joblib.dump({"model": model, "feature_order": FEATURES}, "model.joblib")
print("\nSaved model.joblib")
print("NOTE: with only ~100 seeded rows this is a small, simple demo model - "
      "exactly what an Easy-level assessment expects, not a production model.")
