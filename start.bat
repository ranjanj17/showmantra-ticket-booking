@echo off
echo ========================================================
echo Starting ShowMantra Application (Frontend ^& Backend)
echo ========================================================

echo Starting Spring Boot Backend...
start "ShowMantra Backend" cmd /k "cd backend && mvnw spring-boot:run"

echo Starting React Frontend...
start "ShowMantra Frontend" cmd /k "cd frontend && npm run dev"

echo Both applications are starting in separate terminal windows!
echo Once they are ready:
echo - Frontend will be available at http://localhost:5173
echo - Backend will be available at http://localhost:8080
echo ========================================================
