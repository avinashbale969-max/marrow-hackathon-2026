package com.marrow.companion.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.NoteTag
import com.marrow.companion.ui.screens.quiz.HighlightableText
import com.marrow.companion.ui.screens.quiz.MyNotesSheet
import com.marrow.companion.ui.screens.quiz.NoteInputDialog

private val Teal      = Color(0xFF4DC8D4)
private val DarkBg    = Color(0xFF1A1A1A)

data class LessonChapter(val title: String, val timestamp: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoLessonScreen(
    subjectId: Long,
    subjectName: String,
    lessonTitle: String,
    onBack: () -> Unit,
    onOpenNotes: () -> Unit = {},
    viewModel: VideoNotesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val lessonKey = (subjectId * 1000 + lessonTitle.hashCode()).toLong()

    val highlights by viewModel.getHighlights(lessonKey).collectAsState(initial = emptyList())
    val notes      by viewModel.getNotes(lessonKey).collectAsState(initial = emptyList())
    val tagNotes    = notes.filter { it.tag == NoteTag.TAG.name }

    var showNotes        by remember { mutableStateOf(false) }
    var isPlaying        by remember { mutableStateOf(false) }
    var isCompleted      by remember { mutableStateOf(false) }
    var selectionActive  by remember { mutableStateOf(false) }
    var showNotesSheet   by remember { mutableStateOf(false) }
    var pendingTagQuote  by remember { mutableStateOf<String?>(null) }
    var showTagDialog    by remember { mutableStateOf(false) }
    var currentChapter   by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()
    val chapters    = lessonChapters(lessonTitle)
    val notesText   = lessonNotesContent(lessonTitle, subjectName)

    if (showNotesSheet) {
        MyNotesSheet(
            highlights        = highlights,
            notes             = notes.filter { it.tag != NoteTag.TAG.name },
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote      = { viewModel.deleteNote(it) },
            onAddNote         = { text, tag, quote -> viewModel.addNote(lessonKey, text, tag, quote) },
            onEditNote        = { note, text, tag -> viewModel.updateNote(note, text, tag) },
            onDismiss         = { showNotesSheet = false }
        )
    }
    if (showTagDialog && pendingTagQuote != null) {
        NoteInputDialog(
            initialText = "",
            onConfirm   = { text ->
                if (text.isNotBlank()) viewModel.addNote(lessonKey, text, NoteTag.TAG, pendingTagQuote)
                pendingTagQuote = null
            },
            onDismiss = { showTagDialog = false; pendingTagQuote = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {

        // ── Video player area ────────────────────────────────────────────
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            // Back button
            IconButton(onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            // Top-right actions
            Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("CC", "⤓", "⚙").forEach { label ->
                    Box(Modifier.size(32.dp).clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center) {
                        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // Play controls
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Replay, null, tint = Color.White.copy(0.8f),
                    modifier = Modifier.size(36.dp).clickable {})
                Box(Modifier.size(52.dp).clip(CircleShape)
                    .background(Color.White.copy(0.2f))
                    .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center) {
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Icon(Icons.Filled.Forward10, null, tint = Color.White.copy(0.8f),
                    modifier = Modifier.size(36.dp).clickable {})
            }
            // Progress bar + title
            Column(modifier = Modifier.align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { 0.35f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = Teal, trackColor = Color.White.copy(0.3f))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Text("07:45 / ${chapters.lastOrNull()?.timestamp ?: "30:00"}",
                        color = Color.White, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1x", color = Color.White, fontSize = 11.sp)
                        Icon(Icons.Filled.Fullscreen, null, tint = Color.White,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ── Content area (white) ──────────────────────────────────────────
        Column(modifier = Modifier.weight(1f).background(Color.White)) {
            // Lesson title + download
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(lessonTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = Color(0xFF1A1A1A), modifier = Modifier.weight(1f), lineHeight = 21.sp)
                Icon(Icons.Filled.Download, null, tint = Teal, modifier = Modifier.size(22.dp))
            }

            // Open Notes Image button
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable(onClick = onOpenNotes)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.MenuBook, null, tint = Teal, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Notes (3 pages)", fontSize = 14.sp, color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Medium)
                    Text("Tap to view & highlight notes",
                        fontSize = 11.sp, color = Color.Gray)
                }
                Icon(Icons.Filled.ChevronRight, null, tint = Teal, modifier = Modifier.size(20.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFEEEEEE))

            // Chapter list
            LazyChapterList(
                chapters       = chapters,
                currentChapter = currentChapter,
                onChapterClick = { currentChapter = it }
            )
            // Mark Complete button
            Button(
                onClick = { isCompleted = !isCompleted },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(48.dp),
                shape  = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Color(0xFF66BB6A) else Teal
                )
            ) {
                if (isCompleted) {
                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("COMPLETED", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                } else {
                    Text("MARK COMPLETE", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun LazyChapterList(
    chapters: List<LessonChapter>,
    currentChapter: Int,
    onChapterClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val visibleChapters = if (expanded) chapters else chapters.take(4)

    Column(modifier = Modifier.fillMaxWidth()) {
        visibleChapters.forEachIndexed { idx, chapter ->
            val isCurrent = idx == currentChapter
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(if (isCurrent) Teal.copy(0.06f) else Color.Transparent)
                    .clickable { onChapterClick(idx) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCurrent) {
                    Box(Modifier.width(3.dp).height(36.dp).background(Teal)
                        .clip(RoundedCornerShape(2.dp)))
                } else {
                    Spacer(Modifier.width(3.dp))
                }
                Icon(Icons.Filled.PlayCircleOutline, null,
                    tint = if (isCurrent) Teal else Color(0xFFAAAAAA),
                    modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(chapter.title, fontSize = 13.sp,
                        color = if (isCurrent) Teal else Color(0xFF1A1A1A),
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal)
                    Text(chapter.timestamp, fontSize = 11.sp,
                        color = if (isCurrent) Teal else Color.Gray)
                }
            }
            if (idx < visibleChapters.lastIndex)
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color(0xFFF0F0F0))
        }
        if (chapters.size > 4) {
            TextButton(onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "COLLAPSE" else "EXPAND (${chapters.size - 4})",
                    fontSize = 12.sp, color = Color.Gray)
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── Chapter timestamps ────────────────────────────────────────────────────────

fun lessonChapters(lessonTitle: String): List<LessonChapter> {
    val t = lessonTitle.lowercase()
    return when {
        t.contains("brachial plexus") -> listOf(
            LessonChapter("Introduction and overview", "00:00"),
            LessonChapter("Roots and trunks", "04:20"),
            LessonChapter("Divisions and cords", "10:15"),
            LessonChapter("Terminal branches", "18:30"),
            LessonChapter("Clinical correlations", "32:00"),
            LessonChapter("High yield points", "46:10")
        )
        t.contains("ecg") || t.contains("electrocardiogram") -> listOf(
            LessonChapter("Basics of ECG", "00:00"),
            LessonChapter("P wave and PR interval", "06:30"),
            LessonChapter("QRS complex", "15:45"),
            LessonChapter("ST segment and T wave", "24:10"),
            LessonChapter("Common ECG abnormalities", "35:20")
        )
        t.contains("lung volume") || t.contains("spirometry") -> listOf(
            LessonChapter("Introduction", "00:00"),
            LessonChapter("TV, IRV, ERV, RV", "05:00"),
            LessonChapter("Capacities — IC, FRC, VC, TLC", "12:30"),
            LessonChapter("Spirometry — Obstructive pattern", "20:00"),
            LessonChapter("Spirometry — Restrictive pattern", "27:45")
        )
        t.contains("heart failure") -> listOf(
            LessonChapter("Definition and classification", "00:00"),
            LessonChapter("Pathophysiology", "07:15"),
            LessonChapter("Clinical features", "16:40"),
            LessonChapter("Investigations", "24:50"),
            LessonChapter("Management", "32:00"),
            LessonChapter("Prognosis", "44:30")
        )
        t.contains("malaria") -> listOf(
            LessonChapter("Life cycle", "00:00"),
            LessonChapter("Species comparison", "09:20"),
            LessonChapter("Clinical features", "18:45"),
            LessonChapter("Diagnosis", "26:30"),
            LessonChapter("Treatment", "34:15"),
            LessonChapter("Complications", "41:00")
        )
        else -> listOf(
            LessonChapter("Introduction", "00:00"),
            LessonChapter("Core concepts", "08:00"),
            LessonChapter("Important mechanisms", "16:30"),
            LessonChapter("Clinical applications", "24:45"),
            LessonChapter("High yield summary", "34:00")
        )
    }
}

// ── Rich notes content per lesson ────────────────────────────────────────────

fun lessonNotesContent(lessonTitle: String, subjectName: String): String {
    val t = lessonTitle.lowercase()
    return when {
        t.contains("brachial plexus") -> """
BRACHIAL PLEXUS — COMPLETE NOTES

ANATOMY
Roots: C5, C6, C7, C8, T1 (occasionally C4 and T2)
Formation: Roots → Trunks → Divisions → Cords → Branches

TRUNKS
• Upper trunk: C5 + C6
• Middle trunk: C7 alone
• Lower trunk: C8 + T1

CORDS (named by relation to axillary artery)
• Lateral cord: Anterior divisions of upper + middle trunks
• Medial cord: Anterior division of lower trunk
• Posterior cord: Posterior divisions of all 3 trunks

MAJOR BRANCHES
From Lateral cord:
• Musculocutaneous nerve (C5,C6,C7)
• Lateral root of median nerve

From Medial cord:
• Ulnar nerve (C8,T1)
• Medial root of median nerve
• Medial cutaneous nerves of arm and forearm

From Posterior cord:
• Radial nerve (C5–T1)
• Axillary nerve (C5,C6)
• Thoracodorsal nerve
• Upper and lower subscapular nerves

CLINICAL INJURIES

Erb's Palsy (Upper trunk — C5, C6):
• Cause: Forceful separation of head and shoulder (shoulder dystocia)
• Muscles affected: Deltoid, Biceps, Brachialis, Brachioradialis, Supinator
• Deformity: "Waiter's tip" position — adduction, medial rotation, extended elbow, pronation
• Sensation: Lateral arm and forearm

Klumpke's Palsy (Lower trunk — C8, T1):
• Cause: Forced abduction of arm (grabbing overhead support during fall)
• Muscles: Intrinsic muscles of hand (all)
• Deformity: Claw hand (all 4 fingers)
• If T1 sympathetic fibres involved: Horner's syndrome

MNEMONIC: "Real Teenagers Drink Cold Beer"
Roots → Trunks → Divisions → Cords → Branches
        """.trimIndent()

        t.contains("nerve injur") && t.contains("upper") -> """
NERVE INJURIES OF UPPER LIMB — HIGH YIELD

AXILLARY NERVE (C5, C6 — Posterior cord)
Course: Quadrilateral space → winds around surgical neck of humerus
Injury: Surgical neck fracture, shoulder dislocation
Motor: Deltoid (abduction 15-90°), Teres minor
Sensory: Regimental badge area (lateral upper arm)
Test: Cannot abduct arm >15°

RADIAL NERVE (C5-T1 — Posterior cord)
Course: Winds in spiral groove of humerus
Injury: Mid-shaft humerus fracture ("Saturday night palsy")
Motor: All extensors of forearm (triceps, wrist extensors, finger extensors)
Note: Triceps SPARED in spiral groove injury (proximal branch)
Sensory: Dorsum of hand (lateral 3½ fingers)
Deformity: WRIST DROP

MEDIAN NERVE (C6-T1 — Lateral + Medial cords)
Sites of injury:
1. Carpal tunnel (most common entrapment neuropathy)
   → Sensory loss lateral 3½ fingers, thenar wasting
   → Test: Phalen's + Tinel's sign
2. Pronator teres compression
Motor (hand): LOAF = Lumbricals 1&2, Opponens pollicis, Abductor pollicis brevis, Flexor pollicis brevis
Deformity: APE HAND (thenar wasting), can't make OK sign (AIN injury)

ULNAR NERVE (C8-T1 — Medial cord)
Site: Cubital tunnel (medial epicondyle) — most common site
Injury: Medial epicondyle fracture, leaning on elbow
Motor: Medial 2 lumbricals, all interossei, hypothenar, adductor pollicis
Sensory: Medial 1½ fingers
Deformity: CLAW HAND (ring + little finger)
Note: Ulnar paradox — more proximal lesion = less clawing (FDP not paralysed)
        """.trimIndent()

        t.contains("cardiac cycle") || t.contains("wiggers") -> """
CARDIAC CYCLE — WIGGERS DIAGRAM

PHASES OF CARDIAC CYCLE

1. ISOVOLUMETRIC CONTRACTION
• All valves CLOSED
• Pressure rises rapidly
• Volume unchanged
• Duration: ~50ms

2. EJECTION PHASE (Rapid + Reduced)
• Aortic/Pulmonary valve OPENS (when ventricular P > aortic P)
• Volume decreases (SV ejected)
• Pressure rises to peak then falls

3. ISOVOLUMETRIC RELAXATION
• All valves CLOSED again
• Pressure falls rapidly
• Volume unchanged
• Duration: ~70ms

4. RAPID FILLING
• Mitral valve OPENS (when LA P > LV P)
• Rapid flow from LA to LV
• "E" wave on echo

5. SLOW FILLING (Diastasis)
• Slow filling
• Little change in pressure or volume

6. ATRIAL SYSTOLE (Atrial kick)
• "A" wave on echo
• Contributes ~15-20% of LV filling
• Lost in AF → ↓ CO

HEART SOUNDS
• S1: Closure of Mitral + Tricuspid valves (start of systole)
• S2: Closure of Aortic + Pulmonary valves (end of systole)
• S3: Rapid ventricular filling (pathological in adults — HF)
• S4: Atrial systole against stiff ventricle (LVH, aortic stenosis)

CARDIAC OUTPUT FORMULA
CO = HR × SV
Normal CO = 5 L/min
Cardiac Index = CO / BSA = 3.2 L/min/m²

FRANK-STARLING LAW
↑ Preload → ↑ stretch → ↑ force of contraction → ↑ SV
Operates within physiological limits

PRESSURE-VOLUME LOOP
• Width = Stroke Volume
• Height = Stroke Work
• Area = Work done by ventricle
        """.trimIndent()

        t.contains("heart failure") -> """
HEART FAILURE — COMPREHENSIVE NOTES

DEFINITION
Heart failure is a clinical syndrome where the heart cannot pump enough blood to meet metabolic demands of the body, or can only do so at elevated filling pressures.

CLASSIFICATION
By EF:
• HFrEF (EF <40%): Systolic dysfunction — dilated, ischaemic
• HFpEF (EF >50%): Diastolic dysfunction — hypertrophic, restrictive
• HFmrEF (EF 40-49%): Mid-range

By side:
• Left HF: Pulmonary oedema, orthopnoea, PND, basal crepitations
• Right HF: Peripheral oedema, JVD, hepatomegaly, ascites
• Biventricular: Both

NYHA CLASSIFICATION
Class I: No symptoms with ordinary activity
Class II: Mild symptoms — slight limitation
Class III: Moderate symptoms — marked limitation
Class IV: Symptoms at rest — unable to carry out any activity

PATHOPHYSIOLOGY
Cardiac injury → ↓CO → ↑SNS activation + ↑RAAS → Compensation (initially) → Remodelling → Worsening

INVESTIGATION
• BNP/NT-proBNP: Most useful biomarker (elevated in HF)
• Echo: Gold standard — assess EF, wall motion, valves
• CXR: Cardiomegaly, pulmonary congestion, Kerley B lines
• ECG: LVH, arrhythmias, ischaemia

MANAGEMENT (HFrEF — ABCDE)
A: ACE inhibitor/ARB/ARNI (sacubitril-valsartan)
B: Beta-blocker (carvedilol, metoprolol, bisoprolol)
C: Cardiac resynchronisation if LBBB + EF <35%
D: Diuretics (furosemide) for fluid overload
E: SGLT2 inhibitors (empagliflozin) — newer standard of care
+ Mineralocorticoid receptor antagonist (spironolactone)

PROGNOSIS
1-year mortality: ~20-30% in severe HF
BNP levels correlate with prognosis
ICD implantation for EF <35% with optimal medical therapy
        """.trimIndent()

        t.contains("malaria") -> """
MALARIA — COMPLETE NOTES

CAUSATIVE ORGANISM
Plasmodium species:
• P. falciparum — Malignant tertian (most dangerous)
• P. vivax — Benign tertian (most common globally)
• P. malariae — Quartan malaria
• P. ovale — Benign tertian
• P. knowlesi — Zoonotic (macaque monkey malaria)

LIFE CYCLE
Mosquito (Anopheles female) → Sporozoites inject → Liver (Pre-erythrocytic) → Merozoites → RBCs (Erythrocytic cycle)
Gametocytes → Mosquito → Sporogony

SPECIES COMPARISON TABLE
Feature         | P. falciparum  | P. vivax        | P. malariae
Fever cycle     | 36-48 h        | 48 h (tertian)  | 72 h (quartan)
RBC preference  | All ages       | Reticulocytes   | Old RBCs
Unique feature  | Cerebral/BW fever | Relapse (hypnozoites) | Nephrotic syndrome
Rosette forming | Yes            | No              | No

CLINICAL FEATURES
• Fever, rigors, sweating (classic triad)
• Splenomegaly (chronic)
• Anaemia (haemolytic)
• Thrombocytopenia

COMPLICATIONS (P. falciparum)
• Cerebral malaria: Impaired consciousness, seizures, coma
• Blackwater fever: Massive haemolysis → haemoglobinuria → dark urine
• Hypoglycaemia (quinine-induced or disease itself)
• ARDS, Acute renal failure
• Severe anaemia (Hb <7g/dL)

DIAGNOSIS
• Blood smear (Giemsa stain): Gold standard — thick and thin film
• RDT (Rapid Diagnostic Test): Detects HRP-2 antigen
• PCR: Most sensitive

TREATMENT
Uncomplicated P. falciparum:
• ACT (Artemisinin Combination Therapy): Artemether + Lumefantrine
• Alternative: AS + AQ, DHP

Severe P. falciparum:
• IV Artesunate (drug of choice)
• Alternative: IV Quinine + Doxycycline

P. vivax/ovale (radical cure):
• Chloroquine + Primaquine (eradicates hypnozoites)
• Check G6PD before primaquine!

PREVENTION
• LLIN (Long-lasting Insecticidal Nets)
• IRS (Indoor Residual Spraying)
• Chemoprophylaxis: Mefloquine / Doxycycline
        """.trimIndent()

        t.contains("lung volume") || t.contains("spirometry") || t.contains("respiratory") -> """
LUNG VOLUMES AND SPIROMETRY

LUNG VOLUMES (Normal values — 70 kg male)
• Tidal Volume (TV): 500 mL — normal breathing
• Inspiratory Reserve Volume (IRV): 3000 mL — extra after normal inspiration
• Expiratory Reserve Volume (ERV): 1200 mL — extra expelled after normal expiration
• Residual Volume (RV): 1200 mL — cannot be measured by spirometry

LUNG CAPACITIES
• TLC (Total Lung Capacity): TV + IRV + ERV + RV = 6000 mL
• VC (Vital Capacity): TV + IRV + ERV = 4800 mL
• IC (Inspiratory Capacity): TV + IRV = 3500 mL
• FRC (Functional Residual Capacity): ERV + RV = 2400 mL

SPIROMETRY PATTERNS

Obstructive Pattern (Asthma, COPD, Emphysema):
• FEV1: ↓↓
• FVC: Normal or mildly ↓
• FEV1/FVC ratio: <70% (hallmark)
• RV: ↑ (air trapping)
• TLC: ↑ (emphysema)
• Post-bronchodilator reversibility: ≥12% and ≥200mL = Asthma

Restrictive Pattern (Fibrosis, Obesity, Scoliosis):
• FEV1: ↓
• FVC: ↓↓
• FEV1/FVC ratio: Normal or ↑ (>80%)
• TLC: ↓ (hallmark)
• RV: ↓ or normal

FLOW-VOLUME LOOP
• Obstructive: Scooped-out expiratory limb
• Restrictive: Small loop, normal shape
• Variable intrathoracic obstruction: Expiratory plateau
• Variable extrathoracic obstruction: Inspiratory plateau

FRC SIGNIFICANCE
• At FRC: Elastic recoil of lung INWARDS = Chest wall recoil OUTWARDS
• FRC cannot be measured by spirometry (needs helium dilution or body plethysmography)
• ↓ FRC: Obesity, pregnancy, supine position, ARDS, atelectasis
• ↑ FRC: Emphysema, asthma (hyperinflation)

DLCO (Diffusion capacity for CO)
• ↓ in: Emphysema, interstitial lung disease, anaemia
• Normal in: Obstructive (pure bronchitis), obesity
        """.trimIndent()

        t.contains("pharmacokinetics") || t.contains("adme") -> """
PHARMACOKINETICS — ADME

1. ABSORPTION
Routes and bioavailability:
• IV: 100% (reference standard)
• Oral: Variable (first-pass metabolism)
• Sublingual: High (avoids first-pass)
• IM/SC: Good
• Rectal: ~50% (partial first-pass avoidance)

First-pass effect: Oral drug → Portal vein → Liver → Metabolism BEFORE reaching systemic circulation
High first-pass drugs: Morphine, Propranolol, Lignocaine, GTN
→ These have HIGH oral doses compared to IV doses

Factors affecting absorption:
• Gastric pH (affects ionisation)
• GI motility
• Particle size and formulation
• Lipid solubility

2. DISTRIBUTION
Volume of Distribution (Vd) = Dose / Plasma Concentration
• Small Vd (<1 L/kg): Plasma-bound drugs (Warfarin, Heparin)
• Large Vd (>10 L/kg): Highly tissue-bound (Chloroquine, Amiodarone)

Plasma protein binding:
• Albumin: Binds acidic drugs (NSAIDs, Warfarin, Phenytoin)
• Alpha-1 acid glycoprotein: Binds basic drugs (Propranolol, Lignocaine)
• Only FREE drug is pharmacologically active

3. METABOLISM (Biotransformation)
Phase I: Oxidation, Reduction, Hydrolysis (CYP450 system)
Phase II: Conjugation (Glucuronidation, Sulfation, Acetylation)
• Liver is primary site
• CYP3A4: Most important enzyme (metabolises ~50% of drugs)

Enzyme Induction (↑ metabolism): Rifampicin, Carbamazepine, Phenytoin, Phenobarbitone
Enzyme Inhibition (↓ metabolism): Fluconazole, Erythromycin, Grapefruit, Cimetidine

4. EXCRETION
Renal excretion:
• Glomerular filtration
• Tubular secretion (active transport)
• Tubular reabsorption (passive)
• pH manipulation: Alkaline urine → ↑ excretion of acidic drugs (Aspirin, Phenobarbitone)

Biliary excretion + Enterohepatic circulation:
• Some drugs recirculate (extended action)
• Examples: Oestrogens, Morphine, Chloramphenicol
        """.trimIndent()

        t.contains("fracture") || t.contains("trauma") -> """
FRACTURE HEALING AND CLINICAL FRACTURES

STAGES OF FRACTURE HEALING
1. Haematoma formation (0-48 hours)
   • Bleeding from medullary vessels
   • Clot forms the initial scaffold

2. Inflammatory phase (Days 1-5)
   • Macrophages, neutrophils arrive
   • Cytokines released (IL-1, IL-6, TNF-α)
   • Angiogenesis begins

3. Soft callus formation (Days 5-14)
   • Periosteal cells differentiate into chondroblasts
   • Fibrocartilaginous callus bridges the gap
   • No radiological evidence yet

4. Hard callus (Weeks 2-6)
   • Enchondral ossification
   • Calcification of cartilage → woven bone
   • Visible on X-ray

5. Remodelling (Months to years)
   • Wolff's law: Bone remodels along lines of stress
   • Woven bone → Lamellar bone
   • Medullary canal re-established

IMPORTANT CLINICAL FRACTURES
Colles' fracture: Distal radius — "dinner fork" deformity — FOOSH mechanism
Smith's fracture: Reverse Colles' — volar displacement — fall on flexed wrist
Scaphoid fracture: Anatomical snuffbox tenderness — AVN risk if untreated
Monteggia: Proximal ulna fracture + radial head dislocation (MURR)
Galeazzi: Radial shaft fracture + DRUJ dislocation (GRUFF)

COMPLICATIONS OF FRACTURES
Early:
• Haemorrhage (femur fracture → 1-2L blood loss)
• Nerve injury (associated with specific fractures)
• Vascular injury → compartment syndrome

Late:
• Malunion (incorrect position)
• Non-union (failure to heal → atrophic or hypertrophic)
• Avascular necrosis (scaphoid, femoral head, talus)
• Sudeck's atrophy (complex regional pain syndrome)
• Joint stiffness
        """.trimIndent()

        else -> """
${lessonTitle.uppercase()}
$subjectName — Lecture Notes

OVERVIEW
This lecture covers the key concepts of $lessonTitle as tested in NEET PG examinations. Focus on mechanisms, clinical correlations, and high-yield facts.

KEY DEFINITIONS
The fundamental concepts in this topic form the basis for understanding disease mechanisms and clinical presentations. These definitions are frequently tested in theory examinations.

PATHOPHYSIOLOGY / MECHANISM
Understanding the underlying mechanism is crucial for answering clinical scenario-based questions. The mechanism links basic science to clinical presentation.

Key pathway:
Trigger → Initial response → Cascade of events → Clinical manifestation

CLINICAL FEATURES
• Primary symptoms and signs
• Associated features
• Complications to watch for
• Differentiating features from similar conditions

INVESTIGATIONS
1. First-line: Basic investigations + specific test
2. Gold standard: Most specific/sensitive investigation
3. Additional: To rule out differentials or assess severity

MANAGEMENT
Conservative approach → Medical management → Surgical intervention (if needed)

HIGH YIELD POINTS FOR NEET PG
→ Most common cause / Most common complication
→ Drug of choice and mechanism
→ Diagnostic criteria
→ Staging / Classification system
→ Recent guidelines update

MNEMONICS
Create personal mnemonics using the Tag feature above to remember key facts.

REVISION CHECKLIST
□ Define the condition
□ Know the mechanism
□ List clinical features
□ State investigations (with rationale)
□ Outline management
□ Know complications
        """.trimIndent()
    }
}
