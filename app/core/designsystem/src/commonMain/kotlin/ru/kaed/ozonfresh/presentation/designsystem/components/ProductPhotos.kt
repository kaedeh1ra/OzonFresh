package ru.kaed.ozonfresh.presentation.designsystem.components

import org.jetbrains.compose.resources.DrawableResource
import ru.kaed.ozonfresh.presentation.designsystem.resources.Res
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_apples
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_avocado
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_baguette
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_bananas
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_bread
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_buckwheat
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_butter
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_capsules
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_catfood
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_cheese
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_coffee
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_cottage
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_croissant
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_cucumbers
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_eggs
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_juice
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_kefir
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_litter
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_milk
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_oats
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_pasta
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_sponges
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_tea
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_tissue
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_tomatoes
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_water
import ru.kaed.ozonfresh.presentation.designsystem.resources.product_yogurt
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_faq
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_fav
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_new
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_past
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_sale
import ru.kaed.ozonfresh.presentation.designsystem.resources.story_soon
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_bakery
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_breakfast
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_coffee
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_combo
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_dairy
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_desserts
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_dishes
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_drinks
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_eatsoon
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_fruits
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_grocery
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_healthy
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_hits
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_hotdrinks
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_household
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_mains
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_new
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_pets
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_salads
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_sale
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_soups
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_street
import ru.kaed.ozonfresh.presentation.designsystem.resources.tile_taste

/**
 * Фото товаров по id из моков. Фото с Wikimedia Commons под свободными лицензиями, авторы —
 * в PHOTO_CREDITS.md модуля. Товары без фото показываются эмодзи.
 */
private val PRODUCT_PHOTOS: Map<String, DrawableResource> = mapOf(
    "apples" to Res.drawable.product_apples,
    "avocado" to Res.drawable.product_avocado,
    "baguette" to Res.drawable.product_baguette,
    "bananas" to Res.drawable.product_bananas,
    "bread" to Res.drawable.product_bread,
    "buckwheat" to Res.drawable.product_buckwheat,
    "butter" to Res.drawable.product_butter,
    "capsules" to Res.drawable.product_capsules,
    "catfood" to Res.drawable.product_catfood,
    "cheese" to Res.drawable.product_cheese,
    "coffee" to Res.drawable.product_coffee,
    "cottage" to Res.drawable.product_cottage,
    "croissant" to Res.drawable.product_croissant,
    "cucumbers" to Res.drawable.product_cucumbers,
    "eggs" to Res.drawable.product_eggs,
    "juice" to Res.drawable.product_juice,
    "kefir" to Res.drawable.product_kefir,
    "litter" to Res.drawable.product_litter,
    "milk" to Res.drawable.product_milk,
    "oats" to Res.drawable.product_oats,
    "pasta" to Res.drawable.product_pasta,
    "sponges" to Res.drawable.product_sponges,
    "tea" to Res.drawable.product_tea,
    "tissue" to Res.drawable.product_tissue,
    "tomatoes" to Res.drawable.product_tomatoes,
    "water" to Res.drawable.product_water,
    "yogurt" to Res.drawable.product_yogurt,
)

fun productPhoto(productId: String): DrawableResource? = PRODUCT_PHOTOS[productId]

/**
 * Картинки плиток разделов и подборок: предметы без фона, как на плитках приложения. Ключ — id раздела
 * из моков или название плитки («Новинки», «Супы»). Источник и авторы — в PHOTO_CREDITS.md модуля.
 */
private val TILE_PHOTOS: Map<String, DrawableResource> = mapOf(
    "dairy" to Res.drawable.tile_dairy,
    "bakery" to Res.drawable.tile_bakery,
    "fruits" to Res.drawable.tile_fruits,
    "grocery" to Res.drawable.tile_grocery,
    "drinks" to Res.drawable.tile_drinks,
    "coffee" to Res.drawable.tile_coffee,
    "pets" to Res.drawable.tile_pets,
    "household" to Res.drawable.tile_household,
    "Хиты недели" to Res.drawable.tile_hits,
    "Новинки" to Res.drawable.tile_new,
    "Здоровое питание" to Res.drawable.tile_healthy,
    "Скидки" to Res.drawable.tile_sale,
    "Съешьте скорее" to Res.drawable.tile_eatsoon,
    "Блюда" to Res.drawable.tile_dishes,
    "Напитки" to Res.drawable.tile_hotdrinks,
    "Найдите свой вкус" to Res.drawable.tile_taste,
    "Комбо" to Res.drawable.tile_combo,
    "Завтраки" to Res.drawable.tile_breakfast,
    "Вторые блюда" to Res.drawable.tile_mains,
    "Стритфуд" to Res.drawable.tile_street,
    "Супы" to Res.drawable.tile_soups,
    "Закуски и салаты" to Res.drawable.tile_salads,
    "Десерты" to Res.drawable.tile_desserts,
)

fun tilePhoto(key: String): DrawableResource? = TILE_PHOTOS[key]

/**
 * Картинки «историй» под поиском: 3D-эмодзи Microsoft Fluent Emoji (лицензия MIT), в объёмном стиле, как иконки
 * историй приложения. Ключ — название истории в одну строку.
 */
private val STORY_IMAGES: Map<String, DrawableResource> = mapOf(
    "Все скидки и акции" to Res.drawable.story_sale,
    "Вкусные новинки" to Res.drawable.story_new,
    "Прошлые покупки" to Res.drawable.story_past,
    "Съешьте скорее" to Res.drawable.story_soon,
    "Любимые товары" to Res.drawable.story_fav,
    "Вопросы и ответы" to Res.drawable.story_faq,
)

fun storyImage(title: String): DrawableResource? = STORY_IMAGES[title.replace("\n", " ")]
