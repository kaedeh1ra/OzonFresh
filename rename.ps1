<#
.SYNOPSIS
Переименовывает проект из шаблона: пакет, имя Gradle-проекта и отображаемое имя приложения.

.DESCRIPTION
То же, что rename.sh, для Windows PowerShell 5.1+ и PowerShell 7:
  - package/import и любые упоминания com.example.app в исходниках и скриптах сборки;
  - папки com/example/app в каждом source set (src/*/kotlin, src/*/java, build-logic);
  - namespace и applicationId (берутся из Constants.BASE_PACKAGE), rootProject.name;
  - desktop: mainClass, bundleID для macOS (из Constants), packageName (= rootProject.name), заголовок окна;
  - iOS: PRODUCT_NAME и PRODUCT_BUNDLE_IDENTIFIER в iosApp/Configuration/Config.xcconfig (в bundle id _ → -),
    имя KmpTemplate.app в project.pbxproj и схеме, CFBundleDisplayName в Info.plist;
  - отображаемое имя: strings.xml, <title> в index.html, заголовок окна desktop, Info.plist;
  - файлы и папки с KmpTemplate в имени.
README.md, .github/ и сами скрипты rename не трогает.
Отказывается работать, если проект уже переименован или в git есть незакоммиченные изменения (-Force — всё равно).

.EXAMPLE
.\rename.ps1 -Package ru.myname.coolapp -Name CoolApp -DisplayName "Cool App"
#>
param(
    [Parameter(Mandatory = $true)][string]$Package,
    [Parameter(Mandatory = $true)][string]$Name,
    [string]$DisplayName = "",
    [switch]$Force
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$OldPackage = "com.example.app"
$OldName = "KmpTemplate"
$OldDisplay = "KMP Template"

function Fail([string]$message) {
    [Console]::Error.WriteLine("rename: $message")
    exit 1
}

if (-not $DisplayName) { $DisplayName = $Name }
$root = $PSScriptRoot
Set-Location $root

# --- проверки ввода

if ($Package -cnotmatch '^[a-z][a-z0-9_]*(\.[a-z][a-z0-9_]*)+$') {
    Fail "пакет «$Package» не подходит: минимум два сегмента из строчных латинских букв, цифр и _, например ru.myname.coolapp"
}
$keywords = @("as", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "interface", "is",
    "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias", "typeof", "val", "var",
    "when", "while", "abstract", "assert", "boolean", "byte", "case", "catch", "char", "const", "default", "double",
    "enum", "extends", "final", "finally", "float", "goto", "implements", "import", "instanceof", "int", "long",
    "native", "new", "private", "protected", "public", "short", "static", "strictfp", "switch", "synchronized",
    "throws", "transient", "void", "volatile")
foreach ($segment in $Package.Split('.')) {
    if ($keywords -ccontains $segment) {
        Fail "сегмент «$segment» — ключевое слово Kotlin/Java, в пакете его нельзя использовать"
    }
}
if ($Package -ceq $OldPackage) { Fail "новый пакет совпадает со старым" }
if ($Name -cnotmatch '^[A-Za-z][A-Za-z0-9]*$') {
    Fail "имя «$Name» не подходит: латинские буквы и цифры, с буквы, без пробелов, например CoolApp"
}
if ($DisplayName -match "[<>&'`"]") { Fail "в отображаемом имени нельзя использовать < > & `" '" }

# --- проверки состояния

$OldPath = $OldPackage.Replace('.', '/')
$NewPath = $Package.Replace('.', '/')
$marker = Join-Path $root "build-logic/settings/src/main/kotlin/$OldPath/buildlogic/Constants.kt"
if (-not (Test-Path -LiteralPath $marker) -or
        -not (Select-String -LiteralPath $marker -SimpleMatch "BASE_PACKAGE = `"$OldPackage`"" -Quiet)) {
    Fail "проект уже переименован (нет $marker с пакетом $OldPackage); повторный запуск не нужен"
}

if (-not $Force -and (Get-Command git -ErrorAction SilentlyContinue)) {
    # В Windows PowerShell 5.1 вывод в stderr при ErrorActionPreference=Stop стал бы исключением.
    $ErrorActionPreference = "Continue"
    $inside = & git rev-parse --is-inside-work-tree 2> $null
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE -eq 0 -and $inside -eq "true") {
        $status = & git status --porcelain
        if ($status) { Fail "в рабочей копии есть незакоммиченные изменения: закоммитьте их или запустите с -Force" }
    }
}

# Папки, которые не трогаем: служебные, сборка и всё, что генерируется заново.
$skipDirs = @(".git", ".github", ".gradle", ".kotlin", ".idea", "build", "node_modules")
$textExtensions = @(".kt", ".kts", ".java", ".xml", ".toml", ".properties", ".pro", ".html", ".css", ".js", ".json",
    ".md", ".yml", ".yaml", ".swift", ".plist", ".pbxproj", ".xcconfig", ".xcscheme", ".entitlements", ".txt")
$skipFiles = @("rename.sh", "rename.ps1")

function Get-ProjectItems([string]$dir) {
    foreach ($item in Get-ChildItem -LiteralPath $dir -Force) {
        if ($item.PSIsContainer) {
            if ($skipDirs -notcontains $item.Name) {
                $item
                Get-ProjectItems $item.FullName
            }
        } else {
            $item
        }
    }
}

