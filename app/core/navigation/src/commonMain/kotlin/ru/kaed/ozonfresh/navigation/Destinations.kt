package ru.kaed.ozonfresh.navigation

/**
 * Маршруты, на которые ссылаются из других модулей: оболочка и чужие фичи.
 * Внутренние маршруты фича держит у себя, поэтому новая фича этот файл не правит.
 */
object Destinations {
    /** Стартовый экран. */
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"

    /** Демо-панель для защиты: открывается долгим нажатием на адрес или скрытой кнопкой в профиле. */
    const val DEMO = "demo"

    const val PRODUCT = "product/{id}"
    fun product(id: String) = "product/$id"

    const val CATALOG_CATEGORY = "catalog/{category}"
    fun catalogCategory(id: String) = "catalog/$id"
}
