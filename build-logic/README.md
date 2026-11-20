# build-logic: сборка модулей

Отдельная Gradle-сборка (`includeBuild` в корневом `settings.gradle.kts`) с плагинами, которые
описывают модули `app/*` один раз, а не копией в каждом `build.gradle.kts`.

| Плагин                 | Где применяется                                              | Что делает                                                                                                                |
|------------------------|--------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `kmp.settings`         | `settings.gradle.kts`                                        | Подключает каждую папку `app/feature/<имя>` со своим `build.gradle.kts` как модуль `:app:feature:<имя>`                   |
| `kmp.library`          | `core/domain`, `core/data`, `core/testing`                   | Таргеты Android, iOS, JVM (desktop) и Wasm, namespace из пути модуля, тесты `testAndroidHostTest` и `jvmTest`|
| `kmp.compose`          | `core/designsystem`, `core/mvi`, `core/navigation`, `shared` | То же плюс Compose Multiplatform и lifecycle                                                                              |
| `kmp.feature`          | `app/feature/*`                                              | Compose-модуль с зависимостями на core-модули, навигацию и Koin; в тестах — фейки из `core/testing`                       |
| `kmp.feature.registry` | `shared`                                                     | Зависимость на каждую фичу и задача `generateFeatureRegistry`: генерирует `featureModules` — список Koin-модулей всех фич |
| `kmp.scaffold`         | корневой `build.gradle.kts`                                  | Задача `newFeature`                                                                                                       |

## Константы сборки

Базовый пакет, версия JVM, Android SDK и пути постоянных модулей лежат в
[`Constants.kt`](./settings/src/main/kotlin/ru/kaed/ozonfresh/buildlogic/Constants.kt). Их используют
convention-плагины и обычные скрипты; в любом `build.gradle.kts` они подключаются через
`import ru.kaed.ozonfresh.buildlogic.Constants`.

```kotlin
const val BASE_PACKAGE = "ru.kaed.ozonfresh" // корневой пакет, namespace и applicationId
const val JVM_VERSION = 21                 // байткод всех модулей: Kotlin jvmTarget и Java compileOptions
const val COMPILE_SDK = 37
const val TARGET_SDK = 37
const val MIN_SDK = 31
```

Android namespace каждого модуля выводится из его пути: `:app:core:domain` →
`ru.kaed.ozonfresh.core.domain`.

Gradle-скрипты не видят только JDK, на которой идёт сама сборка. Повышая `JVM_VERSION`, поменяйте и
её: `java-version` в `.github/workflows/ci.yml` и `gradle/gradle-daemon-jvm.properties`
(`./gradlew updateDaemonJvm --jvm-version=<N>`).

## Почему два подпроекта

`kmp.settings` лежит в отдельном подпроекте `settings`: Gradle грузит jar settings-плагина в
classloader настроек, где нет Kotlin и Android Gradle Plugin. Будь он в одном jar с
convention-плагинами, те не нашли бы свои типы.

## Новая фича

```bash
./gradlew newFeature --name=settings                       # экран без ссылки в оболочке
./gradlew newFeature --name=settings --tab="Настройки"     # вкладка в нижней навигации
./gradlew newFeature --name=settings --footer="Настройки"  # пункт меню в верхней панели
```

В `app/feature/settings` появятся:

| Файл                                                               | Что в нём                                                                                                    |
|--------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `build.gradle.kts`                                                 | Только `id("kmp.feature")`                                                                                   |
| `SettingsFeature.kt`                                               | Маршрут, `SettingsPlugin : FeaturePlugin` и `val settingsFeatureModule` — Koin-модуль с ViewModel и плагином |
| `SettingsContract.kt`, `SettingsViewModel.kt`, `SettingsScreen.kt` | MVI-экран: состояние, намерения, эффекты                                                                     |
| `SettingsViewModelTest.kt`                                         | Тест начального состояния                                                                                    |

Больше ничего править не нужно: после синхронизации Gradle модуль подключится сам, реестр добавит
`settingsFeatureModule` в Koin, оболочка покажет вкладку или пункт меню. Генератор не трогает файлы
вне новой папки и не перезаписывает существующую фичу.

Соглашение, на котором держится реестр: фича `files-check` объявляет `val filesCheckFeatureModule` в
пакете `ru.kaed.ozonfresh.presentation.filescheck`. Если имя другое, сборка `shared` упадёт на
сгенерированном `FeatureRegistry.kt` с понятной ошибкой «Unresolved reference».

Если на экран фичи нужно ссылаться из другой фичи, его маршрут переносится в
[
`Destinations`](../app/core/navigation/src/commonMain/kotlin/ru/kaed/ozonfresh/navigation/Destinations.kt).
Если нужен её UI или логика, в `core/navigation` объявляется интерфейс, фича кладёт реализацию в
свой Koin-модуль, другие получают его через `koinInject()`. Зависимость фичи на фичу запрещена:
`kmp.feature` её не даёт, и это держит модули независимыми.
