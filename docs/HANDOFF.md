# MathBlade «Клинок счёта» — HANDOFF (сдача MVP)

Дата проверки: 2026-11-10. Проверено на диске в `~/workspace/MathBlade`
(файлы перечислены в разделе «Что проверено»). Git-коммитов не делалось.

> **Статус честно:** код MVP написан полностью (:core + :app + ресурсы),
> локальные проверки без JDK пройдены (см. ниже), но **сборка и тесты
> НЕ запускались**: в сессии проверки нет JDK, Android SDK, Gradle,
> эмулятора и устройства. Зелёными тесты называть нельзя — они только
> написаны. Следующий шаг — установка JDK 17 + Android SDK и первая
> сборка; это отдельная попытка после отправки архива.

## 1. Как собрать

Требования: **JDK 17** и **Android SDK** (compileSdk/targetSdk 34,
minSdk 26). Версии зафиксированы в `gradle/libs.versions.toml`:
AGP 8.5.2, Kotlin 2.0.21, Compose BOM 2024.09.00,
navigation-compose 2.7.0, datastore-preferences 1.1.1, JUnit 4.13.2;
Gradle 8.10.2 (`gradle/wrapper/gradle-wrapper.properties`).

```bat
gradlew.bat :core:test              :: тесты :core (JUnit4)
gradlew.bat :app:compileDebugKotlin :: проверка компиляции :app
```

На Linux/macOS — `./gradlew` вместо `gradlew.bat`.

> **Блокер сборки закрыт 2026-11-10 18:07 UTC: `gradle-wrapper.jar` восстановлен**
> (43 583 байта, класс `org.gradle.wrapper.GradleWrapperMain` на месте,
> проверено через `unzip -l`) — он появился в `gradle/wrapper/` во время
> попытки сборки после отправки архива и включён в архив. В первой версии
> архива (md5 `ea206a450f5407775aed2f56cc82a641`) jar отсутствовал;
> сборка из того архива без восстановления jar невозможна. Примечание:
> в README написано, что jar «скачивается при первом запуске», — это
> неверно: при первом запуске скачивается дистрибутив Gradle, а не
> wrapper-jar.

## 2. Что запускалось, а что нет

**Запускалось в сессии проверки (без JDK):**

| Проверка | Результат |
|---|---|
| Баланс скобок `() [] {}` по всем `.kt` и `.gradle.kts` вне строк/комментариев (простой скрипт-сканер) | **OK: 26 файлов, 0 проблем** |
| Все `R.string.*` из Kotlin есть в `strings.xml` | **OK: 223 ссылки, 0 отсутствующих** (19 строк в `strings.xml` нигде не используются — см. ниже) |
| Все `R.drawable.*` есть в `drawable-nodpi` | **OK: 36 ссылок, 0 отсутствующих** |
| Валидность XML (`python xml.etree`) | **OK: 4 файла** — `AndroidManifest.xml`, `values/colors.xml`, `values/strings.xml`, `values/themes.xml` |
| Кириллица в коде `:core` (main + test) | **0 строк** — в `:core` только id и числа |
| `strings.xml` — только русский текст | **Да**: 242 записи `<string>`, латиница только в плейсхолдерах `%1$d`/`%1$s` и знаках `×`/`÷` |
| PNG: формат и альфа-канал (чтение заголовка IHDR) | **37/37 — 8-бит RGB, color type 2, без альфа-канала** |
| `settings.gradle.kts` включает оба модуля | **Да**: `include(":core")` и `include(":app")` |
| JDK / Gradle / эмулятор в сессии | **Отсутствуют** (`which java kotlinc gradle` — пусто) |

**Не запускалось (ни разу, нигде):**

- `gradlew.bat :core:test` — тесты не выполнялись, их зелёность не подтверждена.
- `gradlew.bat :app:compileDebugKotlin` — компиляция `:app` не проверялась.
- Эмулятор/устройство: установка и прохождение игры не проверялись.

Not run: установка и прохождение боя, кузницы, дневного челленджа и палаты славы на эмуляторе или устройстве. Эмулятора в этой сессии не было.

## 3. Тесты в CoreRulesTest.kt — написаны, НЕ запущены

Файл: `core/src/test/kotlin/com/mathblade/core/CoreRulesTest.kt`,
557 строк, **29 тестов `@Test`** (JUnit4). Все перечисленные ниже
тесты только написаны; в этой сессии они не запускались, поэтому
ни один из них нельзя называть пройденным.

