# MathBlade — «Клинок счёта»

Android-игра: герой с мечом решает примеры устного счёта и бьёт монстров
уроном, зависящим от скорости ответа. Модуль `:core` — чистая логика
на Kotlin/JVM, без Android и без русских строк в коде (только id и числа);
весь текст для игрока — в `strings.xml` модуля `:app`.

Логика переписана заново по идеям DotMathBot (проприетарная лицензия:
алгоритмы и диапазоны — да, построчный перенос кода — нет).
Подробности изучения: `../MathBlade-study-REPORT.md`.

## Структура

```
MathBlade/
  settings.gradle.kts        # пока только :core; :app добавит себя сам
  build.gradle.kts           # корневой, плагины через version catalog
  gradle/libs.versions.toml  # версии плагинов и библиотек
  gradle/wrapper/            # Gradle 8.10.2 (jar скачивается при первом запуске)
  gradlew / gradlew.bat
  core/                      # чистый JVM-модуль, пакет com.mathblade.core
    src/main/kotlin/com/mathblade/core/
      Difficulty.kt          # EASY/MEDIUM/HARD: baseDamage 12/20/32, norm 8/15/25 с
      Operation.kt           # 9 операций, id "add", "sub", ...
      MathProblem.kt         # задача + parseAnswer / checkAnswer / checkRemainderAnswer
      ProblemGenerator.kt    # генератор: ответ сначала, условие потом
      Combat.kt              # формула урона, BattleState, анимации боя
      World.kt               # 5 регионов, монстры, боссы и их фазы
      Equipment.kt           # 5 мечей, броня, амулеты, ковка
      Daily.kt               # seed дня (SHA-256), 10 задач, одна попытка в день
      Quests.kt              # квесты, контракты, титулы
      SaveData.kt            # сохранение, ручной JSON, version=1
      Economy.kt             # награды: золото/опыт
      Training.kt            # открытие сложности тренировки по опыту
    src/test/kotlin/com/mathblade/core/
      CoreRulesTest.kt       # JUnit4-тесты правил
  app/                       # делает агент :app (не входит в этот модуль)
```

## Фиксированные числа (использует :app)

- `minSdk 26`, `targetSdk 34`, `compileSdk 34`
- AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.09.00,
  navigation-compose 2.7.0, datastore-preferences 1.1.1, JUnit 4.13.2
- Gradle 8.10.2

## Как собрать

```sh
./gradlew :core:test                  # тесты :core
./gradlew :app:compileDebugKotlin    # проверка компиляции :app (после его создания)
```

В сессии создания `:core` эти команды НЕ запускались: JDK/SDK
в сессии отсутствуют, сборка не проверялась.

## Ключевые правила

- **Урон**: `speed = normSeconds*1000/elapsedMs`, clamp `[0.6, speedCap]`;
  `damage = round(base*speed*sword*typeBonus)`, минимум 1;
  при `speed > 1.3` после округления добавляется `preciseBonus`.
  Разбор — только числа: `20 x 1.3 x 1.15 = 30`.
- **Жизни**: неверный ответ −1 жизнь, hp монстра не меняется;
  0 жизней — забег окончен.
- **Боссы**: фазы при hp ≤ 60% и ≤ 30% от max, каждая — один раз;
  фаза меняет школу/сложность и возвращает максимум 1 жизнь;
  нанесённый урон не откатывается.
- **Челлендж дня**: та же формула урона, но всегда `swordMultiplier = 1`,
  `typeBonus = 1`, без точного удара — счёт не зависит от меча.
- **День**: seed = SHA-256(`"mathblade-daily-YYYY-MM-DD"`), первые 8 hex
  как Long; граница дня — Europe/Moscow; попытка тратится в момент старта.
- **Сохранение**: ручной JSON с полем `version`; неизвестная версия
  или битый JSON — сброс к значениям по умолчанию, без исключения.
- **Остаток**: проверяются оба числа — частное и остаток.

## HP монстров и боссов

| Регион | Рядовые (hp) | Босс (hp) | Сложность |
|---|---|---|---|
| 1 Окраина счёта | 60, 60 | ancient_slime 340 | EASY |
| 2 Торговый тракт | 100, 100, 100 | caravan_master 360 | MEDIUM |
| 3 Башня остатка | 140, 140 | remainder_keeper 380 | MEDIUM (фаза 3 — HARD) |
| 4 Руины степеней | 160, 160, 160 | chronicler 400 | HARD |
| 5 Тронный зал | — (только босс) | archchancellor 420 | HARD |

Ориентир: 4–6 ударов по рядовому, 10–12 по боссу при среднем уроне
региона (базовый урон × типичная скорость 1.0–1.3 × меч + точный удар).

## Пороги титулов (суммарный урон)

| Урон | Титул (id) |
|---|---|
| 0 | title_apprentice |
| 1000 | title_squire |
| 5000 | title_knight |
| 15000 | title_master |
| 30000 | title_archmage |

## Экономика (золото/опыт)

- Монстр: из таблицы мира (25/25, 40/40, 55/55, 70/70 по регионам;
  боссы 100–250). Челлендж дня: 100/50. Сундук рекорда: 300.
  Поручение: 30/20. Контракт: 150.
- Ковка: цена `50*(level+1)*regionObtained`, нужно 5 верных,
  провал при 3 неверных, максимум 5-й уровень; золото списывается
  только при успехе.
- Тренировка: EASY при xp ≥ 0, MEDIUM при xp ≥ 500, HARD при xp ≥ 1500.
