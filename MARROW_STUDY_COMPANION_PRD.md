# Marrow Study Companion — Product Requirements Document

## 1. Overview

**Product Name:** Marrow Study Companion  
**Platform:** Android (minSdk 26 / Android 8.0+)  
**Package:** `com.marrow.companion`  
**Version:** 1.0.0 (Hackathon MVP)

Marrow Study Companion is an offline-first Android app that helps MBBS graduates prepare for NEET PG. It combines an adaptive MCQ quiz engine, spaced-repetition flashcards, an AI-powered doubt resolver, and deep performance analytics — all backed by a local Room database so the app works without a network connection.

---

## 2. Goals

| # | Goal |
|---|------|
| G1 | Let students practice topic-wise MCQs with immediate explanations |
| G2 | Reinforce weak concepts using SM-2 spaced-repetition flashcards |
| G3 | Surface actionable performance data (accuracy, speed, weak subjects) |
| G4 | Provide an AI doubt-resolution chat (Claude API) for any question |
| G5 | Maintain a daily study streak to drive habit formation |

---

## 3. Target Users

- MBBS graduates (India) preparing for NEET PG / NEXT
- Students who use the Marrow web/app platform and want an offline companion
- Interns doing rapid revision 4–8 weeks before exam

---

## 4. Core Features (MVP)

### 4.1 Dashboard
- Today's study goal card (questions target, time target)
- Current streak badge
- Quick-start buttons → Subject Quiz, Flashcards, Random 10
- Recent performance sparkline (last 7 days accuracy)

### 4.2 Question Bank / Quiz Engine
- Browse subjects → topics → questions (hierarchical)
- Timed quiz mode (configurable 30 s / 60 s / 90 s per question)
- Four-option MCQ; tap to select, swipe to skip
- Immediate post-answer feedback: correct option highlighted, one-tap explanation sheet
- Bookmark questions for later review
- Difficulty levels: Easy / Medium / Hard (stored on `QuestionEntity`)

### 4.3 Flashcard Deck (Spaced Repetition)
- Front (term/image) → Back (answer/explanation) flip animation
- SM-2 algorithm: stores `easeFactor`, `interval`, `repetitions`, `nextReviewDate`
- "Due today" queue surfaced on Dashboard
- Create custom cards from any question explanation

### 4.4 Performance Analytics
- Per-subject accuracy doughnut chart
- Time-per-question histogram
- Weak-topic list (accuracy < 60 %) with one-tap drill-down quiz
- Study-session history (duration, score) in scrollable timeline

### 4.5 AI Doubt Resolver
- Floating FAB opens a bottom-sheet chat
- User types a doubt; app sends it to Claude API (`claude-sonnet-4-6`) with system prompt for medical context
- Conversation history persisted in-session (not persisted to DB in MVP)

### 4.6 Profile & Settings
- Display name, exam date countdown
- Notification preferences (daily reminder time)
- Clear data / reset streak

---

## 5. Data Model

### Entities

| Entity | Key Fields |
|--------|-----------|
| `UserEntity` | id, name, examDate, streakCount, totalAttempts, correctAttempts |
| `SubjectEntity` | id, name, description, colorHex, iconName |
| `TopicEntity` | id, subjectId, name, questionCount |
| `QuestionEntity` | id, topicId, subjectId, questionText, explanation, difficulty, isBookmarked |
| `QuestionOptionEntity` | id, questionId, optionText, isCorrect, optionIndex |
| `QuestionAttemptEntity` | id, userId, questionId, selectedOptionId, isCorrect, timeTakenMs, attemptedAt |
| `FlashcardEntity` | id, topicId, front, back, isCustom |
| `FlashcardReviewEntity` | id, userId, flashcardId, easeFactor, interval, repetitions, nextReviewDate |
| `StudySessionEntity` | id, userId, subjectId, startTime, endTime, questionsAttempted, correctAnswers, sessionType |

---

## 6. Navigation Structure

```
BottomNav
├── Dashboard (home)
├── Subjects → TopicList → Quiz → ResultSummary
├── Flashcards → FlashcardSession
├── Analytics
└── Profile
```

---

## 7. Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Local DB | Room 2.6 |
| DI | Hilt |
| Async | Kotlin Coroutines + StateFlow |
| AI | Anthropic Claude API (Retrofit) |
| Charts | Vico (Compose charts) |
| Image | Coil |

---

## 8. Non-Functional Requirements

- App cold-start < 2 s on mid-range device
- Offline-first: all quiz/flashcard features work without network
- Room migrations versioned from day 1 (version 1 → destructive allowed in MVP)
- Dark mode support via Material 3 dynamic color

---

## 9. Out of Scope (MVP)

- Backend sync / cloud storage
- Video lectures
- Peer leaderboards
- In-app purchases