#!/usr/bin/env bash
# Переименовывает проект из шаблона: пакет, имя Gradle-проекта и отображаемое имя приложения.
#
#   ./rename.sh --package ru.myname.coolapp --name CoolApp [--display-name "Cool App"] [--force]
#
# Что меняет:
#   - package/import и любые упоминания com.example.app в исходниках и скриптах сборки;
#   - папки com/example/app в каждом source set (src/*/kotlin, src/*/java, build-logic);
#   - namespace и applicationId (берутся из Constants.BASE_PACKAGE), rootProject.name;
#   - desktop: mainClass, bundleID для macOS (из Constants), packageName (= rootProject.name), заголовок окна;
#   - iOS: PRODUCT_NAME и PRODUCT_BUNDLE_IDENTIFIER в iosApp/Configuration/Config.xcconfig (в bundle id _ → -),
#     имя KmpTemplate.app в project.pbxproj и схеме, CFBundleDisplayName в Info.plist;
#   - отображаемое имя: strings.xml, <title> в index.html, заголовок окна desktop, Info.plist;
#   - файлы и папки с KmpTemplate в имени.
# README.md, .github/ и сами скрипты rename не трогает.
#
# Отказывается работать, если проект уже переименован или в git есть незакоммиченные изменения (--force — всё равно).
# Нужны только bash и perl: оба есть в Git for Windows, macOS и Linux.
set -euo pipefail

OLD_PACKAGE="com.example.app"
OLD_NAME="KmpTemplate"
OLD_DISPLAY="KMP Template"

usage() {
    sed -n '2,4p' "$0" | sed 's/^# \{0,1\}//'
    exit "${1:-0}"
}

die() {
    echo "rename: $*" >&2
    exit 1
}

NEW_PACKAGE=""
NEW_NAME=""
NEW_DISPLAY=""
FORCE=0
while [ $# -gt 0 ]; do
    case "$1" in
        --package) NEW_PACKAGE="${2:-}"; shift 2 ;;
        --name) NEW_NAME="${2:-}"; shift 2 ;;
        --display-name) NEW_DISPLAY="${2:-}"; shift 2 ;;
        --force) FORCE=1; shift ;;
        -h | --help) usage 0 ;;
        *) echo "rename: неизвестный аргумент $1" >&2; usage 1 ;;
    esac
done

[ -n "$NEW_PACKAGE" ] && [ -n "$NEW_NAME" ] || usage 1
[ -n "$NEW_DISPLAY" ] || NEW_DISPLAY="$NEW_NAME"

cd "$(dirname "$0")"
command -v perl > /dev/null || die "нужен perl"

# --- проверки ввода

[[ "$NEW_PACKAGE" =~ ^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$ ]] ||
    die "пакет «$NEW_PACKAGE» не подходит: минимум два сегмента из строчных латинских букв, цифр и _, например ru.myname.coolapp"
KEYWORDS=" as break class continue do else false for fun if in interface is null object package return super this throw true try typealias typeof val var when while abstract assert boolean byte case catch char const default double enum extends final finally float goto implements import instanceof int long native new private protected public short static strictfp switch synchronized throws transient void volatile "
IFS='.' read -r -a SEGMENTS <<< "$NEW_PACKAGE"
for segment in "${SEGMENTS[@]}"; do
    if [[ "$KEYWORDS" == *" $segment "* ]]; then
        die "сегмент «$segment» — ключевое слово Kotlin/Java, в пакете его нельзя использовать"
    fi
done
[ "$NEW_PACKAGE" != "$OLD_PACKAGE" ] || die "новый пакет совпадает со старым"

[[ "$NEW_NAME" =~ ^[A-Za-z][A-Za-z0-9]*$ ]] ||
    die "имя «$NEW_NAME» не подходит: латинские буквы и цифры, с буквы, без пробелов, например CoolApp"
