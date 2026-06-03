package com.marrow.companion.data.repository

import com.marrow.companion.data.database.dao.*
import com.marrow.companion.data.database.entities.*
import javax.inject.Inject
import javax.inject.Singleton

data class SeedQuestion(
    val text: String,
    val explanation: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val options: List<SeedOption>
)

data class SeedOption(val text: String, val isCorrect: Boolean, val index: Int)

data class SeedTopic(
    val name: String,
    val questions: List<SeedQuestion>,
    val flashcards: List<Pair<String, String>>
)

data class SeedSubject(
    val name: String,
    val colorHex: String,
    val topics: List<SeedTopic>
)

@Singleton
class SeedRepository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val topicDao: TopicDao,
    private val questionDao: QuestionDao,
    private val flashcardDao: FlashcardDao
) {
    suspend fun seedIfEmpty() {
        if (questionDao.getCount() > 0) return
        seedData.forEach { seedSubject ->
            val subjectId = subjectDao.insertAll(
                listOf(SubjectEntity(name = seedSubject.name, colorHex = seedSubject.colorHex,
                    totalQuestions = seedSubject.topics.sumOf { it.questions.size }))
            ).firstOrNull()?.takeIf { it != -1L }
                ?: subjectDao.getAll().find { it.name == seedSubject.name }?.id
                ?: return@forEach

            seedSubject.topics.forEach { seedTopic ->
                val topicId = topicDao.insert(
                    TopicEntity(subjectId = subjectId, name = seedTopic.name,
                        questionCount = seedTopic.questions.size)
                ).takeIf { it != -1L }
                    ?: return@forEach

                seedTopic.questions.forEach { sq ->
                    questionDao.insertQuestionWithOptions(
                        QuestionEntity(subjectId = subjectId, topicId = topicId,
                            questionText = sq.text, explanation = sq.explanation,
                            difficulty = sq.difficulty),
                        sq.options.map { o ->
                            QuestionOptionEntity(questionId = 0, optionText = o.text,
                                isCorrect = o.isCorrect, optionIndex = o.index)
                        }
                    )
                }

                flashcardDao.insertAll(seedTopic.flashcards.map { (front, back) ->
                    FlashcardEntity(topicId = topicId, front = front, back = back)
                })
            }
        }
    }
}

private fun q(text: String, explanation: String, a: String, b: String, c: String, d: String,
              correct: Int, difficulty: Difficulty = Difficulty.MEDIUM) =
    SeedQuestion(text, explanation, difficulty, listOf(
        SeedOption(a, correct == 0, 0), SeedOption(b, correct == 1, 1),
        SeedOption(c, correct == 2, 2), SeedOption(d, correct == 3, 3)
    ))

private fun fc(front: String, back: String) = front to back

