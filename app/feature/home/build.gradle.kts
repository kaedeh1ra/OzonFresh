plugins {
    id("kmp.feature")
}

// Зависимости на core-модули, Koin и навигацию добавляет плагин kmp.feature.
// Свои библиотеки фичи — здесь, в kotlin { sourceSets { commonMain.dependencies { ... } } }.
