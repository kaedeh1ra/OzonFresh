# KMP Template

Шаблон Kotlin Multiplatform + Compose Multiplatform: Android, iOS, desktop (Windows, Linux, macOS) и веб
(Kotlin/Wasm) из одного кода. Внутри — модульная архитектура, MVI, Koin, навигация через плагины фич, Ktor-клиент и генератор
новых фич. Пакет по умолчанию `com.example.app`, переименовывается одной командой.

## Что внутри

| Модуль                  | Назначение                                                                                                        |
|-------------------------|-------------------------------------------------------------------------------------------------------------------|
| `app/androidApp`        | Точка входа Android: `MainActivity`, манифест, подпись релиза                                                     |
| `app/webApp`            | Точка входа веба (Kotlin/Wasm): `main.kt`, `index.html` с экраном загрузки                                        |
| `app/desktopApp`        | Точка входа desktop (JVM): окно `main()`, установщики MSI/EXE, DEB/RPM, DMG                                       |
| `iosApp`                | Xcode-проект iOS-приложения: SwiftUI-обёртка над экраном Compose из фреймворка `Shared`                           |
| `app/shared`            | Оболочка: тема, панели, `NavHost` из плагинов фич, запуск Koin; для iOS — фреймворк `Shared` и `MainViewController` |
| `app/core/domain`       | Чистый Kotlin: модели, интерфейсы репозиториев, use case'ы                                                        |
| `app/core/data`         | Реализации репозиториев, Ktor `HttpClient`, хранилище настроек под каждую платформу                              |
| `app/core/mvi`          | `MviViewModel` (State / Intent / Effect), `LoadState`, `CollectEffects`                                           |
| `app/core/navigation`   | Контракты между фичами: `FeaturePlugin`, `AppNavigator`, общие маршруты                                           |
| `app/core/designsystem` | `AppTheme` (светлая и тёмная), состояния загрузки и ошибки, `AppCard`, `WindowSize`                               |
| `app/core/testing`      | Фейки для тестов                                                                                                  |
| `app/feature/home`      | Пример фичи: экран-счётчик, MVI, use case'ы, тест ViewModel                                                       |
| `build-logic`           | Convention-плагины, автоподключение фич, задача `newFeature` — см. [build-logic/README.md](build-logic/README.md) |

