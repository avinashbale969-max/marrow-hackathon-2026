package com.marrow.companion.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marrow.companion.data.database.entities.HighlightColor
import com.marrow.companion.data.database.entities.HighlightEntity
import com.marrow.companion.data.database.entities.NoteTag
import com.marrow.companion.ui.screens.quiz.HighlightableText
import com.marrow.companion.ui.screens.quiz.MyNotesSheet
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marrow.companion.data.database.dao.HighlightDao
import com.marrow.companion.data.database.dao.NoteDao
import com.marrow.companion.data.database.entities.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Teal = Color(0xFF4DC8D4)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class VideoNotesViewModel @Inject constructor(
    private val highlightDao: HighlightDao,
    private val noteDao: NoteDao
) : ViewModel() {

    // Use questionId = -subjectId as a unique key for video notes
    // (negative IDs won't conflict with real question IDs)

    fun getHighlights(subjectId: Long): Flow<List<HighlightEntity>> =
        highlightDao.getForQuestion(-subjectId)

    fun getNotes(subjectId: Long) = noteDao.getForQuestion(-subjectId)

    fun addHighlight(subjectId: Long, text: String, color: HighlightColor) {
        viewModelScope.launch {
            highlightDao.deleteByQuestionAndText(-subjectId, text)
            highlightDao.insert(HighlightEntity(questionId = -subjectId,
                text = text, color = color.name))
        }
    }

    fun deleteHighlight(hl: HighlightEntity) {
        viewModelScope.launch { highlightDao.deleteById(hl.id) }
    }

    fun addNote(subjectId: Long, text: String, tag: NoteTag, quote: String?) {
        viewModelScope.launch {
            noteDao.insert(NoteEntity(questionId = -subjectId,
                text = text, tag = tag.name, attachedQuote = quote))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteDao.deleteById(note.id) }
    }

    fun updateNote(note: NoteEntity, text: String, tag: NoteTag) {
        viewModelScope.launch { noteDao.update(note.copy(text = text, tag = tag.name)) }
    }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoNotesScreen(
    subjectId: Long,
    subjectName: String,
    onBack: () -> Unit,
    viewModel: VideoNotesViewModel = hiltViewModel()
) {
    val highlights by viewModel.getHighlights(subjectId).collectAsState(initial = emptyList())
    val notes      by viewModel.getNotes(subjectId).collectAsState(initial = emptyList())
    val tagNotes    = notes.filter { it.tag == NoteTag.TAG.name && it.attachedQuote != null }

    val scrollState  = rememberScrollState()
    var selectionActive by remember { mutableStateOf(false) }
    var showNotesSheet  by remember { mutableStateOf(false) }
    var pendingTagQuote by remember { mutableStateOf<String?>(null) }
    var showTagDialog   by remember { mutableStateOf(false) }

    val notesText = videoNotesContent(subjectName)

    if (showNotesSheet) {
        MyNotesSheet(
            highlights        = highlights,
            notes             = notes.filter { it.tag != NoteTag.TAG.name },
            onDeleteHighlight = { viewModel.deleteHighlight(it) },
            onDeleteNote      = { viewModel.deleteNote(it) },
            onAddNote         = { text, tag, quote -> viewModel.addNote(subjectId, text, tag, quote) },
            onEditNote        = { note, text, tag -> viewModel.updateNote(note, text, tag) },
            onDismiss         = { showNotesSheet = false }
        )
    }

    if (showTagDialog && pendingTagQuote != null) {
        com.marrow.companion.ui.screens.quiz.NoteInputDialog(
            initialText = "",
            onConfirm   = { text ->
                if (text.isNotBlank())
                    viewModel.addNote(subjectId, text, NoteTag.TAG, pendingTagQuote)
                pendingTagQuote = null
            },
            onDismiss = { showTagDialog = false; pendingTagQuote = null }
        )
    }

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
                    TextButton(onClick = { showNotesSheet = true }) {
                        Text("My Notes", color = Color.White,
                            fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor            = Teal,
                    titleContentColor         = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Lecture notes label
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F8F8))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Edit, null, tint = Teal, modifier = Modifier.size(14.dp))
                Text("Lecture Notes — $subjectName", fontSize = 12.sp, color = Color(0xFF555555),
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Text("Select text to highlight", fontSize = 11.sp, color = Color(0xFFAAAAAA))
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))

            // Notes content with highlighting
            Column(
                modifier = Modifier.weight(1f)
                    .verticalScroll(scrollState, enabled = !selectionActive)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                HighlightableText(
                    text                = notesText,
                    highlights          = highlights,
                    notes               = tagNotes,
                    onHighlight         = { text, color, _ ->
                        viewModel.addHighlight(subjectId, text, color)
                    },
                    onTagSelected       = { sel -> pendingTagQuote = sel; showTagDialog = true },
                    onDeleteTag         = { viewModel.deleteNote(it) },
                    onEditTag           = { note, txt -> viewModel.updateNote(note, txt, NoteTag.TAG) },
                    onScrollEnabled     = { en -> selectionActive = !en },
                    scrollOffsetPx      = { scrollState.value },
                )
            }
        }
    }
}

