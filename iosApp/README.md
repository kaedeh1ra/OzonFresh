# Как поставить Ozon Fresh на iPhone

Приложения нет в App Store, поэтому его нужно собрать на Mac и через Xcode установить на телефон по кабелю.
Платный аккаунт разработчика не нужен, хватит обычного Apple ID. С бесплатным аккаунтом приложение
работает 7 дней. Потом его нужно заново поставить из Xcode (шаг 6), данные при этом сохранятся.

Понадобятся Mac, iPhone и кабель.

## 1. Xcode

Поставьте **Xcode 16 или новее** из App Store и запустите его один раз, чтобы он доустановил компоненты.
Потом в Терминале:

```bash
sudo xcode-select -s /Applications/Xcode.app
xcodebuild -runFirstLaunch
xcodebuild -version
```

Последняя команда должна показать `Xcode 16.x` или выше. Если на iPhone iOS новее, чем знает ваш Xcode,
обновите Xcode, иначе он не увидит телефон.

## 2. Homebrew и Java

Нужна Java 21: на ней работает сборка Kotlin. Если Homebrew ещё нет (команда `brew -v` выдаёт ошибку),
поставьте его командой с [brew.sh](https://brew.sh):

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

После установки Homebrew выведет в конце две команды `eval ...`. Выполните их, затем поставьте Java:

```bash
brew install --cask zulu@21
java -version
```

`java -version` должна показать `21`.

## 3. Скачать проект

```bash
cd ~
git clone https://github.com/kaedeh1ra/OzonFresh.git
cd OzonFresh
chmod +x gradlew
```

## 4. Проверочная сборка Kotlin-части

Шаг необязательный, но с ним проще понять, где ошибка, если что-то пойдёт не так. Первый запуск идёт
долго, 10–20 минут: Gradle скачивает себя, JDK и компилятор Kotlin/Native.

```bash
./gradlew :app:shared:linkDebugFrameworkIosArm64
```

В конце должно быть `BUILD SUCCESSFUL`.

## 5. Подписать приложение своим Apple ID

```bash
open iosApp/iosApp.xcodeproj
```

В Xcode:

1. **Xcode → Settings… → Accounts**, внизу **+** → **Apple ID** и войдите.
2. Слева в дереве файлов кликните синюю иконку **iosApp**, затем в середине выберите таргет **iosApp**
   и вкладку **Signing & Capabilities**.
3. В поле **Team** выберите `Ваше Имя (Personal Team)`.

Если Xcode пишет, что bundle identifier `ru.kaed.ozonfresh` недоступен, поменяйте его на свой
(вместо `myname` подставьте любое слово латиницей):

```bash
sed -i '' 's/ru.kaed.ozonfresh/ru.myname.ozonfresh/' iosApp/Configuration/Config.xcconfig
```

После этого вернитесь в Xcode, там всё обновится само.

## 6. Установить на iPhone

1. Подключите iPhone кабелем, разблокируйте его и нажмите **«Доверять этому компьютеру»**.
2. Включите режим разработчика: на iPhone **Настройки → Конфиденциальность и безопасность →
   Режим разработчика** → включить, телефон перезагрузится. Пункт появляется только после того,
   как iPhone подключили к Mac с открытым Xcode.
3. В Xcode вверху, в списке устройств рядом со схемой `iosApp`, выберите свой iPhone.
4. Нажмите **▶ Run** (или `⌘R`). Первая сборка займёт несколько минут.
5. При первом запуске iPhone покажет «Ненадёжный разработчик». Откройте **Настройки → Основные →
   VPN и управление устройством**, выберите свой Apple ID и нажмите **«Доверять»**. Потом откройте
   приложение **Ozon Fresh** с рабочего стола.

После установки кабель можно отключить, приложение останется на телефоне.

## Без телефона: в симуляторе

Только на Mac с Apple Silicon (M1 и новее), Apple ID не нужен. Если симулятора iOS ещё нет:

```bash
xcodebuild -downloadPlatform iOS
```

Затем в Xcode выберите вверху любой iPhone из списка симуляторов и нажмите **▶ Run**.

## Если что-то не так

| Что видно | Что сделать |
|---|---|
| `./gradlew: Permission denied` | `chmod +x gradlew` в папке проекта |
| `Unable to locate a Java Runtime` | Повторить шаг 2, проверить `java -version` |
| В Xcode ошибка в фазе `Compile Kotlin Framework` | Запустить шаг 4 в Терминале и посмотреть ошибку там |
| `Failed to register bundle identifier` | Сменить bundle id командой `sed` из шага 5 |
| iPhone нет в списке устройств | Разблокировать телефон, «Доверять», проверить режим разработчика, переподключить кабель |
| Через неделю приложение не открывается | Срок бесплатной подписи истёк: подключить iPhone и снова нажать **▶ Run** |

Обновить приложение до свежей версии:

```bash
cd ~/OzonFresh
git pull
```

После этого снова **▶ Run** в Xcode.
