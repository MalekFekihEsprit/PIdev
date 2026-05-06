from fastapi import FastAPI, File, UploadFile, HTTPException, Request
from fastapi.responses import JSONResponse
import numpy as np
import cv2
from deepface import DeepFace
import logging
import io

# Configuration des logs
logging.basicConfig(level=logging.INFO)
app = FastAPI(title="Face Recognition Service")

MODEL_NAME = "Facenet"
logging.info(f"Chargement du modèle {MODEL_NAME}...")
DeepFace.build_model(MODEL_NAME)
logging.info("Modèle chargé avec succès")

@app.post("/extract")
async def extract_embedding(file: UploadFile = File(...)):
    """
    Reçoit une image, retourne l'embedding facial (vecteur 128 dimensions)
    """
    try:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise HTTPException(400, "Image invalide")

        # Convertir BGR (OpenCV) en RGB (DeepFace)
        img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

        # Redimensionner si trop grand (optionnel)
        h, w = img_rgb.shape[:2]
        if h > 1024 or w > 1024:
            scale = 1024 / max(h, w)
            new_w = int(w * scale)
            new_h = int(h * scale)
            img_rgb = cv2.resize(img_rgb, (new_w, new_h))

        # Extraire l'embedding
        embedding = DeepFace.represent(
            img_path=img_rgb,
            model_name=MODEL_NAME,
            detector_backend="mtcnn",   # ou "opencv" selon la précision souhaitée
            enforce_detection=True
        )[0]["embedding"]

        return JSONResponse({
            "success": True,
            "embedding": embedding
        })

    except Exception as e:
        logging.exception("Erreur dans /extract")
        raise HTTPException(500, f"Erreur extraction: {str(e)}")

@app.post("/compare")
async def compare_embeddings(request: Request):
    """
    Compare deux embeddings reçus dans le corps JSON
    """
    try:
        body = await request.json()
        logging.info(f"Corps reçu dans /compare : {body}")

        embedding1 = body.get("embedding1")
        embedding2 = body.get("embedding2")

        if embedding1 is None or embedding2 is None:
            raise HTTPException(400, "Paramètres 'embedding1' et 'embedding2' requis")

        v1 = np.array(embedding1)
        v2 = np.array(embedding2)

        # Normalisation
        v1 = v1 / np.linalg.norm(v1)
        v2 = v2 / np.linalg.norm(v2)

        similarity = float(np.dot(v1, v2))
        logging.info(f"Similarité calculée : {similarity}")

        return JSONResponse({
            "similarity": similarity,
            "is_match": similarity > 0.8
        })

    except Exception as e:
        logging.exception("Erreur dans /compare")
        raise HTTPException(500, f"Erreur comparaison: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)