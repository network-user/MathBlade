# MathBlade — сборка на Windows

Архив уже содержит `gradle/wrapper/gradle-wrapper.jar` (восстановлен
2026-11-10), поэтому `gradlew.bat` запускается сразу. На Windows нет
карточек разрешений на хосты, как в Linux-окружении, — первая сборка
просто качает Gradle и зависимости.

## Что нужно

1. **JDK 17** — например, Microsoft OpenJDK 17 или Eclipse Temurin 17.
   Проверьте: `java -version` → `17.x`. JDK 21 не подходит для этой
   конфигурации (AGP 8.5.2 + `jvmTarget = "17"`).
2. **Android SDK, API 34** — проще всего поставить Android Studio и в
   SDK Manager доставить `Android 14 (API 34)`: SDK Platform 34,
   Build-Tools 34.0.0, Platform-Tools. Либо cmdline-tools с тем же
   набором.
3. Переменная `ANDROID_HOME` — путь к SDK, например
   `C:\Users\User\AppData\Local\Android\Sdk`. Или создайте в корне
   проекта `local.properties` со строкой
   `sdk.dir=C\:\\Users\\User\\AppData\\Local\\Android\\Sdk`
   (этот файл в архив не входит и не коммитится).

## Команды

В корне распакованного проекта, в PowerShell или cmd:

```bat
gradlew.bat :core:test
gradlew.bat :app:assembleDebug
```

Первая сборка долгая: скачивается Gradle 8.10.2 и зависимости
(AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.09.00).

## Результат

- Тесты `:core`: отчёт `core\build\test-results\test\`.
- APK: `app\build\outputs\apk\debug\app-debug.apk` — ставится на
  телефон (Android 8.0+, minSdk 26).

## Если что-то не так

- `JAVA_HOME is not set` / неверная версия Java — укажите путь к JDK 17.
- `SDK location not found` — проверьте `ANDROID_HOME` или `local.properties`.
- Ошибки тестов — пришлите лог, чинить будем по нему.

Подробности по проекту, балансу и известным дырам — в `docs/HANDOFF.md`.