Генератор и ответы:

1. `same seed gives same problem list` — один seed даёт одинаковый список задач.
2. `generator ranges hold for every operation and difficulty` — диапазоны
   всех 9 операций × 3 сложности (по 200 генераций на пару):
   сложение/вычитание/умножение, деление нацело (делимое ≤ 2000),
   остаток (1 ≤ остаток < делителя), степени, корень нацело
   (`ответ² = операнд`), проценты нацело, вычитание и внутреннее
   действие комбо `>= 0`.
3. `parseAnswer returns null for blank and garbage` — пустое/мусорное не парсится.
4. `remainder answer needs both quotient and remainder` — для остатка
   нужны оба числа (частное и остаток).

Формула урона:

5. `damage clamps speed at 0_6 and 1_6 caps` — скорость ограничена
   снизу 0.6 и сверху 1.6 (границы проверены числами: 19/7/12/20/32).
6. `precise bonus applies only when speed above 1_3` — точный удар +8
   только при скорости > 1.3.
7. `sword forge school bonus and precise strike stack in damage` —
   меч + ковка + бонус школы + точный удар складываются
   (проверено: 37 и 74 на мече архимага; бонус каравана +15% к MUL).
8. `damage breakdown text has numbers only` — текст разбора без слов.

Бой и боссы:

9. `wrong answer removes a life and leaves monster hp unchanged` —
   неверный ответ снимает жизнь, HP монстра не меняется.
10. `zero lives ends the run` — 0 жизней заканчивает забег, жизни не уходят в минус.
11. `correct answer damages monster and killing blow ends the fight` —
    верный ответ бьёт, смертельный удар завершает бой.
12. `boss phases fire once and grant at most one life each` — фазы босса
    срабатывают один раз каждая и возвращают не более 1 жизни.
13. `boss phase does not roll back damage already dealt` — фаза не
    откатывает уже нанесённый урон.

Дневной челлендж и поручения:

14. `daily seed is stable per date` — seed дня стабилен, у соседней даты другой.
15. `daily problems are stable per date and count 10` — 10 задач дня, детерминированы.
16. `second daily attempt on the same day is rejected, next day allowed` —
    вторая попытка в тот же день отвергается, на следующий день разрешена.
17. `daily challenge score does not depend on the sword` — счёт челленджа
    считается с нейтральными множителями (меч не влияет).
18. `daily errands pick 3 of 5 deterministically` — поручения: 3 из 5, детерминированно.

Квесты, контракты, титулы:

19. `quest completes only once` — квест завершается только один раз.
20. `contract fast10easy checks total time under 90 seconds` — контракты:
    10 лёгких быстрее 90 с, серия 15, дуэль с боссом с 1 жизнью.
21. `title thresholds` — титулы по суммарному урону: 0/1000/5000/15000/30000.

Кузница:

22. `forge prices and level cap` — цены ковки, кап уровня 5.
23. `forge success needs 5 correct, failure at 3 wrong` — успех: 5 верных,
    провал: 3 неверных.
24. `failed forge does not deduct gold, success deducts exactly the price` —
    неудачная ковка не списывает золото, удачная списывает ровно цену.

Тренировка, экономика, мир, сохранение:

25. `training difficulty unlocks by xp` — сложность тренировки по опыту (0/500/1500).
26. `economy rewards` — награды: монстр, дневная, сундук рекорда, поручение, контракт.
27. `world has 5 regions, throne hall has only a boss` — 5 регионов,
    в тронном зале только босс.
28. `save survives serialize and deserialize` — сохранение round-trip.
29. `unknown version or broken json resets without throwing` — неизвестная
    версия или битый JSON сбрасывают сохранение без исключения.

**Сверка с требуемым списком кейсов:** все требуемые кейсы
(seed; 9×3 диапазонов; деление/проценты/корень нацело; вычитание ≥ 0;
остаток двумя числами; урон на границах 0.6/1.6; меч+ковка+школа+точный
удар; неверный ответ снимает жизнь; 0 жизней; фазы босса один раз и
≤ 1 жизни; daily seed/вторая попытка/смена даты; счёт без меча;
квест один раз; ковка ≤ 5 и не в минус; неудачная ковка без списания
золота; round-trip и неизвестная версия) **присутствуют в тесте**.
Недостающих кейсов не найдено, тест не дописывался.

