@echo off
echo Building DeathManager plugins...

echo.
echo Building Velocity plugin...
cd DeathManager-Velocity
call gradlew clean build
cd ..

echo.
echo Building Spigot plugin...
cd Deathmanager-Spigot
call gradlew clean build
cd ..

echo.
echo Creating release directory...
if not exist "release" mkdir release
copy /Y "DeathManager-Velocity\build\libs\*.jar" "release\"
copy /Y "Deathmanager-Spigot\build\libs\*.jar" "release\"

echo.
echo Build complete! Plugins can be found in the release directory.
pause 