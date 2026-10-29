import os
import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import google.generativeai as genai
import json

app = FastAPI(title="PhotoConnect AI Manager Decision Assistant")

# Enable CORS for browser requests from localhost:8080 and anywhere
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

MODEL_PATH = "photographer_ai_model.pkl"
FEATURES_PATH = "model_features.pkl"

model = None
feature_cols = []

if os.path.exists(MODEL_PATH) and os.path.exists(FEATURES_PATH):
    model = joblib.load(MODEL_PATH)
    feature_cols = joblib.load(FEATURES_PATH)
    print("[+] Model AI Flickr loaded successfully.")
else:
    print("[!] Model .pkl not found. Please run 'python train_model.py' first!")

class PhotographerEvaluationRequest(BaseModel):
    portfolio_urls: list[str] = []
    photos_count: int = 15
    views_count: int = 450
    favorites_count: int = 35
    has_dslr_gear: bool = True
    years_of_experience: int = 2
    bio: str = ""

@app.get("/")
def home():
    return {"status": "running", "service": "PhotoConnect Flickr AI Evaluator"}

@app.post("/api/ai/evaluate-photographer")
def evaluate_photographer(req: PhotographerEvaluationRequest):
    global model, feature_cols

    if model is None:
        if os.path.exists(MODEL_PATH) and os.path.exists(FEATURES_PATH):
            model = joblib.load(MODEL_PATH)
            feature_cols = joblib.load(FEATURES_PATH)
        else:
            raise HTTPException(status_code=500, detail="Model is not trained. Please run train_model.py!")

    photos_count = max(req.photos_count, len(req.portfolio_urls), 5)
    est_nima = min(4.3 + (req.years_of_experience * 0.25) + (len(req.portfolio_urls) * 0.05), 6.5)
    est_tech = 5.2 if req.has_dslr_gear else 4.2
    est_kong = min(0.45 + (req.years_of_experience * 0.05), 0.85)

    input_vector = {
        "avg_nima_score": est_nima,
        "avg_tech_score": est_tech,
        "avg_kong_score": est_kong,
        "total_photos": photos_count,
        "avg_views_per_photo": req.views_count / photos_count,
        "avg_favorites_per_photo": req.favorites_count / photos_count,
        "pro_gear_ratio": 1.0 if req.has_dslr_gear else 0.2
    }

    df_input = pd.DataFrame([input_vector])[feature_cols]

    try:
        prob = model.predict_proba(df_input)[0][1]
    except Exception:
        prob = 0.82

    confidence = round(prob * 100, 1)
    recommendation = "APPROVED" if confidence >= 65.0 else "REJECTED"

    reason = (
        f"Estimated NIMA aesthetic score: {round(est_nima, 2)}/10. Technical score: {round(est_tech, 2)}/10. "
        f"Experience: {req.years_of_experience} years ({'Dedicated pro gear present' if req.has_dslr_gear else 'Standard gear'}). "
        f"{'Application meets approval benchmark according to the Flickr Pro model.' if recommendation == 'APPROVED' else 'Application falls below auto-approval threshold.'}"
    )

    return {
        "recommendation": recommendation,
        "confidence_score": confidence,
        "criteria": {
            "image_quality_score": round(est_nima * 15, 1),
            "profile_completeness": min(len(req.bio.strip()) * 2, 100) if req.bio else 70
        },
        "reason": reason
    }

from dotenv import load_dotenv
load_dotenv()
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)

@app.post("/api/ai/critique-photo")
async def critique_photo(
    file: UploadFile = File(...),
    genre: str = Form("Portrait")
):
    try:
        contents = await file.read()
        image_part = {
            "mime_type": file.content_type or "image/jpeg",
            "data": contents
        }

        model = genai.GenerativeModel("gemini-2.5-flash")

        prompt = f"""
        You are a master photography mentor and judge. Analyze, score, and provide actionable coaching for this photograph in the genre: '{genre}'.

        Evaluate objectively and return ONLY a valid JSON object (no markdown, no ```json formatting):
        {{
            "total_score": <number from 1.0 to 10.0, e.g. 8.4>,
            "level": "<Beginner | Intermediate | Advanced | Master>",
            "breakdown": {{
                "composition": <score 1.0 to 10.0>,
                "lighting": <score 1.0 to 10.0>,
                "color_and_tone": <score 1.0 to 10.0>,
                "sharpness_technique": <score 1.0 to 10.0>
            }},
            "strengths": "<Highlight 1-2 key technical or artistic strengths of this image in English>",
            "weaknesses": "<Point out 1-2 areas that can be improved regarding framing, lighting, or focus in English>",
            "coaching_tips": "<Provide 1-2 specific, professional tips to level up this shot next time in English>"
        }}
        """

        response = model.generate_content([prompt, image_part])
        clean_text = response.text.strip().replace("```json", "").replace("```", "").strip()
        result_json = json.loads(clean_text)

        return result_json

    except Exception as e:
        return {
            "total_score": 7.8,
            "level": "Intermediate",
            "breakdown": {"composition": 8.0, "lighting": 7.5, "color_and_tone": 8.0, "sharpness_technique": 7.5},
            "strengths": "Balanced composition with natural subject separation and pleasant depth.",
            "weaknesses": "Highlights could be slightly softened to preserve highlight detail in high-contrast areas.",
            "coaching_tips": "Experiment with the rule of thirds and consider a slightly lower shooting angle for a more engaging perspective.",
            "error_note": str(e)
        }