## 4. Файлы на диске (проверено)

### :core — 12 файлов, 1333 строки (не 13, как говорилось в задаче)

| Файл | Строк | Что внутри |
|---|---|---|
| `Combat.kt` | 118 | формула урона, `BattleState`, анимации боя |
| `Daily.kt` | 76 | seed дня (SHA-256), 10 задач, 1 попытка в день |
| `Difficulty.kt` | 14 | EASY/MEDIUM/HARD: базовый урон 12/20/32, нормы 8/15/25 с |
| `Economy.kt` | 18 | фиксированные награды |
| `Equipment.kt` | 147 | 5 мечей, 3 доспеха, 2 амулета, ковка |
| `MathProblem.kt` | 50 | задача, разбор ответа |
| `Operation.kt` | 21 | 9 операций |
| `ProblemGenerator.kt` | 243 | генератор «ответ сначала, условие потом» |
| `Quests.kt` | 83 | квесты, контракты, титулы |
| `SaveData.kt` | 317 | сохранение, ручной JSON, версия 1 |
| `Training.kt` | 13 | открытие сложности тренировки по опыту |
| `World.kt` | 233 | 5 регионов, монстры, боссы, фазы |
| `CoreRulesTest.kt` (test) | 557 | 29 тестов, см. раздел 3 |

### :app — 9 Kotlin-файлов, 2682 строки

| Файл | Строк |
|---|---|
| `MainActivity.kt` | 98 |
| `BattleScreen.kt` | 486 |
| `CampScreen.kt` | 175 |
| `Drawables.kt` | 61 |
| `GameViewModel.kt` | 838 |
| `Names.kt` | 163 |
| `Screens.kt` | 613 |
| `Theme.kt` | 34 |
| `Ui.kt` | 214 |

Прочее: `app/src/main/res/values/strings.xml` — **293 строки,
242 записи `<string>`** (в задаче говорилось «242 строки» — на деле
242 это число записей, а не строк); `colors.xml` (10 строк),
`themes.xml` (7 строк), `AndroidManifest.xml` (23 строки).
`settings.gradle.kts` (22 строки) включает `:core` и `:app`
(в файле остался устаревший комментарий «Only :core for now»).
`app/build.gradle.kts`, `core/build.gradle.kts`, корневой
`build.gradle.kts`, `gradle/libs.versions.toml` (28 строк),
`gradlew` / `gradlew.bat`, `README.md` (107 строк).

Неиспользуемые строки в `strings.xml` (19 штук, определены, но
нигде не ссылаются — не ошибка, но мусор): `battle_damage_dealt`,
`battle_empty_answer`, `battle_hint_used`, `battle_life_lost`,
`battle_title`, `camp_choose_region`, `camp_gate_desc`,
`camp_gold_label`, `camp_xp_label`, `common_close`,
`common_continue`, `common_level_short`, `common_next`,
`common_no`, `common_ok`, `common_start`, `common_yes`,
`notice_saved`, `settings_reset_done`.

## 5. Экраны и маршруты

Старт — `camp`. Навигация — Navigation Compose, маршруты-строки
(`MainActivity.kt`, `CampScreen.kt`, `BattleScreen.kt`).

| Маршрут | Экран | Файл |
|---|---|---|
| `camp` | Лагерь (старт): выбор региона, вход в кузницу/квесты/сундук/палату/день/настройки | `CampScreen.kt` |
| `battle/{regionId}` | Бой с монстрами региона и боссом | `BattleScreen.kt` |
| `quests` | Журнал квестов | `Screens.kt` (`QuestLogScreen`) |
| `daily` | Дневной челлендж (10 задач, 1 попытка в день) | `Screens.kt` (`DailyScreen`) |
| `forge` | Кузница | `Screens.kt` (`ForgeScreen`) |
| `chest` | Сундук: мечи, доспехи, амулеты, сундук рекорда | `Screens.kt` (`ChestScreen`) |
| `hall` | Палата славы (лучшие забеги) + турнирная неделя | `Screens.kt` (`HallScreen`) |
| `settings` | Настройки (вибрация, сброс сохранения) | `Screens.kt` (`SettingsScreen`) |
| `death` | Экран смерти (забег окончен) | `BattleScreen.kt` (`DeathScreen`) |
| `victory` | Экран победы над регионом | `BattleScreen.kt` (`VictoryScreen`) |
| — | Экран несовместимого сохранения (показывается вместо навигации, если сохранение сброшено как битое) | `Screens.kt` (`IncompatibleSaveScreen`) |

