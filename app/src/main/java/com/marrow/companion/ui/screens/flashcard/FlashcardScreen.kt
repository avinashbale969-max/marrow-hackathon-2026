package com.marrow.companion.ui.screens.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(viewModel: FlashcardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Flashcards") }) }) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.isFinished -> FinishedState(modifier = Modifier.padding(padding))
            else -> {
                val card = state.current ?: return@Scaffold
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        "${state.currentIndex + 1} / ${state.dueCards.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlipCard(
                        front = card.flashcard.front,
                        back = card.flashcard.back,
                        isFlipped = state.isFlipped,
                        onFlip = { viewModel.flip() },
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    )

                    if (state.isFlipped) {
                        RatingRow(onRate = { viewModel.rate(it) })
                    } else {
                        Text("Tap card to reveal answer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(
    front: String,
    back: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "flip"
    )

    ElevatedCard(
        modifier = modifier.graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable { onFlip() }
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                Text(front, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            } else {
                Text(
                    back,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

@Composable
private fun RatingRow(onRate: (ReviewRating) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RatingButton("Again", MaterialTheme.colorScheme.error) { onRate(ReviewRating.AGAIN) }
        RatingButton("Hard", MaterialTheme.colorScheme.tertiary) { onRate(ReviewRating.HARD) }
        RatingButton("Good", MaterialTheme.colorScheme.primary) { onRate(ReviewRating.GOOD) }
        RatingButton("Easy", MaterialTheme.colorScheme.secondary) { onRate(ReviewRating.EASY) }
    }
}

@Composable
private fun RowScope.RatingButton(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
}

@Composable
private fun FinishedState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("All done for today!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Come back tomorrow for new cards.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}