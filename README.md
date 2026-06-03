# Marrow Study Companion

An Android app for NEET PG medical exam preparation, modelled after the Marrow QBank experience. Built with Jetpack Compose + Material 3.

---

## Features

### Auth
- Fake login screen with demo credentials
- Hamburger drawer on Home with Logout
- **Demo credentials:** `demo@marrow.com` / `marrow123`

### QBank
- Subjects screen with progress bars and subject icons
- Topic list — Marrow-style numbered timeline with dashed connector line
- Filter tabs: **All | Paused | Completed | Unattempted**
- Completion badge (✅) and progress % per topic

### Topic Detail
- Shows MCQ count + SOLVE / ▶ CONTINUE / REVIEW button based on state
- Completion message: *"You've completed this module on {date}"*
- **Bookmarks →** and **My Notes →** rows with counts

### Quiz Engine
- 60-second countdown timer per question
- Auto-submit on timeout (shows correct answer)
- Green ✅ / Red ❌ colour feedback on options
- Auto-navigates to explanation after 1.5s

### Pause & Resume
- Close mid-quiz → state saved to DB (question order + progress)
- Tap **CONTINUE** to resume from exact question where paused
- **Start Fresh** link to restart from Q1

### Explanation Page
- Rich multi-paragraph medical explanations
- Medical images loaded from Wikipedia (Coil)
- **Text highlighting** — select text → green 🟢 or orange 🟠 colour toolbar appears directly above selection
- **3-type Bookmarks** — Normal (blue) / Starred (red) / Question (yellow) with screenshot animation
- **My Notes** — standalone or attached to a highlighted quote, with ⭐ Imp / ❓ Doubt tags
- **Translation** — ML Kit on-device (Hindi + Tamil, offline after first download)

### Bookmark Screenshot Animation
When a bookmark is saved:
1. White camera-shutter flash
2. Full-screen coloured border frame (blue / red / yellow) with icon badge
3. Frame shrinks and **flies down to the bookmark icon** in the bottom bar

### Review Flow
- **Quiz Review** (after completing module) — score card, Bookmarks tab, Notes & Highlights tab
- **Topic Review** — paginated list of all questions with bookmark icons
- **Topic Bookmarks** — full list of bookmarked questions (paginated 1-10, 11-20…)
- **Topic Notes** — Notes tab + Highlights tab for the entire topic

### Home
- Circular progress ring (questions attempted)
- MCQ of the Day card with answer + explanation link
- Hamburger drawer: user profile, nav links, Logout

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| DI | Hilt |
| Database | Room 4 (version 7) |
| Image loading | Coil |
| Translation | ML Kit on-device (Hindi + Tamil) |
| Navigation | Compose Navigation |
| Architecture | MVVM + StateFlow |

---

## Project Structure

```
app/src/main/java/com/marrow/companion/
├── data/
│   └── database/
│       ├── AppDatabase.kt          # Room DB (version 7)
│       ├── DatabaseSeeder.kt       # Seeds 56 questions across 10 topics
│       ├── dao/                    # QuestionDao, NoteDao, HighlightDao,
│       │                           # PausedQuizDao, QuestionAttemptDao, …
│       └── entities/               # QuestionEntity, NoteEntity,
│                                   # HighlightEntity, PausedQuizEntity, …
├── di/
│   └── DatabaseModule.kt           # Hilt providers for all DAOs
├── ui/
│   ├── navigation/
│   │   ├── NavRoutes.kt            # All route definitions
│   │   └── AppNavGraph.kt          # Full nav graph wiring
│   └── screens/
│       ├── auth/LoginScreen.kt
│       ├── dashboard/              # Home + DashboardViewModel
│       ├── subjects/               # Subjects, TopicList, TopicDetail,
│       │                           # TopicReview, TopicBookmarks, TopicNotes
│       ├── quiz/                   # QuizScreen, QuizViewModel,
│       │                           # HighlightableText, BookmarkEffect,
│       │                           # MyNotesSheet, SelectionTranslateSheet,
│       │                           # QuizReviewScreen, SubjectReferenceSheet
│       ├── explanation/            # Standalone MCQ explanation
│       └── profile/ analytics/     # Videos + Tests (headers only)
└── MarrowApp.kt                    # Pre-downloads ML Kit models
```

---

## Database Schema

| Table | Purpose |
|---|---|
| `questions` | Question text, explanation, imageUrl, bookmarkType |
| `question_options` | A/B/C/D options with isCorrect flag |
| `question_attempts` | Per-user attempt history |
| `highlights` | Text highlights (GREEN / ORANGE) per question |
| `notes` | Personal notes with tag (NONE / IMP / DOUBT) + optional attached quote |
| `paused_quizzes` | Saved quiz state for pause/resume |
| `subjects` | Subject list with colour |
| `topics` | Topics under each subject with question count |
| `users` | Streak, total attempts, correct count |
| `study_sessions` | Session start/end, questions attempted |

---

## Seeded Content (56 questions across 10 topics)

| Subject | Topic | Questions |
|---|---|---|
| Medicine | Infectious Diseases | 5 |
| Medicine | Cardiology | 5 |
| Surgery | Abdomen | 5 |
| Surgery | Trauma & Burns | 5 |
| OBG | Obstetrics | 6 |
| Pediatrics | Growth & Development | 6 |
| Pharmacology | Antimicrobials | 6 |
| Pathology | Neoplasia | 6 |
| Anatomy | Upper Limb | 6 |
| Physiology | Respiratory Physiology | 6 |

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK API 26+

### Build & Run
```bash
# Clone / open in Android Studio
# Sync Gradle → Run on device or emulator

# Or build APK from terminal:
./gradlew assembleDebug

# APK location:
app/build/outputs/apk/debug/app-debug.apk
```

### First Launch
1. App installs and seeds the database automatically
2. Login with `demo@marrow.com` / `marrow123`
3. ML Kit downloads Hindi + Tamil models on first use (~30 MB, one-time)

---

## Key Implementation Notes

### Text Highlighting
Uses a custom `TextToolbar` via `CompositionLocalProvider(LocalTextToolbar)` to intercept text selection. The toolbar popup is positioned using `onGloballyPositioned` to track the exact window position of the parent Box, ensuring the toolbar always appears directly above the selected text regardless of scroll position.

### Pause & Resume
Quiz state (question IDs in order, current index, correct count, answered map) is saved to `paused_quizzes` table after every answered question. On resume, questions are loaded in the original order using `getQuestionsByIds`. The saved `currentIndex` always points to the first **unanswered** question.

### Bookmark Screenshot Animation
Uses `graphicsLayer` with animated `scaleX/Y` and `translationX/Y` to shrink the full-screen frame and fly it to the bookmark icon position (calculated from screen dimensions and bottom bar layout weights).

---

## Demo Credentials

```
Email:    demo@marrow.com
Password: marrow123
```
