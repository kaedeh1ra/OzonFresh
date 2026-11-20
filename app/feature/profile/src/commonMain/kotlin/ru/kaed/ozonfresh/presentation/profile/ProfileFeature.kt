package ru.kaed.ozonfresh.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = padding)) {
        Row(Modifier.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(FreshColors.PinkSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text("А", fontSize = 26.sp, color = FreshColors.Pink)
            }
            Column(Modifier.padding(start = 14.dp)) {
                Text("Анна", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Демо-клиент · $address",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            listOf("Мои заказы", "Адреса доставки", "Способы оплаты", "Уведомления", "Поддержка").forEachIndexed { i, title ->
                if (i > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Text(
            "В прототипе разделы профиля не работают",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(24.dp))
        // Скрытая кнопка: выглядит как строка версии, открывает демо-панель для защиты.
        Text(
            "Версия 0.1 · прототип",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.align(Alignment.CenterHorizontally).clip(CircleShape)
                .clickable(onClick = onOpenDemo).padding(horizontal = 12.dp, vertical = 16.dp),
        )
    }
}

/** Приложение подключает модуль само: реестр фич ищет `profileFeatureModule` в этом пакете. */
val profileFeatureModule = module {
    singleOf(::ProfilePlugin) bind FeaturePlugin::class
}
