# hytale-mod-template-messages

## Overview

Message formatting and communication patterns for users and operators. This repository is a practical starting point for a Hytale mod.

## Main entrypoint

- Main class from manifest.json: net.hytaledepot.templates.mod.messages.MessagesModPlugin
- Includes asset pack: true

## Source layout

- Java sources: src/main/java
- Manifest: src/main/resources/manifest.json
- Runtime jar output: build/libs/hytale-mod-template-messages-1.0.0.jar

## Key classes

- MessagesModPlugin
- MessagesModTemplate

## Commands

- /hdmessagesmoddemo
- /hdmessagesmodstatus

## Build

1. Ensure the server jar is available in one of these locations:
   - HYTALE_SERVER_JAR
   - HYTALE_HOME/install/$patchline/package/game/latest/Server/HytaleServer.jar
   - workspace root HytaleServer.jar
   - libs/HytaleServer.jar
2. Run: ./gradlew clean build
3. Copy build/libs/hytale-mod-template-messages-1.0.0.jar into your server mods/ folder.

## License

MIT
