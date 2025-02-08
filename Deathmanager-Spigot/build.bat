@echo off
setlocal

REM 设置Java环境变量
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM 创建输出目录
mkdir build\classes 2>nul

REM 编译所有Java文件
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d build\classes -cp "libs\*;C:\Users\Administrator\.m2\repository\org\spigotmc\spigot-api\1.20.4-R0.1-SNAPSHOT\spigot-api-1.20.4-R0.1-SNAPSHOT.jar" @sources.txt
del sources.txt

REM 复制资源文件
xcopy /Y /E src\main\resources\* build\classes\

REM 创建JAR文件
cd build\classes
jar cf ..\DeathmanagerSpigot.jar *
cd ..\..

echo 构建完成！
pause 