// ── Sample video notes content per subject ─────────────────────────────────────

fun videoNotesContent(subjectName: String): String = when {
    subjectName.contains("Anatomy", ignoreCase = true) -> """
UPPER LIMB — KEY POINTS

BRACHIAL PLEXUS (C5–T1)
Roots → Trunks → Divisions → Cords → Branches
• Roots: C5, C6, C7, C8, T1
• Trunks: Upper (C5,C6), Middle (C7), Lower (C8,T1)
• Cords: Lateral, Medial, Posterior

NERVE INJURIES — HIGH YIELD
• Axillary nerve (C5,C6): Surgical neck humerus fracture → Loss of shoulder abduction, regimental badge area anaesthesia
• Radial nerve (C5-T1): Mid-shaft humerus → Wrist drop, finger drop
• Median nerve (C6-T1): Carpal tunnel → Ape hand, thenar wasting
• Ulnar nerve (C8,T1): Medial epicondyle → Claw hand, hypothenar wasting

ROTATOR CUFF — SITS Mnemonic
• Supraspinatus — Initiates abduction (0–15°)
• Infraspinatus — Lateral rotation
• Teres minor — Lateral rotation
• Subscapularis — Medial rotation

BLOOD SUPPLY
• Deep palmar arch: Radial artery (main) + deep branch ulnar
• Superficial palmar arch: Ulnar artery (main) + superficial radial

CLINICAL PEARLS
→ Saturday night palsy = Radial nerve in spiral groove
→ Crutch palsy = Radial nerve in axilla (triceps affected)
→ Carpal tunnel = Median nerve compression (most common entrapment)
→ Cubital tunnel = Ulnar nerve at elbow (second most common)
→ Thoracic outlet syndrome = Lower trunk (C8,T1) compression
    """.trimIndent()

    subjectName.contains("Physiology", ignoreCase = true) -> """
RESPIRATORY PHYSIOLOGY — LECTURE NOTES

LUNG VOLUMES (Normal values)
• Tidal Volume (TV): 500 mL
• IRV: 3000 mL
• ERV: 1200 mL
• Residual Volume (RV): 1200 mL — NOT measurable by spirometry
• Total Lung Capacity (TLC): 6000 mL
• Vital Capacity (VC): 4800 mL
• FRC = ERV + RV = 2400 mL

SPIROMETRY PATTERNS
Obstructive (Asthma, COPD):
• ↓ FEV1/FVC ratio (<70%)
• ↑ RV, ↑ FRC, ↑ TLC (air trapping)

Restrictive (Fibrosis):
• Normal or ↑ FEV1/FVC (>70%)
• ↓ TLC, ↓ FVC, ↓ FRC

OXYGEN-HAEMOGLOBIN DISSOCIATION CURVE
Right shift (↓ O2 affinity, ↑ O2 delivery):
• ↑ Temp, ↑ CO2, ↑ H+ (acidosis), ↑ 2,3-DPG

Left shift (↑ O2 affinity, ↓ O2 delivery):
• ↓ Temp, ↓ CO2, alkalosis, HbF, CO poisoning

CONTROL OF BREATHING
• Primary stimulus: PaCO2 (via central chemoreceptors in medulla)
• Hypoxic drive: PaO2 <60 mmHg (peripheral — carotid bodies)
• COPD patients: depend on hypoxic drive — give controlled O2!

SURFACTANT
• Produced by: Type II pneumocytes
• Main component: DPPC (dipalmitoylphosphatidylcholine)
• Function: ↓ surface tension, prevents alveolar collapse
• Deficiency in: Premature infants → RDS (Hyaline membrane disease)
    """.trimIndent()

    subjectName.contains("Pharmacology", ignoreCase = true) -> """
ANTIMICROBIALS — LECTURE NOTES

MECHANISM OF ACTION
Cell wall synthesis inhibitors:
• β-lactams (Penicillin, Cephalosporins): Bind PBPs → inhibit transpeptidation
• Vancomycin (glycopeptide): Binds D-Ala-D-Ala precursors
• Carbapenems: Broadest β-lactam spectrum

Protein synthesis inhibitors:
30S: Aminoglycosides, Tetracyclines
50S: Macrolides, Chloramphenicol, Linezolid, Clindamycin

DNA/RNA synthesis:
• Fluoroquinolones: Inhibit DNA gyrase (gram-neg) and topoisomerase IV
• Rifampicin: Inhibits RNA polymerase
• Metronidazole: Free radical DNA strand breaks (anaerobes/protozoa)

DRUG OF CHOICE CHART
• MRSA: Vancomycin (IV), Linezolid (oral)
• Pseudomonas: Piperacillin-tazobactam, Ceftazidime, Ciprofloxacin
• Atypicals (Mycoplasma, Chlamydia): Doxycycline, Azithromycin
• Anaerobes: Metronidazole, Clindamycin
• ESBL producers: Carbapenems (Meropenem)

ADVERSE EFFECTS
• Aminoglycosides: Nephrotoxicity, Ototoxicity (dose-related)
• Chloramphenicol: Aplastic anaemia (idiosyncratic), Grey baby syndrome
• Tetracyclines: Teeth staining in children, photosensitivity
• Fluoroquinolones: Tendinopathy, QT prolongation, avoid in pregnancy

RESISTANCE MECHANISMS
1. β-lactamase production (most common) → use β-lactamase inhibitors
2. Modified PBPs (MRSA — mecA gene)
3. Efflux pumps
4. Reduced permeability (gram-negatives)
    """.trimIndent()

    else -> """
${subjectName.uppercase()} — LECTURE NOTES

INTRODUCTION
These are comprehensive lecture notes for $subjectName covering high-yield topics for NEET PG examination.

KEY CONCEPTS
This section covers the fundamental principles and clinical correlates of $subjectName that are frequently tested.

Select any text in these notes to:
• 🟢 Highlight in green
• 🟠 Highlight in orange
• 🏷 Add a Tag with your personal notes
• 🌐 Translate to Hindi or Tamil

IMPORTANT TOPICS
The following topics have high weightage in NEET PG:
• Basic concepts and definitions
• Pathophysiology and mechanisms
• Clinical features and diagnosis
• Treatment and management
• High-yield facts and mnemonics

CLINICAL PEARLS
→ Review previous year questions for pattern recognition
→ Focus on mechanisms, not just drug names
→ Make connections between basic science and clinical presentations
→ Use mnemonics for long lists

REVISION STRATEGY
1. First pass: Read all notes thoroughly
2. Highlight key facts using the highlight toolbar
3. Add personal notes using the Tag feature
4. Revise highlights using My Notes section
    """.trimIndent()
}
