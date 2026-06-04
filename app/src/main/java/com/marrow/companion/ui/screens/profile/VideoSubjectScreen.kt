package com.marrow.companion.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal      = Color(0xFF4DC8D4)
private val GoldBadge = Color(0xFFF9A825)

data class VideoLesson(
    val title: String,
    val duration: String,
    val topic: String,
    val isPro: Boolean = true,
    val isCompleted: Boolean = false
)

data class VideoSection(val name: String, val lessons: List<VideoLesson>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoSubjectScreen(
    subjectId: Long,
    subjectName: String,
    onBack: () -> Unit,
    onLessonClick: (VideoLesson) -> Unit
) {
    val sections = dummyLessons(subjectName)
    val totalLessons = sections.sumOf { it.lessons.size }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subjectName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.FormatListBulleted, null,
                            tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Index", fontSize = 13.sp, color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = Teal,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Filter tabs ───────────────────────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth().background(Teal)) {
                    listOf("All", "Paused", "Completed", "Unattempted").forEachIndexed { i, label ->
                        val sel = i == selectedTab
                        Column(
                            modifier = Modifier.weight(1f).clickable { selectedTab = i }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(label, fontSize = 12.sp,
                                color = if (sel) Color.White else Color.White.copy(alpha = 0.65f),
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                            Box(Modifier.fillMaxWidth(0.6f).height(2.dp)
                                .background(if (sel) Color.White else Color.Transparent))
                        }
                    }
                }
            }

            // ── Intern mode badge + stats ──────────────────────────────────
            item {
                Column(modifier = Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Box(Modifier.clip(RoundedCornerShape(4.dp))
                        .background(GoldBadge).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("INTERN MODE", fontSize = 10.sp, color = Color.White,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Text("0/$totalLessons videos watched", fontSize = 13.sp,
                        color = Color(0xFF555555))

                    // Instructor card
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(Teal.copy(0.15f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, null, tint = Teal,
                                modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("Dr. Marrow Faculty", fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp, color = Color(0xFF1A1A1A))
                            Text("Contributing Editor", fontSize = 11.sp, color = Color.Gray)
                            Text("Expert faculty for $subjectName",
                                fontSize = 11.sp, color = Color(0xFF888888),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Filled.ExpandMore, null, tint = Color.Gray,
                            modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            // ── Sections + lessons ────────────────────────────────────────
            sections.forEachIndexed { sectionIdx, section ->
                item {
                    Text(
                        section.name.uppercase(),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF888888), letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                val globalOffset = sections.take(sectionIdx).sumOf { it.lessons.size }
                itemsIndexed(section.lessons) { idx, lesson ->
                    LessonCard(
                        number  = globalOffset + idx + 1,
                        lesson  = lesson,
                        onClick = { onLessonClick(lesson) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LessonCard(number: Int, lesson: VideoLesson, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Number
            Text("$number", fontSize = 13.sp, color = Color(0xFF999999),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp).width(20.dp))

            // Thumbnail placeholder
            Box(
                modifier = Modifier.size(width = 80.dp, height = 56.dp)
                    .clip(RoundedCornerShape(6.dp)).background(Color(0xFFE8F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayCircle, null, tint = Teal,
                    modifier = Modifier.size(28.dp))
                if (lesson.isPro) {
                    Box(Modifier.align(Alignment.BottomEnd)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF555555))
                        .padding(horizontal = 4.dp, vertical = 1.dp)) {
                        Text("PRO", fontSize = 8.sp, color = Color.White,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(lesson.title, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A), lineHeight = 18.sp, maxLines = 2,
                    overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Star, null, tint = GoldBadge,
                        modifier = Modifier.size(12.dp))
                    Text("4.6", fontSize = 11.sp, color = Color(0xFF888888))
                    Text("│", fontSize = 10.sp, color = Color(0xFFDDDDDD))
                    Text(lesson.duration, fontSize = 11.sp, color = Color(0xFF888888))
                }
            }

            // Completed badge
            if (lesson.isCompleted) {
                Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF66BB6A),
                    modifier = Modifier.size(20.dp).padding(top = 2.dp))
            }
        }

        // Solve QBank link
        Row(
            modifier = Modifier.fillMaxWidth().clickable {}
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Solve QBank Module instead", fontSize = 11.sp, color = Teal)
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Teal,
                modifier = Modifier.size(10.dp))
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
            color = Color(0xFFEEEEEE))
    }
}

// ── Dummy lesson data ─────────────────────────────────────────────────────────

fun dummyLessons(subjectName: String): List<VideoSection> = when {
    subjectName.contains("Anatomy", ignoreCase = true) -> listOf(
        VideoSection("Embryology", listOf(
            VideoLesson("Introduction to Anatomy", "16 Min", "Embryology"),
            VideoLesson("Gametogenesis", "28 Min", "Embryology"),
            VideoLesson("Folding of embryo and Formation of gut tube", "48 Min", "Embryology"),
            VideoLesson("3rd week development and Placental development", "66 Min", "Embryology"),
            VideoLesson("Derivatives of germ layers", "32 Min", "Embryology")
        )),
        VideoSection("Upper Limb", listOf(
            VideoLesson("Brachial Plexus — Complete", "54 Min", "Upper Limb"),
            VideoLesson("Nerve injuries of Upper Limb", "42 Min", "Upper Limb"),
            VideoLesson("Rotator Cuff and Shoulder Joint", "36 Min", "Upper Limb"),
            VideoLesson("Arterial supply of Hand", "28 Min", "Upper Limb"),
            VideoLesson("Carpal Tunnel Syndrome", "22 Min", "Upper Limb")
        )),
        VideoSection("Lower Limb", listOf(
            VideoLesson("Femoral Triangle and Adductor Canal", "38 Min", "Lower Limb"),
            VideoLesson("Nerve supply of Lower Limb", "44 Min", "Lower Limb"),
            VideoLesson("Knee Joint — Applied Anatomy", "30 Min", "Lower Limb"),
            VideoLesson("Foot drop vs Claw foot", "20 Min", "Lower Limb")
        ))
    )
    subjectName.contains("Physiology", ignoreCase = true) -> listOf(
        VideoSection("Respiratory Physiology", listOf(
            VideoLesson("Lung Volumes and Capacities", "34 Min", "Respiratory"),
            VideoLesson("Spirometry and Patterns", "28 Min", "Respiratory"),
            VideoLesson("Control of Breathing", "42 Min", "Respiratory"),
            VideoLesson("Oxygen-Haemoglobin Dissociation Curve", "36 Min", "Respiratory"),
            VideoLesson("Surfactant and RDS", "24 Min", "Respiratory")
        )),
        VideoSection("Cardiovascular Physiology", listOf(
            VideoLesson("Cardiac Cycle and Wiggers Diagram", "48 Min", "CVS"),
            VideoLesson("Frank-Starling Law and Cardiac Output", "32 Min", "CVS"),
            VideoLesson("ECG — Complete Interpretation", "54 Min", "CVS"),
            VideoLesson("Blood Pressure Regulation", "38 Min", "CVS")
        )),
        VideoSection("Renal Physiology", listOf(
            VideoLesson("GFR and its measurement", "30 Min", "Renal"),
            VideoLesson("Tubular reabsorption and secretion", "44 Min", "Renal"),
            VideoLesson("Countercurrent mechanism", "36 Min", "Renal"),
            VideoLesson("Acid-Base Balance", "52 Min", "Renal")
        ))
    )
    subjectName.contains("Pharmacology", ignoreCase = true) -> listOf(
        VideoSection("General Pharmacology", listOf(
            VideoLesson("Pharmacokinetics — ADME", "46 Min", "General"),
            VideoLesson("Drug Receptor Interactions", "38 Min", "General"),
            VideoLesson("Adverse Drug Reactions", "28 Min", "General"),
            VideoLesson("Drug interactions — High Yield", "32 Min", "General")
        )),
        VideoSection("Antimicrobials", listOf(
            VideoLesson("Beta-lactam Antibiotics", "44 Min", "Antimicrobials"),
            VideoLesson("Aminoglycosides and Tetracyclines", "36 Min", "Antimicrobials"),
            VideoLesson("Fluoroquinolones and Macrolides", "30 Min", "Antimicrobials"),
            VideoLesson("Antifungals and Antivirals", "42 Min", "Antimicrobials"),
            VideoLesson("Antitubercular Drugs", "38 Min", "Antimicrobials")
        )),
        VideoSection("CNS Pharmacology", listOf(
            VideoLesson("General Anaesthetics", "40 Min", "CNS"),
            VideoLesson("Antiepileptics", "36 Min", "CNS"),
            VideoLesson("Antipsychotics and Antidepressants", "48 Min", "CNS"),
            VideoLesson("Opioid Analgesics", "32 Min", "CNS")
        ))
    )
    subjectName.contains("Medicine", ignoreCase = true) -> listOf(
        VideoSection("Cardiology", listOf(
            VideoLesson("Heart Failure — Classification and Management", "52 Min", "Cardiology"),
            VideoLesson("Ischaemic Heart Disease and MI", "48 Min", "Cardiology"),
            VideoLesson("Valvular Heart Disease", "44 Min", "Cardiology"),
            VideoLesson("Hypertension — JNC 8 Guidelines", "36 Min", "Cardiology"),
            VideoLesson("Arrhythmias and Anti-arrhythmics", "42 Min", "Cardiology")
        )),
        VideoSection("Infectious Diseases", listOf(
            VideoLesson("Typhoid Fever — Diagnosis and Treatment", "38 Min", "Infectious"),
            VideoLesson("Malaria — Complete", "46 Min", "Infectious"),
            VideoLesson("Tuberculosis — RNTCP Guidelines", "54 Min", "Infectious"),
            VideoLesson("HIV/AIDS — Management", "40 Min", "Infectious")
        )),
        VideoSection("Haematology", listOf(
            VideoLesson("Anaemia — Classification and Approach", "44 Min", "Haematology"),
            VideoLesson("Leukaemia — ALL, AML, CML, CLL", "52 Min", "Haematology"),
            VideoLesson("Bleeding Disorders", "36 Min", "Haematology"),
            VideoLesson("Lymphomas — Hodgkin and Non-Hodgkin", "42 Min", "Haematology")
        ))
    )
    subjectName.contains("Surgery", ignoreCase = true) -> listOf(
        VideoSection("Abdomen", listOf(
            VideoLesson("Acute Abdomen — Approach", "40 Min", "Abdomen"),
            VideoLesson("Appendicitis — Complete", "32 Min", "Abdomen"),
            VideoLesson("Intestinal Obstruction", "38 Min", "Abdomen"),
            VideoLesson("Colorectal Carcinoma", "44 Min", "Abdomen"),
            VideoLesson("Hernia — Types and Management", "48 Min", "Abdomen")
        )),
        VideoSection("Trauma and Burns", listOf(
            VideoLesson("Fracture Healing and Complications", "36 Min", "Trauma"),
            VideoLesson("Burns — Assessment and Management", "42 Min", "Trauma"),
            VideoLesson("Nerve injuries in Fractures", "34 Min", "Trauma"),
            VideoLesson("ATLS Protocol", "28 Min", "Trauma")
        )),
        VideoSection("Oncology", listOf(
            VideoLesson("Carcinoma Breast — Staging and Treatment", "46 Min", "Oncology"),
            VideoLesson("Thyroid Carcinoma", "34 Min", "Oncology"),
            VideoLesson("Head and Neck Tumours", "40 Min", "Oncology")
        ))
    )
    subjectName.contains("OBG", ignoreCase = true) || subjectName.contains("Obs", ignoreCase = true) -> listOf(
        VideoSection("Obstetrics", listOf(
            VideoLesson("Normal Labour — Mechanisms", "44 Min", "Obstetrics"),
            VideoLesson("Antepartum Haemorrhage", "36 Min", "Obstetrics"),
            VideoLesson("Hypertensive Disorders of Pregnancy", "42 Min", "Obstetrics"),
            VideoLesson("Postpartum Haemorrhage", "32 Min", "Obstetrics"),
            VideoLesson("Preterm Labour and PROM", "38 Min", "Obstetrics")
        )),
        VideoSection("Gynaecology", listOf(
            VideoLesson("Polycystic Ovarian Syndrome", "34 Min", "Gynaecology"),
            VideoLesson("Carcinoma Cervix — CIN to Invasive", "46 Min", "Gynaecology"),
            VideoLesson("Ovarian Tumours", "40 Min", "Gynaecology"),
            VideoLesson("Infertility — Investigation and Management", "36 Min", "Gynaecology")
        ))
    )
    subjectName.contains("Pathology", ignoreCase = true) -> listOf(
        VideoSection("General Pathology", listOf(
            VideoLesson("Cell Injury and Death", "38 Min", "General Pathology"),
            VideoLesson("Inflammation — Acute and Chronic", "44 Min", "General Pathology"),
            VideoLesson("Neoplasia — Basics", "48 Min", "General Pathology"),
            VideoLesson("Haemodynamic Disorders", "36 Min", "General Pathology")
        )),
        VideoSection("Systemic Pathology", listOf(
            VideoLesson("Cardiovascular Pathology", "44 Min", "Systemic"),
            VideoLesson("Respiratory Pathology", "40 Min", "Systemic"),
            VideoLesson("Renal Pathology", "46 Min", "Systemic"),
            VideoLesson("Liver and GI Pathology", "42 Min", "Systemic")
        ))
    )
    subjectName.contains("Pediatrics", ignoreCase = true) -> listOf(
        VideoSection("Growth and Development", listOf(
            VideoLesson("Developmental Milestones", "36 Min", "Development"),
            VideoLesson("Nutritional Disorders", "32 Min", "Development"),
            VideoLesson("Immunization Schedule", "28 Min", "Development"),
            VideoLesson("Neonatal Jaundice", "38 Min", "Development")
        )),
        VideoSection("Paediatric Diseases", listOf(
            VideoLesson("Respiratory Infections in Children", "40 Min", "Diseases"),
            VideoLesson("Childhood Exanthemas", "34 Min", "Diseases"),
            VideoLesson("Nephrotic and Nephritic Syndrome", "42 Min", "Diseases"),
            VideoLesson("Thalassaemia and Sickle Cell", "36 Min", "Diseases")
        ))
    )
    else -> listOf(
        VideoSection("Fundamentals", listOf(
            VideoLesson("Introduction to $subjectName", "30 Min", "Basics"),
            VideoLesson("High Yield Concepts — Part 1", "42 Min", "Basics"),
            VideoLesson("High Yield Concepts — Part 2", "38 Min", "Basics"),
            VideoLesson("Clinical Correlations", "34 Min", "Basics"),
            VideoLesson("Previous Year Questions Review", "46 Min", "Basics")
        )),
        VideoSection("Advanced Topics", listOf(
            VideoLesson("Revision Class — Complete", "60 Min", "Advanced"),
            VideoLesson("Recent Advances", "28 Min", "Advanced"),
            VideoLesson("Mock Test Discussion", "44 Min", "Advanced")
        ))
    )
}
