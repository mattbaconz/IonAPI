@echo off
echo Setting up IonAPI test server...

REM Create test server directory
if not exist test-server mkdir test-server
if not exist test-server\plugins mkdir test-server\plugins

REM Copy plugins
echo Copying IonAPI...
copy platforms\ion-paper\build\libs\ion-paper-1.0.0-SNAPSHOT-all.jar test-server\plugins\

echo Copying test plugin...
copy test-plugin\build\libs\test-plugin-1.0.0.jar test-server\plugins\

echo.
echo Done! Now:
echo 1. Download Paper 1.20.1+ from https://papermc.io/downloads
echo 2. Put paper.jar in test-server folder
echo 3. Run: cd test-server ^&^& java -jar paper.jar
echo 4. Accept EULA and restart
echo 5. Test with commands: /testitem, /testgui, /testbossbar, /testscoreboard
pause
