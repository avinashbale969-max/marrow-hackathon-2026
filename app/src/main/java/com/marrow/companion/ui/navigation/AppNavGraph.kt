package com.marrow.companion.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.marrow.companion.ui.screens.analytics.AnalyticsScreen
import com.marrow.companion.ui.screens.auth.LoginScreen
import com.marrow.companion.ui.screens.bookmarks.BookmarksScreen
import com.marrow.companion.ui.screens.explanation.QuestionExplanationScreen
import com.marrow.companion.ui.screens.dashboard.DashboardScreen
import com.marrow.companion.ui.screens.flashcard.FlashcardScreen
import com.marrow.companion.ui.screens.profile.ProfileScreen
import com.marrow.companion.ui.screens.quiz.QuizScreen
import com.marrow.companion.ui.screens.bookmarks.SubjectBookmarksScreen
import com.marrow.companion.ui.screens.splash.SplashScreen
import com.marrow.companion.ui.screens.subjects.AllNotesScreen
import com.marrow.companion.ui.screens.subjects.SubjectNotesScreen
import com.marrow.companion.ui.screens.subjects.SubjectsScreen
import com.marrow.companion.ui.screens.subjects.TopicBookmarksScreen
import com.marrow.companion.ui.screens.subjects.TopicDetailScreen
import com.marrow.companion.ui.screens.subjects.TopicListScreen
import com.marrow.companion.ui.screens.subjects.TopicNotesScreen
import com.marrow.companion.ui.screens.subjects.TopicReviewScreen

