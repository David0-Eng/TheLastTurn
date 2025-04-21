import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import com.example.thelastturn.R

@Composable
fun WoodBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF8B4513), Color(0xFFD2B48C)), // Degradado marrón
                    tileMode = TileMode.Repeated
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_thelastturn),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}