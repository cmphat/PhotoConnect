import os
import json
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
import joblib

def extract_photographer_features(json_path):
    try:
        with open(json_path, 'r', encoding='utf-8') as f:
            photos = json.load(f)

        if not isinstance(photos, list) or len(photos) == 0:
            return None

        total_photos = len(photos)
        nima_scores = []
        nima_tech_scores = []
        kong_scores = []
        total_views = 0
        total_favorites = 0
        has_pro_gear_count = 0

        for p in photos:
            if "nima_score" in p and p["nima_score"]:
                nima_scores.append(float(p["nima_score"]))
            if "nima_tech_score" in p and p["nima_tech_score"]:
                nima_tech_scores.append(float(p["nima_tech_score"]))
            if "kong_score" in p and p["kong_score"]:
                kong_scores.append(float(p["kong_score"]))
            
            total_views += int(p.get("views", 0) or 0)
            total_favorites += int(p.get("favorites_n", 0) or 0)

            exif = p.get("exif", {})
            if isinstance(exif, dict):
                model = str(exif.get("Model", "")).lower()
                make = str(exif.get("Make", "")).lower()
                if any(b in make for b in ["nikon", "canon", "sony", "fujifilm", "leica"]) or "dslr" in model:
                    has_pro_gear_count += 1

        avg_nima = sum(nima_scores) / len(nima_scores) if nima_scores else 4.5
        avg_nima_tech = sum(nima_tech_scores) / len(nima_tech_scores) if nima_tech_scores else 4.5
        avg_kong = sum(kong_scores) / len(kong_scores) if kong_scores else 0.5
        pro_gear_ratio = has_pro_gear_count / total_photos

        is_pro = 1 if (avg_nima >= 4.75 and avg_nima_tech >= 4.7 and pro_gear_ratio >= 0.5 and (total_views / total_photos) >= 30) else 0

        return {
            "avg_nima_score": avg_nima,
            "avg_tech_score": avg_nima_tech,
            "avg_kong_score": avg_kong,
            "total_photos": total_photos,
            "avg_views_per_photo": total_views / total_photos,
            "avg_favorites_per_photo": total_favorites / total_photos,
            "pro_gear_ratio": pro_gear_ratio,
            "is_pro": is_pro
        }
    except Exception:
        return None

def main():
    # 1. Xác định thư mục chứa file train_model.py
    base_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 2. Tìm kiếm thư mục photo_features bên trong base_dir
    target_folder = None
    for root, dirs, _ in os.walk(base_dir):
        if "photo_features" in dirs:
            target_folder = os.path.join(root, "photo_features")
            break

    if not target_folder or not os.path.exists(target_folder):
        print(f"[!] Khong tim thay thu muc 'photo_features' ben trong: {base_dir}")
        return

    print(f"[*] Da tim thay thu muc dataset: {target_folder}")

    # Lấy mẫu tối đa 1.000 file JSON
    MAX_SAMPLES = 1000
    dataset = []

    files = [f for f in os.listdir(target_folder) if f.endswith(".json")]
    print(f"[*] Tong so file tim thay: {len(files)}. Bat dau xu ly {min(MAX_SAMPLES, len(files))} ho so...")

    for i, filename in enumerate(files[:MAX_SAMPLES]):
        file_path = os.path.join(target_folder, filename)
        features = extract_photographer_features(file_path)
        if features:
            dataset.append(features)
        if (i + 1) % 200 == 0:
            print(f"[*] Da xu ly: {i + 1}/{min(MAX_SAMPLES, len(files))} ho so...")

    df = pd.DataFrame(dataset)
    print(f"\n[+] Da tao DataFrame thanh cong voi {len(df)} ho so.")
    print(f"[*] Phan phoi nhan: Chuyen nghiep (1): {sum(df['is_pro'] == 1)} | Nghiep du (0): {sum(df['is_pro'] == 0)}")

    feature_cols = [
        "avg_nima_score", 
        "avg_tech_score", 
        "avg_kong_score", 
        "total_photos", 
        "avg_views_per_photo", 
        "avg_favorites_per_photo", 
        "pro_gear_ratio"
    ]

    X = df[feature_cols].fillna(0)
    y = df["is_pro"]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    clf = RandomForestClassifier(n_estimators=100, max_depth=10, random_state=42)
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)
    print("\n================== KET QUA DANH GIA MO HINH (EVALUATION REPORT) ==================")
    print(classification_report(y_test, y_pred))
    print("==================================================================================\n")

    model_file = os.path.join(base_dir, "photographer_ai_model.pkl")
    features_file = os.path.join(base_dir, "model_features.pkl")
    joblib.dump(clf, model_file)
    joblib.dump(feature_cols, features_file)
    print(f"[+] DA LUU: '{model_file}' va '{features_file}' thanh cong!")

if __name__ == "__main__":
    main()