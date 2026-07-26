"""
Task 5 (integration): tiny Flask service that wraps the model trained in
train_model.py and serves it over HTTP so the Spring Boot backend can call it
each time a new grievance is created.

Run:  python app.py
      (train_model.py must have been run first, so model.joblib exists)

POST /predict
  body: {"ward": 7, "category": "Drainage", "department": "Public Works",
         "day_of_week": 3, "description_length": 42}
  response: {"label": "At risk of delay", "confidence": 0.71}

If model.joblib is missing, the service still starts but returns a clear
error so the caller can fall back to "no prediction" instead of crashing.
"""

from flask import Flask, request, jsonify
import pandas as pd
import joblib
import os

app = Flask(__name__)

MODEL_PATH = "model.joblib"
bundle = None
if os.path.exists(MODEL_PATH):
    bundle = joblib.load(MODEL_PATH)


@app.route("/predict", methods=["POST"])
def predict():
    if bundle is None:
        return jsonify({"error": "model not trained yet - run train_model.py first"}), 503

    data = request.get_json(force=True)
    model = bundle["model"]
    feature_order = bundle["feature_order"]

    row = {
        "ward": data.get("ward"),
        "category": data.get("category", "Unknown"),
        "department": data.get("department") or "Unknown",
        "day_of_week": data.get("day_of_week", 1),
        "description_length": data.get("description_length", 0),
    }
    X = pd.DataFrame([row])[feature_order]

    proba = model.predict_proba(X)[0]
    classes = model.named_steps["classifier"].classes_
    # class 1 = delayed, class 0 = on time
    delayed_idx = list(classes).index(1) if 1 in classes else None

    if delayed_idx is not None:
        delayed_prob = proba[delayed_idx]
    else:
        delayed_prob = 0.0

    if delayed_prob >= 0.5:
        label = "At risk of delay"
        confidence = float(delayed_prob)
    else:
        label = "Likely on time"
        confidence = float(1 - delayed_prob)

    return jsonify({"label": label, "confidence": round(confidence, 2)})


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "model_loaded": bundle is not None})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=False)
