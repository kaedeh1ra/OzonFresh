package ru.kaed.ozonfresh.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import org.koin.compose.koinInject
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.kaed.ozonfresh.domain.usecase.shop.ObserveTodayUseCase
import ru.kaed.ozonfresh.navigation.Destinations
import ru.kaed.ozonfresh.navigation.FeatureEntry
import ru.kaed.ozonfresh.navigation.FeatureHost
import ru.kaed.ozonfresh.navigation.FeaturePlugin
import ru.kaed.ozonfresh.navigation.screen
import ru.kaed.ozonfresh.presentation.designsystem.components.NavIcon
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopHeader
import ru.kaed.ozonfresh.presentation.designsystem.components.ShopSection
import ru.kaed.ozonfresh.presentation.designsystem.components.contentPadding
import ru.kaed.ozonfresh.presentation.designsystem.components.windowSize
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

/** Профиль — заглушка. Строка версии внизу — скрытый вход в демо-панель. */
internal class ProfilePlugin : FeaturePlugin {
    override val routes = listOf(Destinations.PROFILE)
    override val entry = FeatureEntry.Tab("Профиль", NavIcon.Profile, order = 3)

    override fun NavGraphBuilder.register(host: FeatureHost) {
        screen(Destinations.PROFILE) {
            val today = koinInject<ObserveTodayUseCase>()
            ProfileScreen(address = today.address, onOpenDemo = { host.navigator.push(Destinations.DEMO) })
        }
    }
}

@Composable
fun ProfileScreen(address: String, onOpenDemo: () -> Unit) {
    val padding = windowSize.contentPadding
    Column(Modifier.fillMaxSize().background(FreshColors.Canvas).verticalScroll(rememberScrollState())) {
        ShopHeader(address)
        ShopSection(onDark = true) {
            Row(Modifier.padding(horizontal = padding), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(FreshColors.PinkSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("А", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = FreshColors.Pink)
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Анна", style = MaterialTheme.typography.headlineSmall)
                    Text("Демо-клиент", style = MaterialTheme.typography.bodyLarge, color = FreshColors.Muted)
                }
            }
        }
        ShopSection {
            listOf("Мои заказы", "Адреса доставки", "Способы оплаты", "Уведомления", "Поддержка").forEachIndexed { i, title ->
                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = padding), color = FreshColors.Line)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = padding, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text("›", style = MaterialTheme.typography.titleLarge, color = FreshColors.Muted)
                }
            }
        }
        Text(
            "В прототипе разделы профиля не работают",
            style = MaterialTheme.typography.bodyMedium,
            color = FreshColors.Muted,
            modifier = Modifier.padding(horizontal = padding, vertical = 4.dp),
        )
        // Скрытая кнопка: выглядит как строка версии, открывает демо-панель для защиты.
        Text(
            "Версия 0.1 · прототип",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp).clip(CircleShape)
                .clickable(onClick = onOpenDemo).padding(horizontal = 12.dp, vertical = 16.dp),
        )
    }
}

/** Приложение подключает модуль само: реестр фич ищет `profileFeatureModule` в этом пакете. */
val profileFeatureModule = module {
    singleOf(::ProfilePlugin) bind FeaturePlugin::class
}