[[ "$NEW_DISPLAY" != *[\<\>\&\"\']* ]] || die "в отображаемом имени нельзя использовать < > & \" '"

# --- проверки состояния

MARKER="build-logic/settings/src/main/kotlin/${OLD_PACKAGE//.//}/buildlogic/Constants.kt"
[ -f "$MARKER" ] && grep -q "BASE_PACKAGE = \"$OLD_PACKAGE\"" "$MARKER" ||
    die "проект уже переименован (нет $MARKER с пакетом $OLD_PACKAGE); повторный запуск не нужен"

if [ "$FORCE" -eq 0 ] && git rev-parse --is-inside-work-tree > /dev/null 2>&1; then
    [ -z "$(git status --porcelain)" ] ||
        die "в рабочей копии есть незакоммиченные изменения: закоммитьте их или запустите с --force"
fi

OLD_PATH="${OLD_PACKAGE//.//}"
NEW_PATH="${NEW_PACKAGE//.//}"

# Папки, которые не трогаем: служебные, сборка и всё, что генерируется заново.
PRUNE=(-name .git -o -name .github -o -name .gradle -o -name .kotlin -o -name .idea -o -name build -o -name node_modules)

# --- 1. текст файлов

echo "1/3 Заменяю $OLD_PACKAGE → $NEW_PACKAGE, $OLD_NAME → $NEW_NAME, «$OLD_DISPLAY» → «$NEW_DISPLAY»"
export OLD_PACKAGE NEW_PACKAGE OLD_PATH NEW_PATH OLD_NAME NEW_NAME OLD_DISPLAY NEW_DISPLAY
changed=0
while IFS= read -r -d '' file; do
    if grep -qF -e "$OLD_PACKAGE" -e "$OLD_PATH" -e "$OLD_NAME" -e "$OLD_DISPLAY" "$file"; then
        perl -pi -e '
            s/\Q$ENV{OLD_PACKAGE}\E(?![A-Za-z0-9_])/$ENV{NEW_PACKAGE}/g;
            s/\Q$ENV{OLD_PATH}\E(?![A-Za-z0-9_])/$ENV{NEW_PATH}/g;
            s/\Q$ENV{OLD_NAME}\E/$ENV{NEW_NAME}/g;
            s/\Q$ENV{OLD_DISPLAY}\E/$ENV{NEW_DISPLAY}/g;
        ' "$file"
        changed=$((changed + 1))
    fi
done < <(find . \( "${PRUNE[@]}" \) -prune -o -type f \( \
    -name '*.kt' -o -name '*.kts' -o -name '*.java' -o -name '*.xml' -o -name '*.toml' -o -name '*.properties' \
    -o -name '*.pro' -o -name '*.html' -o -name '*.css' -o -name '*.js' -o -name '*.json' -o -name '*.md' \
    -o -name '*.yml' -o -name '*.yaml' -o -name '*.swift' -o -name '*.plist' -o -name '*.pbxproj' \
    -o -name '*.xcconfig' -o -name '*.xcscheme' -o -name '*.entitlements' -o -name '*.txt' \
    \) -not -path './README.md' -not -name 'rename.sh' -not -name 'rename.ps1' -print0)
echo "    изменено файлов: $changed"

# В bundle id iOS допустимы только буквы, цифры, точка и дефис, а в пакете Kotlin бывает подчёркивание.
XCCONFIG="iosApp/Configuration/Config.xcconfig"
if [ -f "$XCCONFIG" ]; then
    perl -pi -e 's/^(PRODUCT_BUNDLE_IDENTIFIER=)(.*)$/$1 . ($2 =~ tr{_}{-}r)/e' "$XCCONFIG"
fi

# --- 2. папки пакета

echo "2/3 Переношу $OLD_PATH → $NEW_PATH в source set'ах"
# Список собирается заранее: find не должен видеть папки, которые цикл переносит.
package_dirs=()
while IFS= read -r -d '' dir; do
    package_dirs+=("$dir")
done < <(find . \( "${PRUNE[@]}" \) -prune -o -type d -path "*/src/*/$OLD_PATH" -print0 -prune)
moved=0
for dir in ${package_dirs[@]+"${package_dirs[@]}"}; do
    root="${dir%/"$OLD_PATH"}"
    tmp="$root/.rename-tmp"
    mv "$dir" "$tmp"
    # Пустые остатки старого пути (com/example), не трогая соседние пакеты.
    parent="$(dirname "$dir")"
    while [ "$parent" != "$root" ] && rmdir "$parent" 2> /dev/null; do
        parent="$(dirname "$parent")"
    done
    target="$root/$NEW_PATH"
    if [ -d "$target" ]; then
        # Новый пакет — предок старого или его сосед с тем же именем: сливаем содержимое.
        (shopt -s dotglob && mv "$tmp"/* "$target"/)
        rmdir "$tmp"
    else
        mkdir -p "$(dirname "$target")"
        mv "$tmp" "$target"
    fi
    moved=$((moved + 1))
done
echo "    перенесено папок: $moved"

# --- 3. имена файлов и папок

echo "3/3 Переименовываю файлы и папки с $OLD_NAME в имени"
# -depth: сначала содержимое, потом сама папка, иначе пути внутри переименованной папки устареют.
named_paths=()
while IFS= read -r -d '' path; do
    named_paths+=("$path")
done < <(find . -depth -name "*$OLD_NAME*" -not -path './.git/*' -not -path './.github/*' \
    -not -path '*/build/*' -not -path '*/.gradle/*' -not -path '*/.kotlin/*' -not -path '*/.idea/*' \
    -not -path '*/node_modules/*' -not -name 'rename.sh' -not -name 'rename.ps1' -print0)
renamed=0
for path in ${named_paths[@]+"${named_paths[@]}"}; do
    base="$(basename "$path")"
    mv "$path" "$(dirname "$path")/${base//"$OLD_NAME"/"$NEW_NAME"}"
    renamed=$((renamed + 1))
done
echo "    переименовано: $renamed"

cat << EOF

Готово: $NEW_PACKAGE, проект $NEW_NAME, приложение «$NEW_DISPLAY».
Дальше:
  ./gradlew clean testAndroidHostTest   # старые сборки лежат под прежним пакетом
  перепишите README.md под свой проект
  удалите rename.sh, rename.ps1 и .github/workflows/rename.yml — они больше не нужны
EOF
