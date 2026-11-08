package com.trainfuel.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

enum class Tab(val label: String, val emoji: String) {
    TODAY("Today", "🏠"),
    PLAN("Plan", "🗓️"),
    RECIPES("Recipes", "🍽️"),
    GROCERY("Grocery", "🛒"),
    PROFILE("Profile", "👤")
}

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dark app: always use light status/navigation bar icons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            TrainFuelTheme {
                TrainFuelApp(vm)
            }
        }
    }
}

@Composable
fun TrainFuelApp(vm: AppViewModel) {
    val profile = vm.profile

    Surface(modifier = Modifier.fillMaxSize(), color = TfBlack) {
        if (profile == null || vm.editingProfile) {
            val cancel: (() -> Unit)? =
                if (profile != null) ({ vm.editingProfile = false }) else null
            OnboardingScreen(
                initial = profile,
                onSave = { vm.saveProfile(it) },
                onCancel = cancel,
                modifier = Modifier.fillMaxSize().systemBarsPadding()
            )
        } else {
            var tab by remember { mutableStateOf(Tab.TODAY) }
            Scaffold(
                containerColor = TfBlack,
                bottomBar = {
                    NavigationBar(containerColor = TfSurface) {
                        Tab.values().forEach { t ->
                            NavigationBarItem(
                                selected = tab == t,
                                onClick = { tab = t },
                                icon = { Text(t.emoji) },
                                label = { Text(t.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedTextColor = TfYellow,
                                    unselectedTextColor = TfMuted,
                                    indicatorColor = TfSurface2
                                )
                            )
                        }
                    }
                }
            ) { inner ->
                Box(Modifier.padding(inner).fillMaxSize()) {
                    when (tab) {
                        Tab.TODAY -> TodayScreen(vm)
                        Tab.PLAN -> PlannerScreen(vm)
                        Tab.RECIPES -> RecipesScreen(vm)
                        Tab.GROCERY -> GroceryScreen(vm)
                        Tab.PROFILE -> ProfileScreen(vm)
                    }
                }
            }
        }
    }
}
