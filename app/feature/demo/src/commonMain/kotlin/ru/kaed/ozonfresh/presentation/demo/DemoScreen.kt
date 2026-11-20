package ru.kaed.ozonfresh.presentation.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import ru.kaed.ozonfresh.domain.model.ExperimentGroup
import ru.kaed.ozonfresh.domain.model.PilotEvent
import ru.kaed.ozonfresh.domain.model.PilotEventType
import ru.kaed.ozonfresh.domain.model.formatLong
import ru.kaed.ozonfresh.domain.model.formatShort
import ru.kaed.ozonfresh.domain.model.weekdayShort
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentItem
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentRules
import ru.kaed.ozonfresh.domain.replenishment.ReplenishmentStatus
import ru.kaed.ozonfresh.navigation.AppNavigator
import ru.kaed.ozonfresh.presentation.demo.DemoContract.Effect
import ru.kaed.ozonfresh.presentation.demo.DemoContract.Intent
import ru.kaed.ozonfresh.presentation.demo.DemoContract.State
import ru.kaed.ozonfresh.presentation.designsystem.components.Pill
import ru.kaed.ozonfresh.presentation.designsystem.components.ScreenHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors
import ru.kaed.ozonfresh.presentation.mvi.CollectEffects

@Composable
internal fun DemoRoute(navigator: AppNavigator) {
    val viewModel = koinViewModel<DemoViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is Effect.ShowMessage -> scope.launch { snackbar.showSnackbar(effect.text) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        DemoScreen(state, viewModel::onIntent, onBack = navigator::up)
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(8.dp))
    }
}

/** Демо-панель для защиты: управление «сегодняшней» датой, группой A/B-теста и данными, метрики пилота. */
@Composable
fun DemoScreen(state: State, onIntent: (Intent) -> Unit, onBack: () -> Unit) {
    val padding = windowSize.contentPadding
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Демо-панель", onBack)
        LazyColumn(
            contentPadding = PaddingValues(start = padding, end = padding, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "date") { DateCard(state, onIntent) }
            item(key = "group") { GroupCard(state.group, onIntent) }
            item(key = "candidates") { CandidatesCard(state, onIntent) }
            item(key = "metrics") { MetricsCard(state.metrics) }
            item(key = "log-title") {
                Text(
                    "Лог событий",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (state.events.isEmpty()) {
                item(key = "log-empty") {
                    Text(
                        "Пока пусто. Откройте главную, нажмите кнопки в ленте или оплатите заказ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.events.asReversed(), key = { "event-${it.id}" }) { EventRow(it) }
            item(key = "reset") {
                OutlinedButton(
                    onClick = { onIntent(Intent.Reset) },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, FreshColors.Pink),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshColors.Pink),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp),
                ) {
                    Text("Сбросить к исходным данным")
                }
            }
        }
    }
}

@Composable
private fun DemoCard(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content()
    }
}

