# Java chat
Simple chat working on LAN networks, created with JavaFX.

Requires:
- JDK 11 or later
- JavaFX SDK
- JavaFX jmods

## How it's done

 Application uses multicast channel as discovery method for publishing and receiving data.
 Connecting with other user creates client thread that connects over TCP/IP.
 Application doesn't require main server as every server is built within application so
 every user acts also as server.
 
## How to use (Tested on Windows 10)

Set up properly JAVA_HOME for JDK 11, then add PATHs to JavaFX jmods and SDK
```
set PATH_TO_FX="path\to\javafx-sdk-14.0.1"
set PATH_TO_FX_MODS="path\to\javafx-jmods-11.0.2"
```

Download project in desired location.

Open folder with project compile it
```
dir /s /b src\*.java > sources.txt & javac --module-path %PATH_TO_FX% -d mods/chat @sources.txt & del sources.txt
```

To run it use
```
java --module-path "%PATH_TO_FX%;mods" -m JavaFX_Chat/com.fxc.Main
```

To create custom runtime image
```
jlink --module-path "%PATH_TO_FX_MODS%;mods" --add-modules JavaFX --output chat
chat\bin\java -m JavaFX_Chat/com.fxc.Main
```

