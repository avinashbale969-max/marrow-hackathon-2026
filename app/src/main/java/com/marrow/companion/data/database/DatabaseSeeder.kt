package com.marrow.companion.data.database

import com.marrow.companion.data.database.dao.*
import com.marrow.companion.data.database.entities.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val questionDao: QuestionDao
) {
    suspend fun seedIfEmpty() {
        if (questionDao.getCount() > 0) return

        val subjects = listOf(
            SubjectEntity(id = 1,  name = "Medicine",          colorHex = "#1565C0", totalQuestions = 10),
            SubjectEntity(id = 2,  name = "Surgery",           colorHex = "#2E7D32", totalQuestions = 10),
            SubjectEntity(id = 3,  name = "OBG",               colorHex = "#6A1B9A", totalQuestions = 6),
            SubjectEntity(id = 4,  name = "Pediatrics",        colorHex = "#E65100", totalQuestions = 6),
            SubjectEntity(id = 5,  name = "Pharmacology",      colorHex = "#00695C", totalQuestions = 6),
            SubjectEntity(id = 6,  name = "Pathology",         colorHex = "#4527A0", totalQuestions = 6),
            SubjectEntity(id = 7,  name = "Anatomy",           colorHex = "#37474F", totalQuestions = 6),
            SubjectEntity(id = 8,  name = "Physiology",        colorHex = "#558B2F", totalQuestions = 6),
            SubjectEntity(id = 9,  name = "Biochemistry",      colorHex = "#F57F17", totalQuestions = 3),
            SubjectEntity(id = 10, name = "Microbiology",      colorHex = "#00838F", totalQuestions = 3),
            SubjectEntity(id = 11, name = "Forensic Medicine", colorHex = "#546E7A", totalQuestions = 3),
            SubjectEntity(id = 12, name = "Community Medicine",colorHex = "#388E3C", totalQuestions = 3),
            SubjectEntity(id = 13, name = "Ophthalmology",     colorHex = "#0288D1", totalQuestions = 3),
            SubjectEntity(id = 14, name = "ENT",               colorHex = "#7B1FA2", totalQuestions = 3),
            SubjectEntity(id = 15, name = "Orthopedics",       colorHex = "#D84315", totalQuestions = 3),
            SubjectEntity(id = 16, name = "Psychiatry",        colorHex = "#C2185B", totalQuestions = 3),
            SubjectEntity(id = 17, name = "Dermatology",       colorHex = "#689F38", totalQuestions = 3),
            SubjectEntity(id = 18, name = "Radiology",         colorHex = "#01579B", totalQuestions = 3),
            SubjectEntity(id = 19, name = "Anesthesia",        colorHex = "#4A148C", totalQuestions = 3),
        )
        subjectDao.insertAll(subjects)

        val topics = listOf(
            TopicEntity(id = 1,  subjectId = 1,  name = "Infectious Diseases",     questionCount = 5),
            TopicEntity(id = 2,  subjectId = 1,  name = "Cardiology",              questionCount = 5),
            TopicEntity(id = 3,  subjectId = 2,  name = "Abdomen",                 questionCount = 5),
            TopicEntity(id = 4,  subjectId = 2,  name = "Trauma & Burns",          questionCount = 5),
            TopicEntity(id = 5,  subjectId = 3,  name = "Obstetrics",              questionCount = 6),
            TopicEntity(id = 6,  subjectId = 4,  name = "Growth & Development",    questionCount = 6),
            TopicEntity(id = 7,  subjectId = 5,  name = "Antimicrobials",          questionCount = 6),
            TopicEntity(id = 8,  subjectId = 6,  name = "Neoplasia",               questionCount = 6),
            TopicEntity(id = 9,  subjectId = 7,  name = "Upper Limb",              questionCount = 6),
            TopicEntity(id = 10, subjectId = 8,  name = "Respiratory Physiology",  questionCount = 6),
            TopicEntity(id = 11, subjectId = 9,  name = "Metabolic Disorders",     questionCount = 3),
            TopicEntity(id = 12, subjectId = 10, name = "Bacteriology",            questionCount = 3),
            TopicEntity(id = 13, subjectId = 11, name = "Thanatology",             questionCount = 3),
            TopicEntity(id = 14, subjectId = 12, name = "Epidemiology",            questionCount = 3),
            TopicEntity(id = 15, subjectId = 13, name = "Anterior Segment",        questionCount = 3),
            TopicEntity(id = 16, subjectId = 14, name = "Ear Disorders",           questionCount = 3),
            TopicEntity(id = 17, subjectId = 15, name = "Fractures",               questionCount = 3),
            TopicEntity(id = 18, subjectId = 16, name = "Mood Disorders",          questionCount = 3),
            TopicEntity(id = 19, subjectId = 17, name = "Skin Infections",         questionCount = 3),
            TopicEntity(id = 20, subjectId = 18, name = "Imaging Techniques",      questionCount = 3),
            TopicEntity(id = 21, subjectId = 19, name = "General Anaesthesia",     questionCount = 3),
        )
        topicDao.insertAll(topics)

        sampleQuestions().forEach { pair ->
            val (question, options) = pair.withIndexedOptions()
            questionDao.insertQuestionWithOptions(question, options)
        }
    }

    private fun sampleQuestions(): List<Pair<QuestionEntity, List<QuestionOptionEntity>>> = listOf(

        // ── MEDICINE: Infectious Diseases ──────────────────────────────────────
        q(
            subjectId = 1, topicId = 1,
            text = "Rose spots in typhoid fever are caused by:",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2a/Typhoid_fever_rose_spots.jpeg/400px-Typhoid_fever_rose_spots.jpeg",
            explanation = """
Rose spots are 2–4 mm erythematous, slightly raised macules found predominantly on the trunk (abdomen and lower chest) of patients with typhoid fever. They typically appear during the 2nd week of illness.

PATHOGENESIS
Rose spots represent foci of bacterial embolisation. Salmonella typhi organisms multiply within the skin capillaries and small arterioles, causing a localised inflammatory reaction. They are not a hypersensitivity reaction.

CLINICAL FEATURES
• Number: 10–20 spots typically
• Duration: Each spot lasts 3–4 days then fades
• Character: Blanch on pressure (distinguishes from petechiae)
• Location: Central trunk — rarely on limbs or face

DIAGNOSIS
Organisms can be cultured from the rose spots (positive in ~60% of cases). Widal test, blood culture (positive in 1st week), bone marrow culture (most sensitive — 90%).

KEY POINTS TO REMEMBER
→ Rose spots = pathognomonic of typhoid (Salmonella typhi)
→ They blanch on pressure → bacterial emboli (NOT immune complex)
→ Widal test: O antigen titre ≥1:160 is significant in unvaccinated
→ Drug of choice: Ceftriaxone (or Azithromycin); Ciprofloxacin resistance increasing

MNEMONIC: "ROSE = Rickettsia? NO! — Organisms Scattered in Epidermis of typhoid"
            """.trimIndent(),
            options = listOf(
                opt("Bacterial emboli in skin capillaries", true),
                opt("IgE-mediated hypersensitivity", false),
                opt("Circulating endotoxin", false),
                opt("Immune complex deposition", false)
            )
        ),

        q(
            subjectId = 1, topicId = 1,
            text = "Weil-Felix test is positive in:",
            explanation = """
The Weil-Felix test is a serological test used to detect antibodies against rickettsial organisms. It exploits cross-reacting antigens between Rickettsia species and certain strains of Proteus bacteria.

BASIS OF THE TEST
Rickettsiae share polysaccharide antigens with Proteus OX strains. Patient serum is tested for agglutination against:
• Proteus OX-19 (P. vulgaris)
• Proteus OX-2 (P. vulgaris)
• Proteus OX-K (P. mirabilis)

INTERPRETATION TABLE
Disease                    | OX-19 | OX-2 | OX-K
Epidemic typhus (R. prowazekii) | +++ | +   | –
Murine typhus (R. typhi)       | +++  | +   | –
Scrub typhus (O. tsutsugamushi)| –    | –   | +++
Rocky Mountain SF (R. rickettsii)| ++ | ++  | –
Q fever (Coxiella burnetii)    | –    | –   | –
Rickettsial pox (R. akari)     | –    | –   | –

IMPORTANT NEGATIVES
Weil-Felix is NEGATIVE in: Q fever, Rickettsial pox, Brucellosis, Leptospirosis, Typhoid.

LIMITATIONS
• Low sensitivity (~50%) and low specificity
• False positives: UTI caused by Proteus spp.
• Better tests: IFA (immunofluorescence), ELISA for specific Rickettsia antibodies

KEY POINT: Weil-Felix OX-K positivity = Scrub typhus (mnemonic: K = scrub typhus endemic in Kerala/Kutch)
            """.trimIndent(),
            options = listOf(
                opt("Rickettsial infections", true),
                opt("Typhoid fever", false),
                opt("Brucellosis", false),
                opt("Leptospirosis", false)
            )
        ),

        q(
            subjectId = 1, topicId = 1,
            text = "Drug of choice for scrub typhus is:",
            explanation = """
Scrub typhus is caused by Orientia tsutsugamushi (previously Rickettsia tsutsugamushi), transmitted by trombiculid mite larvae (chiggers). It is endemic in the "tsutsugamushi triangle" — from Japan to Pakistan and Australia.

DRUG OF CHOICE: DOXYCYCLINE
• Mechanism: Inhibits 30S ribosomal subunit → blocks protein synthesis
• Dose: 100 mg BD × 7 days (adults)
• Effect: Dramatic improvement within 24–48 hours (fever lysis — diagnostic)
• Also the DOC for: All rickettsial infections, Q fever, Chlamydia, Atypical pneumonia

ALTERNATIVES
• Azithromycin: DOC in pregnancy and children <8 years (doxycycline stains teeth in children)
• Chloramphenicol: Historical alternative, still used in resource-limited settings
• Rifampicin: Used in doxycycline resistance (Southeast Asia)

CLINICAL FEATURES TO REMEMBER
1. Eschar (tache noire): pathognomonic — black crusted painless ulcer at bite site
2. Fever + rash + eschar = classic triad
3. Weil-Felix: OX-K positive
4. Complications: Meningoencephalitis, myocarditis, ARDS, AKI

MNEMONIC: "DART" = Doxycycline for All Rickettsial Treatment
            """.trimIndent(),
            options = listOf(
                opt("Doxycycline", true),
                opt("Penicillin", false),
                opt("Ciprofloxacin", false),
                opt("Metronidazole", false)
            )
        ),

        q(
            subjectId = 1, topicId = 1,
            text = "Which malaria species causes blackwater fever?",
            explanation = """
Blackwater fever is a severe, life-threatening complication of malaria characterised by massive intravascular haemolysis leading to haemoglobinuria (dark cola-coloured urine) — hence the name "blackwater."

CAUSATIVE SPECIES: Plasmodium falciparum (exclusively)

PATHOGENESIS
The exact mechanism is not fully understood, but involves:
1. Complement-mediated lysis of parasitised red blood cells
2. Autoimmune haemolysis of non-parasitised RBCs
3. Previously associated with irregular quinine use (now rare)
4. Host genetic factors (G6PD deficiency increases susceptibility)

CLINICAL FEATURES
• Sudden onset rigors, fever, vomiting
• Dark brown/black urine (haemoglobinuria — NOT haematuria)
• Jaundice (from haemolysis)
• Anaemia (rapid, severe)
• Acute renal failure (haemoglobin cast nephropathy) — most fatal complication
• Mortality: 20–30% if renal failure develops

MANAGEMENT
• IV artesunate (DOC for severe falciparum)
• IV fluids (prevent renal failure)
• Blood transfusion
• Dialysis if AKI develops
• Avoid quinine (can worsen haemolysis)

COMPARISON OF SPECIES
Species      | Fever Cycle | Unique Feature
P. falciparum | 36–48 h (malignant tertian) | Blackwater fever, cerebral malaria
P. vivax      | 48 h (benign tertian)       | Relapse (hypnozoites in liver)
P. malariae   | 72 h (quartan)              | Nephrotic syndrome
P. ovale      | 48 h                        | Mild, self-limiting
            """.trimIndent(),
            options = listOf(
                opt("Plasmodium falciparum", true),
                opt("Plasmodium vivax", false),
                opt("Plasmodium malariae", false),
                opt("Plasmodium ovale", false)
            )
        ),

        q(
            subjectId = 1, topicId = 1,
            text = "The incubation period of rabies is longest when the bite is on the:",
            explanation = """
Rabies incubation period ranges from 10 days to over a year (average 1–3 months). The critical determinant is the distance the virus must travel from the bite site to the CNS via retrograde axonal transport along peripheral nerves.

WHY LOWER LIMB = LONGEST INCUBATION
• Lower limbs (especially feet) are the farthest from the brain
• The virus travels at ~12–24 mm/day along peripheral nerve axons
• Distance from foot to brain: ~100–150 cm → weeks to months of travel
• Face/head bites: shortest distance → shortest incubation (as little as 10 days)

FACTORS AFFECTING INCUBATION
1. Distance from CNS (most important) — lower limb > trunk > upper limb > neck > face
2. Severity of bite — deep bites, multiple bites: shorter incubation
3. Amount of virus inoculated
4. Innervation density of bite site

PATHOGENESIS
Virus → binds nicotinic ACh receptors → enters motor nerve endings → retrograde axonal transport → spinal cord → brainstem → limbic system → hippocampus (explains behavioural changes).

CLINICAL STAGES
1. Prodrome: Paraesthesia at bite site, fever, anxiety (2–10 days)
2. Acute neurological phase:
   • Furious rabies (80%): hydrophobia, aerophobia, agitation
   • Dumb/paralytic rabies (20%): ascending paralysis (like GBS)
3. Coma and death (invariably fatal once symptoms appear)

POST-EXPOSURE PROPHYLAXIS (PEP)
• Wound washing with soap + water (most important first step)
• Rabies vaccine: 4-dose schedule (days 0, 3, 7, 14)
• Rabies immunoglobulin (RIG): for category III bites
            """.trimIndent(),
            options = listOf(
                opt("Lower limb", true),
                opt("Face", false),
                opt("Neck", false),
                opt("Hand", false)
            )
        ),

        // ── MEDICINE: Cardiology ───────────────────────────────────────────────
        q(
            subjectId = 1, topicId = 2,
            text = "The earliest cardiac enzyme to rise after acute MI is:",
            explanation = """
Cardiac biomarkers are released into the bloodstream following myocardial necrosis. Timing of elevation is crucial for diagnosis and management of acute MI.

BIOMARKER TIMELINE AFTER ACUTE MI
Biomarker  | Rises    | Peak    | Returns to Normal | Specificity
Myoglobin  | 1–2 h   | 4–6 h  | 24 h             | Low (also in skeletal muscle)
CK-MB      | 3–4 h   | 12–24 h| 48–72 h          | High (but some in skeletal muscle)
Troponin I | 3–4 h   | 12–24 h| 5–10 days        | Highest (cardiac specific)
Troponin T | 3–4 h   | 12–24 h| 10–14 days       | Highest (cardiac specific)
LDH        | 8–12 h  | 24–72 h| 10–14 days       | Low (very non-specific)
AST (SGOT) | 6–8 h   | 24–48 h| 3–5 days         | Non-specific

KEY POINTS
• EARLIEST to rise: Myoglobin (1–2 h) — but non-specific
• MOST SPECIFIC and SENSITIVE: Troponin I and T (gold standard)
• BEST for REINFARCTION: CK-MB (normalises by 72h, re-elevation indicates new MI; Troponin stays elevated too long)
• LAST to normalise: LDH and Troponin T (useful for late presentation)

MNEMONIC: "My (Myoglobin) Cardiac (CK-MB) Troponin (Troponin I/T) Levels Are High"
Order of rise: M → CK → TnI/TnT → LDH

HIGH-SENSITIVITY TROPONIN (hsTnT)
• Can detect MI at 1 hour with serial measurements
• Negative predictive value >99% at 3 hours
• Replaces standard troponin in modern practice
            """.trimIndent(),
            options = listOf(
                opt("Myoglobin", true),
                opt("Troponin I", false),
                opt("CK-MB", false),
                opt("LDH", false)
            )
        ),

        q(
            subjectId = 1, topicId = 2,
            text = "Dressler's syndrome occurs after MI due to:",
            explanation = """
Dressler's syndrome (post-cardiac injury syndrome) is a form of pericarditis occurring 2–10 weeks after acute MI. It is an autoimmune reaction and represents one of the late complications of MI.

PATHOGENESIS
1. Myocardial necrosis releases cardiac antigens (myosin, troponin) into the circulation
2. These act as autoantigens → immune sensitisation
3. Autoantibodies and T-cell mediated immunity attack the pericardium and pleura
4. Anti-heart antibodies detectable in serum

CLINICAL FEATURES
• Timing: 2–10 weeks post-MI (can occur after cardiac surgery or trauma — hence "post-cardiac injury syndrome")
• Fever + pleuritic chest pain (sharp, worse on lying flat, better sitting forward)
• Pericardial friction rub
• Pleural and pericardial effusion on CXR/echo
• Elevated ESR, CRP, leucocytosis
• ECG: Saddle-shaped ST elevation (diffuse), PR depression

DIFFERENTIATION FROM EARLY PERICARDITIS
Feature          | Early Pericarditis | Dressler's
Timing           | Within 24–72 h     | 2–10 weeks
Cause            | Extension of MI    | Autoimmune
Troponin         | Elevated           | Normal/mildly elevated
Response to NSAIDs| Variable          | Good

TREATMENT
• NSAIDs (aspirin 650 mg QID) — first-line
• Colchicine — added for recurrence prevention
• Steroids — reserved for refractory cases
• Avoid anticoagulants (risk of haemorrhagic pericarditis)
            """.trimIndent(),
            options = listOf(
                opt("Autoimmune reaction to myocardial antigens", true),
                opt("Direct bacterial infection of pericardium", false),
                opt("Extension of MI to pericardium", false),
                opt("Pulmonary embolism", false)
            )
        ),

        q(
            subjectId = 1, topicId = 2,
            text = "Pulsus paradoxus is seen in:",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Pulsus_paradoxus.png/400px-Pulsus_paradoxus.png",
            explanation = """
Pulsus paradoxus is defined as an exaggerated fall in systolic blood pressure of MORE than 10 mmHg during normal inspiration.

NORMAL PHYSIOLOGY
During inspiration, intrathoracic pressure decreases → venous return to right heart increases → right ventricle expands → interventricular septum shifts left → left ventricular filling decreases → slight fall in BP (normally <10 mmHg).

PATHOLOGICAL EXAGGERATION (>10 mmHg drop)
In cardiac tamponade, the pericardial fluid is under tension. Both ventricles are constrained by the rigid pericardial sac. The normal inspiratory increase in RV filling further compresses the LV (exaggerated ventricular interdependence) → marked fall in LV output and BP.

CONDITIONS CAUSING PULSUS PARADOXUS
• Cardiac tamponade ★ (classic — MOST IMPORTANT)
• Severe bronchial asthma (large negative intrathoracic pressure)
• Constrictive pericarditis (mild, inconsistent)
• Massive pulmonary embolism
• Tension pneumothorax
• Hypovolaemic shock

BECK'S TRIAD of Cardiac Tamponade
1. Hypotension (↓BP)
2. Elevated JVP / distended neck veins
3. Muffled heart sounds

HOW TO MEASURE
• Using sphygmomanometer, inflate cuff above systolic
• Slowly deflate — note pressure when Korotkoff sounds are first heard (inspiration only)
• Continue deflating — note when sounds heard throughout respiratory cycle
• Difference >10 mmHg = pulsus paradoxus

KUSSMAUL'S SIGN vs PULSUS PARADOXUS
• Kussmaul sign: JVP RISES on inspiration (constrictive pericarditis, RV failure)
• Pulsus paradoxus: BP FALLS on inspiration (tamponade)
            """.trimIndent(),
            options = listOf(
                opt("Cardiac tamponade", true),
                opt("Aortic stenosis", false),
                opt("Mitral regurgitation", false),
                opt("VSD", false)
            )
        ),

        q(
            subjectId = 1, topicId = 2,
            text = "The most common cause of mitral stenosis is:",
            explanation = """
Mitral stenosis (MS) is the narrowing of the mitral valve orifice, obstructing blood flow from the left atrium to left ventricle during diastole. Normal mitral valve area: 4–6 cm². Severe MS: <1 cm².

AETIOLOGY
• Rheumatic heart disease: >99% of cases worldwide (especially in developing countries like India)
• Congenital MS: Rare — parachute mitral valve
• Other rare causes: Lutembacher syndrome, carcinoid, SLE, RA (very rare)

RHEUMATIC PATHOLOGY
Group A Streptococcus pharyngitis → rheumatic fever → carditis → valvular scarring:
• Leaflet thickening and fibrosis
• Commissural fusion (characteristic)
• Chordae tendineae shortening, thickening, fusion
• Calcification (late disease)

HAEMODYNAMIC CONSEQUENCES
Obstruction → ↑LA pressure → LA enlargement → Pulmonary venous hypertension → Pulmonary arterial hypertension → RV hypertrophy and failure

SYMPTOMS (in order of severity progression)
1. Dyspnoea on exertion
2. Orthopnoea, PND
3. Haemoptysis (rupture of bronchial veins)
4. Features of pulmonary hypertension (RVF)

AUSCULTATION
• Loud S1 (mitral snap)
• Opening snap (OS) — early in diastole
• Mid-diastolic rumble (MDR) — low-pitched, best heard at apex with bell
• Shorter S2-OS interval = more severe MS

COMPLICATIONS: AF (most common), systemic embolism (stroke), IE, pulmonary hypertension
            """.trimIndent(),
            options = listOf(
                opt("Rheumatic fever", true),
                opt("Infective endocarditis", false),
                opt("Congenital heart disease", false),
                opt("SLE", false)
            )
        ),

        q(
            subjectId = 1, topicId = 2,
            text = "In ECG, the PR interval represents:",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9e/SinusRhythmLabels.svg/600px-SinusRhythmLabels.svg.png",
            explanation = """
The electrocardiogram (ECG) measures the electrical activity of the heart. Each component of the ECG waveform corresponds to a specific event in the cardiac cycle.

PR INTERVAL: 0.12–0.20 seconds (3–5 small squares)

WHAT IT REPRESENTS
The PR interval measures the time from the onset of atrial depolarisation (start of P wave) to the onset of ventricular depolarisation (start of QRS complex). It includes:
1. Atrial depolarisation (P wave itself)
2. AV nodal delay (the critical "gatekeeper" delay)
3. Conduction through the Bundle of His and its branches

The AV nodal delay (0.06–0.10s) is the dominant component — it allows complete atrial systole before ventricular systole begins.

CLINICAL SIGNIFICANCE
PR INTERVAL      | Interpretation
<0.12 s (short)  | Pre-excitation syndromes (WPW — delta wave), Lown-Ganong-Levine
0.12–0.20 s      | Normal
>0.20 s          | 1st degree AV block (delayed AV conduction)
Progressive ↑PR  | 2nd degree AV block, Mobitz type I (Wenckebach)
Fixed ↑PR + dropped beats | 2nd degree AV block, Mobitz type II
No relationship P-QRS | 3rd degree (complete) AV block

ECG INTERVALS SUMMARY
Wave/Interval | Normal Duration | Represents
P wave        | <0.12 s         | Atrial depolarisation
PR interval   | 0.12–0.20 s     | AV conduction (SA→Bundle of His)
QRS complex   | <0.12 s         | Ventricular depolarisation
ST segment    | Isoelectric     | Ventricular plateau (phase 2)
T wave        | Variable        | Ventricular repolarisation
QT interval   | <0.44 s (men), <0.46 s (women) | Ventricular depolarisation + repolarisation

MNEMONIC: "PR = Pause at the node for the Room to fill (AV node delay allows LA to empty into LV)"
            """.trimIndent(),
            options = listOf(
                opt("AV nodal conduction time", true),
                opt("Ventricular depolarisation", false),
                opt("Ventricular repolarisation", false),
                opt("Atrial repolarisation only", false)
            )
        ),

        // ── SURGERY ────────────────────────────────────────────────────────────
        q(
            subjectId = 2, topicId = 3,
            text = "McBurney's point is located at:",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/McBurney%27s_point.jpg/300px-McBurney%27s_point.jpg",
            explanation = """
McBurney's point is a clinical landmark used in the diagnosis of acute appendicitis. It was described by Charles McBurney in 1889.

LOCATION
McBurney's point = the point 1/3 of the way from the right anterior superior iliac spine (ASIS) to the umbilicus on the right side.

This approximates the base of the appendix, which arises from the posteromedial wall of the caecum, 2 cm below the ileocaecal valve.

CLINICAL SIGNS OF APPENDICITIS AT McBURNEY'S POINT
1. McBurney's tenderness: Direct tenderness at this point
2. McBurney's sign: Rebound tenderness (release pain > palpation pain)
3. Rovsing's sign: Pain at McBurney's point when left iliac fossa is pressed (gas displacement)
4. Psoas sign: Pain on passive extension of right hip (retrocaecal appendix)
5. Obturator sign: Pain on internal rotation of flexed right hip (pelvic appendix)

SCORING SYSTEMS
Alvarado score (MANTRELS):
M - Migration of pain to RIF: 1
A - Anorexia: 1
N - Nausea/Vomiting: 1
T - Tenderness in RIF: 2
R - Rebound tenderness: 1
E - Elevated temperature: 1
L - Leucocytosis: 2
S - Shift of WBC to left: 1
TOTAL: 10 points; Score ≥7 = likely appendicitis

POSITIONS OF APPENDIX (affects presentation)
• Retrocaecal (75%): Most common — psoas sign positive
• Pelvic (20%): Urinary/bowel symptoms — obturator sign positive
• Subcaecal, pre-ileal, post-ileal: Various symptoms

INVESTIGATION OF CHOICE: Ultrasound abdomen; CT scan (gold standard for diagnosis)
            """.trimIndent(),
            options = listOf(
                opt("1/3 from ASIS to umbilicus on right", true),
                opt("2/3 from ASIS to umbilicus on right", false),
                opt("Mid-point of right inguinal ligament", false),
                opt("Right iliac fossa, 2 cm above inguinal ligament", false)
            )
        ),

        q(
            subjectId = 2, topicId = 3,
            text = "Courvoisier's law states that in obstructive jaundice, a palpable gall bladder suggests:",
            explanation = """
Courvoisier's law (1890): "In obstructive jaundice, if the gallbladder is palpable and non-tender, the obstruction is unlikely to be due to gallstones."

REASONING
• Cholelithiasis (gallstones): Chronic inflammation → fibrosis and scarring of the gallbladder wall → the GB cannot dilate → NOT palpable
• Carcinoma of head of pancreas: The bile duct is compressed externally → the gallbladder is normal (no prior inflammation) → distends easily → PALPABLE

CAUSES OF PALPABLE GB IN OBSTRUCTIVE JAUNDICE (Courvoisier's sign positive)
1. Carcinoma of head of pancreas ★★ (most common)
2. Cholangiocarcinoma (Klatskin tumour)
3. Carcinoma of ampulla of Vater
4. Carcinoma of bile duct

EXCEPTIONS TO COURVOISIER'S LAW
• Stone impacted in Hartmann's pouch (Mirizzi syndrome) — can cause GB distension
• Double pathology: Stone + carcinoma coexisting
• Mucocele of GB with impacted cystic duct stone

CARCINOMA HEAD OF PANCREAS — KEY FEATURES
• Painless progressive jaundice + weight loss + anorexia
• Obstructive jaundice: dark urine, pale stools, pruritus
• Trousseau's sign: Migratory thrombophlebitis
• CA 19-9: Tumour marker (not specific, used for monitoring)
• Contrast CT: Best investigation for diagnosis and staging
• Whipple's procedure (pancreaticoduodenectomy): Curative surgery
• 5-year survival: <5% (poor prognosis)

MNEMONIC: Courvoisier's = Carcinoma → Can dilate (no prior inflammation)
            """.trimIndent(),
            options = listOf(
                opt("Carcinoma of head of pancreas", true),
                opt("Choledocholithiasis", false),
                opt("Acute cholecystitis", false),
                opt("Primary sclerosing cholangitis", false)
            )
        ),

        q(
            subjectId = 2, topicId = 3,
            text = "Murphy's sign is positive in:",
            explanation = """
Murphy's sign is a clinical examination finding used to diagnose acute cholecystitis. It was described by John B. Murphy in 1903.

HOW TO ELICIT MURPHY'S SIGN
1. Place your fingers under the right costal margin at the midclavicular line (over the gallbladder fossa)
2. Ask the patient to take a deep breath
3. As the diaphragm descends, the inflamed gallbladder moves downward and strikes your fingers
4. Positive: Patient experiences sudden pain causing INSPIRATORY ARREST (abruptly stops breathing in)

ACUTE CHOLECYSTITIS — PATHOPHYSIOLOGY
• Gallstone impaction in cystic duct → bile accumulation → chemical inflammation → secondary bacterial infection (E. coli, Klebsiella, Enterococcus)
• 90–95% associated with gallstones (calculous cholecystitis)

CLINICAL FEATURES
• RUQ pain (biliary colic → constant pain)
• Fever, nausea, vomiting
• Murphy's sign positive
• Boas' sign: Right subscapular pain (referred via phrenic nerve)
• WBC elevated; mild jaundice may occur

INVESTIGATIONS
• USG abdomen: Investigation of choice — shows GB wall thickening (>4 mm), pericholecystic fluid, gallstones, positive sonographic Murphy's sign
• HIDA scan (hepatobiliary iminodiacetic acid): Most sensitive — if GB not visualised = cystic duct obstruction

TREATMENT
• Conservative: NBM, IV fluids, antibiotics (ceftriaxone + metronidazole)
• Definitive: Laparoscopic cholecystectomy (within 72h ideally)

COMPLICATIONS: Empyema, perforation, pericholecystic abscess, Mirizzi syndrome, cholecystoenteric fistula, gallstone ileus

ULTRASOUND FINDING: "Wall-echo-shadow" (WES) sign — multiple gallstones
            """.trimIndent(),
            options = listOf(
                opt("Acute cholecystitis", true),
                opt("Acute pancreatitis", false),
                opt("Appendicitis", false),
                opt("Peptic ulcer perforation", false)
            )
        ),

        q(
            subjectId = 2, topicId = 3,
            text = "The most common site of carcinoma of the colon in India is:",
            explanation = """
Colorectal carcinoma is the third most common cancer worldwide but its distribution differs between developed and developing countries.

DISTRIBUTION IN INDIA vs WEST
Location        | India         | Western Countries
Rectum + RS     | 50–55% ★     | 25%
Sigmoid colon   | 20%           | 25%
Right colon     | 15%           | 35% (increasing)
Transverse      | 10%           | 10%
Left colon      | 5%            | 5%

KEY DIFFERENCE: In India, rectum and rectosigmoid junction predominate. In Western countries, the right colon (caecum, ascending colon) is increasingly common, possibly due to diet differences (higher fat, lower fibre) and molecular differences (microsatellite instability pathway common in right-sided CRC).

CLINICAL PRESENTATION BY LOCATION
Right colon (caecum/ascending): Insidious → occult bleeding, anaemia, weight loss, palpable mass (presents LATE)
Left colon (descending/sigmoid): Change in bowel habits, constipation, PR bleeding, obstruction
Rectum: Fresh PR bleeding (bright red), tenesmus, mucus in stool

RISK FACTORS
• Familial adenomatous polyposis (FAP) — APC gene mutation
• Hereditary non-polyposis CRC (Lynch syndrome — HNPCC) — MLH1, MSH2 genes
• IBD (UC > Crohn's) — after 10 years
• Diet: High fat, low fibre, red/processed meat
• Smoking, alcohol, obesity

STAGING (Duke's → TNM)
Duke's A: Mucosa/submucosa → 5-yr survival >90%
Duke's B: Through muscularis, no LN → 70–80%
Duke's C: LN involved → 30–40%
Duke's D: Distant metastases → <5%

MNEMONIC for right vs left colon CRC: "Right = Silent (occult bleed, anaemia), Left = Loud (obstruction, PR bleed)"
            """.trimIndent(),
            options = listOf(
                opt("Rectum and rectosigmoid", true),
                opt("Caecum", false),
                opt("Transverse colon", false),
                opt("Splenic flexure", false)
            )
        ),

        q(
            subjectId = 2, topicId = 3,
            text = "Virchow's triad for venous thrombosis consists of:",
            explanation = """
Virchow's triad was described by Rudolf Virchow in 1856. It identifies three major factors contributing to thrombosis formation in blood vessels, particularly veins.

THE THREE COMPONENTS

1. ENDOTHELIAL INJURY / DAMAGE
• Normally, intact endothelium is antithrombotic (produces prostacyclin, NO, thrombomodulin)
• Injury exposes collagen and tissue factor → platelet adhesion → coagulation cascade
• Causes: Surgery, trauma, catheter insertion, infection (sepsis), hypertension

2. STASIS OF BLOOD FLOW
• Normal laminar flow keeps platelets away from vessel wall
• Stasis allows platelet-vessel wall contact and localised concentration of activated clotting factors
• Causes: Prolonged immobility (long flights, bed rest), heart failure, varicose veins, obesity

3. HYPERCOAGULABILITY (Thrombophilia)
• Congenital: Factor V Leiden (most common hereditary thrombophilia), Prothrombin G20210A, Protein C/S deficiency, Antithrombin III deficiency
• Acquired: OCP use, malignancy (Trousseau syndrome), pregnancy, antiphospholipid syndrome, nephrotic syndrome

DEEP VEIN THROMBOSIS (DVT) — CLINICAL
• Calf pain, swelling, warmth, erythema
• Homan's sign: Calf pain on dorsiflexion (not sensitive or specific)
• Wells score: Clinical probability scoring

DIAGNOSIS
• D-dimer: High sensitivity, low specificity (good for ruling out)
• Doppler USG: Investigation of choice for DVT
• CT pulmonary angiography: Gold standard for PE

TREATMENT
• LMWH (Heparin) → transition to DOAC (rivaroxaban, apixaban) or Warfarin
• Duration: Provoked DVT = 3 months; Unprovoked = 3–6 months; Recurrent = lifelong
            """.trimIndent(),
            options = listOf(
                opt("Endothelial injury, stasis, hypercoagulability", true),
                opt("Arterial spasm, platelet aggregation, fibrin", false),
                opt("Low antithrombin, low protein C, stasis", false),
                opt("Hypertension, smoking, hyperlipidaemia", false)
            )
        ),

        q(
            subjectId = 2, topicId = 4,
            text = "Wallace rule of nines: the head and neck account for what percentage of body surface area in an adult?",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3b/Burns_percentage_-_adult.JPG/250px-Burns_percentage_-_adult.JPG",
            explanation = """
The Wallace Rule of Nines (1951) is a rapid bedside method to estimate the total body surface area (TBSA) burned in adults. It divides the body into regions each representing approximately 9% (or multiples thereof).

RULE OF NINES — ADULT
Body Region                   | % TBSA
Head and neck                 | 9% ★
Each upper limb (arm)         | 9% each (18% total)
Chest (anterior trunk)        | 9%
Abdomen (anterior trunk)      | 9%
Upper back                    | 9%
Lower back/buttocks            | 9%
Each thigh                    | 9%
Each leg + foot               | 9%
Genitalia/perineum            | 1%
TOTAL                         | 100%

MODIFICATIONS FOR CHILDREN (Lund & Browder chart)
In children, the head is proportionally larger:
Age      | Head | Each leg
Infant   | 18%  | 14%
5 years  | 13%  | 16%
10 years | 11%  | 17%
Adult    | 9%   | 18%
(Lund & Browder is more accurate for children than Rule of Nines)

FLUID RESUSCITATION — PARKLAND FORMULA
4 mL × Weight (kg) × % TBSA = Total Ringer's Lactate in 24 hours
• First 8 hours (from time of burn): Half the calculated volume
• Next 16 hours: Remaining half
• Colloid added after 24 hours if needed

BURN DEPTH CLASSIFICATION
Degree | Depth | Appearance | Pain | Healing
1st   | Epidermis | Red, dry | ++ | 3–5 days
2nd superficial | Superficial dermis | Blistered, moist, pink | +++ | 14–21 days
2nd deep | Deep dermis | White, wet | + | >21 days, graft needed
3rd   | Full thickness | White/black, leathery | None (nerve damage) | Graft essential
4th   | Subcutaneous/bone | Charred | None | Amputation may be needed
            """.trimIndent(),
            options = listOf(
                opt("9%", true),
                opt("18%", false),
                opt("4.5%", false),
                opt("14%", false)
            )
        ),

        q(
            subjectId = 2, topicId = 4,
            text = "Parkland formula for burns fluid resuscitation uses:",
            explanation = """
The Parkland formula (Baxter-Shires formula) is the most widely used formula for fluid resuscitation in adults with major burns. It was developed at Parkland Memorial Hospital, Dallas by Charles Baxter.

PARKLAND FORMULA
Total volume in 24 hours = 4 mL × Weight (kg) × % TBSA burned

Fluid of choice: Ringer's Lactate (Hartmann's solution)
• Composition: Na 130 mEq/L, Cl 109 mEq/L, K 4 mEq/L, Ca 3 mEq/L, Lactate 28 mEq/L
• Why RL? Closest to plasma composition, buffered (lactate converted to bicarbonate)
• NOT normal saline (hyperchloraemic acidosis risk)

TIME SCHEDULE
• First 8 hours: ½ of total volume (calculated from TIME OF BURN, not time of admission)
• Next 16 hours: ½ of total volume

IMPORTANT NOTE: If patient arrives late (say, 4 hours after burn), the first 8-hour period is already partially elapsed. Calculate remaining time and give remaining portion in that time.

ALTERNATIVE FORMULAS
Formula       | Volume        | Fluid
Parkland      | 4 mL/kg/% TBSA| Ringer's Lactate
Modified Brooke| 2 mL/kg/% TBSA| Ringer's Lactate
Muir & Barclay (UK) | Complex | Colloid + electrolyte
Evans         | Complex       | Saline + Colloid

MONITORING FLUID RESUSCITATION
• Target urine output: 0.5–1 mL/kg/hour (adults), 1 mL/kg/hour (children)
• If UO too low: increase rate by 10–20%
• If UO too high: decrease rate

WHEN TO ADD COLLOID
After 24 hours: 5% albumin may be added to reduce total crystalloid requirement (reduces oedema and abdominal compartment syndrome risk)

COMPLICATIONS OF UNDER-RESUSCITATION: Renal failure, multi-organ failure
COMPLICATIONS OF OVER-RESUSCITATION: Abdominal compartment syndrome, pulmonary oedema, "fluid creep"
            """.trimIndent(),
            options = listOf(
                opt("Ringer's lactate 4 mL/kg/% burn", true),
                opt("Normal saline 2 mL/kg/% burn", false),
                opt("Colloid 1 mL/kg/% burn", false),
                opt("5% dextrose 3 mL/kg/% burn", false)
            )
        ),

        q(
            subjectId = 2, topicId = 4,
            text = "The most common nerve injured in fracture of surgical neck of humerus is:",
            explanation = """
The surgical neck of humerus is the narrowest part of the humerus just below the greater and lesser tubercles, proximal to the shaft. It is a common fracture site in elderly (osteoporotic) patients after a fall on an outstretched hand.

AXILLARY NERVE — ANATOMY
• Origin: Posterior cord of brachial plexus (C5, C6)
• Course: Winds around the surgical neck of humerus within the quadrilateral space
• Quadrilateral space boundaries: Teres major (inferior), teres minor (superior), long head of triceps (medial), humerus (lateral)

AXILLARY NERVE SUPPLIES
Motor:
• Deltoid muscle (shoulder abduction — main function)
• Teres minor (lateral rotation)

Sensory:
• "Regimental badge area" = Lateral upper arm (just below the deltoid tuberosity)

CLINICAL FEATURES OF AXILLARY NERVE INJURY
• Loss of shoulder abduction (deltoid paralysis) — cannot initiate abduction
• Flattening of shoulder contour (loss of deltoid bulk)
• Anaesthesia over regimental badge area
• Test: Ask patient to abduct arm > 15° against resistance

COMPARISON WITH OTHER HUMERUS FRACTURES
Fracture Site    | Nerve Injured | Clinical Feature
Surgical neck    | Axillary (C5,C6) | Loss of shoulder abduction, regimental badge area anaesthesia
Mid-shaft        | Radial nerve  | Wrist drop, loss of finger/thumb extension, anaesthesia dorsum of hand
Supracondylar    | Anterior interosseous (AIN) | Loss of FPL, FDP of index finger (pen test positive)
Medial epicondyle| Ulnar nerve   | Claw hand (ring+little), ulnar anaesthesia

TREATMENT OF SURGICAL NECK FRACTURE
• Undisplaced: Conservative (arm sling, physiotherapy)
• Displaced: ORIF (open reduction internal fixation) or hemiarthroplasty (elderly)
• Nerve injury: Usually neuropraxia → observe for 3–6 months; if no recovery → surgery
            """.trimIndent(),
            options = listOf(
                opt("Axillary nerve", true),
                opt("Radial nerve", false),
                opt("Musculocutaneous nerve", false),
                opt("Median nerve", false)
            )
        ),

        q(
            subjectId = 2, topicId = 4,
            text = "'Dinner fork' deformity is seen in fracture of:",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/9/95/Colles_fracture.jpg/250px-Colles_fracture.jpg",
            explanation = """
Colles' fracture is a transverse fracture of the distal radius within 2.5 cm of the wrist, with:
1. Dorsal displacement of distal fragment
2. Dorsal angulation (apex volar)
3. Radial deviation
4. Impaction (shortening)
5. Supination of distal fragment

MECHANISM: Fall on outstretched hand (FOOSH) with wrist dorsiflexed — most common in elderly osteoporotic women.

DINNER FORK DEFORMITY
When viewed from the side, the displacement of the distal fragment gives the appearance of a dinner fork (the angulation creates a step that resembles the tines of a fork). Also called "bayonet" deformity.

X-RAY FEATURES
• Dorsal angulation of distal fragment
• Impaction
• Radial shift
• Ulnar styloid fracture (50% of cases)

COLLES' vs SMITH'S FRACTURE
Feature           | Colles' Fracture | Smith's Fracture
Displacement      | Dorsal (backward) | Volar (forward) — "reverse Colles'"
Mechanism         | FOOSH (wrist extended) | Fall on flexed wrist
Deformity         | Dinner fork       | Garden spade
Age group         | Elderly (osteoporotic) | Young adults
Frequency         | Common            | Less common

TREATMENT
• Undisplaced: Below-elbow plaster in "position of safe immobilisation" (wrist: slight ulnar deviation + volar flexion)
• Displaced: Reduction (under haematoma block/conscious sedation) + plaster
• Comminuted/unstable: External fixator or ORIF with volar locking plate

COMPLICATIONS
• Malunion (most common complication)
• Sudeck's atrophy (complex regional pain syndrome)
• Median nerve injury (acute carpal tunnel syndrome)
• Extensor pollicis longus rupture (late)
• Triangular fibrocartilage complex (TFCC) injury
            """.trimIndent(),
            options = listOf(
                opt("Distal radius (Colles' fracture)", true),
                opt("Scaphoid", false),
                opt("Clavicle", false),
                opt("Olecranon", false)
            )
        ),

        // ── OBG: Obstetrics ────────────────────────────────────────────────────
        q(subjectId = 3, topicId = 5,
            text = "Pre-eclampsia is defined as hypertension after 20 weeks with:",
            explanation = """Pre-eclampsia: New-onset hypertension (BP ≥140/90 mmHg) after 20 weeks with proteinuria (≥300 mg/24h or PCR ≥0.3) OR end-organ dysfunction.

PATHOGENESIS: Defective trophoblast invasion → placental ischaemia → systemic endothelial dysfunction → hypertension, proteinuria, multi-organ damage.

SEVERE FEATURES (any one): BP ≥160/110, thrombocytopenia <100,000, renal failure (creatinine >1.1 mg/dL), hepatic involvement, pulmonary oedema, new-onset headache/visual disturbance.

MANAGEMENT: Definitive treatment = delivery. Antihypertensives: labetalol (first-line), nifedipine, methyldopa. Seizure prophylaxis: MgSO4 (loading dose 4–6 g IV, then 1–2 g/h infusion).

COMPLICATIONS: Eclampsia (seizures), HELLP syndrome (Haemolysis, Elevated Liver enzymes, Low Platelets), abruption, DIC, IUGR.""",
            options = listOf(opt("Proteinuria ≥300 mg/24h", true), opt("Pedal oedema alone", false), opt("Headache and visual changes only", false), opt("BP ≥160/110 mmHg only", false))
        ),
        q(subjectId = 3, topicId = 5,
            text = "The most common cause of postpartum haemorrhage is:",
            explanation = """PPH = blood loss >500 mL after vaginal delivery, >1000 mL after CS, within 24 hours (primary) or 24h–12 weeks (secondary).

THE 4 T's OF PPH:
• TONE (80%): Uterine atony — most common
• TRAUMA (10%): Lacerations of cervix, vagina, perineum
• TISSUE (5%): Retained placenta/membranes
• THROMBIN (5%): Coagulation disorders (DIC, vWD)

MANAGEMENT OF ATONY (HAEMOSTATICS):
1. Uterine massage + bimanual compression
2. Oxytocin 10 IU IM/IV (first-line uterotonic)
3. Ergometrine 0.5 mg IM (contraindicated in hypertension)
4. Carboprost (15-methyl PGF2α) 0.25 mg IM — DOC if oxytocin fails
5. Tranexamic acid IV (within 3 hours)
6. Surgical: B-Lynch suture, uterine artery ligation, hysterectomy (last resort)""",
            options = listOf(opt("Uterine atony", true), opt("Retained placenta", false), opt("Cervical laceration", false), opt("Coagulopathy", false))
        ),
        q(subjectId = 3, topicId = 5,
            text = "Snowstorm appearance on ultrasound is characteristic of:",
            explanation = """Hydatidiform mole (complete) = gestational trophoblastic disease where fertilised egg has no maternal nuclear DNA → all chromosomal material is paternal (46 XX typically).

ULTRASOUND: "Snowstorm" pattern — diffuse echogenic intrauterine mass with multiple small cystic spaces (hydropic villi). No foetal parts in complete mole.

β-hCG: Markedly elevated (often >100,000 mIU/mL).

CLASSIFICATION:
Complete mole: No foetus, 46XX (androgenetic), high malignant potential (15–20%)
Partial mole: Abnormal foetus, triploid (69 XXY), low malignant potential

MANAGEMENT: Suction curettage + follow up β-hCG until undetectable. Chemotherapy if β-hCG plateaus/rises → choriocarcinoma suspected.

MNEMONIC: "MOLE = Markedly elevated hCG, Ovarian theca-lutein cysts, Large-for-dates uterus, Emesis (hyperemesis)".""",
            options = listOf(opt("Hydatidiform mole", true), opt("Placenta praevia", false), opt("Abruptio placentae", false), opt("Ectopic pregnancy", false))
        ),
        q(subjectId = 3, topicId = 5,
            text = "Bishop score is used to assess:",
            explanation = """Bishop score assesses cervical favourability before induction of labour. A score ≥8 predicts successful induction.

COMPONENTS (DECPS):
Parameter      | 0      | 1        | 2        | 3
Dilation (cm)  | Closed | 1–2      | 3–4      | ≥5
Effacement (%) | 0–30   | 40–50    | 60–70    | ≥80
Consistency    | Firm   | Medium   | Soft     | —
Position       | Post   | Mid      | Ant      | —
Station        | –3     | –2       | –1/0     | +1/+2

SCORE INTERPRETATION:
≥8 = Favourable → induction likely to succeed
5–7 = Moderate → consider cervical ripening first
≤4 = Unfavourable → cervical ripening needed (PGE2 gel, balloon, misoprostol)

INDICATIONS FOR INDUCTION: Post-dates (>41 weeks), IUGR, pre-eclampsia, diabetes, PROM, IUD.""",
            options = listOf(opt("Cervical favourability for induction of labour", true), opt("Fetal well-being in non-stress test", false), opt("Risk of placenta praevia", false), opt("Gestational age by ultrasound", false))
        ),
        q(subjectId = 3, topicId = 5,
            text = "Oxytocin is used in labour because it:",
            explanation = """Oxytocin is a nonapeptide hormone released from the posterior pituitary (synthesised in hypothalamic nuclei: supraoptic and paraventricular).

MECHANISM: Binds Gq-coupled oxytocin receptors → ↑IP3/DAG → ↑intracellular Ca²⁺ → uterine smooth muscle contraction. Receptors increase dramatically near term (oestrogen upregulates them).

USES:
1. Induction of labour (term)
2. Augmentation of labour (hypotonic contractions)
3. Prevention and treatment of PPH (third stage)
4. Milk ejection reflex (let-down reflex)

DOSE: 0.5–2 mU/min IV infusion, titrated every 15–30 min.
CAUTION: Can cause uterine hyperstimulation → foetal distress; water retention (antidiuretic effect).

SYNTHETIC ANALOGUE: Carbetocin (long-acting oxytocin analogue) — single IM/IV dose for PPH prevention after CS.""",
            options = listOf(opt("Stimulates uterine contractions", true), opt("Relaxes the cervix", false), opt("Inhibits prostaglandin synthesis", false), opt("Promotes fetal lung maturity", false))
        ),
        q(subjectId = 3, topicId = 5,
            text = "Nagele's rule calculates EDD as:",
            explanation = """Nägele's rule (1812): EDD = First day of LMP + 9 calendar months + 7 days (equivalent to subtracting 3 months and adding 7 days).

EXAMPLE: LMP = 1st January → EDD = 8th October.

BASIS: Average gestation = 280 days (40 weeks) from LMP, assuming regular 28-day cycles with ovulation on Day 14.

ADJUSTMENTS NEEDED:
• Cycle >28 days: Add extra days (e.g., 32-day cycle → add 4 days to EDD)
• Cycle <28 days: Subtract days
• IVF: EDD = embryo transfer date + 266 days (38 weeks from conception)

METHODS TO CONFIRM GESTATIONAL AGE:
1. LMP + Nägele's rule (if reliable LMP, regular cycles)
2. Ultrasound (most accurate in 1st trimester):
   • Crown-rump length (CRL): 6–13 weeks (±5 days accuracy)
   • BPD/FL: 14–20 weeks (±1–2 weeks)
   • After 28 weeks: Less accurate (±3–4 weeks)""",
            options = listOf(opt("LMP + 9 months + 7 days", true), opt("LMP + 10 months", false), opt("LMP + 280 days from conception", false), opt("LMP − 3 months + 7 days only in 28-day cycle", false))
        ),

        // ── Pediatrics: Growth & Development ──────────────────────────────────
        q(subjectId = 4, topicId = 6,
            text = "The anterior fontanelle closes at:",
            explanation = """Fontanelles are membranous gaps between skull bones that allow moulding during delivery and brain growth after birth.

ANTERIOR FONTANELLE (Bregma — at junction of coronal + sagittal sutures):
• Shape: Diamond/rhombus
• Size at birth: ~2–3 cm
• Closes: 9–18 months (remember: 12–18 months is the most quoted answer)

POSTERIOR FONTANELLE (Lambda — junction of lambdoid + sagittal sutures):
• Shape: Triangular
• Size: 0.5 cm at birth
• Closes: 6–8 weeks (by 2 months)

CLINICAL SIGNIFICANCE:
Bulging fontanelle: ↑ICP (meningitis, hydrocephalus, vitamin A toxicity)
Sunken fontanelle: Dehydration
Large/late-closing AF: Hypothyroidism, rickets, Down syndrome, achondroplasia, hydrocephalus
Early closure (craniosynostosis): Can cause raised ICP, abnormal skull shape

MNEMONIC: "Anterior closes Annually (≈12 months); Posterior closes Post-partum quickly (2 months)".""",
            options = listOf(opt("12–18 months", true), opt("6–8 weeks", false), opt("2–3 years", false), opt("By birth", false))
        ),
        q(subjectId = 4, topicId = 6,
            text = "Normal birth weight of a term infant is:",
            explanation = """Normal birth weight (term, 37–42 weeks): 2.5–4.0 kg (average 3.0–3.5 kg in India, 3.4 kg globally).

BIRTH WEIGHT DEFINITIONS:
• Normal birth weight: ≥2500 g (≥2.5 kg)
• Low birth weight (LBW): <2500 g
• Very low birth weight (VLBW): <1500 g
• Extremely low birth weight (ELBW): <1000 g
• Macrosomia: >4000 g (or >4500 g)

CAUSES OF LBW:
Preterm (<37 weeks): Prematurity — most common cause globally
SGA (Small for gestational age): IUGR — birth weight <10th percentile for gestation

PHYSIOLOGICAL WEIGHT LOSS:
• Normal infants lose 5–10% of birth weight in first 3–5 days
• Regained by 10–14 days of life
• Breastfed infants may lose up to 10%

WEIGHT MILESTONES:
• Doubles by: 5 months
• Triples by: 1 year
• Quadruples by: 2 years""",
            options = listOf(opt("2.5–4.0 kg", true), opt("1.5–2.5 kg", false), opt("4.0–5.0 kg", false), opt("3.5–4.5 kg", false))
        ),
        q(subjectId = 4, topicId = 6,
            text = "Which milestone is achieved at 9 months of age?",
            explanation = """DEVELOPMENTAL MILESTONES — KEY AGES

GROSS MOTOR:
• 3 months: Holds head steady
• 6 months: Sits with support, rolls over
• 9 months: Stands with support, crawls ★
• 12 months: Walks with support, stands independently
• 15 months: Walks independently
• 18 months: Runs, climbs stairs

FINE MOTOR:
• 6 months: Palmar grasp, transfers objects
• 9 months: Pincer grasp (inferior) ★
• 12 months: Mature pincer grasp, throws objects

LANGUAGE:
• 2 months: Social smile
• 6 months: Polysyllabic babble (ma-ma, ba-ba — but not meaningful)
• 9–10 months: "Dada/Mama" meaningfully ★
• 12 months: 1–2 meaningful words
• 18 months: 8–10 words
• 2 years: 2-word sentences

SOCIAL:
• 6 months: Stranger anxiety begins
• 9 months: Peek-a-boo, waves bye-bye ★
• 12 months: Imitates actions

MNEMONIC for 9 months: "Stands-Pincer-Dada-Peek" = Stands with support, Pincer grasp, Dada/Mama meaningful, Peek-a-boo.""",
            options = listOf(opt("Stands with support", true), opt("Walks independently", false), opt("Sits without support", false), opt("Runs", false))
        ),
        q(subjectId = 4, topicId = 6,
            text = "Koplik spots are pathognomonic of:",
            explanation = """Koplik spots are small, bluish-white spots with a red halo on the buccal mucosa (opposite lower molars), appearing 1–2 days BEFORE the measles rash.

MEASLES (Rubeola) — CAUSED BY: Paramyxovirus (RNA virus), spreads by droplets.

CLINICAL COURSE:
1. Incubation: 10–14 days
2. Prodrome (3–4 days): 4 C's — Cough, Coryza, Conjunctivitis, Fever (Koplik spots appear here)
3. Rash: Erythematous maculopapular rash starting at hairline/face → trunk → extremities (centrifugal spread, top-to-bottom)

KEY FACTS:
• Koplik spots: Pathognomonic (diagnostic even before rash)
• Most infectious: During prodrome (Koplik spot stage)
• Rash lasts 5–6 days, leaves brownish discolouration
• Most contagious exanthem

COMPLICATIONS: Pneumonia (most common cause of death), encephalitis (1:1000), SSPE (subacute sclerosing panencephalitis — years later), otitis media, diarrhoea.

TREATMENT: Supportive + Vitamin A (reduces severity and mortality, especially in malnourished).
PREVENTION: MMR vaccine at 9–12 months and 15–18 months.""",
            options = listOf(opt("Measles", true), opt("Chickenpox", false), opt("Mumps", false), opt("Rubella", false))
        ),
        q(subjectId = 4, topicId = 6,
            text = "The most common cause of acute diarrhoea in children under 5 is:",
            explanation = """ROTAVIRUS is the most common cause of acute gastroenteritis in children <5 years globally, responsible for ~40% of hospitalisations for diarrhoea.

ROTAVIRUS:
• Type: Double-stranded RNA virus (Reoviridae family), 11 segments
• Transmission: Feco-oral route
• Age: 6 months–2 years most affected
• Seasonality: Winter (in temperate climates)
• Diarrhoea: Watery, profuse — NOT bloody
• Mechanism: Enterotoxin (NSP4) + villous destruction → secretory + osmotic diarrhoea

MANAGEMENT:
1. Oral Rehydration Solution (ORS) — cornerstone
2. Zinc supplementation (20 mg/day × 10–14 days)
3. Continue breastfeeding
4. Ondansetron for vomiting
5. IV fluids only if severe dehydration

PREVENTION: Rotavirus vaccine (oral, live attenuated) — part of India's national immunisation programme
Pentavalent vaccine doses at 6, 10, 14 weeks.

OTHER CAUSES OF ACUTE DIARRHOEA IN CHILDREN:
Viral: Norovirus, Adenovirus 40/41
Bacterial: E. coli (most common bacterial cause), Salmonella, Shigella (bloody diarrhoea)
Parasitic: Giardia lamblia (fatty, frothy, pale stools)""",
            options = listOf(opt("Rotavirus", true), opt("E. coli", false), opt("Salmonella", false), opt("Shigella", false))
        ),
        q(subjectId = 4, topicId = 6,
            text = "Regarding the MMR vaccine, which of the following is correct?",
            explanation = """MMR vaccine (Measles, Mumps, Rubella) — live attenuated combined vaccine.

SCHEDULE IN INDIA (NIP):
• 1st dose: 9–12 months
• 2nd dose: 15–18 months (or 4–6 years in IAP schedule)

Note: Rubella vaccine is critical for girls to prevent Congenital Rubella Syndrome (CRS).

CONTRAINDICATIONS:
• Immunocompromised states (HIV with CD4 <15%)
• Pregnancy (teratogenic risk — avoid pregnancy for 1 month after)
• Allergy to vaccine components
• Recent blood/immunoglobulin transfusion (wait 3 months)

Note: Egg allergy is NOT a contraindication (MMR is grown on chick embryo fibroblasts, not egg white).

ADVERSE EFFECTS:
• Fever at 5–12 days post-vaccination
• Mild rash at 7–12 days
• Thrombocytopenic purpura (rare) — measles component
• Febrile seizures (rare)
• Aseptic meningitis (mumps component, Urabe strain — no longer used)

NO ASSOCIATION WITH AUTISM (Wakefield study was fraudulent and retracted).""",
            options = listOf(opt("Given at 9 months and 15 months", true), opt("Contraindicated in egg allergy", false), opt("Live attenuated vaccine given SC", false), opt("Provides lifelong immunity after one dose", false))
        ),

        // ── Pharmacology: Antimicrobials ───────────────────────────────────────
        q(subjectId = 5, topicId = 7,
            text = "Penicillin acts by:",
            explanation = """Penicillin's mechanism: INHIBITION OF BACTERIAL CELL WALL SYNTHESIS.

MECHANISM (step by step):
1. Penicillin enters bacterial cell through porin channels
2. Binds covalently to Penicillin-Binding Proteins (PBPs) — these are transpeptidase enzymes
3. PBPs normally catalyse the cross-linking (transpeptidation) of peptidoglycan chains in the cell wall
4. Inhibition of transpeptidation → weakened, incomplete cell wall
5. Osmotic lysis → BACTERICIDAL effect

STRUCTURE: β-lactam ring (4-membered) + thiazolidine ring (5-membered)

RESISTANCE MECHANISMS:
1. β-lactamase production (most common) — breaks the β-lactam ring
2. Modified PBPs (e.g., PBP2a in MRSA — mecA gene) — β-lactam cannot bind
3. Efflux pumps
4. Reduced outer membrane permeability (gram-negatives)

COVERAGE:
Narrow spectrum: Penicillin G/V — gram-positives (Strep, Staph non-MRSA), syphilis
Extended spectrum: Ampicillin/Amoxicillin — + gram-negatives (H. influenzae, E. coli)
Anti-pseudomonal: Piperacillin + tazobactam

β-LACTAMASE INHIBITORS: Clavulanic acid, sulbactam, tazobactam — protect the β-lactam ring.""",
            options = listOf(opt("Inhibition of cell wall synthesis", true), opt("Inhibition of protein synthesis (30S)", false), opt("Disruption of cell membrane", false), opt("Inhibition of DNA gyrase", false))
        ),
        q(subjectId = 5, topicId = 7,
            text = "Drug of choice for MRSA is:",
            explanation = """MRSA (Methicillin-Resistant Staphylococcus aureus) has the mecA gene encoding PBP2a, which has low affinity for all β-lactam antibiotics.

DRUG OF CHOICE: VANCOMYCIN (glycopeptide antibiotic)

MECHANISM OF VANCOMYCIN:
• Inhibits cell wall synthesis at a DIFFERENT step from β-lactams
• Binds to D-Ala-D-Ala terminus of peptidoglycan precursors → prevents transglycosylation AND transpeptidation
• Bactericidal (time-dependent killing)

ALTERNATIVES FOR MRSA:
• Linezolid: Oxazolidinone — inhibits 50S ribosome (initiation); used for soft tissue MRSA; oral bioavailability excellent
• Daptomycin: Lipopeptide — depolarises bacterial membrane; NOT for pneumonia (inactivated by surfactant)
• Teicoplanin: Another glycopeptide (similar to vancomycin)
• Ceftaroline: 5th-generation cephalosporin — active against MRSA (unique PBP2a binding)
• TMP-SMX: For community-acquired MRSA (CA-MRSA) skin/soft tissue infections

MONITORING VANCOMYCIN:
• Trough levels (AUC/MIC guided dosing)
• Nephrotoxicity (especially with aminoglycosides)
• "Red man syndrome" (not allergy — histamine release with rapid infusion → slow the infusion)""",
            options = listOf(opt("Vancomycin", true), opt("Penicillin G", false), opt("Methicillin", false), opt("Erythromycin", false))
        ),
        q(subjectId = 5, topicId = 7,
            text = "Aminoglycosides are nephrotoxic because they:",
            explanation = """Aminoglycosides (gentamicin, amikacin, tobramycin, streptomycin) are bactericidal antibiotics that inhibit 30S ribosomal subunit → misreading of mRNA → abnormal protein synthesis.

NEPHROTOXICITY MECHANISM:
1. Aminoglycosides are cationic (positively charged) molecules
2. Taken up by proximal tubule epithelial cells via endocytosis (megalin receptor)
3. Accumulate in lysosomes → phospholipidosis
4. Lysosomal membrane disruption → release of proteases and phospholipases
5. Tubular cell necrosis → non-oliguric acute tubular necrosis (ATN)

CLINICAL FEATURES:
• Presents 5–10 days after starting therapy
• Non-oliguric initially (tubules lose concentrating ability)
• Usually reversible after stopping drug

OTOTOXICITY:
• Cochlear (hearing loss): All aminoglycosides, especially amikacin, neomycin
• Vestibular (vertigo, ataxia): Especially gentamicin, streptomycin
• Irreversible damage to hair cells of organ of Corti

RISK FACTORS FOR TOXICITY:
• Pre-existing renal disease
• Prolonged therapy, high doses
• Concomitant nephrotoxins (NSAIDs, contrast, vancomycin)
• Elderly patients

MONITORING: Serum drug levels (peak and trough), renal function, audiometry.
PREVENTION: Once-daily dosing (pulse therapy) is LESS nephrotoxic than multiple daily doses.""",
            options = listOf(opt("Accumulate in proximal tubule cells", true), opt("Cause renal artery vasoconstriction", false), opt("Block angiotensin II receptors", false), opt("Inhibit prostaglandin synthesis in kidney", false))
        ),
        q(subjectId = 5, topicId = 7,
            text = "Chloramphenicol causes which serious adverse effect?",
            explanation = """Chloramphenicol is a broad-spectrum bacteriostatic antibiotic that inhibits the 50S ribosomal subunit (peptidyl transferase enzyme) → blocks peptide chain elongation.

SERIOUS ADVERSE EFFECTS:

1. APLASTIC ANAEMIA (most feared, dose-independent)
• Idiosyncratic reaction — not dose related
• Incidence: 1 in 25,000–40,000 patients
• Mechanism: Toxic effect on stem cells; may involve nitrosochloramphenicol metabolite
• Often irreversible; can occur weeks after stopping drug
• NOT related to the reversible bone marrow suppression

2. DOSE-DEPENDENT BONE MARROW SUPPRESSION (reversible)
• Occurs at high doses (>4 g/day)
• Reversible anaemia, leukopenia, thrombocytopenia
• Mechanism: Inhibits mitochondrial ribosomes (70S) in human cells

3. GREY BABY SYNDROME (neonates):
• Mechanism: Immature liver (deficient glucuronosyltransferase) → drug accumulation
• Features: Cyanosis, abdominal distension, vasomotor collapse, ashen-grey colour, death
• Occurs at doses >50 mg/kg/day in neonates

USES: Typhoid (not first-line), bacterial meningitis (alternative), rickettsial infections in pregnancy, anaerobic infections (brain abscess).""",
            options = listOf(opt("Aplastic anaemia", true), opt("Peripheral neuropathy", false), opt("Hepatotoxicity", false), opt("QT prolongation", false))
        ),
        q(subjectId = 5, topicId = 7,
            text = "Metronidazole is the drug of choice for:",
            explanation = """Metronidazole is a nitroimidazole drug active against anaerobic bacteria and protozoa.

MECHANISM: Pro-drug → reduced by ferredoxin (in anaerobes/protozoa) → toxic nitro radical → DNA strand breaks → cell death. Aerobic organisms cannot reduce the drug → selectivity.

SPECTRUM:
PROTOZOAL: Giardia lamblia ★, Entamoeba histolytica (intestinal + hepatic amoebiasis) ★, Trichomonas vaginalis ★, Balantidium coli
ANAEROBIC BACTERIA: Bacteroides fragilis ★ (abdominal infections), Clostridium difficile ★, Clostridium perfringens, Fusobacterium, Prevotella

CLINICAL USES:
• Amoebic liver abscess: Metronidazole (course) + luminal agent (diloxanide furoate)
• Giardiasis: Single dose metronidazole or tinidazole
• Trichomoniasis: Single dose 2g (treat partner simultaneously)
• Pseudomembranous colitis (C. diff): Oral vancomycin (first-line); metronidazole (alternative/mild disease)
• H. pylori eradication: Triple therapy (PPI + amoxicillin + metronidazole)
• BV (Bacterial vaginosis): Metronidazole or clindamycin

ADVERSE EFFECTS: Metallic taste (most common), nausea, disulfiram-like reaction with alcohol (avoid alcohol during treatment), peripheral neuropathy (long-term use).

MNEMONIC: "Metro goes ANAEROBIC" — Metronidazole = Anaerobes + Amoeba + Anaerobic protozoa.""",
            options = listOf(opt("Giardiasis", true), opt("Malaria", false), opt("Pneumocystis pneumonia", false), opt("Candidiasis", false))
        ),
        q(subjectId = 5, topicId = 7,
            text = "Fluconazole is used in the treatment of:",
            explanation = """Fluconazole is a triazole antifungal drug that inhibits the fungal cytochrome P450 enzyme 14α-demethylase → blocks conversion of lanosterol to ergosterol → impairs fungal cell membrane integrity → fungistatic.

SPECTRUM AND USES:
• Candida infections: Oropharyngeal, oesophageal, vaginal candidiasis, candidaemia (non-albicans Candida may be resistant — C. krusei inherently resistant, C. glabrata often resistant)
• Cryptococcal meningitis: Fluconazole (maintenance/consolidation after amphotericin B induction) ★
• Dermatophyte infections: Tinea capitis, tinea corporis
• Prophylaxis in immunocompromised (HIV, transplant)

ADVANTAGES:
• Excellent oral bioavailability (~90%) — IV = oral dosing
• Good CSF penetration
• Once-daily dosing
• Well tolerated

ADVERSE EFFECTS: Nausea, headache, hepatotoxicity (rare), QT prolongation, drug interactions (CYP3A4 inhibitor — warfarin, cyclosporin levels increased)

COMPARISON:
Fluconazole: Narrow spectrum, excellent Candida/Crypto coverage
Itraconazole: Broader (also Aspergillus), but poor oral bioavailability
Voriconazole: Invasive Aspergillosis (first-line), also Candida, CSF penetration good
Amphotericin B: Polyene, broadest spectrum, nephrotoxic, IV only""",
            options = listOf(opt("Cryptococcal meningitis", true), opt("Malaria", false), opt("Bacterial meningitis", false), opt("Aspergillosis (first-line)", false))
        ),

        // ── Pathology: Neoplasia ───────────────────────────────────────────────
        q(subjectId = 6, topicId = 8,
            text = "The most important prognostic factor in breast cancer is:",
            explanation = """Breast cancer is the most common cancer in women worldwide. Several factors determine prognosis and guide treatment decisions.

MOST IMPORTANT PROGNOSTIC FACTOR: Axillary lymph node status (lymph node involvement) ★★

PROGNOSTIC FACTORS IN ORDER OF IMPORTANCE:
1. Axillary LN involvement (number of positive nodes)
2. Tumour size
3. Tumour grade (histological — Bloom-Richardson/Nottingham grade)
4. Hormone receptor status (ER/PR — paradoxically better prognosis, responds to hormonal therapy)
5. HER2/neu status (HER2+ = worse prognosis but targetable with trastuzumab)
6. Ki-67 proliferation index
7. Lymphovascular invasion
8. Age <35: Worse prognosis

MOLECULAR SUBTYPES:
Luminal A (ER+/PR+, HER2−, low Ki67): Best prognosis
Luminal B (ER+/PR+, HER2+ or high Ki67): Intermediate
HER2-enriched (ER−/PR−, HER2+): Aggressive but targetable
Triple negative (ER−/PR−, HER2−): Worst prognosis, no targeted therapy

SENTINEL LYMPH NODE BIOPSY: Standard to assess LN status without full axillary dissection.""",
            options = listOf(opt("Axillary lymph node involvement", true), opt("Tumour size", false), opt("Patient age", false), opt("ER/PR receptor status", false))
        ),
        q(subjectId = 6, topicId = 8,
            text = "Philadelphia chromosome is associated with:",
            explanation = """Philadelphia chromosome (Ph chromosome): Described in 1960 by Nowell and Hungerford in Philadelphia — hence the name.

WHAT IS IT: Translocation t(9;22)(q34;q11)
• ABL gene (chromosome 9) fuses with BCR gene (chromosome 22)
• Creates BCR-ABL1 fusion oncogene
• Encodes BCR-ABL protein — a constitutively active tyrosine kinase
• Drives uncontrolled proliferation of myeloid cells

ASSOCIATED WITH:
• CML (Chronic Myeloid Leukaemia): Present in >95% of cases ★★
• ALL (Acute Lymphoblastic Leukaemia): Present in 25–30% of adult ALL (poor prognosis)
• Rarely in AML

CML CHARACTERISTICS:
• Leukocytosis (WBC >100,000) with full myeloid spectrum
• Low LAP score (Leukocyte Alkaline Phosphatase) — differentiates from leukaemoid reaction
• Splenomegaly (often massive)
• Phases: Chronic → Accelerated → Blast crisis (↑blasts)

TREATMENT:
• Imatinib (Gleevec) — first TKI (Tyrosine Kinase Inhibitor) targeting BCR-ABL
• 2nd generation: Dasatinib, Nilotinib (for imatinib resistance/intolerance)
• Dramatic improvement in survival — 5-year survival now >90%

MNEMONIC: "Ph+CML = BCR-ABL = Brilliant Cancer Medicine with ABl inhibitors".""",
            options = listOf(opt("Chronic myeloid leukaemia (CML)", true), opt("Acute myeloid leukaemia (AML)", false), opt("Multiple myeloma", false), opt("Hodgkin's lymphoma", false))
        ),
        q(subjectId = 6, topicId = 8,
            text = "Reed-Sternberg cells are characteristic of:",
            explanation = """Reed-Sternberg (RS) cells are giant binucleated or multinucleated cells with prominent "owl-eye" nucleoli — they are the pathognomonic hallmark of Hodgkin's lymphoma (HL).

MORPHOLOGY: Large cell (20–50 μm) with bilobed or multilobed nucleus; each lobe has a large acidophilic nucleolus surrounded by a clear halo ("owl eyes").

IMMUNOPHENOTYPE: CD15+ CD30+ (most important), CD45−, CD20− (in classical HL), PAX5+

ORIGIN: Derived from germinal centre B-cells (have non-functional immunoglobulin genes).

HODGKIN'S LYMPHOMA — CLASSIFICATION (WHO):
Classical HL (95%):
1. Nodular Sclerosis (most common, 65%) — young women, mediastinal
2. Mixed Cellularity (25%) — older males, EBV associated
3. Lymphocyte Rich (5%)
4. Lymphocyte Depleted (rare, worst prognosis)
Nodular Lymphocyte Predominant HL (NLPHL, 5%):
• Variant RS cell: "Popcorn cell" (lymphocytic/histiocytic L&H variant)
• CD20+ CD45+ CD15− CD30−

ANN ARBOR STAGING:
Stage I: One LN region
Stage II: ≥2 LN regions, same side of diaphragm
Stage III: Both sides of diaphragm
Stage IV: Disseminated extranodal involvement

B SYMPTOMS (poor prognosis): Fever >38°C, drenching night sweats, weight loss >10% in 6 months""",
            options = listOf(opt("Hodgkin's lymphoma", true), opt("Non-Hodgkin's lymphoma", false), opt("Multiple myeloma", false), opt("Burkitt's lymphoma", false))
        ),
        q(subjectId = 6, topicId = 8,
            text = "The most common primary bone tumour in children is:",
            explanation = """OSTEOSARCOMA is the most common primary malignant bone tumour overall and in children/adolescents (peak age 10–20 years).

KEY FEATURES:
• Location: Metaphysis of long bones — distal femur (most common) > proximal tibia > proximal humerus
• Cells: Malignant osteoblasts producing osteoid
• Bimodal age: 10–20 years (primary) and >65 years (secondary — Paget's disease, radiation)

X-RAY FINDINGS:
• Codman's triangle: Periosteal reaction at tumour edges (elevated periosteum)
• Sunburst/sunray pattern: Spiculated new bone perpendicular to cortex
• Mixed lytic + sclerotic lesion in metaphysis

CLINICAL: Pain (worse at night), swelling, pathological fracture.

STAGING & MANAGEMENT:
• Biopsy: Confirm diagnosis
• Chemotherapy (pre-op neoadjuvant): Methotrexate, doxorubicin, cisplatin (MAP)
• Surgery: Wide local excision / limb-salvage (preferred) or amputation
• Post-op adjuvant chemotherapy
• 5-year survival: ~70% with limb-salvage surgery

COMPARISON:
Osteosarcoma: Metaphysis, 10–20 years, osteoid production, sunburst pattern
Ewing's sarcoma: Diaphysis, 5–15 years, onion-skin periosteal reaction, CD99+, t(11;22)
Giant cell tumour (GCT): Epiphysis, 20–40 years, "soap bubble" appearance, locally aggressive""",
            options = listOf(opt("Osteosarcoma", true), opt("Ewing's sarcoma", false), opt("Chondrosarcoma", false), opt("Giant cell tumour", false))
        ),
        q(subjectId = 6, topicId = 8,
            text = "Which tumour marker is elevated in hepatocellular carcinoma?",
            explanation = """ALPHA-FETOPROTEIN (AFP) is the tumour marker for Hepatocellular Carcinoma (HCC).

NORMAL AFP: <10 ng/mL in adults. AFP is normally produced by the yolk sac and fetal liver.

AFP IN HCC:
• Elevated in 70% of HCC cases
• Level >400 ng/mL in the setting of cirrhosis/chronic liver disease is diagnostic of HCC
• Used for: Diagnosis, monitoring treatment response, detecting recurrence

CAUSES OF AFP ELEVATION:
• HCC ★★ (most important)
• Hepatoblastoma (children — AFP very high, >10,000)
• Non-seminomatous germ cell tumours (NSGCT): Yolk sac tumour, mixed NSGCT
• Pregnancy (normal elevation)
• Chronic hepatitis/cirrhosis (mild elevation)

TUMOUR MARKERS SUMMARY:
AFP: HCC, yolk sac tumour
β-hCG: Choriocarcinoma, gestational trophoblastic disease, seminoma (low level)
CEA: Colorectal carcinoma (monitoring), also lung, breast, GI
PSA: Prostate carcinoma
CA 19-9: Pancreatic carcinoma (monitoring)
CA 125: Ovarian carcinoma (monitoring)
CA 15-3: Breast carcinoma (monitoring)
LDH: Lymphoma, testicular tumours (non-specific)
Chromogranin A: Neuroendocrine tumours (carcinoid)

HCC RISK FACTORS: Hepatitis B, Hepatitis C, cirrhosis (any cause), aflatoxin B1 (Aspergillus in grain)""",
            options = listOf(opt("Alpha-fetoprotein (AFP)", true), opt("CEA", false), opt("CA 125", false), opt("PSA", false))
        ),
        q(subjectId = 6, topicId = 8,
            text = "Warburg effect in cancer refers to:",
            explanation = """The Warburg Effect (Otto Warburg, 1924): Cancer cells preferentially use AEROBIC GLYCOLYSIS even in the presence of oxygen, instead of oxidative phosphorylation.

Normal cells: Glycolysis → pyruvate → Krebs cycle + oxidative phosphorylation (efficient: 36 ATP/glucose)
Cancer cells: Glycolysis → lactate (even with O₂) — "aerobic glycolysis" (inefficient: 2 ATP/glucose)

WHY DO CANCER CELLS DO THIS?
1. Biosynthesis: Glycolytic intermediates needed for biosynthesis (nucleotides, amino acids, lipids for rapid proliferation)
2. Acidic microenvironment (lactate production) — aids invasion and immunosuppression
3. Oncogene activation (Myc, Ras) → upregulate glycolytic enzymes
4. HIF-1α (hypoxia-inducible factor) — upregulated even in normoxia in cancer

CLINICAL RELEVANCE:
• PET scan uses 18F-FDG (fluorodeoxyglucose) — cancer cells take up MORE glucose (Warburg effect) → appear as "hot spots" on PET
• Basis for metabolic targeting of cancer cells

KEY ENZYME UPREGULATED: Lactate dehydrogenase (LDH), Hexokinase II, Pyruvate kinase M2 (PKM2)

MNEMONIC: "Warburg = cancer cells are WASTEFUL with glucose — prefer glycolysis even with oxygen — to BUILD not just BURN".""",
            options = listOf(opt("Preferential use of glycolysis even in presence of oxygen", true), opt("Increased oxidative phosphorylation", false), opt("Increased fatty acid oxidation", false), opt("Decreased glucose uptake", false))
        ),

        // ── Anatomy: Upper Limb ────────────────────────────────────────────────
        q(subjectId = 7, topicId = 9,
            text = "The nerve most commonly injured in dislocation of the shoulder joint is:",
            explanation = """The shoulder (glenohumeral) joint is the most mobile and most commonly dislocated joint. Anterior dislocation accounts for 95% of cases.

ANTERIOR DISLOCATION — NERVE INJURED: AXILLARY NERVE ★

WHY AXILLARY NERVE?
• The axillary nerve winds around the surgical neck of humerus within the quadrilateral space
• In anterior dislocation, the humeral head displaces anteriorly and inferiorly → stretches/tears the axillary nerve as it winds below

AXILLARY NERVE (C5, C6 — from posterior cord of brachial plexus):
Motor: Deltoid (abduction 15–90°) + Teres minor (lateral rotation)
Sensory: Regimental badge area (lateral upper arm)

CLINICAL FEATURES OF AXILLARY NERVE INJURY:
• Loss of shoulder abduction (deltoid paralysis)
• Flattening of shoulder contour
• Anaesthesia over regimental badge area

TYPES OF SHOULDER DISLOCATION:
Anterior (95%): Subcoracoid (most common), subglenoid, subclavicular
Posterior (3%): Associated with seizures, electrocution ("lightbulb sign" on AP X-ray)
Inferior (luxatio erecta, rare): Arm fixed in abduction overhead

INVESTIGATIONS: X-ray AP + axillary/scapular Y view
Bankart lesion: Anterior glenoid labral tear (recurrent anterior dislocation)
Hill-Sachs lesion: Compression fracture of posterolateral humeral head

TREATMENT: Reduction (Hippocratic, Kocher's method), immobilisation, then physiotherapy.""",
            options = listOf(opt("Axillary nerve", true), opt("Radial nerve", false), opt("Median nerve", false), opt("Long thoracic nerve", false))
        ),
        q(subjectId = 7, topicId = 9,
            text = "Wrist drop is caused by injury to which nerve?",
            explanation = """WRIST DROP (inability to extend wrist and fingers) = RADIAL NERVE INJURY ★

RADIAL NERVE (C5–T1 — from posterior cord of brachial plexus):
• Supplies all extensors of upper limb ("radial = rear extensors")
• Winds in the spiral groove of the humerus
• Most commonly injured in mid-shaft humerus fracture (Saturday night palsy)

MOTOR SUPPLY:
• Triceps (elbow extension) — proximal branch, spared in spiral groove injury
• Brachioradialis
• Wrist extensors (ECRL, ECRB, ECU)
• Finger/thumb extensors (EDC, EPL, EPB, APL)

SENSORY: Dorsum of hand (lateral 3½ fingers — but not fingertips which are median territory)

WRIST DROP APPEARANCE:
• Patient cannot extend wrist (drops to floor-side due to gravity)
• Cannot extend MCPs (knuckles)
• Thumb abduction/extension weak
• Grip weakness (wrist flexion needed for effective grip)

SITES OF RADIAL NERVE INJURY:
1. Axilla: "Crutch palsy", fracture shaft humerus proximal — triceps ALSO affected
2. Spiral groove (MID-SHAFT HUMERUS): Saturday night palsy, mid-shaft fracture — triceps SPARED
3. Posterior interosseous nerve (forearm): Lateral epicondyle fracture/radial neck — NO wrist drop (ECRL spared), no sensory loss

SATURDAY NIGHT PALSY: Pressure on nerve in spiral groove (drunk person sleeping with arm over chair back)""",
            options = listOf(opt("Radial nerve", true), opt("Ulnar nerve", false), opt("Median nerve", false), opt("Musculocutaneous nerve", false))
        ),
        q(subjectId = 7, topicId = 9,
            text = "Which muscle is tested by asking the patient to abduct the thumb away from the palm?",
            explanation = """Abductor pollicis brevis (APB) is the muscle that abducts the thumb PERPENDICULAR TO THE PALM (palmar abduction).

MEDIAN NERVE INNERVATION OF HAND:
The recurrent/motor branch of median nerve supplies the thenar muscles:
• Abductor pollicis brevis (APB) ★ — most reliably median-only
• Flexor pollicis brevis (FPB) — superficial head (deep head by ulnar)
• Opponens pollicis (OP)
• 1st and 2nd lumbricals

THENAR EMINENCE WASTING = MEDIAN NERVE LESION
(e.g., Carpal tunnel syndrome, pronator teres syndrome)

CARPAL TUNNEL SYNDROME (CTS):
• Most common peripheral nerve entrapment
• Median nerve compressed under flexor retinaculum at wrist
• SYMPTOMS: Tingling/numbness in lateral 3½ fingers (worse at night), thenar wasting (late)
• PHALEN'S TEST: Wrist flexion for 60s → symptoms (sensitivity 70%)
• TINEL'S SIGN: Percussion over carpal tunnel → tingling (sensitivity 60%)
• NERVE CONDUCTION STUDY: Gold standard
• TREATMENT: Splinting at night (conservative), steroid injection, surgical decompression (carpal tunnel release)

ULNAR NERVE HAND MUSCLES ("all intrinsics except LOAF"):
• Medial 2 lumbricals (ring + little finger)
• All interossei (palmar + dorsal)
• Hypothenar muscles
• Adductor pollicis

LOAF muscles (Median nerve): Lumbricals 1&2, Opponens pollicis, APB, FPB superficial""",
            options = listOf(opt("Abductor pollicis brevis", true), opt("Abductor pollicis longus", false), opt("Adductor pollicis", false), opt("Extensor pollicis brevis", false))
        ),
        q(subjectId = 7, topicId = 9,
            text = "The rotator cuff does NOT include which muscle?",
            explanation = """The ROTATOR CUFF (SITS) consists of 4 muscles that stabilise the shoulder joint by holding the humeral head against the glenoid.

MNEMONIC: "SITS"
S — Supraspinatus: Initiates abduction (0–15°), tests suprascapular nerve (C5,C6)
I — Infraspinatus: Lateral rotation (tests suprascapular nerve C5,C6)
T — Teres minor: Lateral rotation (tests axillary nerve C5,C6)
S — Subscapularis: Medial rotation (tests upper/lower subscapular nerve C5,C6)

MOST COMMONLY TORN: Supraspinatus (70–80% of rotator cuff tears) — "painful arc" 60–120°

NOT PART OF ROTATOR CUFF:
• Deltoid (abduction 15–90°, initiates after supraspinatus, axillary nerve)
• Teres major (adduction + medial rotation, lower subscapular nerve)
• Biceps brachii (not a rotator cuff muscle)

CLINICAL TESTS:
Empty can test: Arm at 90° abduction, 30° forward flexion, internally rotated ("empty can position") — resist downward force → tests supraspinatus
Lift-off test: Back of hand pressed against lumbar spine, patient lifts hand away → tests subscapularis
External rotation lag sign: Tests infraspinatus + teres minor

INVESTIGATIONS: MRI scan (gold standard for rotator cuff tears)
TREATMENT: Physiotherapy, subacromial steroid injection, arthroscopic surgical repair (complete tears)""",
            options = listOf(opt("Teres major", true), opt("Supraspinatus", false), opt("Infraspinatus", false), opt("Subscapularis", false))
        ),
        q(subjectId = 7, topicId = 9,
            text = "The deep palmar arch is formed mainly by:",
            explanation = """The DEEP PALMAR ARCH is formed primarily by the RADIAL ARTERY (terminal branch) — making up ~90% of the arch, anastomosing with the deep branch of the ulnar artery.

PALMAR ARCHES:
1. DEEP PALMAR ARCH:
   • Main contributor: Radial artery (enters through anatomical snuffbox → between heads of 1st dorsal interosseous → perforates adductor pollicis → deep palm)
   • Small contribution: Deep branch of ulnar artery
   • Lies on metacarpal bases (deep to flexor tendons)
   • Gives: Palmar metacarpal arteries (join superficial arch via perforating branches)

2. SUPERFICIAL PALMAR ARCH:
   • Main contributor: Ulnar artery (>90%)
   • Small contribution: Superficial branch of radial artery (or princeps pollicis)
   • Lies superficial to flexor tendons, deep to palmar aponeurosis
   • Gives: Common palmar digital arteries → proper digital arteries

CLINICAL RELEVANCE:
• Allen's test: Tests ulnar/radial patency — compress both arteries at wrist, patient makes fist, release one → colour should return in <5–10 seconds (positive = intact)
• Important before radial artery cannulation (Allen's test first)

ANATOMICAL SNUFFBOX (bounded by APL + EPB laterally, EPL medially):
• Contains: Radial artery (deep), superficial branch of radial nerve, cephalic vein
• Tenderness: Scaphoid fracture (most common wrist fracture)""",
            options = listOf(opt("Radial artery", true), opt("Ulnar artery", false), opt("Brachial artery", false), opt("Anterior interosseous artery", false))
        ),
        q(subjectId = 7, topicId = 9,
            text = "Sensation over the little finger is supplied by:",
            explanation = """The LITTLE FINGER (5th digit, digitus minimus) is supplied exclusively by the ULNAR NERVE for both sensation and motor function.

ULNAR NERVE SENSORY DISTRIBUTION:
• Palmar surface: Medial 1½ fingers (little finger + medial half of ring finger) + corresponding palm
• Dorsal surface: Medial 1½ fingers + corresponding dorsum of hand (medial side)

ULNAR NERVE (C8, T1 — from medial cord of brachial plexus):
COURSE: Medial epicondyle of humerus (cubital tunnel) → Guyon's canal at wrist → deep + superficial branches in hand

SITES OF ULNAR NERVE INJURY:
1. Cubital tunnel (medial epicondyle): Most common site
   Causes: Cubital tunnel syndrome, fracture medial epicondyle, repeated leaning on elbow
   Signs: Claw hand (ring + little finger), weak grip, hypothenar wasting, loss of sensation little finger
2. Guyon's canal (wrist): Cycle handlebars, hamate fracture
   Signs: Similar to cubital but NO sensory loss on dorsum (dorsal cutaneous branch exits above wrist)

CLAW HAND:
• Ulnar nerve lesion (at elbow): "Benediction sign" — ring and little fingers clawed
• T1 root/lower trunk lesion (Klumpke's): All 4 fingers clawed
• Why NOT index/middle in ulnar lesion? Lumbricals to index/middle supplied by median nerve (no hyperextension at MCP → no claw)

SENSORY DEMARCATION: "Line through centre of ring finger" divides median (lateral) from ulnar (medial) territory.""",
            options = listOf(opt("Ulnar nerve", true), opt("Median nerve", false), opt("Radial nerve", false), opt("Musculocutaneous nerve", false))
        ),

        // ── Physiology: Respiratory ────────────────────────────────────────────
        q(subjectId = 8, topicId = 10,
            text = "The normal value of arterial PaO2 is approximately:",
            explanation = """ARTERIAL BLOOD GAS (ABG) — NORMAL VALUES:

pH:           7.35–7.45 (normal)
PaO2:         80–100 mmHg ★ (arterial oxygen partial pressure)
PaCO2:        35–45 mmHg (arterial carbon dioxide)
HCO3-:        22–26 mEq/L
SaO2:         94–100% (oxygen saturation)
Base excess:  −2 to +2 mEq/L

CLINICAL SIGNIFICANCE OF PaO2:
• <80 mmHg: Hypoxaemia
• 60–80 mmHg: Mild hypoxaemia
• 40–60 mmHg: Moderate hypoxaemia
• <40 mmHg: Severe hypoxaemia (life-threatening)
• <60 mmHg: Criterion for supplemental oxygen therapy

FACTORS AFFECTING PaO2:
1. Inspired O2 (FiO2): Room air = 21%
2. Alveolar ventilation (V) — ↑VA → ↑PAO2
3. V/Q matching — dead space (V>Q) and shunt (Q>V)
4. Diffusion capacity
5. Age: PaO2 decreases with age: PaO2 = 100 − (0.3 × age)

ALVEOLAR GAS EQUATION:
PAO2 = FiO2 × (Patm − PH2O) − PaCO2/R
= 0.21 × (760 − 47) − 40/0.8 = ~100 mmHg

CAUSES OF HYPOXAEMIA:
1. Hypoventilation: ↑PaCO2, normal A-a gradient
2. V/Q mismatch: Most common — corrects with O2
3. Shunt: Does NOT correct with O2
4. Diffusion impairment: Exertion-related
5. Low FiO2: High altitude""",
            options = listOf(opt("80–100 mmHg", true), opt("35–45 mmHg", false), opt("40–60 mmHg", false), opt("60–80 mmHg", false))
        ),
        q(subjectId = 8, topicId = 10,
            text = "Surfactant is produced by which cells in the lung?",
            explanation = """SURFACTANT is produced by TYPE II PNEUMOCYTES (Type II alveolar epithelial cells) ★

TYPE II PNEUMOCYTES (Granular pneumocytes):
• Cuboidal cells covering ~5% of alveolar surface area
• Contains lamellar bodies (stores surfactant)
• Functions:
  1. Synthesise and secrete surfactant
  2. Precursors for Type I pneumocytes (replace after injury)
  3. Resist oxidative damage better than Type I cells

TYPE I PNEUMOCYTES (Squamous pneumocytes):
• Cover 95% of alveolar surface
• Thin (for gas exchange)
• Cannot replicate

SURFACTANT COMPOSITION:
• Dipalmitoylphosphatidylcholine (DPPC) — main component (~50%)
• Surfactant proteins: SP-A, SP-B, SP-C, SP-D
• Cholesterol, other phospholipids

FUNCTION: Reduces surface tension in alveoli → prevents collapse (atelectasis)
Laplace's law: P = 2T/r — without surfactant, small alveoli have higher pressure → collapse into large ones

SURFACTANT DEFICIENCY:
Respiratory Distress Syndrome (RDS) / Hyaline Membrane Disease:
• Premature infants (<36 weeks): Immature type II cells → insufficient surfactant
• Surfactant matures: 36 weeks (critical threshold), L:S ratio >2:1 = maturity
• Treatment: Exogenous surfactant replacement (e.g., Beractant/Poractant alfa), antenatal steroids (dexamethasone) accelerate lung maturity""",
            options = listOf(opt("Type II pneumocytes", true), opt("Type I pneumocytes", false), opt("Alveolar macrophages", false), opt("Club cells (Clara cells)", false))
        ),
        q(subjectId = 8, topicId = 10,
            text = "The oxygen-haemoglobin dissociation curve shifts to the right in:",
            explanation = """The oxygen-haemoglobin dissociation curve (ODC) shows the relationship between PaO2 and haemoglobin saturation. It is SIGMOIDAL (S-shaped) — due to cooperative binding.

RIGHT SHIFT = ↓Hb affinity for O2 = O2 readily released to tissues (GOOD for exercising tissues)
Caused by: ↑Temp, ↑CO2, ↑H+ (↓pH), ↑2,3-DPG (2,3-bisphosphoglycerate)
Mnemonic: "CADET face RIGHT" — CO2, Acid, Diphosphoglycerate (2,3-DPG), Exercise, Temperature

LEFT SHIFT = ↑Hb affinity for O2 = O2 held tightly (poor tissue delivery)
Caused by: ↓Temp, ↓CO2, ↓H+ (↑pH), ↓2,3-DPG, CO (carbon monoxide), Foetal Hb (HbF), Methaemoglobin (in some sources)
Mnemonic: "CO-HFLAT" — CO, HbF, Liver disease (↓BPG), Alkalosis, Temperature ↓

BOHR EFFECT: ↑CO2/↑H+ → right shift → O2 released in tissues (exact physiological purpose)
HALDANE EFFECT: O2 binding → CO2 unloading from Hb

P50: PaO2 at which Hb is 50% saturated
• Normal P50 = 26–27 mmHg
• Right shift → ↑P50 (lower O2 affinity)
• Left shift → ↓P50

2,3-DPG:
• Produced in RBC glycolysis (Rapoport-Luebering shunt)
• Binds β-chains of deoxyHb → stabilises deoxy form → right shift
• Increased in: Anaemia, high altitude, hypoxia, acidosis, exercise
• Decreased in: Stored blood (DECREASES over time → stored blood has left-shifted curve → poor O2 delivery)""",
            options = listOf(opt("Acidosis and fever", true), opt("Alkalosis", false), opt("Carbon monoxide poisoning", false), opt("Foetal haemoglobin", false))
        ),
        q(subjectId = 8, topicId = 10,
            text = "The primary stimulus for breathing is:",
            explanation = """CONTROL OF BREATHING — PRIMARY STIMULUS: PaCO2 (Carbon dioxide) ★★

CENTRAL CHEMORECEPTORS (medullary — most important):
• Location: Ventral surface of medulla oblongata
• Stimulus: CO2 diffuses across blood-brain barrier → reacts with H2O → H2CO3 → H+ + HCO3-
• H+ (not CO2 directly) stimulates the receptors → increases ventilation
• Responds to: ↑PaCO2 and ↓pH of CSF
• DOES NOT respond to: Hypoxaemia directly

PERIPHERAL CHEMORECEPTORS (less important for CO2):
• Location: Carotid bodies (most important) and aortic bodies
• Respond to: ↓PaO2 (<60 mmHg) ★, ↑PaCO2, ↓pH
• Connected via: Glossopharyngeal nerve (CN IX — carotid), Vagus (CN X — aortic)
• Primary O2 sensors

WHY CO2, NOT O2, IS PRIMARY STIMULUS:
1. PaCO2 fluctuates quickly with metabolism (sensitive)
2. Central chemoreceptors are highly responsive to CO2
3. Normal PaO2 has little effect until it falls significantly (<60 mmHg)

HYPOXIC DRIVE:
In COPD patients with chronic hypercapnia, central receptors are reset (tolerant to high CO2). These patients depend on hypoxia (↓PaO2) as their main ventilatory stimulus → giving HIGH O2 can suppress breathing → CO2 retention → CO2 narcosis → respiratory failure.
Therefore: Controlled O2 in COPD (target SaO2 88–92%).""",
            options = listOf(opt("Elevated PaCO2", true), opt("Low PaO2", false), opt("Low pH (metabolic acidosis)", false), opt("High arterial bicarbonate", false))
        ),
        q(subjectId = 8, topicId = 10,
            text = "Functional residual capacity (FRC) is the sum of:",
            explanation = """LUNG VOLUMES AND CAPACITIES — a fundamental topic in respiratory physiology.

DEFINITIONS:
Tidal Volume (TV): ~500 mL — air breathed in/out normally
Inspiratory Reserve Volume (IRV): ~3000 mL — extra air after normal inspiration
Expiratory Reserve Volume (ERV): ~1200 mL — extra air forcefully expired
Residual Volume (RV): ~1200 mL — air remaining after maximal expiration (cannot be measured by spirometry)

CAPACITIES (sum of 2 or more volumes):
FRC = ERV + RV = 1200 + 1200 = 2400 mL ★
• Air remaining after normal (passive) expiration
• At FRC, elastic recoil of lung INWARDS = chest wall recoil OUTWARDS (equilibrium)
• Measured by: Body plethysmography or helium dilution (NOT spirometry — RV component)

TLC = TV + IRV + ERV + RV = ~6000 mL (Total Lung Capacity)
VC (Vital Capacity) = TV + IRV + ERV = ~4800 mL (measurable by spirometry)
IC (Inspiratory Capacity) = TV + IRV = ~3500 mL
RV = TLC − VC

OBSTRUCTIVE vs RESTRICTIVE PATTERN:
Obstructive (asthma, COPD): ↓FEV1/FVC ratio (<70%), ↑RV, ↑FRC, ↑TLC (air trapping)
Restrictive (fibrosis, obesity): ↓FVC, normal/↑FEV1/FVC ratio (>70%), ↓TLC, ↓FRC

IMPORTANCE OF FRC:
• Prevents alveolar collapse between breaths
• Oxygen reservoir during apnoea
• ↓FRC in: Obesity, pregnancy, supine position, ARDS""",
            options = listOf(opt("Expiratory reserve volume + residual volume", true), opt("Tidal volume + inspiratory reserve volume", false), opt("Tidal volume + expiratory reserve volume", false), opt("Residual volume + tidal volume", false))
        ),
        q(subjectId = 8, topicId = 10,
            text = "Dead space in the lungs refers to:",
            explanation = """DEAD SPACE: The volume of air that is VENTILATED but does NOT participate in gas exchange.

TYPES OF DEAD SPACE:

1. ANATOMICAL DEAD SPACE (~150 mL):
• Air in conducting airways: Nose → trachea → bronchi → bronchioles (down to terminal bronchioles)
• These airways have no alveoli → no gas exchange occurs
• Normal value: ~1 mL/pound body weight = ~150 mL (70 kg person)
• Fowler's method: Measured by nitrogen washout technique

2. ALVEOLAR DEAD SPACE:
• Alveoli that are ventilated but NOT perfused (V/Q → ∞)
• Normal: Nearly zero (in health, all alveoli are perfused)
• Increased in: Pulmonary embolism, pulmonary hypertension

3. PHYSIOLOGICAL DEAD SPACE = Anatomical + Alveolar Dead Space
• Measured by Bohr equation:
  VD/VT = (PaCO2 − PECO2) / PaCO2
• In disease: Physiological > Anatomical

DEAD SPACE INCREASES WITH:
• Pulmonary embolism (↑alveolar dead space) ★
• COPD (destroyed alveolar walls + V/Q mismatch)
• Positive pressure ventilation (over-inflated alveoli compress capillaries)
• Age (progressive)

SHUNT vs DEAD SPACE:
Dead space: V/Q = ∞ (ventilated, not perfused) → air wasted
Shunt: V/Q = 0 (perfused, not ventilated) → blood not oxygenated, does NOT correct with O2""",
            options = listOf(opt("Ventilated but not perfused lung regions", true), opt("Perfused but not ventilated regions", false), opt("Collapsed alveoli", false), opt("Alveoli with fibrosis", false))
        ),

        // ── BIOCHEMISTRY: Metabolic Disorders ─────────────────────────────────
        q(subjectId = 9, topicId = 11,
            text = "Phenylketonuria (PKU) is caused by deficiency of:",
            explanation = """Phenylketonuria (PKU) is an autosomal recessive inborn error of amino acid metabolism.

ENZYME DEFECT: Phenylalanine hydroxylase (PAH) — converts phenylalanine → tyrosine in the liver. Deficiency leads to accumulation of phenylalanine and its toxic metabolites (phenylpyruvate, phenylacetate, phenyllactate).

CLINICAL FEATURES (untreated):
• Intellectual disability (IQ <50)
• Mousy/musty odour of urine (phenylacetic acid)
• Fair skin, blue eyes, blonde hair (reduced melanin — tyrosine needed for melanin)
• Seizures (EEG abnormalities)
• Eczema

SCREENING: Guthrie test (bacterial inhibition assay) — heel-prick blood at 48–72 hours of life. Serum phenylalanine >20 mg/dL is diagnostic.

TREATMENT: Phenylalanine-restricted diet (low-phenylalanine formula) started within first 3 weeks of life. Tyrosine becomes an essential amino acid (must be supplemented). BH4 (tetrahydrobiopterin) — cofactor for PAH; sapropterin (BH4 analogue) helps ~25–50% of patients.

MNEMONIC: "PKU = Pale kids (fair skin), Keen smell (mousy urine), Unkempt brain (intellectual disability)".""",
            options = listOf(opt("Phenylalanine hydroxylase", true), opt("Homogentisic acid oxidase", false), opt("Branched-chain keto acid dehydrogenase", false), opt("Tyrosinase", false))
        ),
        q(subjectId = 9, topicId = 11,
            text = "HbA1c reflects average blood glucose over the preceding:",
            explanation = """HbA1c (Glycated haemoglobin) is formed by non-enzymatic glycation of the N-terminal valine of the β-chain of haemoglobin by glucose. It is a stable ketoamine (Amadori product).

TIME PERIOD: Reflects mean blood glucose over the preceding 8–12 weeks (2–3 months) — the average lifespan of an RBC is ~120 days (about 3 months), but HbA1c reflects mainly the last 8–12 weeks.

DIAGNOSTIC CRITERIA (ADA):
• ≥6.5% = Diabetes mellitus (confirm with repeat test if asymptomatic)
• 5.7–6.4% = Pre-diabetes
• <5.7% = Normal

TREATMENT TARGETS:
• General: HbA1c <7% (ADA), <6.5% (some guidelines)
• Elderly/frail: <8% (to avoid hypoglycaemia)
• Pregnancy (GDM): <6%

FALSE HIGH HbA1c: Iron deficiency anaemia (longer RBC survival), lead poisoning, alcohol.
FALSE LOW HbA1c: Haemolytic anaemia (shorter RBC survival), sickle cell disease, recent blood transfusion, EPO therapy.

MNEMONIC: "HbA1c = 3-month memory of sugar" — like a 3-month report card for diabetes control.""",
            options = listOf(opt("8–12 weeks", true), opt("2–4 weeks", false), opt("6 months", false), opt("1 year", false))
        ),
        q(subjectId = 9, topicId = 11,
            text = "The most sensitive cardiac biomarker after myocardial infarction is:",
            explanation = """CARDIAC BIOMARKERS IN MI — RISE AND FALL:

Marker          | Rise       | Peak        | Return to normal
Troponin I/T    | 3–6 hrs ★  | 12–24 hrs   | 7–10 days (gold standard)
CK-MB           | 3–6 hrs    | 12–24 hrs   | 2–3 days
Myoglobin       | 1–3 hrs    | 6–9 hrs     | 24–36 hrs (earliest, not cardiac-specific)
LDH             | 12–24 hrs  | 3–4 days    | 8–14 days (late marker)

TROPONIN (I and T):
• Most sensitive AND specific for myocardial injury
• Rises at 3–6 hours; remains elevated for 7–10 days (useful for late presenters)
• High-sensitivity troponin (hs-cTn): Can detect MI within 1–2 hours
• Used in NSTEMI and STEMI diagnosis

CK-MB:
• Useful for diagnosing re-infarction (short half-life, falls to normal in 2–3 days)
• If troponin remains elevated but CK-MB rises again = re-infarction

MYOGLOBIN:
• Earliest to rise (1–3 hours) but NOT cardiac-specific (also in skeletal muscle)
• Negative myoglobin = good rule-out tool in early presentation

LDH ISOFORMS: LDH1 > LDH2 = "flipped pattern" in MI (normally LDH2 > LDH1).""",
            options = listOf(opt("Troponin I", true), opt("CK-MB", false), opt("Myoglobin", false), opt("LDH", false))
        ),

        // ── MICROBIOLOGY: Bacteriology ─────────────────────────────────────────
        q(subjectId = 10, topicId = 12,
            text = "The Gram stain uses which decolorizer?",
            explanation = """GRAM STAINING — STEPS (in order):

1. CRYSTAL VIOLET (primary stain) — applied to heat-fixed smear; stains ALL bacteria purple
2. GRAM'S IODINE (mordant) — forms crystal violet-iodine complex (CVI) within cell wall; fixed in both Gram+ve and –ve
3. ACETONE-ALCOHOL (decolorizer) ★ — key step: washes out CVI from Gram–ve (thin PG, outer membrane disrupted); Gram+ve retain CVI (thick PG locks complex)
4. SAFRANIN/NEUTRAL RED (counterstain) — stains decolorized Gram–ve bacteria pink/red

RESULT:
• Gram-positive (thick peptidoglycan): Purple/violet
• Gram-negative (thin peptidoglycan + outer membrane): Pink/red

WHY GRAM+VE RETAIN STAIN: Thick peptidoglycan (20–80 nm) dehydrates with alcohol → pores close → CVI complex trapped.

WHY GRAM–VE LOSE STAIN: Thin peptidoglycan (2–7 nm) + lipopolysaccharide-rich outer membrane (dissolved by alcohol/acetone) → CVI washes out.

CLINICAL RELEVANCE: Gram stain guides empirical antibiotic therapy within minutes of specimen collection (e.g., CSF, sputum, wound swab).""",
            options = listOf(opt("Acetone-alcohol", true), opt("Dilute sulphuric acid", false), opt("Hydrochloric acid-alcohol", false), opt("Carbol fuchsin", false))
        ),
        q(subjectId = 10, topicId = 12,
            text = "Ziehl-Neelsen stain is used to identify:",
            explanation = """ZIEHL-NEELSEN (ZN) STAIN — ACID-FAST STAINING:

PRINCIPLE: Mycobacteria have a waxy cell wall rich in mycolic acids (long-chain fatty acids) that resist decolorisation by acid-alcohol (hence "acid-fast").

STEPS:
1. Carbol fuchsin (primary stain) + heat (Ziehl-Neelsen) or without heat (Kinyoun cold method) — penetrates waxy wall
2. Acid-alcohol (3% HCl in 95% alcohol) — decolorizer: removes stain from non-acid-fast organisms
3. Methylene blue / Malachite green (counterstain) — stains non-acid-fast bacteria blue/green

RESULT:
• Acid-fast bacilli (AFB): Bright red, beaded rods on blue/green background
• Non-acid-fast: Blue/green

ACID-FAST ORGANISMS (MNEMONIC — "MANN"):
• Mycobacterium tuberculosis ★ (and other mycobacteria)
• Actinomyces israelii — weakly acid-fast
• Nocardia — weakly acid-fast
• Cryptosporidium oocysts (modified ZN)

CLINICAL USE: Diagnosis of TB (sputum smear); WHO requires 2 positive smears for diagnosis in resource-limited settings. Sensitivity: ~60% if 3 smears taken.""",
            options = listOf(opt("Acid-fast bacilli (Mycobacteria)", true), opt("Gram-negative cocci", false), opt("Anaerobic organisms", false), opt("Spirochaetes", false))
        ),
        q(subjectId = 10, topicId = 12,
            text = "The causative organism of gas gangrene is:",
            explanation = """GAS GANGRENE (Clostridial myonecrosis):

CAUSATIVE ORGANISM: Clostridium perfringens (Type A) — most common (80–90% of cases). Other species: C. novyi, C. septicum, C. histolyticum.

CHARACTERISTICS OF CLOSTRIDIUM:
• Gram-positive, spore-forming, anaerobic rods
• Spores: Central/subterminal (C. perfringens has no spores on Gram stain — unique!)
• Exotoxins: Alpha toxin (lecithinase/phospholipase C) — most important; causes RBC lysis, platelet destruction, increased capillary permeability

PATHOGENESIS:
• Contaminated wound (soil, faeces) → anaerobic environment → spore germination → alpha toxin production → muscle cell lysis → CO2 + H2 gas → crepitus

CLINICAL FEATURES:
• Severe pain at wound site (disproportionate to appearance)
• Skin: Bronze/brownish discolouration → bullae → necrosis
• Crepitus (gas in tissues — palpable or on X-ray)
• Systemic toxaemia: Fever, tachycardia, hypotension, shock (can be fatal in 24–48 hrs)

TREATMENT:
• Surgical: Wide debridement / amputation (mainstay)
• Antibiotics: High-dose IV Penicillin G + Metronidazole or Clindamycin
• Hyperbaric oxygen (adjunct — inhibits anaerobes)

MNEMONIC: "Clostridium = Gas, Gangrene, Gram+ve, anaerobic rod" """,
            options = listOf(opt("Clostridium perfringens", true), opt("Staphylococcus aureus", false), opt("Bacteroides fragilis", false), opt("Pseudomonas aeruginosa", false))
        ),

        // ── FORENSIC MEDICINE: Thanatology ────────────────────────────────────
        q(subjectId = 11, topicId = 13,
            text = "Rigor mortis is first seen in which muscles?",
            explanation = """RIGOR MORTIS — POST-MORTEM RIGIDITY:

DEFINITION: Stiffening of muscles after death due to depletion of ATP → permanent actin-myosin cross-bridge formation (cannot relax without ATP).

ONSET AND PROGRESSION:
• Starts: 2–6 hours after death (typically 3–4 hours)
• Appears first in: Involuntary muscles (heart, diaphragm) simultaneously with all muscles, but NOTICED FIRST in: Small muscles of face and jaw (masseter, eyelids) ★
• Progression: Face/neck → trunk → upper limbs → lower limbs (Nysten's Law — cephalocaudal direction)
• Maximum stiffness: 12–24 hours
• Passes off: 24–48 hours (proteolytic decomposition of muscle proteins)

NYSTEN'S LAW: Rigor mortis appears and disappears in the same order — from head to feet.

FACTORS AFFECTING ONSET:
Earlier onset: High temperature, muscular person, violent death (exercising muscles), children
Later onset: Cold temperature, elderly, debilitated persons

CADAVERIC SPASM (instantaneous rigor):
• No primary relaxation; occurs instantly at moment of death
• Due to extreme nervous exhaustion (e.g., drowning while clutching vegetation, soldier dying with weapon in hand)
• MEDICOLEGAL importance: Indicates cause/manner of death (suicide vs homicide)

DECOMPOSITION SEQUENCE: Primary relaxation → rigor mortis → secondary relaxation (decomposition)""",
            options = listOf(opt("Small muscles of face and jaw", true), opt("Lower limb muscles", false), opt("Hand muscles", false), opt("Abdominal muscles", false))
        ),
        q(subjectId = 11, topicId = 13,
            text = "Diatom test is performed in cases of suspected:",
            explanation = """DIATOM TEST (Plankton test):

PURPOSE: Used in medicolegal investigation of suspected DROWNING to confirm antemortem (vital) drowning.

PRINCIPLE: Diatoms are microscopic unicellular algae with silica cell walls (frustules), found in all natural water bodies. If a person is alive when submerged (antemortem drowning), diatoms enter the lungs, are absorbed into the bloodstream, and transported to distant organs: liver, kidney, bone marrow, brain.

SIGNIFICANCE:
• Diatoms found in BONE MARROW or DISTANT ORGANS = antemortem drowning (person was alive when submerged, heart was beating to circulate diatoms)
• Diatoms in LUNGS ONLY = postmortem submersion (body placed in water after death — passive entry, no circulation)
• No diatoms = drowning in distilled/purified water, or body retrieved from sea (saltwater diatoms)

PROCEDURE: Acid digestion of tissues (strong acid destroys organic matter, silica frustules remain) → centrifuge → examine under microscope

BEST SPECIMEN: Bone marrow (femur) — most protected from contamination; remains positive even after decomposition.

MEDICOLEGAL IMPORTANCE: Distinguishes antemortem drowning from postmortem submersion (concealing homicide as accidental drowning).""",
            options = listOf(opt("Drowning", true), opt("Electrocution", false), opt("Hanging", false), opt("Poisoning", false))
        ),
        q(subjectId = 11, topicId = 13,
            text = "Tardieu spots are found in cases of death due to:",
            explanation = """TARDIEU SPOTS:

DEFINITION: Subpleural (and subpericardial) petechial haemorrhages seen on the surface of the lungs (and heart) in asphyxial deaths. Named after Auguste Ambroise Tardieu (French forensic pathologist, 1818–1879).

MECHANISM: Asphyxia → raised intrathoracic pressure during struggle to breathe → venous obstruction → increased capillary pressure → rupture of small pulmonary capillaries → petechiae.

FOUND IN:
• HANGING ★ (most classic)
• Strangulation (manual and ligature)
• Suffocation
• Traumatic asphyxia

APPEARANCE:
• Multiple small (pin-point to 3 mm) dark red/purplish spots
• Located under pleura (lung surface) and pericardium (heart surface)
• Can also appear on conjunctivae (subconjunctival petechiae), skin of face/neck

SIGNIFICANCE: Tardieu spots = SIGN OF ASPHYXIA — indicates antemortem struggle for respiration.

DIFFERENCE FROM PETECHIAE IN OTHER CONDITIONS:
• Tardieu spots: Located subpleurally/subpericardially, associated with asphyxial deaths
• Wischnewski spots: Gastric haemorrhages in hypothermia
• Paltauf's haemorrhages: Subpleural pale haemorrhages in drowning""",
            options = listOf(opt("Asphyxia (hanging/strangulation)", true), opt("Drowning", false), opt("Poisoning", false), opt("Burns", false))
        ),

        // ── COMMUNITY MEDICINE: Epidemiology ──────────────────────────────────
        q(subjectId = 12, topicId = 14,
            text = "Sensitivity of a diagnostic test is defined as:",
            explanation = """SENSITIVITY AND SPECIFICITY — CORE CONCEPTS:

2×2 TABLE:
                    Disease +    Disease –
Test Positive:        TP           FP
Test Negative:        FN           TN

SENSITIVITY = TP / (TP + FN) ★
• Ability to correctly identify TRUE POSITIVES (diseased persons)
• = "Positivity in disease" (PID)
• High sensitivity → few false negatives → good SCREENING test
• If sensitivity = 100%: NO false negatives (misses no one with disease)

SPECIFICITY = TN / (TN + FP)
• Ability to correctly identify TRUE NEGATIVES (non-diseased persons)
• = "Negativity in health" (NIH)
• High specificity → few false positives → good CONFIRMATORY test

PPV = TP / (TP + FP) — depends on prevalence (↑ prevalence → ↑ PPV)
NPV = TN / (TN + FN) — depends on prevalence (↑ prevalence → ↓ NPV)

MNEMONIC:
• "SNOUT" = Sensitive test, Negative result rules OUT disease
• "SPIN" = Specific test, Positive result rules IN disease

IDEAL TEST SEQUENCE:
1. Sensitive test first (screening) — cast wide net, avoid missing cases
2. Specific test second (confirmation) — confirm true positives""",
            options = listOf(opt("True positive rate (TP/TP+FN)", true), opt("True negative rate (TN/TN+FP)", false), opt("Proportion of positives that are true", false), opt("Proportion of negatives that are true", false))
        ),
        q(subjectId = 12, topicId = 14,
            text = "Basic Reproduction Number (R0) of >1 indicates:",
            explanation = """BASIC REPRODUCTION NUMBER (R0 — "R-naught"):

DEFINITION: Average number of secondary infections produced by ONE infectious person in a COMPLETELY SUSCEPTIBLE population, in the absence of interventions.

INTERPRETATION:
• R0 > 1: Epidemic will SPREAD (each case infects >1 person → exponential growth) ★
• R0 = 1: Endemic state (disease persists, neither grows nor declines)
• R0 < 1: Epidemic will DIE OUT (each case infects <1 person)

EXAMPLES OF R0 VALUES:
Disease         | R0
Measles         | 12–18 (highest known)
COVID-19 (orig) | 2–3
COVID-19 (Delta)| 5–8
Influenza       | 2–3
Polio           | 5–7
Smallpox        | 5–7
Ebola           | 1.5–2.5

HERD IMMUNITY THRESHOLD (HIT):
HIT = 1 − (1/R0)
Example: Measles R0=15 → HIT = 1 − 1/15 = 93.3% (need 93% immune for herd protection)

EFFECTIVE REPRODUCTION NUMBER (Re/Rt):
• Accounts for partial immunity and interventions
• Re = R0 × proportion susceptible
• Goal of vaccination/lockdowns: Reduce Re below 1""",
            options = listOf(opt("Epidemic will spread in the population", true), opt("Epidemic will die out", false), opt("Disease is endemic", false), opt("Herd immunity has been achieved", false))
        ),
        q(subjectId = 12, topicId = 14,
            text = "In a cohort study, the measure of association calculated is:",
            explanation = """STUDY DESIGNS AND THEIR MEASURES OF ASSOCIATION:

COHORT STUDY:
• Design: Follow exposed vs unexposed groups forward in time → observe who develops disease
• Measure: RELATIVE RISK (RR) = Risk Ratio ★
• RR = (Incidence in exposed) / (Incidence in unexposed)
• RR = 1: No association; RR > 1: Risk factor; RR < 1: Protective factor
• Example: Follow smokers vs non-smokers for 20 years → calculate RR for lung cancer

CASE-CONTROL STUDY:
• Design: Cases (with disease) vs controls (without disease) → look back at exposure
• Measure: ODDS RATIO (OR)
• OR approximates RR when disease is RARE (<10%)
• Used for rare diseases, quick and cheap

RANDOMISED CONTROLLED TRIAL (RCT):
• Experimental study; gold standard for causality
• Measure: RR, ARR (Absolute Risk Reduction), NNT (Number Needed to Treat)

CROSS-SECTIONAL STUDY:
• Point-in-time snapshot
• Measure: Prevalence; Prevalence Ratio; cannot determine causality (no time sequence)

MNEMONIC: "Cohort = Relative Risk; Case-Control = Odds Ratio"
"Can't Calculate RR from Case-Control (use OR instead)".""",
            options = listOf(opt("Relative Risk (Risk Ratio)", true), opt("Odds Ratio", false), opt("Attributable Risk only", false), opt("Prevalence Ratio", false))
        ),

        // ── OPHTHALMOLOGY: Anterior Segment ───────────────────────────────────
        q(subjectId = 13, topicId = 15,
            text = "The commonest cause of preventable blindness worldwide is:",
            explanation = """GLOBAL CAUSES OF BLINDNESS (WHO Data):

LEADING CAUSES OF BLINDNESS (GLOBALLY):
1. Cataract ★ — 51% of world blindness; MOST COMMON and MOST PREVENTABLE cause
2. Glaucoma — 8% (irreversible)
3. Age-related macular degeneration — 5%
4. Corneal opacity — 4%
5. Diabetic retinopathy — 1%
6. Trachoma — 3% (most common INFECTIOUS cause of preventable blindness)

CATARACT:
• Opacity of the crystalline lens
• Causes: Ageing (senile — most common), congenital (TORCH infections, galactosaemia), metabolic (diabetes), trauma, radiation, drugs (steroids → posterior subcapsular)
• Treatment: Surgical (Phacoemulsification + IOL implantation)

IN INDIA SPECIFICALLY:
• Cataract remains the leading cause of blindness
• National Programme for Control of Blindness (NPCB) targets cataract

TRACHOMA: Chlamydia trachomatis (serotypes A, B, Ba, C) → repeated infections → conjunctival scarring → entropion/trichiasis → corneal ulceration → blindness. SAFE strategy (Surgery, Antibiotics, Facial cleanliness, Environmental improvement).

MNEMONIC: "Cataract Causes Cloudy sight Chronically" — commonest preventable cause globally.""",
            options = listOf(opt("Cataract", true), opt("Glaucoma", false), opt("Trachoma", false), opt("Diabetic retinopathy", false))
        ),
        q(subjectId = 13, topicId = 15,
            text = "Increased cup-to-disc ratio is a feature of:",
            explanation = """CUP-TO-DISC RATIO (CDR):

NORMAL CDR: ≤0.5 (optic cup ≤ half the diameter of the optic disc). A CDR of 0.3–0.4 is typical in healthy eyes.

INCREASED CDR (>0.6, especially asymmetric):
• GLAUCOMA ★ — most important cause of raised CDR
• Pathophysiology: Raised intraocular pressure (IOP) → compression of optic nerve fibres at lamina cribrosa → progressive loss of nerve fibres → enlargement of optic cup
• Clinical: CDR >0.7, asymmetric CDR (difference >0.2 between eyes), notching of the rim (inferior > superior > nasal > temporal — ISNT rule violated)

TYPES OF GLAUCOMA:
Primary open-angle glaucoma (POAG): Most common; painless; gradual peripheral visual field loss; IOP usually >21 mmHg
Primary angle-closure glaucoma (PACG): Acute painful red eye, haloes, nausea, fixed mid-dilated pupil, shallow anterior chamber; commoner in Asians and hypermetropes

MEASUREMENT: Slit-lamp biomicroscopy, Optical coherence tomography (OCT) for RNFL thickness.

TREATMENT: Reduce IOP — Prostaglandin analogues (latanoprost — first-line), beta-blockers (timolol), carbonic anhydrase inhibitors (dorzolamide), miotics (pilocarpine). Laser trabeculoplasty. Surgery: Trabeculectomy.""",
            options = listOf(opt("Glaucoma", true), opt("Papilloedema", false), opt("Retinal detachment", false), opt("Cataract", false))
        ),
        q(subjectId = 13, topicId = 15,
            text = "Leukocoria (white pupillary reflex) in a child is most likely due to:",
            explanation = """LEUKOCORIA — WHITE PUPILLARY REFLEX:

DEFINITION: Abnormal white or yellowish light reflection from the pupil (instead of normal red reflex). A RED FLAG sign in children requiring urgent evaluation.

COMMONEST CAUSE IN CHILDREN: RETINOBLASTOMA ★ — most common intraocular malignancy of childhood.

CAUSES OF LEUKOCORIA (MNEMONIC — "CLAMPS"):
• Cataract (congenital) — most common cause overall
• Leukocoria from Retinoblastoma ★ — most common MALIGNANT cause, most important
• Astrocytic hamartoma (tuberous sclerosis)
• Myelinated nerve fibres
• Persistent fetal vasculature (PFV)
• Severe toxocariasis (Toxocara canis — endophthalmitis)

RETINOBLASTOMA:
• Gene: RB1 tumour suppressor gene (chromosome 13q14) — "Two-hit hypothesis" (Knudson)
• Hereditary (40%): Bilateral, earlier onset, AD inheritance, germline mutation
• Sporadic (60%): Unilateral, later onset, somatic mutation
• Most common presentation: Leukocoria (60%), strabismus (20%)
• Treatment: Depends on extent — chemotherapy, laser, cryotherapy, enucleation for advanced disease

IMPORTANT: Any child with absent red reflex or leukocoria → URGENT ophthalmology referral.""",
            options = listOf(opt("Retinoblastoma", true), opt("Glaucoma", false), opt("Conjunctivitis", false), opt("Optic neuritis", false))
        ),

        // ── ENT: Ear Disorders ─────────────────────────────────────────────────
        q(subjectId = 14, topicId = 16,
            text = "Cholesteatoma is best described as:",
            explanation = """CHOLESTEATOMA:

DEFINITION: A destructive, expanding growth consisting of keratinising stratified squamous epithelium within the middle ear cleft. Despite the name, it is NOT a tumour and does NOT contain cholesterol.

TYPES:
• Congenital: Behind intact tympanic membrane; no history of ear discharge
• Acquired (most common):
  - Primary acquired: Invagination of Shrapnell's membrane (pars flaccida) — most common type
  - Secondary acquired: Entry of squamous epithelium through a perforation (unsafe CSOM)

PATHOLOGY: Keratin accumulation → expansion → enzymatic bone erosion (collagenases) → destruction of ossicles, mastoid, tegmen, facial canal, semicircular canals.

CLINICAL FEATURES:
• Foul-smelling, scanty, purulent ear discharge (attic discharge) ★
• Hearing loss (conductive → mixed as sensorineural component develops)
• Pars flaccida (attic) retraction pocket with keratin debris
• Granulation tissue

COMPLICATIONS (SERIOUS):
Extracranial: Facial nerve palsy, labyrinthitis, mastoid abscess
Intracranial: Meningitis, brain abscess, lateral sinus thrombosis, otitic hydrocephalus

DIAGNOSIS: CT temporal bone (gold standard for extent of disease)
TREATMENT: Surgical — Tympanomastoidectomy (complete removal of cholesteatoma; canal wall down / up)""",
            options = listOf(opt("Keratinising squamous epithelium within middle ear", true), opt("A benign cholesterol cyst", false), opt("Malignant tumour of the ear", false), opt("Chronic suppurative otitis media with no complications", false))
        ),
        q(subjectId = 14, topicId = 16,
            text = "The most common cause of conductive hearing loss in adults is:",
            explanation = """TYPES OF HEARING LOSS:

CONDUCTIVE HEARING LOSS (CHL): Problem in outer or middle ear — sound transmission impaired.
SENSORINEURAL HEARING LOSS (SNHL): Problem in cochlea or auditory nerve — sound perception impaired.
MIXED: Both components.

MOST COMMON CAUSE OF CHL IN ADULTS: OTOSCLEROSIS ★

OTOSCLEROSIS:
• Abnormal bone remodelling (spongiosis) of the bony labyrinth, specifically fixation of the STAPES footplate in the oval window
• Demographics: Young adults (15–45 years); females > males (2:1); often bilateral
• Inheritance: Autosomal dominant with variable penetrance; measles virus implicated
• Symptoms: Progressive CHL, may have tinnitus; paracusis Willisii (hears better in noisy environments)

AUDIOLOGICAL FINDINGS:
• Carhart's notch: Dip in bone conduction at 2000 Hz (characteristic of otosclerosis)
• Type As (shallow) tympanogram (stiff ossicular chain)
• Absent stapedial reflexes

TREATMENT:
• Surgical: Stapedectomy / Stapedotomy (replacement of stapes — excellent results)
• Medical (slow progression): Sodium fluoride, bisphosphonates
• Hearing aid (if surgery declined)

OTHER COMMON CAUSES OF CHL: Wax (cerumen) impaction, Otitis media with effusion (glue ear — commonest in children), Tympanic membrane perforation, Ossicular discontinuity.""",
            options = listOf(opt("Otosclerosis", true), opt("Presbycusis", false), opt("Acoustic neuroma", false), opt("Ménière's disease", false))
        ),
        q(subjectId = 14, topicId = 16,
            text = "Rinne test is negative (BC > AC) in:",
            explanation = """RINNE TEST:

PROCEDURE: 512 Hz tuning fork placed on mastoid process (bone conduction — BC), then in front of external auditory meatus (air conduction — AC).

NORMAL (Rinne Positive): AC > BC (air conduction better; sound heard longer/louder via air) ★ — indicates NORMAL hearing or SENSORINEURAL HEARING LOSS

ABNORMAL (Rinne Negative): BC > AC ★ — indicates CONDUCTIVE HEARING LOSS (≥15–20 dB)

REASON: In CHL, the middle ear mechanism is impaired, so AC is reduced; BC bypasses the middle ear and is unaffected.

FALSE NEGATIVE RINNE (Pseudo Rinne Negative):
• Occurs in SEVERE UNILATERAL SENSORINEURAL HEARING LOSS (dead ear)
• The dead ear appears BC > AC — actually hearing bone conduction via the OPPOSITE (normal) cochlea
• Corrected by Weber test and masking

WEBER TEST:
• Lateralises to AFFECTED ear in CHL (better bone conduction to that side)
• Lateralises to UNAFFECTED ear in SNHL (better cochlea)

ABSOLUTE BONE CONDUCTION (ABC) TEST (Schwabach):
• Compare patient's BC with examiner's BC
• Reduced in SNHL; normal in CHL

COMMON CAUSES GIVING NEGATIVE RINNE: Otosclerosis, wax, otitis media with effusion (OME), tympanic membrane perforation.""",
            options = listOf(opt("Conductive hearing loss", true), opt("Sensorineural hearing loss", false), opt("Normal hearing", false), opt("Mixed hearing loss", false))
        ),

        // ── ORTHOPEDICS: Fractures ─────────────────────────────────────────────
        q(subjectId = 15, topicId = 17,
            text = "Garden classification is used for fractures of the:",
            explanation = """GARDEN CLASSIFICATION — FEMORAL NECK FRACTURES:

SITE: Intracapsular fracture of the femoral neck (subcapital fracture)

CLASSIFICATION (based on degree of displacement and trabecular alignment):
Stage I: Incomplete (impacted in valgus) — trabecular pattern not fully interrupted
Stage II: Complete but undisplaced — trabeculae misaligned but no displacement
Stage III: Complete, partially displaced — trabecular pattern disrupted, partial displacement
Stage IV: Complete, fully displaced — complete loss of trabecular alignment

CLINICAL IMPORTANCE:
• Garden I & II = Undisplaced → Internal fixation (cannulated screws) — preserve femoral head
• Garden III & IV = Displaced → Higher risk of avascular necrosis (AVN) → Hemiarthroplasty (elderly) or Total Hip Replacement

BLOOD SUPPLY OF FEMORAL HEAD:
• Main supply: Medial femoral circumflex artery (retinacular vessels — posterior capsule)
• Minor: Ligamentum teres (foveolar artery) — significant only in children
• Fracture disrupts retinacular vessels → AVN risk 15–35% (displaced) vs <5% (undisplaced)

COMPLICATIONS:
Early: Fat embolism, DVT/PE, pressure sores
Late: AVN (most important), non-union, osteoarthritis

MNEMONIC: "Garden I-II = Fix it; Garden III-IV = Replace it (elderly) or Fix cautiously (young)".""",
            options = listOf(opt("Femoral neck", true), opt("Distal radius", false), opt("Humeral shaft", false), opt("Patella", false))
        ),
        q(subjectId = 15, topicId = 17,
            text = "Volkmann's ischaemic contracture most commonly follows fracture of:",
            explanation = """VOLKMANN'S ISCHAEMIC CONTRACTURE:

DEFINITION: A serious complication resulting from compartment syndrome → ischaemia → fibrosis and contracture of muscles of the forearm, resulting in a characteristic deformity.

MOST COMMON CAUSE: Supracondylar fracture of the humerus in CHILDREN ★ (most common paediatric elbow fracture — extension type, Gartland classification)

PATHOPHYSIOLOGY:
Supracondylar fracture / tight cast / swelling → Increased compartment pressure → Compression of brachial artery and anterior interosseous nerve → Muscle ischaemia → Necrosis → Fibrosis → Contracture

6 P's of COMPARTMENT SYNDROME:
• Pain (disproportionate, especially on passive stretch) — earliest and most reliable
• Pressure (tense/woody compartment)
• Paraesthesia (nerve ischaemia)
• Paralysis (late sign — motor nerve ischaemia)
• Pallor
• Pulselessness (late, unreliable — pulse may be present despite compartment syndrome)

DEFORMITY OF ESTABLISHED CONTRACTURE:
• Elbow: Flexed
• Forearm: Pronated
• Wrist: Flexed
• Fingers: Flexed at MCP and IP joints (intrinsic minus hand)
• Thumb: Adducted

TREATMENT:
Acute (compartment syndrome): Emergency fasciotomy (within 6 hours)
Established: Physiotherapy → Muscle slide operation (Page's) → Tendon lengthening""",
            options = listOf(opt("Supracondylar fracture of humerus", true), opt("Fracture of both bones of forearm", false), opt("Colles fracture", false), opt("Monteggia fracture-dislocation", false))
        ),
        q(subjectId = 15, topicId = 17,
            text = "The most common primary malignant bone tumour in adolescents is:",
            explanation = """PRIMARY MALIGNANT BONE TUMOURS — AGE AND SITE:

OSTEOSARCOMA (Osteogenic sarcoma):
• Most common primary malignant bone tumour overall ★
• Peak age: 10–20 years (adolescents) — second peak in elderly (Paget's disease associated)
• Most common site: Distal femur (metaphysis) > proximal tibia > proximal humerus — around the knee

EWING'S SARCOMA:
• Second most common primary malignant bone tumour in children/adolescents
• Peak age: 5–15 years
• Site: Diaphysis of long bones (unlike osteosarcoma which affects metaphysis)
• X-ray: "Onion skin" periosteal reaction

CHONDROSARCOMA:
• Cartilage-forming malignant tumour
• Peak age: >40 years (older adults)
• Site: Pelvis, proximal femur, shoulder girdle

MULTIPLE MYELOMA: Most common overall bone malignancy (but it is haematological, not primary bone tumour in strict sense)

OSTEOSARCOMA — X-RAY:
• Codman's triangle: Elevated periosteum (reactive new bone at periphery of tumour)
• Sunburst pattern: Perpendicular periosteal reaction (classic but less specific)
• Mixed lytic and sclerotic lesion at metaphysis

TREATMENT: Wide surgical resection + limb salvage surgery (where possible) + chemotherapy (neoadjuvant + adjuvant: methotrexate, doxorubicin, cisplatin — MAP regimen)""",
            options = listOf(opt("Osteosarcoma", true), opt("Chondrosarcoma", false), opt("Ewing's sarcoma", false), opt("Giant cell tumour", false))
        ),

        // ── PSYCHIATRY: Mood Disorders ─────────────────────────────────────────
        q(subjectId = 16, topicId = 18,
            text = "First-rank symptoms (Schneider) of schizophrenia include:",
            explanation = """SCHNEIDER'S FIRST-RANK SYMPTOMS (FRS) OF SCHIZOPHRENIA:

DEFINITION: Specific psychopathological experiences described by Kurt Schneider (1959) as highly characteristic (though not pathognomonic) of schizophrenia.

FIRST-RANK SYMPTOMS (MNEMONIC — "ABCDE"):
A — Auditory hallucinations (3 types):
  • Voices discussing the patient in 3rd person
  • Voices giving a running commentary on actions
  • Echo of thought (thought echo / écho de la pensée)
B — Broadcasting of thought (Thought broadcasting)
C — Controlled passivity experiences (Made actions, impulses, feelings)
D — Delusional perception (Normal perception → abnormal meaning suddenly, in 2 steps)
E — Experiences of thought alienation:
  • Thought insertion (thoughts inserted by external agency)
  • Thought withdrawal (thoughts removed by external force)

SECOND-RANK SYMPTOMS: Other hallucinations, perplexity, depressive/euphoric changes, emotional blunting (less specific).

CURRENT STATUS: DSM-5 no longer distinguishes FRS specifically (moved away from Schneiderian criteria). ICD-10 still uses FRS prominently. FRS are present in ~70% of schizophrenia but also in ~10–15% of mania and psychotic depression.

TREATMENT: Antipsychotics (dopamine D2 antagonists). Atypical (clozapine — treatment-resistant). CBT, family therapy.""",
            options = listOf(opt("Thought broadcasting and auditory hallucinations in 3rd person", true), opt("Visual hallucinations and amnesia", false), opt("Depressed mood and suicidal ideation", false), opt("Obsessions and compulsions", false))
        ),
        q(subjectId = 16, topicId = 18,
            text = "Drug of choice for acute mania is:",
            explanation = """ACUTE MANIA — PHARMACOLOGICAL MANAGEMENT:

FIRST-LINE TREATMENT FOR ACUTE MANIA:
• Lithium carbonate ★ (drug of choice for classic euphoric mania, less effective for mixed/dysphoric mania)
• Valproate (sodium valproate / divalproex) — preferred when rapid control needed, mixed features, or substance abuse
• Atypical antipsychotics: Olanzapine, risperidone, quetiapine, aripiprazole

LITHIUM IN MANIA:
• Mechanism: Inhibits inositol monophosphatase → reduces phosphatidylinositol signalling; also affects GSK-3β
• Time to effect: 5–10 days (slow onset — often combined with antipsychotic for immediate control)
• Therapeutic range: 0.6–1.2 mEq/L (acute mania); 0.8–1.0 mEq/L (maintenance)
• Toxic level: >1.5 mEq/L

LITHIUM TOXICITY SIGNS (in order of increasing severity):
• Mild (1.5–2.0): Nausea, vomiting, diarrhoea, fine tremor, polyuria
• Moderate (2.0–2.5): Coarse tremor, ataxia, confusion, drowsiness
• Severe (>2.5): Seizures, coma, arrhythmias, death

MONITORING: Thyroid function (hypothyroidism), renal function (nephrogenic DI), ECG (T-wave changes), serum levels every 3 months.

CONTRAINDICATIONS: Renal impairment, first trimester pregnancy (Ebstein's anomaly risk), dehydration.""",
            options = listOf(opt("Lithium", true), opt("Haloperidol alone", false), opt("Diazepam", false), opt("Fluoxetine", false))
        ),
        q(subjectId = 16, topicId = 18,
            text = "Electroconvulsive therapy (ECT) is the treatment of choice in:",
            explanation = """ELECTROCONVULSIVE THERAPY (ECT):

MECHANISM: Brief electrical stimulus → generalised seizure → therapeutic effect (mechanism not fully understood). Increases monoamine neurotransmission, promotes neurogenesis, modulates HPA axis.

ABSOLUTE INDICATIONS (life-saving, ECT is DOC):
• Severe depression with suicidal intent (high immediate risk) ★
• Catatonia (especially malignant catatonia / lethal catatonia)
• Severe depression with refusal to eat/drink (danger of starvation)
• Pregnancy with severe depression (safer than antidepressants)

RELATIVE INDICATIONS:
• Treatment-resistant depression (failed ≥2 antidepressants)
• Puerperal (post-partum) psychosis
• Acute mania not responding to drugs
• Severe psychotic depression

CONTRAINDICATIONS:
• No absolute contraindications to ECT
• Relative: Recent MI (<3 months), raised intracranial pressure, phaeochromocytoma, aortic aneurysm

SIDE EFFECTS:
• Cognitive: Anterograde + retrograde amnesia (usually transient) — most common complaint
• Cardiovascular: Transient bradycardia (vagal) → tachycardia
• Headache, myalgia (from succinylcholine)

TECHNIQUE: Modified ECT (with anaesthesia + succinylcholine for muscle relaxation); bilateral or unilateral (non-dominant) electrode placement; 6–12 sessions typically.""",
            options = listOf(opt("Severe depression with high suicidal risk", true), opt("Mild anxiety disorder", false), opt("Substance use disorder", false), opt("Obsessive-compulsive disorder", false))
        ),

        // ── DERMATOLOGY: Skin Infections ──────────────────────────────────────
        q(subjectId = 17, topicId = 19,
            text = "Nikolsky sign is positive in:",
            explanation = """NIKOLSKY SIGN:

DEFINITION: Lateral sliding pressure on clinically normal skin adjacent to a blister causes the superficial epidermis to slide/detach, revealing a moist red erosion beneath. Named after Pyotr Nikolsky (Russian dermatologist, 1858–1940).

MECHANISM: Present when there is intraepidermal splitting (loss of cohesion between epidermal keratinocytes) due to destruction of desmoglein (desmosomal protein).

POSITIVE IN (INTRAEPIDERMAL BLISTERING — LEVEL: STRATUM SPINOSUM/GRANULOSA):
• Pemphigus vulgaris ★ (anti-desmoglein 3 antibodies) — most classic
• Staphylococcal Scalded Skin Syndrome (SSSS) — exfoliative toxin cleaves desmoglein 1
• Toxic Epidermal Necrolysis (TEN) — drug-induced, widespread epidermal detachment
• Pemphigus foliaceus (anti-desmoglein 1)

NEGATIVE IN (SUBEPIDERMAL BLISTERING — LEVEL: BELOW EPIDERMIS):
• Bullous pemphigoid (anti-BP180/BP230 — basement membrane) — Nikolsky NEGATIVE
• Dermatitis herpetiformis
• Epidermolysis bullosa

PEMPHIGUS VULGARIS vs BULLOUS PEMPHIGOID:
Feature            | Pemphigus vulgaris | Bullous pemphigoid
Age                | 40–60 years        | Elderly (>60)
Nikolsky           | Positive ★         | Negative
Blister            | Flaccid, fragile    | Tense, firm
Mucous membrane    | Involved (90%)     | Rarely involved
Antibody           | Anti-Dsg3          | Anti-BP180""",
            options = listOf(opt("Pemphigus vulgaris", true), opt("Bullous pemphigoid", false), opt("Lichen planus", false), opt("Psoriasis", false))
        ),
        q(subjectId = 17, topicId = 19,
            text = "Wickham's striae are seen in:",
            explanation = """WICKHAM'S STRIAE:

DEFINITION: Fine, whitish, lace-like network or network of lines seen on the surface of papules of lichen planus. Described by Louis Frédéric Wickham (1895). Best seen with dermatoscope or hand lens with oil immersion.

LICHEN PLANUS (LP) — 6 P's:
• Pruritic
• Purple (violaceous)
• Polygonal
• Planar (flat-topped)
• Papules (and plaques)
• Present on flexor aspects (wrists, ankles, lower back)

WICKHAM'S STRIAE: Pathognomonic of lichen planus ★. Represent focal thickening of granular layer (hypergranulosis) seen through the epidermis.

HISTOPATHOLOGY OF LP:
• Hyperkeratosis + wedge-shaped hypergranulosis (Wickham's striae)
• Irregular acanthosis (saw-toothed rete ridges)
• Band-like lymphocytic infiltrate (T cells) at dermoepidermal junction
• Civatte bodies (colloid/cytoid bodies) = dyskeratotic keratinocytes

VARIANTS: Oral LP (reticular pattern on buccal mucosa most common), hypertrophic LP (legs), atrophic LP, bullous LP, annular LP

TREATMENT: Topical steroids (first-line); systemic steroids for extensive disease; acitretin; tacrolimus for oral LP

LP ASSOCIATIONS: Hepatitis C (strong), primary biliary cirrhosis, autoimmune thyroid disease""",
            options = listOf(opt("Lichen planus", true), opt("Psoriasis", false), opt("Pemphigus vulgaris", false), opt("Tinea corporis", false))
        ),
        q(subjectId = 17, topicId = 19,
            text = "Auspitz sign is characteristic of:",
            explanation = """AUSPITZ SIGN:

DEFINITION: Appearance of multiple bleeding points (like drops of blood or dew) when a psoriatic scale is removed by scraping. The bleeding occurs because the dilated capillaries of the elongated dermal papillae are exposed when the thin suprapapillary plates are removed.

PSORIASIS — KEY FEATURES:
• Chronic, relapsing, immune-mediated disorder (Th1/Th17 driven) — IL-17, IL-23 pathway
• Genetics: HLA-Cw6 (strongest association)

SIGNS ON SCRAPING (Grattage test — done with curette/glass slide):
1. Candle grease sign (Bougie de cire): Scraping produces whitish scales (like scraping wax from candle)
2. Last membrane / Bulkeley's membrane: Further scraping reveals thin transparent membrane
3. Auspitz sign ★: Further scraping reveals multiple bleeding points (pinpoint bleeding)

HISTOPATHOLOGY:
• Acanthosis (epidermal thickening)
• Munro microabscesses (neutrophils in stratum corneum)
• Spongiform pustules of Kogoj (neutrophils in spinous layer)
• Elongated rete ridges, suprapapillary plate thinning

TREATMENT: Topical (steroids, calcipotriol, coal tar, dithranol); Phototherapy (NB-UVB); Systemic (methotrexate, cyclosporine, acitretin); Biologics (TNF inhibitors: adalimumab, etanercept; IL-17 inhibitors: secukinumab; IL-23 inhibitors: guselkumab)""",
            options = listOf(opt("Psoriasis", true), opt("Lichen planus", false), opt("Pemphigus vulgaris", false), opt("Seborrhoeic dermatitis", false))
        ),

        // ── RADIOLOGY: Imaging Techniques ─────────────────────────────────────
        q(subjectId = 18, topicId = 20,
            text = "X-ray of chest in tension pneumothorax shows tracheal shift to the:",
            explanation = """TENSION PNEUMOTHORAX:

PATHOPHYSIOLOGY: One-way valve mechanism — air enters pleural space on inspiration but cannot escape → progressive accumulation → increasing intrapleural pressure → ipsilateral lung collapse → mediastinal shift to OPPOSITE side → compression of contralateral lung and great veins → reduced venous return → cardiovascular collapse (obstructive shock).

X-RAY FINDINGS:
• Absent lung markings on affected side ★
• Hyperlucent hemithorax
• Trachea shifted to OPPOSITE (contralateral) side ★ (pushed away from the tension)
• Mediastinum shifted to opposite side
• Depressed/flattened ipsilateral hemidiaphragm
• Contralateral lung compressed

CLINICAL PRESENTATION: (Tension PNX is a CLINICAL diagnosis — do NOT wait for X-ray if patient is deteriorating)
• Severe respiratory distress
• Tracheal deviation (late sign, contralateral)
• Absent breath sounds on affected side
• Distended neck veins (raised JVP) — due to impaired venous return
• Hypotension, tachycardia → cardiovascular collapse

COMPARE with simple PNX: No tracheal shift, no haemodynamic compromise.

TREATMENT: DO NOT wait for X-ray — immediate needle decompression (2nd ICS, midclavicular line) followed by formal chest drain (5th ICS, midaxillary line — safe triangle).""",
            options = listOf(opt("Opposite (contralateral) side", true), opt("Same (ipsilateral) side", false), opt("Trachea is central", false), opt("Trachea is not visible", false))
        ),
        q(subjectId = 18, topicId = 20,
            text = "Investigation of choice for detection of pulmonary embolism is:",
            explanation = """PULMONARY EMBOLISM (PE) — DIAGNOSIS:

GOLD STANDARD / INVESTIGATION OF CHOICE: CT Pulmonary Angiography (CTPA) ★

CTPA:
• Sensitivity: ~95–98%; Specificity: ~97%
• Fast, widely available, non-invasive
• Can see clot in pulmonary arteries directly (saddle embolus, lobar, segmental)
• Also identifies alternative diagnoses (pneumonia, pleural effusion, etc.)
• Contraindications: Contrast allergy, severe renal impairment

OTHER INVESTIGATIONS:
V/Q Scan (Ventilation/Perfusion scan):
• Useful when CTPA contraindicated (allergy to contrast, pregnancy, renal failure)
• Matched defect = normal; Unmatched (ventilated, non-perfused) = high probability PE
• Result interpreted as: Low/intermediate/high probability

D-dimer:
• High sensitivity (>95%) but LOW specificity
• Negative D-dimer effectively RULES OUT PE in LOW pre-test probability (Wells score ≤4)
• Positive D-dimer → proceed to imaging

ECG in PE: Sinus tachycardia (most common); S1Q3T3 pattern (classic but uncommon); right heart strain (RBBB, P-pulmonale); T-wave inversion in V1–V4

CXR in PE: Usually normal; Hampton's hump (wedge opacity, infarction); Westermark sign (oligaemia distal to embolus)

TREATMENT: Anticoagulation (LMWH, DOACs); thrombolysis (alteplase) for massive PE with haemodynamic compromise.""",
            options = listOf(opt("CT pulmonary angiography (CTPA)", true), opt("Chest X-ray", false), opt("V/Q scan", false), opt("ECG", false))
        ),
        q(subjectId = 18, topicId = 20,
            text = "MRI is preferred over CT for imaging of:",
            explanation = """MRI vs CT — WHEN TO USE WHICH:

MRI ADVANTAGES OVER CT:
• Superior soft tissue contrast (no ionising radiation)
• Better for: Brain parenchyma, spinal cord, posterior fossa, musculoskeletal soft tissues, liver lesions

SPECIFIC INDICATIONS WHERE MRI IS PREFERRED:
1. BRAIN: Posterior fossa lesions (CT has beam hardening artefact from bone) ★
2. SPINAL CORD: Myelopathy, cord compression, syringomyelia, disc disease
3. MUSCULOSKELETAL: Soft tissue tumours, ligament/meniscal tears (ACL, MCL), osteomyelitis, avascular necrosis
4. LIVER: Characterisation of hepatic lesions (HCC, haemangioma, metastases)
5. Breast: Breast MRI for high-risk screening, implant assessment
6. Prostate: Multiparametric MRI for prostate cancer staging
7. BRAIN STROKE: DWI MRI detects acute ischaemic infarct within minutes (CT misses early infarcts)

CT ADVANTAGES OVER MRI:
• Faster (seconds vs minutes) — trauma, unstable patients
• Better for: Bone detail, calcifications, lung parenchyma, bowel (pneumoperitoneum)
• Cheaper, more available

CONTRAINDICATIONS TO MRI:
• Cardiac pacemakers (most older types), cochlear implants, metallic foreign body in orbit
• Claustrophobia (can use open MRI or sedation)
• First trimester pregnancy (relative — avoid unless essential)""",
            options = listOf(opt("Posterior fossa and spinal cord lesions", true), opt("Acute bony fractures", false), opt("Lung parenchymal lesions", false), opt("Bowel perforation", false))
        ),

        // ── ANESTHESIA: General Anaesthesia ───────────────────────────────────
        q(subjectId = 19, topicId = 21,
            text = "Mallampati classification is used to assess:",
            explanation = """MALLAMPATI CLASSIFICATION:

PURPOSE: Predicts difficult laryngoscopy/intubation by assessing the relationship between tongue size and pharyngeal space. Described by SR Mallampati (1985), modified by Samsoon and Young.

ASSESSMENT: Patient sits upright, opens mouth maximally, protrudes tongue, no phonation.

CLASSES (what is visible):
Class I: Soft palate, uvula, fauces, tonsillar pillars — all visible (EASY intubation)
Class II: Soft palate, uvula, fauces visible (pillars not seen)
Class III: Only soft palate and base of uvula visible
Class IV: Only hard palate visible (DIFFICULT intubation ★)

CORRELATION WITH INTUBATION:
Class I & II = Expected easy laryngoscopy
Class III & IV = Expected difficult laryngoscopy (consider awake fibreoptic intubation)

OTHER PREDICTORS OF DIFFICULT AIRWAY (MNEMONIC — "LEMON"):
L — Look externally (obesity, receding chin, large tongue, beard, short neck)
E — Evaluate 3-3-2 rule:
  • 3 fingers between incisors (inter-incisor gap ≥3 cm)
  • 3 fingers from chin to hyoid (thyromental distance ≥3 finger-breadths = 6 cm)
  • 2 fingers from hyoid to thyroid notch
M — Mallampati (Class III/IV)
O — Obstruction (tumour, haematoma, epiglottitis, Ludwig's angina)
N — Neck mobility (restricted = risk: ankylosing spondylitis, cervical spine injury)""",
            options = listOf(opt("Difficulty of endotracheal intubation", true), opt("Depth of anaesthesia", false), opt("Risk of aspiration", false), opt("Cardiac output", false))
        ),
        q(subjectId = 19, topicId = 21,
            text = "Minimum Alveolar Concentration (MAC) of an inhalational anaesthetic is:",
            explanation = """MINIMUM ALVEOLAR CONCENTRATION (MAC):

DEFINITION: The alveolar concentration (expressed as % of 1 atmosphere) of an inhalational anaesthetic at 1 atmosphere that prevents movement in 50% of patients in response to a standardised surgical stimulus (skin incision). Measured at steady state.

• MAC is an ED50 — median effective dose
• At 1 MAC: 50% of patients do NOT move with surgical incision
• At 1.3 MAC: ~95% of patients do not move (MAC-95)

MAC VALUES (memorise these):
Agent           | MAC (%)
Nitrous oxide   | 104% (>100% — cannot achieve MAC at 1 atm alone, used as adjunct)
Desflurane      | 6–7%
Sevoflurane     | 2%
Isoflurane      | 1.17%
Halothane       | 0.75%
Xenon           | 71%

LOWER MAC = MORE POTENT (less concentration needed)

FACTORS DECREASING MAC (↓ anaesthetic requirement):
• Increasing age (elderly need less)
• Hypothermia (↓ metabolic rate)
• Pregnancy (↑ progesterone)
• Opioids, sedatives, alcohol
• Anaemia, hypoxia, hypotension

FACTORS INCREASING MAC (↑ anaesthetic requirement):
• Hyperthermia
• Chronic alcohol use (CNS tolerance)
• Hypernatraemia
• Young children

MNEMONIC: "HOT increases MAC; COLD decreases MAC" — temperature effect is most important.""",
            options = listOf(opt("Alveolar concentration preventing movement in 50% of patients", true), opt("Maximum safe dose of anaesthetic", false), opt("Dose causing unconsciousness in all patients", false), opt("Concentration in blood at equilibrium", false))
        ),
        q(subjectId = 19, topicId = 21,
            text = "Suxamethonium (succinylcholine) is classified as a:",
            explanation = """NEUROMUSCULAR BLOCKING AGENTS (NMBAs):

TWO TYPES:
1. DEPOLARISING NMBAs — e.g., Suxamethonium (succinylcholine) ★
2. NON-DEPOLARISING NMBAs — e.g., vecuronium, rocuronium, atracurium, pancuronium

SUXAMETHONIUM (SUCCINYLCHOLINE):
• Mechanism: Binds nicotinic ACh receptors at NMJ → persistent depolarisation (Phase I block) → fasciculations followed by flaccid paralysis
• Onset: 30–60 seconds (FASTEST of all NMBAs)
• Duration: 3–5 minutes (SHORTEST — hydrolysed by plasma pseudocholinesterase/butyrylcholinesterase)
• Use: Rapid Sequence Induction (RSI) for emergency intubation (full stomach, aspiration risk)

ADVANTAGES: Ultra-short duration (reverses spontaneously), fastest onset

DISADVANTAGES / COMPLICATIONS:
• Hyperkalaemia: K⁺ rises ~0.5 mEq/L normally; DANGEROUS RISE (2–3 mEq/L) in: Burns, crush injury, prolonged immobilisation, denervation, spinal injury, myopathies → use rocuronium instead
• Malignant hyperthermia (rare, triggered by suxamethonium + volatile agents — RYR1 gene mutation)
• Suxamethonium apnoea: Pseudocholinesterase deficiency (genetic) → prolonged paralysis (hours); test: Dibucaine number
• Raised IOP, ICP, intragastric pressure (fasciculations)
• Bradycardia (muscarinic effect — especially repeated doses, in children)

REVERSAL: Cannot be reversed pharmacologically (waits for pseudocholinesterase to hydrolyse it).""",
            options = listOf(opt("Depolarising neuromuscular blocking agent", true), opt("Non-depolarising neuromuscular blocker", false), opt("Volatile inhalational agent", false), opt("Opioid analgesic", false))
        ),
    )

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun q(
        subjectId: Long,
        topicId: Long,
        text: String,
        explanation: String,
        options: List<QuestionOptionEntity>,
        imageUrl: String? = null
    ) = QuestionEntity(
        subjectId   = subjectId,
        topicId     = topicId,
        questionText = text,
        explanation = explanation,
        imageUrl    = imageUrl
    ) to options

    private fun opt(text: String, correct: Boolean) =
        QuestionOptionEntity(questionId = 0, optionText = text, isCorrect = correct, optionIndex = 0)

    private fun Pair<QuestionEntity, List<QuestionOptionEntity>>.withIndexedOptions() =
        first to second.mapIndexed { i, o -> o.copy(optionIndex = i) }
}
