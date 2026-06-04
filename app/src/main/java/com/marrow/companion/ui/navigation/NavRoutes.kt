package com.marrow.companion.ui.navigation

sealed class NavRoutes(val route: String) {
    object Splash          : NavRoutes("splash")
    object Login           : NavRoutes("login")
    object Dashboard       : NavRoutes("dashboard")
    object HighlightRecall : NavRoutes("highlight_recall")
    object Subjects : NavRoutes("subjects")
    object TopicList : NavRoutes("topics/{subjectId}") {
        fun createRoute(subjectId: Long) = "topics/$subjectId"
    }
    object Quiz : NavRoutes("quiz?subjectId={subjectId}&topicId={topicId}&random={random}&fresh={fresh}") {
        fun forTopic(topicId: Long, fresh: Boolean = false) = "quiz?subjectId=-1&topicId=$topicId&random=false&fresh=$fresh"
        fun forSubject(subjectId: Long) = "quiz?subjectId=$subjectId&topicId=-1&random=false&fresh=false"
        fun random() = "quiz?subjectId=-1&topicId=-1&random=true&fresh=false"
    }
    object Flashcards : NavRoutes("flashcards")
    object Analytics : NavRoutes("analytics")
    object Profile : NavRoutes("profile")
    object Bookmarks        : NavRoutes("bookmarks")
    object SubjectBookmarks : NavRoutes("subject_bookmarks/{subjectId}") {
        fun create(subjectId: Long) = "subject_bookmarks/$subjectId"
    }
    object AllNotes     : NavRoutes("all_notes")
    object VideoSubject : NavRoutes("video_subject/{subjectId}/{subjectName}") {
        fun create(subjectId: Long, subjectName: String) =
            "video_subject/$subjectId/${android.net.Uri.encode(subjectName)}"
    }
    object ImageNotes   : NavRoutes("image_notes/{imageId}/{title}") {
        fun create(imageId: String, title: String) =
            "image_notes/${android.net.Uri.encode(imageId)}/${android.net.Uri.encode(title)}"
    }
    object VideoLesson  : NavRoutes("video_lesson/{subjectId}/{subjectName}/{lessonTitle}") {
        fun create(subjectId: Long, subjectName: String, lessonTitle: String) =
            "video_lesson/$subjectId/${android.net.Uri.encode(subjectName)}/${android.net.Uri.encode(lessonTitle)}"
    }
    object VideoNotes   : NavRoutes("video_notes/{subjectId}/{subjectName}/{lessonTitle}") {
        fun create(subjectId: Long, subjectName: String, lessonTitle: String) =
            "video_notes/$subjectId/${android.net.Uri.encode(subjectName)}/${android.net.Uri.encode(lessonTitle)}"
    }
    object SubjectNotes : NavRoutes("subject_notes/{subjectId}?tab={tab}&color={color}") {
        fun create(subjectId: Long, tab: Int = 0, color: String = "") =
            "subject_notes/$subjectId?tab=$tab&color=$color"
    }
    object QuestionExplanation : NavRoutes("explanation/{questionId}?selectedOptionId={selectedOptionId}") {
        fun create(questionId: Long, selectedOptionId: Long = -1L) =
            "explanation/$questionId?selectedOptionId=$selectedOptionId"
    }
    object DoubtChat : NavRoutes("doubt_chat?questionId={questionId}") {
        fun create(questionId: Long? = null) = "doubt_chat?questionId=${questionId ?: -1}"
    }
    object TopicDetail : NavRoutes("topic_detail/{subjectId}/{topicId}") {
        fun create(subjectId: Long, topicId: Long) = "topic_detail/$subjectId/$topicId"
    }
    object TopicReview : NavRoutes("topic_review/{subjectId}/{topicId}") {
        fun create(subjectId: Long, topicId: Long) = "topic_review/$subjectId/$topicId"
    }
    object TopicBookmarks : NavRoutes("topic_bookmarks/{subjectId}/{topicId}") {
        fun create(subjectId: Long, topicId: Long) = "topic_bookmarks/$subjectId/$topicId"
    }
    object TopicNotes : NavRoutes("topic_notes/{subjectId}/{topicId}") {
        fun create(subjectId: Long, topicId: Long) = "topic_notes/$subjectId/$topicId"
    }
    object TopicSolve : NavRoutes("topic_solve/{topicId}") {
        fun create(topicId: Long) = "topic_solve/$topicId"
    }
    object Reference : NavRoutes("reference/{subjectId}") {
        fun create(subjectId: Long) = "reference/$subjectId"
    }
}

val bottomNavItems = listOf(
    NavRoutes.Dashboard,
    NavRoutes.Subjects,
    NavRoutes.Flashcards,
    NavRoutes.Analytics,
    NavRoutes.Profile
)