$utf8 = New-Object System.Text.UTF8Encoding($false)

# --- 1. текст файлов

Write-Host "1/3 Заменяю $OldPackage → $Package, $OldName → $Name, «$OldDisplay» → «$DisplayName»"
$readme = Join-Path $root "README.md"
$rules = @(
    @{ Pattern = [regex]::Escape($OldPackage) + '(?![A-Za-z0-9_])'; Value = $Package },
    @{ Pattern = [regex]::Escape($OldPath) + '(?![A-Za-z0-9_])'; Value = $NewPath },
    @{ Pattern = [regex]::Escape($OldName); Value = $Name },
    @{ Pattern = [regex]::Escape($OldDisplay); Value = $DisplayName }
)
$changed = 0
$files = Get-ProjectItems $root | Where-Object {
    -not $_.PSIsContainer -and
            $textExtensions -contains $_.Extension.ToLowerInvariant() -and
            $skipFiles -notcontains $_.Name -and
            $_.FullName -ne $readme
}
foreach ($file in $files) {
    $text = [System.IO.File]::ReadAllText($file.FullName, $utf8)
    $updated = $text
    foreach ($rule in $rules) {
        # $ в замене — ссылка на группу, поэтому экранируется.
        $updated = [regex]::Replace($updated, $rule.Pattern, $rule.Value.Replace('$', '$$'))
    }
    if ($updated -cne $text) {
        [System.IO.File]::WriteAllText($file.FullName, $updated, $utf8)
        $changed++
    }
}
Write-Host "    изменено файлов: $changed"

# В bundle id iOS допустимы только буквы, цифры, точка и дефис, а в пакете Kotlin бывает подчёркивание.
$xcconfig = Join-Path $root "iosApp/Configuration/Config.xcconfig"
if (Test-Path -LiteralPath $xcconfig) {
    $text = [System.IO.File]::ReadAllText($xcconfig, $utf8)
    $lines = $text -split "`n"
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i].StartsWith("PRODUCT_BUNDLE_IDENTIFIER=")) {
            $lines[$i] = "PRODUCT_BUNDLE_IDENTIFIER=" + $lines[$i].Substring("PRODUCT_BUNDLE_IDENTIFIER=".Length).Replace('_', '-')
        }
    }
    $fixed = $lines -join "`n"
    if ($fixed -cne $text) { [System.IO.File]::WriteAllText($xcconfig, $fixed, $utf8) }
}

# --- 2. папки пакета

Write-Host "2/3 Переношу $OldPath → $NewPath в source set'ах"
$oldSuffix = [System.IO.Path]::DirectorySeparatorChar + $OldPath.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
# Список собирается заранее: обход не должен видеть папки, которые цикл переносит.
$packageDirs = @(Get-ProjectItems $root | Where-Object {
    $_.PSIsContainer -and $_.FullName.EndsWith($oldSuffix) -and
            ($_.FullName.Substring(0, $_.FullName.Length - $oldSuffix.Length) -match '[\\/]src[\\/][^\\/]+[\\/][^\\/]+$')
} | ForEach-Object { $_.FullName })
$moved = 0
foreach ($dir in $packageDirs) {
    $sourceRoot = $dir.Substring(0, $dir.Length - $oldSuffix.Length)
    $tmp = Join-Path $sourceRoot ".rename-tmp"
    Move-Item -LiteralPath $dir -Destination $tmp
    # Пустые остатки старого пути (com/example), не трогая соседние пакеты.
    $parent = Split-Path $dir -Parent
    while ($parent -ne $sourceRoot -and -not (Get-ChildItem -LiteralPath $parent -Force)) {
        Remove-Item -LiteralPath $parent
        $parent = Split-Path $parent -Parent
    }
    $target = Join-Path $sourceRoot $NewPath
    if (Test-Path -LiteralPath $target) {
        # Новый пакет — предок старого или его сосед с тем же именем: сливаем содержимое.
        Get-ChildItem -LiteralPath $tmp -Force | Move-Item -Destination $target
        Remove-Item -LiteralPath $tmp
    } else {
        New-Item -ItemType Directory -Force -Path (Split-Path $target -Parent) | Out-Null
        Move-Item -LiteralPath $tmp -Destination $target
    }
    $moved++
}
Write-Host "    перенесено папок: $moved"

# --- 3. имена файлов и папок

Write-Host "3/3 Переименовываю файлы и папки с $OldName в имени"
# Сначала самые глубокие пути, иначе пути внутри переименованной папки устареют.
$named = @(Get-ProjectItems $root | Where-Object {
    $_.Name.Contains($OldName) -and $skipFiles -notcontains $_.Name
} | Sort-Object { $_.FullName.Length } -Descending | ForEach-Object { $_.FullName })
foreach ($path in $named) {
    Rename-Item -LiteralPath $path -NewName ((Split-Path $path -Leaf).Replace($OldName, $Name))
}
Write-Host "    переименовано: $($named.Count)"

Write-Host @"

Готово: $Package, проект $Name, приложение «$DisplayName».
Дальше:
  .\gradlew clean testAndroidHostTest   # старые сборки лежат под прежним пакетом
  перепишите README.md под свой проект
  удалите rename.sh, rename.ps1 и .github/workflows/rename.yml — они больше не нужны
"@
