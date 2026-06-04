package com.marrow.companion.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.SubjectEntity
import com.marrow.companion.ui.screens.subjects.SubjectsViewModel

private val Teal      = Color(0xFF4DC8D4)
private val TealLight = Color(0xFFE0F7FA)
private val Gray      = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onSubjectClick: (Long, String) -> Unit = { _, _ -> },
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Video Edition", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Menu, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Teal,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Quick cards ─────────────────────────────────────────────
            item(span = { GridItemSpan(2) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VideoQuickCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.Bookmark,
                        title    = "Saved Videos",
                        subtitle = "0/15 Saved",
                        onClick  = {}
                    )
                    VideoQuickCard(
                        modifier = Modifier.weight(1f),
                        icon     = Icons.Filled.PlayCircle,
                        title    = "Sample Videos",
                        subtitle = "Explore",
                        onClick  = {}
                    )
                }
            }

            // ── Section header ───────────────────────────────────────────
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SUBJECTS", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF888888), letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Sort by", fontSize = 11.sp, color = Color(0xFF888888))
                        Text("Default", fontSize = 11.sp, color = Teal, fontWeight = FontWeight.SemiBold)
                        Icon(Icons.Filled.ArrowDropDown, null, tint = Teal,
                            modifier = Modifier.size(14.dp))
                    }
                }
            }

            // ── Subject cards ────────────────────────────────────────────
            items(subjects) { subject ->
                VideoSubjectCard(
                    subject  = subject,
                    onClick  = { onSubjectClick(subject.id, subject.name) }
                )
            }
        }
    }
}

@Composable
private fun VideoQuickCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick    = onClick,
        modifier   = modifier,
        shape      = RoundedCornerShape(12.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White),
        elevation  = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = Teal, modifier = Modifier.size(22.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1A1A1A))
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun VideoSubjectCard(subject: SubjectEntity, onClick: () -> Unit) {
    val icon = videoSubjectIcon(subject.name)
    val progress = 0f  // demo — would come from attempts

    Card(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth().height(140.dp),
        shape      = RoundedCornerShape(12.dp),
        colors     = CardDefaults.cardColors(containerColor = Color.White),
        elevation  = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon circle
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Teal, modifier = Modifier.size(28.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(subject.name, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = Color(0xFF1A1A1A), lineHeight = 18.sp)
                Text("${subject.totalQuestions} modules", fontSize = 11.sp, color = Color.Gray)
                LinearProgressIndicator(
                    progress    = { progress },
                    modifier    = Modifier.fillMaxWidth().height(3.dp)
                        .clip(RoundedCornerShape(50)),
                    color       = Teal,
                    trackColor  = Color(0xFFE0E0E0)
                )
            }
        }
    }
}

private fun videoSubjectIcon(name: String): ImageVector = when {
    name.contains("Anat",      ignoreCase = true) -> Icons.Filled.AccessibilityNew
    name.contains("Biochem",   ignoreCase = true) -> Icons.Filled.Science
    name.contains("Physio",    ignoreCase = true) -> Icons.Filled.MonitorHeart
    name.contains("Pharm",     ignoreCase = true) -> Icons.Filled.Medication
    name.contains("Micro",     ignoreCase = true) -> Icons.Filled.Biotech
    name.contains("Path",      ignoreCase = true) -> Icons.Filled.Biotech
    name.contains("Community", ignoreCase = true) ||
    name.contains("PSM",       ignoreCase = true) -> Icons.Filled.Groups
    name.contains("Forensic",  ignoreCase = true) -> Icons.Filled.Gavel
    name.contains("Ophthal",   ignoreCase = true) -> Icons.Filled.RemoveRedEye
    name.contains("ENT",       ignoreCase = true) -> Icons.Filled.Hearing
    name.contains("Anaes",     ignoreCase = true) -> Icons.Filled.MedicalServices
    name.contains("Derm",      ignoreCase = true) -> Icons.Filled.Face
    name.contains("Psych",     ignoreCase = true) -> Icons.Filled.Psychology
    name.contains("Radio",     ignoreCase = true) -> Icons.Filled.MedicalServices
    name.contains("Med",       ignoreCase = true) -> Icons.Filled.MedicalServices
    name.contains("Surg",      ignoreCase = true) -> Icons.Filled.ContentCut
    name.contains("OBG",       ignoreCase = true) -> Icons.Filled.ChildFriendly
    name.contains("Pedi",      ignoreCase = true) -> Icons.Filled.ChildCare
    else                                           -> Icons.Filled.MenuBook
}
