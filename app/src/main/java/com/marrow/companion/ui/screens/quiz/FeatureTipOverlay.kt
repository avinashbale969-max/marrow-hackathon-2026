package com.marrow.companion.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// In-memory singleton — resets on app kill (= after logout/reopen)
object FeatureTipState {
    var explanationTipShown = false
}

private val Teal      = Color(0xFF4DC8D4)
private val GreenHL   = Color(0xFF66BB6A)
private val OrangeHL  = Color(0xFFFFA726)

@Composable
fun ExplanationFeatureTip(onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(),
        exit    = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Illustration strip at top of card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Feature illustration row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFE8F5E9), Color(0xFFE0F7FA))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Green highlight swatch
                        Box(
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(GreenHL.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(24.dp).clip(CircleShape).background(GreenHL))
                        }
                        // Orange highlight swatch
                        Box(
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(OrangeHL.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(24.dp).clip(CircleShape).background(OrangeHL))
                        }
                        // Tag icon
                        Box(
                            Modifier.size(44.dp).clip(CircleShape)
                                .background(Color(0xFFFFF176).copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.StickyNote2, null,
                                tint = Color(0xFF795548), modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // Card body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Supercharge your learning!",
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF1A1A1A),
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.fillMaxWidth()
                    )

                    // Feature rows
                    TipRow(
                        color = GreenHL,
                        icon  = Icons.Filled.Highlight,
                        title = "Highlight text",
                        desc  = "Long-press any text → select → pick 🟢 or 🟠 colour"
                    )
                    TipRow(
                        color = Color(0xFF795548),
                        icon  = Icons.Filled.StickyNote2,
                        title = "Tag a reason",
                        desc  = "Select text → tap 🏷 Tag → note why you highlighted it"
                    )
                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick  = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) {
                        Text(
                            "GOT IT",
                            fontSize      = 15.sp,
                            fontWeight    = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TipRow(
    color: Color,
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(36.dp).clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A))
            Text(desc, fontSize = 12.sp, color = Color(0xFF666666), lineHeight = 17.sp)
        }
    }
}
