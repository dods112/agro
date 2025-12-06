@echo off
cd /d "%~dp0"
if not exist PetAdoptionSystem.class (
    echo Compiling...
    javac -cp "sqlite-jdbc-3.51.0.jar" *.java
)
java -cp ".;sqlite-jdbc-3.51.0.jar" PetAdoptionSystem
pause