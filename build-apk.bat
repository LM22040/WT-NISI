@echo off
REM Limpiar el proyecto
gradlew clean

REM Construir el APK en modo Debug
gradlew assembleDebug

REM Pausar para que puedas ver los resultados
pause
