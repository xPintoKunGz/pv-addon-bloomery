# Bloomery Voice Visualizer (BPVAddon)

Base: existing BPVAddon project, Minecraft 1.20.1 / Fabric / Java 17 / Plasmo Voice 2.1.17.

## Current implementation
- Press **O** in-game to open the visualizer settings.
- Toggle the visualizer, cycle radius and opacity, change color, toggle inner zones, and toggle the pulse effect.
- Settings are saved to `.minecraft/config/bpvaddon.json`.
- English and Thai keybind/category translations are included.

## Important limitation
The circles are a **client-side HUD visualization only**. They do not alter Plasmo Voice's actual microphone transmission distance or server-side listener range. Changing the real audible distance requires integration with supported Plasmo Voice settings/API and, where applicable, server configuration/permissions. This first implementation intentionally does not claim to change the real voice range.

## Build
Open this folder in IntelliJ IDEA as a Gradle project and run:
- `gradle build` (or `.\gradlew.bat build` if you add/use the Gradle wrapper for your environment)

The project targets Minecraft 1.20.1, Java 17, Fabric API 0.92.12+1.20.1, and Plasmo Voice API 2.1.17.