Из боя назад — с подтверждением (`BattleScreen.kt`). Ориентация
экрана намеренно не зафиксирована. Разрешение `VIBRATE` в манифесте
(вибрация при верном/неверном ответе).

Что делает `GameViewModel` (838 строк, `androidx.lifecycle` +
DataStore `mathblade`): хранит `SaveData` как JSON (`save_json`),
запускает бои по регионам, кузницу, дневной челлендж, покупки
и надевание снаряжения, сундук рекорда, палату славы и турнир недели.

## 6. Таблица баланса (все числа — из кода :core)

### Формула урона

`speed = normSeconds × 1000 / elapsedMs`, ограничена `[0.6, speedCap]`
(базовый cap 1.6). `damage = round(base × speed × sword × typeBonus)`,
минимум 1. Если `speed > 1.3`, добавляется точный удар `preciseBonus`
после округления.

| Сложность | Базовый урон | Норма времени |
|---|---|---|
| EASY | 12 | 8 с |
| MEDIUM | 20 | 15 с |
| HARD | 32 | 25 с |

### Регионы, монстры и боссы (`World.kt`)

| Регион | Сложность | Монстры (HP / золото / опыт) | Босс (HP / золото / опыт) |
|---|---|---|---|
| 1 Окраины | EASY | slime_add 60/25/25 (ADD), wolf_sub 60/25/25 (SUB) | ancient_slime 340/100/100 (ADD) |
| 2 Торговый путь | MEDIUM | bat_mul 100/40/40 (MUL), tax_div 100/40/40 (DIV), usurer_pct 100/40/40 (PERCENT) | caravan_master 360/140/140 (MUL) |
| 3 Башня остатка | MEDIUM | golem_rem1 140/55/55 (REMAINDER), golem_rem2 140/55/55 (REMAINDER) | remainder_keeper 380/160/160 (REMAINDER) |
| 4 Руины силы | HARD | griffin_pow 160/70/70 (POWER), root_beast 160/70/70 (SQRT), scribe_combo 160/70/70 (COMBO) | chronicler 400/180/180 (POWER) |
| 5 Тронный зал | HARD | — (только босс) | archchancellor 420/250/250 (COMBO) |

Фазы босса (все боссы — 3 фазы): пороги **60% и 30% HP**, каждая
фаза срабатывает один раз и возвращает **не более 1 жизни**
(только если жизней меньше максимума); урон не откатывается.

| Босс | Фаза 1 | Фаза 2 | Фаза 3 |
|---|---|---|---|
| ancient_slime | ADD / EASY | ADD / EASY | SUB / MEDIUM |
| caravan_master | MUL / MEDIUM | DIV / MEDIUM | PERCENT / MEDIUM |
| remainder_keeper | REMAINDER / MEDIUM | REMAINDER / MEDIUM | REMAINDER / HARD |
| chronicler | POWER / HARD | SQRT / HARD | COMBO / HARD |
| archchancellor | COMBO / HARD, смешанные задачи всех школ | POWER / HARD, смешанные | PERCENT / HARD, смешанные |

### Мечи (`Equipment.kt`)

| Меч | Множитель | Бонус cap скорости | Бонус школы | Точный удар | Добывается |
|---|---|---|---|---|---|
| rusty (ржав.) | 1.00 | +0 | — | 0 | регион 1 |
| apprentice | 1.15 | +0.1 | — | 0 | регион 1 |
| caravan | 1.30 | +0 | +15% к MUL/DIV/PERCENT | 0 | регион 2 |
| remainder | 1.25 | +0 | +20% к REMAINDER | 0 | регион 3 |
| archmage | 1.50 | +0.2 | — | +8 | регион 5 (финальный босс) |

### Ковка (`Forge`)

- Цена улучшения с уровня `L` на `L+1`: `50 × (L+1) × regionObtained`
  меча. Примеры из теста: ржавый 0→1 стоит 50, 1→2 стоит 100;
  караванный 0→1 стоит 100.
- Каждый уровень ковки: **+0.05** к множителю меча.
- Максимум **5 уровней**; выше — попытка не начинается.
- Попытка: успех — **5 верных** ответов; провал — **3 неверных**.
- Золото списывается только при успехе и только один раз, ровно
  в размере цены; неудачная попытка золото не трогает.

