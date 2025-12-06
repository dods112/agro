from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# CORS
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

# ADD YOUR ROUTES HERE:

@app.get("/")
def read_root():
    return {"message": "Welcome to AgroWeather API", "status": "online"}

@app.post("/login")
def login(username: str, password: str):
    # Your login logic here
    return {"message": "Login endpoint"}

@app.post("/register")
def register():
    # Your registration logic here
    return {"message": "Register endpoint"}

@app.get("/dashboard")
def dashboard():
    # Your dashboard logic here
    return {"message": "Dashboard endpoint"}

# Add more routes as needed...