Стек: Kotlin 2.4, Compose Multiplatform 1.12, AGP 9, Gradle 9, Koin 4, Ktor 3, Navigation Compose.
Версии — в [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

Таргеты каждого модуля (задаются в `build-logic`, плагин `kmp.library`):

| Платформа          | Kotlin-таргет                    | Точка входа                                     | Где собирается          |
|--------------------|----------------------------------|-------------------------------------------------|-------------------------|
| Android (minSdk 31)| `android`                        | `app/androidApp`                                | любая ОС                |
| iOS 15+            | `iosArm64`, `iosSimulatorArm64`  | `iosApp` (Xcode) → фреймворк из `app/shared`    | только macOS + Xcode 16 |
| Windows/Linux/macOS| `jvm`                            | `app/desktopApp`                                | любая ОС (установщик — под текущую) |
| Браузер            | `wasmJs`                         | `app/webApp`                                    | любая ОС                |

Симулятор на Intel-маках (`iosX64`) не включён: при необходимости добавьте `iosX64()` в
`KmpLibraryConventionPlugin` и уберите `EXCLUDED_ARCHS` в настройках Xcode-проекта. На Windows и Linux
iOS-таргеты пропускаются (`kotlin.native.ignoreDisabledTargets` в `gradle.properties`), остальное
собирается как обычно.

## Как начать проект из шаблона

1. На GitHub: **Use this template → Create a new repository**.
   (Чтобы кнопка появилась у самого шаблона: Settings → General → отметить **Template repository**.)
2. Переименуйте проект — один из вариантов ниже.
3. Перепишите этот README под свой проект и удалите `rename.sh`, `rename.ps1`,
   `.github/workflows/rename.yml`: после переименования они не нужны.

## Переименование

Скрипт меняет:

- `package` / `import` и все упоминания `com.example.app` в коде и скриптах сборки;
- папки `com/example/app` → `ru/myname/coolapp` в каждом source set (`src/*/kotlin`, включая `build-logic`);
- `Constants.BASE_PACKAGE`, а от него — Android `namespace` всех модулей и `applicationId`;
- `rootProject.name` в `settings.gradle.kts` (`KmpTemplate` → ваше имя);
- desktop: `mainClass`, `bundleID` для macOS (из `Constants`), имя пакета установщика (`rootProject.name`);
- iOS: `PRODUCT_NAME` и `PRODUCT_BUNDLE_IDENTIFIER` в `iosApp/Configuration/Config.xcconfig`
  (подчёркивания в bundle id заменяются на `-`), имя `.app` в `project.pbxproj` и схеме;
- отображаемое имя приложения: `strings.xml` (Android), `<title>` в `index.html` (веб), заголовок окна
  (desktop), `CFBundleDisplayName` в `Info.plist` (iOS).

`README.md`, `.github/` и сами скрипты не трогаются. Скрипт откажется работать, если проект уже
переименован или в git есть незакоммиченные изменения (флаг `--force` / `-Force` — всё равно
выполнить). Проверки ввода: пакет — минимум два сегмента из `[a-z0-9_]`, без ключевых слов Kotlin/Java;
имя — латинские буквы и цифры; в отображаемом имени нельзя `< > & " '`.

**macOS / Linux / Git Bash** (нужны bash и perl, оба есть в Git for Windows):

```bash
./rename.sh --package ru.myname.coolapp --name CoolApp --display-name "Cool App"
```

**Windows PowerShell:**

```powershell
.\rename.ps1 -Package ru.myname.coolapp -Name CoolApp -DisplayName "Cool App"
```

**Прямо на GitHub:** Actions → **Rename project** → Run workflow, ввести пакет и имя. Workflow запустит
`rename.sh` и закоммитит результат в выбранную ветку. В репозитории-шаблоне он не запускается.
Если Actions не может пушить, включите Settings → Actions → General → Workflow permissions → **Read and write**.

`--display-name` необязателен, по умолчанию совпадает с `--name`. После переименования:

```bash
./gradlew clean testAndroidHostTest jvmTest
```

## Сборка и запуск

Нужны JDK 21+ и Android SDK (путь в `local.properties` → `sdk.dir`, Android Studio создаёт его сама).
JDK для демона Gradle задана в `gradle/gradle-daemon-jvm.properties`.

```bash
./gradlew :app:androidApp:installDebug                 # Android: собрать и поставить на устройство/эмулятор
./gradlew :app:androidApp:assembleRelease              # Android: релизный APK (подпись — ниже)
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun      # веб: dev-сервер с автооткрытием браузера
./gradlew :app:webApp:wasmJsBrowserDistribution        # веб: статика в app/webApp/build/dist/wasmJs/productionExecutable
./gradlew testAndroidHostTest jvmTest                  # тесты всех модулей на JVM (Android host и desktop)
```

На Windows — `gradlew.bat` или `.\gradlew` в PowerShell.

### Desktop (Windows, Linux, macOS)

```bash
./gradlew :app:desktopApp:run                          # запустить окно приложения
./gradlew :app:desktopApp:createDistributable          # папка с приложением и встроенной JRE, без установщика
./gradlew :app:desktopApp:packageDistributionForCurrentOS  # установщик под текущую ОС
```

Установщики лежат в `app/desktopApp/build/compose/binaries/main/<формат>/`. Каждый формат собирается
только на своей ОС: MSI и EXE — на Windows (WiX плагин Compose скачает сам), DEB и RPM — на Linux
(для RPM нужен `rpmbuild`), DMG — на macOS. Отдельные задачи: `packageMsi`, `packageExe`, `packageDeb`,
`packageRpm`, `packageDmg`. Для обновления MSI поверх установленной версии задайте свой постоянный
`upgradeUuid` в `app/desktopApp/build.gradle.kts`; для публикации DMG нужны подпись и нотаризация
(`macOS { signing { ... } notarization { ... } }`).

### iOS (только на Mac)

Нужны Xcode 16+ и JDK 21. Первый запуск Gradle скачает Kotlin/Native toolchain в `~/.konan` (сотни мегабайт).

1. Откройте `iosApp/iosApp.xcodeproj` в Xcode, выберите схему `iosApp` и симулятор, нажмите Run.
   Xcode сам вызовет `./gradlew :app:shared:embedAndSignAppleFrameworkForXcode` (build phase
   «Compile Kotlin Framework») и подключит фреймворк `Shared`.
2. Для запуска на устройстве впишите свой Team ID в `iosApp/Configuration/Config.xcconfig`
   (`TEAM_ID=`) — подпись настроится автоматически.

Из терминала без Xcode-интерфейса:

```bash
./gradlew :app:shared:linkDebugFrameworkIosSimulatorArm64   # только Kotlin-часть
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' ARCHS=arm64 CODE_SIGNING_ALLOWED=NO build
```

Как устроено: `app/shared` собирается в статический фреймворк `Shared`; в нём функция
`MainViewController()` (`app/shared/src/iosMain`) запускает Koin и возвращает `UIViewController` с
экраном Compose. SwiftUI-приложение (`iosApp/iosApp/iOSApp.swift`, `ContentView.swift`) просто
показывает этот контроллер на весь экран. Имя, bundle id, версия и адрес API —
в `iosApp/Configuration/Config.xcconfig`.

### Адрес API

`HttpClient` уже лежит в Koin (`dataModule`): репозиторий берёт его через `get()` и ходит по
относительным путям. Базовый адрес:

- веб — origin страницы (`https://ваш-сайт/`);
- Android — gradle-свойство `apiBaseUrl`, по умолчанию `https://example.com/`:
  `./gradlew :app:androidApp:installDebug -PapiBaseUrl=http://10.0.2.2:8080/` или строка
  `apiBaseUrl=...` в `gradle.properties`;
- desktop — то же свойство: `./gradlew :app:desktopApp:run -PapiBaseUrl=http://localhost:8080/`
  (в приложение попадает как `-Dapp.apiBaseUrl`);
- iOS — `API_BASE_URL` в `iosApp/Configuration/Config.xcconfig` (`//` там пишется как `/$()/`).

### Подпись релиза Android

Скопируйте `keystore.properties.example` в `keystore.properties` (он в `.gitignore`) и заполните, либо
задайте переменные окружения `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Без
ключа релиз подписывается debug-ключом.

## Новая фича

```bash
./gradlew newFeature --name=settings --tab="Настройки"
```

Создаст модуль `app/feature/settings` с MVI-экраном, Koin-модулем и тестом; settings, Koin и навигация
подхватят его сами. Подробности — в [build-logic/README.md](build-logic/README.md).

## CI

`.github/workflows/ci.yml` на каждый push и PR:

- `build` (Linux): тесты, debug APK, веб-дистрибутив, desktop-приложение;
- `apple` (macOS): Kotlin для iOS, фреймворк для симулятора, DMG и сборка `iosApp` через `xcodebuild`;
- `rename-smoke` — только в репозитории-шаблоне: после `rename.sh` проект собирается.
