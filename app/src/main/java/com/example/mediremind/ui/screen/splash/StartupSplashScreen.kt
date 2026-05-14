package com.example.mediremind.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediremind.ui.theme.MediCream
import com.example.mediremind.ui.theme.MediInk
import com.example.mediremind.ui.theme.MediPrimary
import com.example.mediremind.ui.theme.MediPrimaryDark
import com.example.mediremind.ui.theme.MediPrimaryLight

@Composable
fun StartupSplashScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MediPrimaryLight,
                        MediCream,
                        Color(0xFFFFF7EF)
                    )
                )
            )
            .padding(28.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 42.dp, y = (-52).dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(MediPrimary.copy(alpha = 0.16f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-62).dp, y = 72.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(MediPrimaryDark.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashPillBowl()
            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "TIME FOR YOUR PILLS.",
                color = MediInk,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.8.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "MediRemind",
                color = MediPrimaryDark,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun SplashPillBowl() {
    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(166.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                FloatingTablet(
                    modifier = Modifier.offset(x = (-32).dp, y = (-28).dp),
                    rotation = -16f
                )
                FloatingCapsule(
                    modifier = Modifier.offset(x = 36.dp, y = (-12).dp),
                    rotation = 34f,
                    firstColor = Color(0xFFF472B6),
                    secondColor = Color(0xFFEC4899)
                )
                FloatingCapsule(
                    modifier = Modifier.offset(x = 4.dp, y = 40.dp),
                    rotation = -33f,
                    firstColor = Color(0xFFBAE6FD),
                    secondColor = Color(0xFF38BDF8)
                )
            }
        }
    }
}

@Composable
private fun FloatingTablet(
    modifier: Modifier = Modifier,
    rotation: Float
) {
    Surface(
        modifier = modifier
            .size(width = 96.dp, height = 52.dp)
            .graphicsLayer(rotationZ = rotation),
        shape = RoundedCornerShape(50),
        color = Color(0xFFF8FAFC),
        shadowElevation = 7.dp
    ) {}
}

@Composable
private fun FloatingCapsule(
    modifier: Modifier = Modifier,
    rotation: Float,
    firstColor: Color,
    secondColor: Color
) {
    Surface(
        modifier = modifier
            .size(width = 104.dp, height = 46.dp)
            .graphicsLayer(rotationZ = rotation),
        shape = RoundedCornerShape(50),
        color = secondColor,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 52.dp, height = 46.dp)
                    .clip(RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp))
                    .background(firstColor)
            )
        }
    }
}