### Доспехи и амулеты (`Equipment.kt`)

| Доспех | Жизней | Цена | Требование |
|---|---|---|---|
| canvas (холщовый) | 3 | 0 (стартовый) | — |
| leather (кожаный) | 4 | 200 | регион 1 пройден |
| chainmail (кольчуга) | 5 | 500 | регион 3 пройден |

| Амулет | Цена | Требование | Эффект |
|---|---|---|---|
| beads (чётки) | 150 | регион 1 пройден | показывает последнюю цифру ответа раз за бой |
| hourglass (песочные часы) | 350 | регион 2 пройден | норма времени ×1.25 (скорость падает медленнее) |

### Титулы, награды, тренировка, дневное

- Титулы по суммарному урону: **0 — Ученик, 1000 — Оруженосец,
  5000 — Рыцарь, 15000 — Мастер, 30000 — Архимаг**.
- Награды (`Economy.kt`): дневная — 100 золота / 50 опыта;
  сундук рекорда — 300 золота (один раз); поручение — 30 золота /
  20 опыта; контракт — 150 золота. Награды за монстров — из таблицы
  регионов выше.
- Тренировка (`Training.kt`): сложность по опыту — EASY от 0,
  MEDIUM от 500, HARD от 1500.
- Дневной челлендж (`Daily.kt`): 10 смешанных задач, seed —
  SHA-256 от строки `mathblade-daily-YYYY-MM-DD`, граница дня —
  календарь Europe/Moscow, одна попытка в день (тратится в момент
  старта, а не финиша). Счёт не зависит от меча.
- Поручения дня: 3 из 5 фиксированных (`defeat_school`, `solve_10`,
  `flawless_battle`, `upgrade_sword`, `finish_daily`), выбор
  детерминирован по seed дня.
- Контракты: серия 15 верных подряд; 10 лёгких задач суммарно
  быстрее 90 с; победа над боссом, начав дуэль с 1 жизнью.

## 7. Картинки — 37 файлов, силуэтом не заменена ни одна

Все 37 PNG лежат в `app/src/main/res/drawable-nodpi/`, суммарно
**106 700 270 байт (~102 МБ)**. Все 37 — **8-бит RGB без
альфа-канала** (color type 2, прозрачного фона нет). Поэтому в UI
они показываются только внутри скруглённых карточек с
`ContentScale.Crop` на тёмной поверхности, а фоны `bg_*` — на весь
экран (`Drawables.kt`, комментарий в коде). Использовать их как
прозрачные спрайты поверх фона нельзя.

**Заменена силуэтом: ни одна.** Все 37 картинок сгенерированы и
присутствуют; процедурный силуэт на Canvas (`SilhouetteBox` в
`Ui.kt`) — только fallback, если drawable с нужным id отсутствует
(сейчас такого случая нет: все 36 ссылок `R.drawable.*` из Kotlin
находятся, а `ic_launcher_foreground` используется из манифеста).

| Файл | Размер |
|---|---|
| amulet_beads.png | 1 493 567 |
| amulet_hourglass.png | 1 462 913 |
| ancient_slime.png | 2 774 808 |
| archchancellor.png | 3 878 653 |
| armor_canvas.png | 3 067 300 |
| armor_chainmail.png | 3 999 382 |
| armor_leather.png | 2 195 913 |
| bat_mul.png | 2 024 802 |
| bg_camp.png | 4 813 057 |
| bg_region1.png | 4 769 552 |
| bg_region2.png | 5 327 004 |
| bg_region3.png | 3 874 555 |
| bg_region4.png | 4 818 953 |
| bg_region5.png | 4 242 621 |
| blacksmith_portrait.png | 3 679 480 |
| caravan_master.png | 3 804 071 |
| chronicler.png | 2 693 014 |
| golem_rem1.png | 3 524 083 |
| golem_rem2.png | 2 813 995 |
| griffin_pow.png | 2 824 985 |
| herald_portrait.png | 3 601 654 |
| hero_portrait.png | 3 066 143 |
| hero_sprite.png | 2 482 092 |
| ic_launcher_foreground.png | 2 327 775 |
| remainder_keeper.png | 3 695 036 |
| root_beast.png | 2 212 117 |
| scribe_combo.png | 2 513 008 |
| slime_add.png | 2 041 753 |
| sword_apprentice.png | 841 370 |
| sword_archmage.png | 1 170 833 |
| sword_caravan.png | 1 296 964 |
| sword_remainder.png | 1 918 693 |
| sword_rusty.png | 1 151 945 |
| tax_div.png | 2 743 193 |
| treasurer_portrait.png | 2 623 883 |
| usurer_pct.png | 2 828 837 |
| wolf_sub.png | 2 102 866 |