@Composable
private fun DateCard(state: State, onIntent: (Intent) -> Unit) {
    val today = state.today ?: return
    DemoCard(
        "«Сегодня» в прототипе",
        "Листайте дни и смотрите, как меняется лента. " +
                "Окно показа: от ${ReplenishmentRules.DAYS_BEFORE_DUE} дн. до срока до ${ReplenishmentRules.DAYS_AFTER_DUE} дн. после.",
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundButton("‹", "Предыдущий день") { onIntent(Intent.ShiftDay(-1)) }
            Text(
                "${today.weekdayShort()}, ${today.formatLong()} ${today.year}",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            RoundButton("›", "Следующий день") { onIntent(Intent.ShiftDay(1)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            listOf(-7 to "−7 дн.", 7 to "+7 дн.").forEach { (days, label) ->
                Text(
                    label,
                    color = FreshColors.TealDark,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clip(CircleShape).clickable { onIntent(Intent.ShiftDay(days)) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun RoundButton(symbol: String, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(FreshColors.White)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 26.sp, color = FreshColors.Ink)
    }
}

@Composable
private fun GroupCard(group: ExperimentGroup, onIntent: (Intent) -> Unit) {
    DemoCard(
        "Группа A/B-теста",
        "Пилот: 15% клиентов в «Тест», 15% в «Контроль», остальные вне пилота. " +
                "«Контроль» ленту не видит, но эпизоды для него тоже считаются — так группы можно сравнить.",
    ) {
        Row(
            Modifier.fillMaxWidth().clip(CircleShape).background(FreshColors.White).padding(4.dp),
        ) {
            ExperimentGroup.entries.forEach { option ->
                val selected = option == group
                Text(
                    option.title,
                    textAlign = TextAlign.Center,
                    color = if (selected) FreshColors.White else FreshColors.Ink,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f).clip(CircleShape)
                        .background(if (selected) FreshColors.Teal else Color.Transparent)
                        .clickable { onIntent(Intent.SetGroup(option)) }
                        .padding(vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun CandidatesCard(state: State, onIntent: (Intent) -> Unit) {
    val diagnostics = state.diagnostics ?: return
    val today = state.today ?: return
    DemoCard(
        "Кандидаты на ${today.formatLong()}",
        "Товары, доставленные от ${ReplenishmentRules.MIN_DELIVERED_PURCHASES} раз. " +
                "Переключатель — наличие на складе адреса.",
    ) {
        diagnostics.candidates.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            CandidateRow(
                item = item,
                group = state.group,
                inStock = (state.stock[item.product.id] ?: 0) > 0,
                onStockChange = { onIntent(Intent.SetInStock(item.product.id, it)) },
            )
        }
        if (diagnostics.shortHistory.isNotEmpty()) {
            Text(
                "Мало покупок, в ленту не попадут: " +
                        diagnostics.shortHistory.joinToString { "${it.product.name} (${it.deliveredPurchases})" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun CandidateRow(item: ReplenishmentItem, group: ExperimentGroup, inStock: Boolean, onStockChange: (Boolean) -> Unit) {
    val forecast = item.forecast
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(item.product.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.product.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${formatDays(forecast.daysPerUnit)} дн./шт. · последняя ${forecast.lastPurchase.formatShort()} × ${forecast.lastQuantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Срок ${item.dueDate.formatShort()} · окно ${item.windowStart.formatShort()} – ${item.windowEnd.formatShort()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StatusPill(item.status, group)
        }
        Switch(
            checked = inStock,
            onCheckedChange = onStockChange,
            colors = SwitchDefaults.colors(checkedTrackColor = FreshColors.Teal),
        )
    }
}

@Composable
private fun StatusPill(status: ReplenishmentStatus, group: ExperimentGroup) {
    val (text, colors) = when (status) {
        ReplenishmentStatus.SHOWN ->
            (if (group == ExperimentGroup.TEST) "В ленте" else "Подходит, но контроль") to (FreshColors.Teal to FreshColors.White)

        ReplenishmentStatus.TOO_EARLY -> "Окно ещё не началось" to (FreshColors.White to FreshColors.Muted)
        ReplenishmentStatus.TOO_LATE -> "Окно закончилось" to (FreshColors.White to FreshColors.Muted)
        ReplenishmentStatus.OUT_OF_STOCK -> "Нет в наличии" to (FreshColors.PinkSoft to FreshColors.PinkDark)
        ReplenishmentStatus.HIDDEN_STILL_HAVE -> "Скрыт: «Ещё есть»" to (FreshColors.TealSoft to FreshColors.TealDark)
        ReplenishmentStatus.HIDDEN_LATER -> "Скрыт: «Позже»" to (FreshColors.TealSoft to FreshColors.TealDark)
    }
    Pill(text, colors.first, colors.second, Modifier.padding(top = 2.dp))
}

@Composable
private fun MetricsCard(metrics: DemoContract.Metrics) {
    DemoCard("Метрики пилота", "Считаются по логу ниже. Эпизод — одно показанное предложение набора товаров.") {
        val rows = listOf(
            "Эпизоды с показом (тест)" to metrics.shownEpisodes,
            "Эпизоды без показа (контроль)" to metrics.controlEpisodes,
            "«Добавить»" to metrics.adds,
            "«Ещё есть»" to metrics.stillHave,
            "«Позже»" to metrics.later,
            "Оплачено заказов с товарами из ленты" to metrics.ordersWithFeed,
            "Товар закончился перед оплатой" to metrics.outOfStockAtCheckout,
        )
        rows.forEach { (title, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("$value", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EventRow(event: PilotEvent) {
    val accent = when (event.type) {
        PilotEventType.FEED_SHOWN, PilotEventType.ORDER_WITH_FEED -> FreshColors.Teal
        PilotEventType.ADD -> FreshColors.Pink
        PilotEventType.OUT_OF_STOCK_AT_CHECKOUT -> FreshColors.PinkDark
        else -> FreshColors.Muted
    }
    Row(Modifier.fillMaxWidth()) {
        Box(Modifier.padding(top = 6.dp, end = 10.dp).size(8.dp).clip(CircleShape).background(accent))
        Column(Modifier.weight(1f)) {
            val episode = event.episode?.let { " · эпизод $it" }.orEmpty()
            Text(
                "#${event.id} · ${event.day.formatShort()} · ${event.group.title}$episode",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(event.type.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(event.details, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** 7.0 → «7», 21.5 → «21,5». */
private fun formatDays(value: Double): String {
    val tenths = kotlin.math.round(value * 10).toLong()
    return if (tenths % 10 == 0L) "${tenths / 10}" else "${tenths / 10},${tenths % 10}"
}
