package ru.kaed.ozonfresh.presentation.cart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.kaed.ozonfresh.domain.model.rub
import ru.kaed.ozonfresh.presentation.designsystem.components.SheetScreen
import ru.kaed.ozonfresh.presentation.designsystem.theme.FreshColors

@Composable
fun OrderDoneScreen(orderId: String, total: Int, onHome: () -> Unit) {
    SheetScreen {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Canvas(Modifier.size(96.dp)) {
                drawCircle(FreshColors.Teal)
                val w = size.width
                val check = Path().apply {
                    moveTo(w * 0.3f, w * 0.52f)
                    lineTo(w * 0.45f, w * 0.66f)
                    lineTo(w * 0.72f, w * 0.38f)
                }
                drawPath(check, Color.White, style = Stroke(w * 0.08f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(Color.White.copy(alpha = 0.15f), radius = w * 0.42f, center = Offset(w / 2, w / 2), style = Stroke(w * 0.02f))
            }
            Text(
                "Заказ оформлен",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                "Заказ $orderId на ${total.rub()}\nСоберём и привезём за 15–30 минут",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onHome,
                shape = CircleShape,
                modifier = Modifier.padding(top = 28.dp).widthIn(max = 360.dp).fillMaxWidth().height(52.dp),
            ) {
                Text("На главную")
            }
        }
    }
}
