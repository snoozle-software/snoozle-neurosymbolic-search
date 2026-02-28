from fastapi import FastAPI
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from typing import List

app = FastAPI(title="Embedding Service")
model = SentenceTransformer("sentence-transformers/all-MiniLM-L6-v2")

class EmbedRequest(BaseModel):
    inputs: List[str]

class EmbedResponse(BaseModel):
    embeddings: List[List[float]]

@app.post("/embed", response_model=EmbedResponse)
def embed(request: EmbedRequest) -> EmbedResponse:
    if not request.inputs:
        return EmbedResponse(embeddings=[])
    embeddings = model.encode(request.inputs, convert_to_numpy=True)
    return EmbedResponse(embeddings=embeddings.tolist())