private val seedData = listOf(
    SeedSubject("Medicine", "#1565C0", listOf(
        SeedTopic("Cardiology", listOf(
            q("Most common cause of mitral stenosis in India?",
                "Rheumatic fever causes commissural fusion and leaflet thickening leading to characteristic 'fish mouth' mitral orifice.",
                "Rheumatic fever", "Congenital", "SLE", "Infective endocarditis", 0),
            q("Classic ECHO finding in mitral stenosis?",
                "Hockey stick (bent stick) appearance on M-mode echo is pathognomonic of rheumatic mitral stenosis due to restricted leaflet motion.",
                "Hockey stick appearance", "Flail leaflet", "Mitral prolapse", "Vegetation on leaflet", 0),
            q("Normal mitral valve area?",
                "Normal MVA is 4–6 cm². Mild MS: 1.5–2 cm², Moderate: 1–1.5 cm², Severe/Critical: <1 cm².",
                "4–6 cm²", "1–2 cm²", "7–8 cm²", "2–4 cm²", 0, Difficulty.EASY),
            q("Waterfall appearance on chest X-ray is characteristic of?",
                "Left atrial enlargement in mitral stenosis produces double shadow at right heart border and splaying of carina (waterfall sign).",
                "Mitral stenosis", "Aortic stenosis", "VSD with Eisenmenger", "Tricuspid stenosis", 0),
            q("Opening snap in mitral stenosis is heard best at?",
                "Opening snap (OS) is heard best at the lower left sternal border/apex. Short A2-OS interval indicates severe stenosis.",
                "Lower left sternal border", "Apex only", "Aortic area", "Pulmonary area", 0, Difficulty.HARD)
        ), listOf(
            fc("Mitral Stenosis: causes & classic sign", "Rheumatic fever (most common) → fish mouth orifice → hockey stick echo → waterfall CXR"),
            fc("MVA severity grading", "Normal: 4–6 cm² | Mild MS: 1.5–2 | Moderate: 1–1.5 | Severe: <1 cm²"),
            fc("A2-OS interval in MS", "Short A2-OS interval → severe MS (high LAP snaps valve open quickly)")
        )),
        SeedTopic("Respiratory", listOf(
            q("Gold standard investigation for pulmonary embolism?",
                "CT pulmonary angiography (CTPA) is the gold standard for PE diagnosis. D-dimer is used as a screening test (high sensitivity, low specificity).",
                "CT pulmonary angiography", "V/Q scan", "D-dimer assay", "Chest X-ray", 0),
            q("Diurnal PEF variability diagnostic of asthma is?",
                "Peak expiratory flow variability >20% on ≥3 days per week for 2 weeks is diagnostic of asthma (reversible airway obstruction).",
                ">20%", ">10%", ">30%", ">15%", 0),
            q("Type of hypoxia caused by pulmonary embolism?",
                "PE causes V/Q mismatch (dead space effect) → hypoxic hypoxia. Also causes right heart strain and paradoxical embolism.",
                "Hypoxic hypoxia", "Stagnant hypoxia", "Histotoxic hypoxia", "Anemic hypoxia", 0),
            q("Pancoast tumor classically involves which nerve root?",
                "Pancoast (superior sulcus) tumor involves T1 root causing Horner syndrome + intrinsic hand muscle wasting + shoulder/arm pain.",
                "T1", "C8", "T2", "C7", 0, Difficulty.HARD),
            q("Most common causative organism in community-acquired pneumonia?",
                "Streptococcus pneumoniae is the most common cause of CAP in all age groups. Presents with lobar consolidation and rusty sputum.",
                "Streptococcus pneumoniae", "Haemophilus influenzae", "Mycoplasma pneumoniae", "Klebsiella pneumoniae", 0, Difficulty.EASY)
        ), listOf(
            fc("Investigation of choice for PE", "CTPA (gold standard) | V/Q scan if contrast contraindicated | D-dimer for exclusion only"),
            fc("Asthma diagnosis criterion", "PEF variability >20% on ≥3 days/week × 2 weeks OR >12% FEV1 reversibility post-bronchodilator"),
            fc("Pancoast tumor features", "T1 root: hand wasting + Horner (ptosis/miosis/anhidrosis) + shoulder pain + Pancoast syndrome")
        )),
        SeedTopic("Nephrology", listOf(
            q("Most common cause of nephrotic syndrome in adults?",
                "Membranous nephropathy (idiopathic/secondary to malignancy/HBV/drugs) is the most common cause of nephrotic syndrome in adults >60 years.",
                "Membranous nephropathy", "Minimal change disease", "FSGS", "IgA nephropathy", 0),
            q("Most common cause of nephrotic syndrome in children?",
                "Minimal change disease (nil lesion) accounts for 80% of pediatric nephrotic syndrome. Steroid-sensitive. Fusion of foot processes on EM.",
                "Minimal change disease", "FSGS", "Membranous nephropathy", "Diabetic nephropathy", 0, Difficulty.EASY),
            q("Kimmelstiel-Wilson nodules are pathognomonic of?",
                "Kimmelstiel-Wilson nodules (nodular glomerulosclerosis) are pathognomonic of diabetic nephropathy. PAS-positive deposits in glomeruli.",
                "Diabetic nephropathy", "Membranous nephropathy", "Amyloidosis", "FSGS", 0),
            q("Classic nephritic syndrome triad includes?",
                "Nephritic syndrome: hematuria (RBC casts in urine), hypertension, and oliguria. Prototype: post-streptococcal GN.",
                "Hematuria, hypertension, oliguria", "Heavy proteinuria, edema, hypoalbuminemia",
                "Hematuria, proteinuria, hypercalciuria", "Polyuria, polydipsia, glycosuria", 0),
            q("Type of RTA associated with osteomalacia and rickets?",
                "Type 2 (proximal) RTA causes bicarbonate wasting (reabsorption threshold ↓) → metabolic acidosis + osteomalacia due to phosphate loss.",
                "Type 2 (proximal) RTA", "Type 1 (distal) RTA", "Type 4 RTA", "Type 3 RTA", 0, Difficulty.HARD)
        ), listOf(
            fc("Nephrotic vs Nephritic causes (common)", "Nephrotic - Adults: Membranous | Children: MCD | Nephritic: Post-strep GN, IgA, RPGN"),
            fc("Kimmelstiel-Wilson nodules", "Diabetic nephropathy | Nodular mesangial deposits | PAS positive | Also: GBM thickening"),
            fc("Nephritic vs Nephrotic syndrome", "Nephritic: hematuria + HT + oliguria | Nephrotic: proteinuria>3.5g + edema + hypoalbuminemia")
        ))
    )),
    SeedSubject("Pharmacology", "#00695C", listOf(
        SeedTopic("Autonomic Drugs", listOf(
            q("Mechanism of action of atropine?",
                "Atropine is a competitive (reversible) antagonist of muscarinic receptors causing parasympatholytic effects: tachycardia, mydriasis, dry mouth, urinary retention.",
                "Competitive muscarinic receptor antagonist", "Ganglionic nicotinic blocker",
                "Alpha-1 adrenergic blocker", "Beta-2 agonist", 0),
            q("Drug of choice for muscarinic symptoms in organophosphate poisoning?",
                "Atropine blocks muscarinic effects (SLUDGE + bradycardia). Pralidoxime regenerates AChE if given early (<48h). High-dose atropine until secretions dry.",
                "Atropine", "Pralidoxime", "Physostigmine", "Neostigmine", 0, Difficulty.EASY),
            q("Pilocarpine is used in?",
                "Pilocarpine (muscarinic M3 agonist) increases aqueous humor outflow through trabecular meshwork → used in open-angle glaucoma.",
                "Open-angle glaucoma", "Acute angle-closure glaucoma", "Mydriasis induction", "Cycloplegia", 0),
            q("Propranolol is contraindicated in?",
                "Propranolol (non-selective beta blocker) blocks bronchial β2 receptors → bronchoconstriction → contraindicated in asthma and COPD.",
                "Bronchial asthma", "Hypertension", "Angina pectoris", "Thyrotoxicosis", 0),
            q("Phenoxybenzamine is classified as?",
                "Phenoxybenzamine is a non-competitive (irreversible) alpha blocker. Used pre-operatively in pheochromocytoma for 2 weeks before surgery.",
                "Irreversible non-competitive alpha blocker", "Reversible competitive alpha-1 blocker",
                "Selective alpha-2 agonist", "Non-selective beta blocker", 0, Difficulty.HARD)
        ), listOf(
            fc("Atropine effects mnemonic", "Can't See (mydriasis), Can't Spit (dry mouth), Can't Pee (retention), Can't Shit (↓gut motility), Red as beet (flushing), Hot as hare (↑temp)"),
            fc("OPC poisoning - SLUDGE+B", "Salivation, Lacrimation, Urination, Defecation, GI cramps, Emesis + Bradycardia → Rx: Atropine + Pralidoxime"),
            fc("Alpha blockers: reversible vs irreversible", "Irreversible: Phenoxybenzamine | Reversible: Phentolamine, Prazosin (α1-selective)")
        )),
        SeedTopic("Antibiotics", listOf(
            q("Mechanism of action of penicillin?",
                "Penicillins bind penicillin-binding proteins (PBPs/transpeptidases) → inhibit cross-linking of peptidoglycan → weakened cell wall → bacterial lysis.",
                "Inhibit cell wall peptidoglycan synthesis", "Inhibit 30S ribosome",
                "Inhibit DNA gyrase", "Inhibit folate synthesis", 0, Difficulty.EASY),
            q("Drug causing gray baby syndrome?",
                "Chloramphenicol accumulates in neonates due to immature UDP-glucuronyl transferase and renal excretion → cardiovascular collapse, gray discoloration.",
                "Chloramphenicol", "Tetracycline", "Streptomycin", "Erythromycin", 0),
            q("Aminoglycosides act by binding to?",
                "Aminoglycosides irreversibly bind 30S ribosomal subunit (16S rRNA) → misreading of mRNA → premature chain termination. Bactericidal.",
                "30S ribosomal subunit", "50S ribosomal subunit",
                "DNA gyrase", "Beta-lactam receptors", 0),
            q("Drug of choice for MRSA infections?",
                "Vancomycin (glycopeptide) is DOC for MRSA. It inhibits cell wall synthesis by binding D-Ala-D-Ala terminal of peptidoglycan precursors.",
                "Vancomycin", "Methicillin", "Amoxicillin-clavulanate", "Ceftriaxone", 0),
            q("Fluoroquinolones mechanism of action?",
                "Fluoroquinolones inhibit bacterial DNA gyrase (topoisomerase II) in gram-negatives and topoisomerase IV in gram-positives → prevent DNA replication.",
                "Inhibit DNA gyrase and topoisomerase IV", "Inhibit cell wall synthesis",
                "Bind 30S ribosome", "Inhibit dihydrofolate reductase", 0)
        ), listOf(
            fc("Antibiotics: ribosome targets", "30S: Aminoglycosides + Tetracyclines | 50S: Macrolides + Chloramphenicol + Clindamycin | Mnemonic: '30S MATE, 50S CCLE'"),
            fc("Gray baby syndrome", "Chloramphenicol in neonates → immature glucuronyl transferase → drug accumulation → cardiovascular collapse, abdominal distension"),
            fc("DOC for MRSA vs VRSA", "MRSA: Vancomycin | VRSA: Linezolid or Daptomycin | MSSA: Cloxacillin/Flucloxacillin")
        )),
        SeedTopic("CNS Drugs", listOf(
            q("Drug of choice for absence (petit mal) seizures?",
                "Ethosuximide blocks T-type Ca²⁺ channels in thalamic neurons → disrupts 3 Hz spike-and-wave discharges of absence seizures.",
                "Ethosuximide", "Phenytoin", "Carbamazepine", "Phenobarbitone", 0),
            q("Lithium is drug of choice for?",
                "Lithium is the gold standard mood stabilizer for bipolar disorder (both acute mania and prophylaxis). Narrow therapeutic index (0.6–1.2 mEq/L).",
                "Bipolar disorder (mania + prophylaxis)", "Schizophrenia", "Generalized anxiety", "Major depression only", 0),
            q("Benzodiazepines potentiate GABA-A by?",
                "BZDs allosterically bind γ-subunit of GABA-A → increase FREQUENCY of Cl⁻ channel opening (barbiturates increase DURATION).",
                "Increasing frequency of Cl⁻ channel opening", "Increasing duration of Cl⁻ channel opening",
                "Directly activating the channel", "Inhibiting GABA reuptake", 0, Difficulty.HARD),
            q("Most serious unique adverse effect of clozapine?",
                "Clozapine causes agranulocytosis in 1–2% → mandatory weekly WBC monitoring for 18 weeks, then monthly. Minimal EPS due to loose D2 binding.",
                "Agranulocytosis", "Extrapyramidal symptoms", "Tardive dyskinesia", "Hyperprolactinemia", 0),
            q("DOC for status epilepticus (first-line)?",
                "IV lorazepam (or diazepam) is first-line for status epilepticus. If seizures persist → phenytoin/fosphenytoin → then phenobarbitone/propofol.",
                "IV Lorazepam/Diazepam", "Phenytoin", "Carbamazepine", "Sodium valproate oral", 0, Difficulty.HARD)
        ), listOf(
            fc("Seizure type → DOC", "Absence: Ethosuximide | GTCS: Valproate | Focal: Carbamazepine | Status epilepticus: IV Lorazepam → Phenytoin"),
            fc("BZD vs Barbiturate on GABA-A", "BZD → ↑FREQUENCY of Cl⁻ opening | Barbiturates → ↑DURATION of opening | Both shift GABA dose-response curve left"),
            fc("Clozapine monitoring protocol", "CBC weekly × 18 weeks, then monthly. Stop if WBC <3000/mm³ or neutrophils <1500/mm³")
        ))
    )),
    SeedSubject("Anatomy", "#37474F", listOf(
        SeedTopic("Upper Limb", listOf(
            q("Nerve most commonly injured in supracondylar fracture of humerus?",
                "Anterior interosseous nerve (branch of median nerve) is most commonly injured. Supplies FPL, lateral FDP (index/middle), pronator quadratus.",
                "Anterior interosseous nerve", "Ulnar nerve", "Radial nerve", "Musculocutaneous nerve", 0, Difficulty.HARD),
            q("Which nerve passes through the carpal tunnel?",
                "Carpal tunnel contains median nerve + 9 flexor tendons (4 FDS, 4 FDP, 1 FPL). Ulnar nerve travels through Guyon's canal (outside carpal tunnel).",
                "Median nerve", "Ulnar nerve", "Radial nerve", "Anterior interosseous nerve", 0, Difficulty.EASY),
            q("Radial nerve injury at spiral groove of humerus causes?",
                "Injury at spiral groove (Saturday night palsy / mid-shaft humeral fracture) causes wrist drop. Triceps spared (its branch comes off proximal).",
                "Wrist drop with intact triceps", "Complete wrist and elbow drop",
                "Claw hand", "Ape thumb deformity", 0),
            q("Floor of the anatomical snuffbox is formed by?",
                "Floor: scaphoid + trapezium bones. Roof: tendons of EPB and APL. Radial artery passes through it. Tenderness → scaphoid fracture.",
                "Scaphoid and trapezium", "Trapezoid and capitate",
                "Lunate and triquetrum", "Radius and ulna", 0, Difficulty.HARD),
            q("Axillary nerve injury causes loss of?",
                "Axillary nerve (C5, C6) from posterior cord. Injured in shoulder dislocation. Supplies deltoid + teres minor + 'regimental badge' skin sensation.",
                "Shoulder abduction and regimental badge sensation", "Elbow flexion",
                "Wrist extension", "Finger abduction", 0)
        ), listOf(
            fc("Carpal tunnel contents", "9 flexor tendons (4 FDS + 4 FDP + 1 FPL) + Median nerve | Ulnar nerve is in Guyon's canal"),
            fc("Wrist drop nerve", "Radial nerve at spiral groove → wrist drop, finger drop, sensory loss dorsal web space | Triceps intact"),
            fc("Axillary nerve injury", "Shoulder dislocation/neck of humerus fracture → deltoid wasting + regimental badge area sensory loss")
        )),
        SeedTopic("Lower Limb", listOf(
            q("Common peroneal nerve injury at neck of fibula causes?",
                "Common peroneal nerve (most commonly injured nerve in lower limb) → foot drop: loss of dorsiflexion + eversion + sensation over dorsum of foot.",
                "Foot drop", "Loss of plantarflexion", "Loss of knee extension", "Loss of hip abduction", 0, Difficulty.EASY),
            q("Unhappy triad of O'Donoghue involves?",
                "Unhappy triad: ACL + MCL + medial meniscus (medial meniscus attached to MCL, so MCL injury often tears medial meniscus together).",
                "ACL + MCL + medial meniscus", "PCL + LCL + lateral meniscus",
                "ACL + LCL + lateral meniscus", "PCL + MCL + lateral meniscus", 0),
            q("Posterior cruciate ligament (PCL) prevents?",
                "PCL (stronger, intracapsular) prevents posterior displacement of tibia on femur. PCL tear → positive posterior drawer test.",
                "Posterior displacement of tibia on femur", "Anterior displacement of tibia",
                "Medial rotation of knee", "Hyperextension of knee", 0),
            q("Femoral nerve injury causes?",
                "Femoral nerve (L2-L4) supplies quadriceps. Injury causes loss of knee extension, absent knee jerk, positive FNST, medial leg sensory loss.",
                "Loss of knee extension", "Loss of hip extension", "Loss of ankle dorsiflexion", "Foot drop", 0),
            q("Most common site of sciatic nerve compression in disc prolapse?",
                "L4-L5 (compresses L5 root) and L5-S1 (compresses S1 root) are most common disc levels for sciatica. L4-L5 is the single most common level.",
                "L4-L5 and L5-S1", "L2-L3 and L3-L4", "L3-L4 only", "L5-S1 only", 0, Difficulty.HARD)
        ), listOf(
            fc("Unhappy triad of knee", "ACL + MCL + Medial meniscus | Mechanism: valgus stress + external rotation | Rx: ACL reconstruction"),
            fc("Common peroneal nerve", "Neck of fibula → foot drop (dorsiflexion + eversion lost) | Sensory: dorsum of foot | Most common nerve injury in LL"),
            fc("PCL vs ACL tests", "PCL: posterior drawer positive | ACL: anterior drawer + Lachman test positive | Both: pivot shift test")
        )),
        SeedTopic("Head and Neck", listOf(
            q("Horner's syndrome triad is?",
                "Horner's syndrome: interruption of sympathetic pathway. Ptosis (superior tarsal muscle), miosis (dilator pupillae lost), anhidrosis (ipsilateral face).",
                "Ptosis + miosis + anhidrosis", "Ptosis + mydriasis + anhidrosis",
                "Ptosis + miosis + hyperhidrosis", "Exophthalmos + miosis + anhidrosis", 0),
            q("Taste from anterior 2/3 of tongue is carried by?",
                "Chorda tympani (branch of facial nerve, CN VII) carries taste from anterior 2/3. Lingual nerve (V3) carries general sensation. Posterior 1/3: IX.",
                "Chorda tympani (VII)", "Glossopharyngeal (IX)",
                "Lingual nerve (V3)", "Vagus nerve (X)", 0),
            q("Most common site of epistaxis?",
                "Little's area (Kiesselbach's plexus) on the anteroinferior nasal septum has rich anastomosis of anterior ethmoidal, sphenopalatine, greater palatine, and superior labial arteries.",
                "Kiesselbach's plexus (Little's area)", "Posterior nasal cavity",
                "Superior turbinate", "Cribriform plate", 0, Difficulty.EASY),
            q("CSF rhinorrhea after trauma suggests fracture of?",
                "CSF rhinorrhea indicates cribriform plate fracture (anterior cranial fossa). Olfactory nerves pass through it. Ring test / β2-transferrin confirms CSF.",
                "Cribriform plate of ethmoid", "Sphenoid body",
                "Frontal sinus", "Temporal bone", 0),
            q("Nerve at risk during parotid surgery?",
                "Facial nerve (CN VII) divides within the parotid gland into temporofacial and cervicofacial divisions. Damage causes lower motor neuron facial palsy.",
                "Facial nerve (CN VII)", "Auriculotemporal nerve",
                "Great auricular nerve", "Hypoglossal nerve", 0, Difficulty.EASY)
        ), listOf(
            fc("Horner's syndrome triad", "Ptosis (superior tarsal) + Miosis (dilator lost) + Anhidrosis (sympathetic lost) | Causes: Pancoast, carotid dissection, brainstem lesion"),
            fc("Taste pathways", "Anterior 2/3: Chorda tympani (VII) + Lingual nerve (V3) general sensation | Posterior 1/3: IX | Epiglottis: X"),
            fc("Kiesselbach's plexus vessels", "Anterior ethmoidal + Sphenopalatine + Greater palatine + Superior labial arteries anastomose on Little's area")
        ))
    )),
    SeedSubject("Physiology", "#558B2F", listOf(
        SeedTopic("Cardiovascular", listOf(
            q("Normal cardiac output at rest?",
                "CO = HR × SV = 70 bpm × 70 mL = ~5 L/min. Cardiac index = CO/BSA = 3.2 L/min/m². Increases up to 25 L/min during maximal exercise.",
                "5 L/min", "3 L/min", "7 L/min", "10 L/min", 0, Difficulty.EASY),
            q("Frank-Starling law of the heart states?",
                "Increased preload (venous return) → increased EDV → increased myocyte fiber length → increased force of contraction (↑Ca²⁺ sensitivity) → increased SV.",
                "Increased venous return increases stroke volume",
                "CO is inversely related to preload",
                "CO is independent of afterload",
                "Sympathetic stimulation decreases CO", 0),
            q("P wave on ECG represents?",
                "P wave = atrial depolarization | QRS = ventricular depolarization | T wave = ventricular repolarization | Atrial repolarization hidden within QRS complex.",
                "Atrial depolarization", "Ventricular depolarization",
                "Atrial repolarization", "Ventricular repolarization", 0, Difficulty.EASY),
            q("Dicrotic notch on arterial pulse tracing is caused by?",
                "Dicrotic notch = closure of aortic valve at end of systole (produces incisura). Followed by dicrotic wave (elastic recoil of aortic wall).",
                "Closure of aortic valve", "Closure of mitral valve",
                "Opening of aortic valve", "Peak ventricular pressure", 0),
            q("Normal pulmonary artery pressure?",
                "Normal PAP = 25/10 mmHg (systolic/diastolic), mean ~15 mmHg. Pulmonary hypertension defined as mean PAP >25 mmHg at rest.",
                "25/10 mmHg", "120/80 mmHg", "40/20 mmHg", "10/5 mmHg", 0, Difficulty.HARD)
        ), listOf(
            fc("Frank-Starling law", "↑Preload → ↑EDV → ↑fiber stretch → ↑force (length-tension) → ↑SV | Fails in dilated cardiomyopathy"),
            fc("ECG wave meanings", "P = atrial depol | PR interval = AV conduction | QRS = ventricular depol | ST = plateau | T = ventricular repol"),
            fc("Normal pressures", "Aorta: 120/80 (mean 95) | PA: 25/10 (mean 15) | RA: 2–8 | PCWP: 6–12 mmHg")
        )),
        SeedTopic("Respiratory", listOf(
            q("Normal tidal volume in adults?",
                "TV = 500 mL. Anatomical dead space = 150 mL. Alveolar ventilation = (TV - dead space) × RR = 350 × 14 = 4.9 L/min.",
                "500 mL", "3500 mL", "1500 mL", "150 mL", 0, Difficulty.EASY),
            q("Hering-Breuer reflex is triggered by?",
                "Pulmonary stretch receptors (slowly adapting, in airway smooth muscle) → afferents via vagus → inhibit inspiration when lungs are sufficiently inflated.",
                "Stretch receptors in airway smooth muscle", "Peripheral chemoreceptors",
                "Central chemoreceptors in medulla", "J-receptors (juxtacapillary)", 0),
            q("Right shift of oxygen-hemoglobin dissociation curve is caused by?",
                "Right shift ↓O2 affinity (↑P50) → ↑O2 unloading to tissues. Caused by ↑CO2, ↑H+ (Bohr effect), ↑temperature, ↑2,3-DPG. Altitude adaptation.",
                "Increased CO2, temperature, 2,3-DPG, H+", "Decreased CO2 and alkalosis",
                "Decreased temperature", "Increased pH (alkalosis)", 0),
            q("Primary stimulus for central chemoreceptors?",
                "Central chemoreceptors (ventral surface of medulla) respond to H+ in CSF. CO2 freely crosses BBB → carbonic acid → H+ (not O2 or plasma H+).",
                "H+ concentration in CSF", "PaO2 in blood",
                "PaCO2 directly in blood", "Plasma bicarbonate level", 0, Difficulty.HARD),
            q("Functional residual capacity (FRC) equals?",
                "FRC = ERV + RV = 1200 + 1200 = 2400 mL. It is the volume of air remaining in lungs after normal (tidal) expiration.",
                "ERV + RV (2400 mL)", "TV + IRV (3500 mL)",
                "TLC - TV (5500 mL)", "IC + ERV (4800 mL)", 0, Difficulty.HARD)
        ), listOf(
            fc("Bohr effect", "↑CO2/H+/Temp/2,3-DPG → right shift ODC → ↓O2 affinity → ↑O2 delivery to active tissues"),
            fc("Lung volumes (adults)", "TV=500 | IRV=3000 | ERV=1200 | RV=1200 | IC=3500 | FRC=2400 | VC=4700 | TLC=6000 mL"),
            fc("Hering-Breuer vs Deflation reflex", "Inflation reflex: stretch receptors → ↑vagus → stops inspiration | Deflation reflex: prevents excess expiration (opposite)")
        )),
        SeedTopic("Renal", listOf(
            q("Gold standard substance for measuring GFR?",
                "Inulin (plant polysaccharide) is freely filtered at glomerulus, not reabsorbed or secreted by tubules → inulin clearance = GFR (~125 mL/min).",
                "Inulin", "Creatinine", "Para-aminohippurate (PAH)", "Glucose", 0),
            q("Normal GFR in adults?",
                "Normal GFR ~125 mL/min (180 L/day). ~178.5 L/day is filtered; 1.5 L/day excreted as urine. CKD = GFR <60 mL/min for >3 months.",
                "125 mL/min", "60 mL/min", "250 mL/min", "500 mL/min", 0, Difficulty.EASY),
            q("Percentage of filtered sodium reabsorbed in proximal convoluted tubule?",
                "PCT reabsorbs 65–70% of filtered Na+ (iso-osmotic). Loop of Henle: ~25% (ascending limb). Distal nephron: ~5–10%, regulated by aldosterone.",
                "65–70%", "25–30%", "5–10%", "1–2%", 0),
            q("ADH acts on which part of the nephron?",
                "ADH (vasopressin) binds V2 receptors on collecting duct principal cells → activates adenylyl cyclase → PKA → inserts AQP-2 → water reabsorption.",
                "Collecting duct (principal cells)", "Proximal convoluted tubule",
                "Ascending loop of Henle", "Bowman's capsule", 0),
            q("Juxtaglomerular cells secrete?",
                "JG cells (modified smooth muscle of afferent arteriole) secrete renin in response to ↓renal perfusion pressure and ↓NaCl at macula densa.",
                "Renin", "Aldosterone", "ADH (vasopressin)", "Atrial natriuretic peptide", 0, Difficulty.EASY)
        ), listOf(
            fc("GFR measurement", "Gold standard: Inulin clearance | Clinical: Creatinine clearance | RPF: PAH clearance | Filtration fraction = GFR/RPF = 20%"),
            fc("ADH mechanism", "V2 receptor (collecting duct) → cAMP → PKA → AQP-2 insertion → water reabsorption | Deficiency → Diabetes insipidus"),
            fc("RAAS cascade", "↓BP/Na → JG cells release Renin → Angiotensinogen → Ang I → ACE (lung) → Ang II → Aldosterone (adrenal zona glomerulosa)")
        ))
    ))
)