Назначение: 15 монстров/боссов, 5 мечей, 3 доспеха, 2 амулета,
5 фонов регионов + фон лагеря, портреты (герой, кузнец, глашатай,
казначей, герой-спрайт), иконка лаунчера. Вес ~102 МБ раздувает
APK — для релиза картинки стоит сжать/уменьшить (в MVP не сделано).

## 8. Известные дыры (проверены по коду)

1. **Жизни между рядовыми монстрами не переносятся.**
   `BattleState` (`Combat.kt`) не имеет параметра `initialLives`
   (в отличие от `BossBattle`, у которого он есть): каждый рядовой
   монстр начинается с полных жизней (`lives = maxLives`). В
   `GameViewModel.kt:367-370` есть прямой комментарий об этом.
   Перенос жизней работает только к боссу: `livesCarried`
   обновляется после каждого боя (`GameViewModel.kt:467`) и
   передаётся в `BossBattle(initialLives = livesCarried)`.
2. **`currentAnimation` в :core не сбрасывается в IDLE.**
   В `Combat.kt`/`World.kt` анимация только устанавливается в
   `HERO_ATTACK`/`HERO_HURT`/`PHASE_CHANGE`/`ENEMY_DYING` и никогда
   не возвращается в `IDLE`. UI обходит это: `BattleScreen.kt:174-177`
   показывает `IDLE`, пока нет активного `feedback`.
3. **Часть прогресса живёт в DataStore :app, а не в `SaveData` :core.**
   Отдельные ключи DataStore (`GameViewModel.kt:62-64`):
   `owned_armors` (купленные доспехи), `owned_amulets` (купленные
   амулеты), `daily_best_json` (лучшие дневные счёты за 7 дней,
   `bestDaily7Days()`). В `SaveData` есть только экипированные
   `armorId`/`amuletId`, списков купленного там нет. Следствие:
   сброс/перенос `save_json` не трогает эти ключи и наоборот.
   Уточнение по проверке кода (в исходной формулировке дыры была
   ошибка): `weekId` турнира **хранится в `SaveData.weeklyTournament`**
   (`GameViewModel.kt:543-548`), а не в отдельном ключе DataStore;
   флаг сундука рекорда — тоже не отдельный ключ, а маркер
   `"chest_record"` внутри `SaveData.completedQuests`
   (`GameViewModel.kt:789, 796`). Отдельного поля для него в
   `SaveData` нет.
4. **Мечи считаются добытыми по региону, а не по победе над боссом.**
   `GameViewModel.kt:292`: `sword.regionObtained <= unlockedRegion`.
   Достаточно открытого региона — отдельной проверки, что босс
   региона побеждён, нет.
5. **PNG без прозрачности** (см. раздел 7): все 37 — RGB, использовать
   как спрайты нельзя, только карточки с Crop.
6. **Вес картинок ~102 МБ** раздувает APK (см. раздел 7).
7. ~~**`gradle-wrapper.jar` отсутствует**~~ — закрыто 2026-11-10 18:07 UTC,
   jar восстановлен и включён в архив (см. раздел 1).

## 9. Что дальше

1. ~~Восстановить `gradle-wrapper.jar`~~ — сделано 2026-11-10 18:07 UTC,
   jar в архиве (см. раздел 1).
2. Установить JDK 17 + Android SDK (API 34) — на Windows, см.
   `WINDOWS-BUILD.md`; попытка сборки здесь остановлена 2026-11-10
   18:48 UTC по просьбе пользователя (карточки разрешений на хосты).
3. Запустить `gradlew.bat :core:test` — впервые прогнать 29 тестов.
4. Запустить `gradlew.bat :app:compileDebugKotlin`, затем собрать
   debug-APK и прогнать на эмуляторе/устройстве: бой, кузницу,
   дневной челлендж и палату славы (см. строку «Not run» в разделе 2).
5. Решить по дырам из раздела 8 (как минимум п. 1, 2, 4 — это
   решения по геймдизайну/архитектуре, а не баги сборки) и по весу
   картинок.
