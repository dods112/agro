from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# CORS - allows your GitHub Pages to connect
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "https://dods112.github.io",
        "http://localhost:8080",
        "http://localhost:3000"
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Your routes below...