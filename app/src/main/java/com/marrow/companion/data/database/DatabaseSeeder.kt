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
            SubjectEntity(id = 1, name = "Medicine",     colorHex = "#1565C0", totalQuestions = 10),
            SubjectEntity(id = 2, name = "Surgery",      colorHex = "#2E7D32", totalQuestions = 10),
            SubjectEntity(id = 3, name = "OBG",          colorHex = "#6A1B9A", totalQuestions = 6),
            SubjectEntity(id = 4, name = "Pediatrics",   colorHex = "#E65100", totalQuestions = 6),
            SubjectEntity(id = 5, name = "Pharmacology", colorHex = "#00695C", totalQuestions = 6),
            SubjectEntity(id = 6, name = "Pathology",    colorHex = "#4527A0", totalQuestions = 6),
            SubjectEntity(id = 7, name = "Anatomy",      colorHex = "#37474F", totalQuestions = 6),
            SubjectEntity(id = 8, name = "Physiology",   colorHex = "#558B2F", totalQuestions = 6),
        )
        subjectDao.insertAll(subjects)

        val topics = listOf(
            TopicEntity(id = 1,  subjectId = 1, name = "Infectious Diseases",     questionCount = 5),
            TopicEntity(id = 2,  subjectId = 1, name = "Cardiology",              questionCount = 5),
            TopicEntity(id = 3,  subjectId = 2, name = "Abdomen",                 questionCount = 5),
            TopicEntity(id = 4,  subjectId = 2, name = "Trauma & Burns",          questionCount = 5),
            TopicEntity(id = 5,  subjectId = 3, name = "Obstetrics",              questionCount = 6),
            TopicEntity(id = 6,  subjectId = 4, name = "Growth & Development",    questionCount = 6),
            TopicEntity(id = 7,  subjectId = 5, name = "Antimicrobials",          questionCount = 6),
            TopicEntity(id = 8,  subjectId = 6, name = "Neoplasia",               questionCount = 6),
            TopicEntity(id = 9,  subjectId = 7, name = "Upper Limb",              questionCount = 6),
            TopicEntity(id = 10, subjectId = 8, name = "Respiratory Physiology",  questionCount = 6),
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