private val navItems = listOf(
    BottomNavItem(NavRoutes.Dashboard.route, Icons.Filled.Home,       "Home"),
    BottomNavItem(NavRoutes.Subjects.route,  Icons.Filled.Quiz,       "QBank"),
    BottomNavItem(NavRoutes.Analytics.route, Icons.Filled.Assignment, "Tests"),
    BottomNavItem(NavRoutes.Profile.route,   Icons.Filled.VideoLibrary,"Videos"),
)

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = navItems.any { it.route == currentRoute }
    val showLogin     = currentRoute == NavRoutes.Login.route

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (showBottomBar) {
                MarrowBottomNav(
                    navItems     = navItems,
                    currentRoute = currentRoute,
                    onNavigate   = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.Splash.route) {
                SplashScreen(
                    onNavigate = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Dashboard.route) {
                            popUpTo(NavRoutes.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Dashboard.route) {
                DashboardScreen(
                    onStartQuiz        = { navController.navigate(NavRoutes.Quiz.random()) },
                    onStartSubjectQuiz = { navController.navigate(NavRoutes.Subjects.route) },
                    onOpenFlashcards   = { navController.navigate(NavRoutes.Flashcards.route) },
                    onOpenExplanation  = { qId, selId ->
                        navController.navigate(NavRoutes.QuestionExplanation.create(qId, selId))
                    },
                    onLogout = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.QuestionExplanation.route,
                arguments = listOf(
                    navArgument("questionId")       { type = NavType.LongType },
                    navArgument("selectedOptionId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { backStack ->
                val qId   = backStack.arguments?.getLong("questionId")       ?: return@composable
                val selId = backStack.arguments?.getLong("selectedOptionId") ?: -1L
                QuestionExplanationScreen(
                    questionId       = qId,
                    initialSelectedId = selId.takeIf { it != -1L },
                    onBack           = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Subjects.route) {
                SubjectsScreen(
                    onSubjectClick   = { subjectId ->
                        navController.navigate(NavRoutes.TopicList.createRoute(subjectId))
                    },
                    onBookmarksClick = { navController.navigate(NavRoutes.Bookmarks.route) },
                    onNotesClick     = { navController.navigate(NavRoutes.AllNotes.route) }
                )
            }

            composable(NavRoutes.AllNotes.route) {
                AllNotesScreen(
                    onBack = { navController.popBackStack() },
                    onSubjectClick = { subjectId ->
                        if (subjectId == -1L) {
                            navController.navigate(NavRoutes.SubjectNotes.create(0L))
                        } else {
                            navController.navigate(NavRoutes.SubjectNotes.create(subjectId))
                        }
                    },
                    onNotesClick  = { navController.navigate(NavRoutes.SubjectNotes.create(0L, tab = 0)) },
                    onGreenClick  = { navController.navigate(NavRoutes.SubjectNotes.create(0L, tab = 1, color = "GREEN")) },
                    onOrangeClick = { navController.navigate(NavRoutes.SubjectNotes.create(0L, tab = 1, color = "ORANGE")) }
                )
            }

            composable(
                route = NavRoutes.SubjectNotes.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("tab")       { type = NavType.IntType;    defaultValue = 0  },
                    navArgument("color")     { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                val tab       = backStack.arguments?.getInt("tab")        ?: 0
                val color     = backStack.arguments?.getString("color")   ?: ""
                SubjectNotesScreen(
                    subjectId           = subjectId,
                    onBack              = { navController.popBackStack() },
                    initialTab          = tab,
                    initialColorFilter  = color
                )
            }

            composable(NavRoutes.Bookmarks.route) {
                BookmarksScreen(
                    onBack              = { navController.popBackStack() },
                    onStartBookmarkQuiz = { navController.navigate(NavRoutes.Quiz.random()) },
                    onSubjectClick      = { subjectId ->
                        navController.navigate(NavRoutes.SubjectBookmarks.create(subjectId))
                    }
                )
            }

            composable(
                route = NavRoutes.SubjectBookmarks.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                SubjectBookmarksScreen(
                    subjectId = subjectId,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TopicList.route,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                TopicListScreen(
                    subjectId    = subjectId,
                    onTopicClick = { topicId ->
                        navController.navigate(NavRoutes.TopicDetail.create(subjectId, topicId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TopicDetail.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("topicId")   { type = NavType.LongType }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                val topicId   = backStack.arguments?.getLong("topicId")   ?: return@composable
                TopicDetailScreen(
                    subjectId    = subjectId,
                    topicId      = topicId,
                    onBack       = { navController.popBackStack() },
                    onSolve  = { tId -> navController.navigate(NavRoutes.Quiz.forTopic(tId)) },
                    onReview = { sId, tId -> navController.navigate(NavRoutes.TopicReview.create(sId, tId)) },
                    onBookmarks  = { sId, tId -> navController.navigate(NavRoutes.TopicBookmarks.create(sId, tId)) },
                    onNotes      = { sId, tId -> navController.navigate(NavRoutes.TopicNotes.create(sId, tId)) }
                )
            }

            composable(
                route = NavRoutes.TopicReview.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("topicId")   { type = NavType.LongType }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                val topicId   = backStack.arguments?.getLong("topicId")   ?: return@composable
                TopicReviewScreen(
                    subjectId = subjectId,
                    topicId   = topicId,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TopicBookmarks.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("topicId")   { type = NavType.LongType }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                val topicId   = backStack.arguments?.getLong("topicId")   ?: return@composable
                TopicBookmarksScreen(
                    subjectId = subjectId,
                    topicId   = topicId,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TopicNotes.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType },
                    navArgument("topicId")   { type = NavType.LongType }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId") ?: return@composable
                val topicId   = backStack.arguments?.getLong("topicId")   ?: return@composable
                TopicNotesScreen(
                    subjectId = subjectId,
                    topicId   = topicId,
                    onBack    = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.Quiz.route,
                arguments = listOf(
                    navArgument("subjectId") { type = NavType.LongType;  defaultValue = -1L    },
                    navArgument("topicId")   { type = NavType.LongType;  defaultValue = -1L    },
                    navArgument("random")    { type = NavType.BoolType;  defaultValue = false  },
                    navArgument("fresh")     { type = NavType.BoolType;  defaultValue = false  }
                )
            ) { backStack ->
                val subjectId = backStack.arguments?.getLong("subjectId")?.takeIf { it != -1L }
                val topicId   = backStack.arguments?.getLong("topicId")?.takeIf   { it != -1L }
                val random    = backStack.arguments?.getBoolean("random") ?: false
                val fresh     = backStack.arguments?.getBoolean("fresh")  ?: false
                QuizScreen(
                    subjectId  = subjectId,
                    topicId    = topicId,
                    random     = random,
                    startFresh = fresh,
                    onFinish   = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Flashcards.route) { FlashcardScreen() }

            composable(NavRoutes.Analytics.route) { AnalyticsScreen() }

            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
