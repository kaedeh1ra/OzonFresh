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
