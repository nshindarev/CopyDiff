@echo off
mkdir "%~dp0/../log" 2>NUL
cd "%~dp0/.."
java -cp "%~dp0/../conf/;%~dp0/../lib/*" nshindarev.copydiff.appl.Main *
