package com.avans.gymtracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.avans.gymtracker.ui.screens.*
import com.avans.gymtracker.utils.PreferencesManager

/** Alle navigatieroutes van de app als sealed class. */
sealed class Screen(val route: String) {
    data object Splash         : Screen("splash")
    data object Login          : Screen("login")
    data object Exercises      : Screen("exercises")
    data object ExerciseDetail : Screen("exercise_detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "exercise_detail/$exerciseId"
    }
    data object Workouts       : Screen("workouts")
    data object WorkoutDetail  : Screen("workout_detail/{workoutId}") {
        fun createRoute(workoutId: Long) = "workout_detail/$workoutId"
    }
    data object AddWorkout     : Screen("add_workout")
    data object Settings       : Screen("settings")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    prefs: PreferencesManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    val dest = if (prefs.isLoggedIn) Screen.Exercises.route else Screen.Login.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                prefs = prefs,
                onLoginSuccess = {
                    navController.navigate(Screen.Exercises.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Exercises.route) {
            ExercisesScreen(
                onExerciseClick = { exerciseId ->
                    navController.navigate(Screen.ExerciseDetail.createRoute(exerciseId))
                },
                onNavigateToWorkouts = { navController.navigate(Screen.Workouts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.ExerciseDetail.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
            ExerciseDetailScreen(
                exerciseId = exerciseId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Workouts.route) {
            WorkoutsScreen(
                onWorkoutClick = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId))
                },
                onAddWorkout = { navController.navigate(Screen.AddWorkout.route) },
                onNavigateToExercises = { navController.navigate(Screen.Exercises.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.WorkoutDetail.route,
            arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable
            WorkoutDetailScreen(
                workoutId = workoutId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddWorkout.route) {
            AddWorkoutScreen(
                onWorkoutCreated = { workoutId ->
                    navController.navigate(Screen.WorkoutDetail.createRoute(workoutId)) {
                        popUpTo(Screen.Workouts.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                prefs = prefs,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
