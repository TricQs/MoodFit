package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.database.MoodFitDatabase
import com.example.database.MoodLog
import com.example.database.SavedActivity
import com.example.model.ActivitySuggestion
import com.example.repository.MoodFitRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MoodFitViewModel
import com.example.viewmodel.RecommendationState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.TileMode
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.HeartBroken
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.SentimentNeutral
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Add
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Room database and repositories
        val database = MoodFitDatabase.getDatabase(this)
        val repository = MoodFitRepository(database.moodFitDao())
        val sharedPrefs = getSharedPreferences("moodfit_prefs", Context.MODE_PRIVATE)
        val viewModel = MoodFitViewModel(repository, sharedPrefs)

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val localSharedPrefs = remember { context.getSharedPreferences("moodfit_prefs", Context.MODE_PRIVATE) }
            val isDarkMode = remember { mutableStateOf(localSharedPrefs.getBoolean("dark_mode", false)) }
            var splashComplete by remember { mutableStateOf(false) }
            val activeTabState = rememberSaveable { mutableStateOf("explore") }
            val profileImgState = remember { mutableStateOf<String>(localSharedPrefs.getString("profile_image_path", "") ?: "") }
            
            if (!splashComplete) {
                MyApplicationTheme(darkTheme = isDarkMode.value) {
                    SplashScreenContent(isDarkMode.value, onSplashComplete = { splashComplete = true })
                }
            } else {
                AnimatedContent(
                    targetState = isDarkMode.value,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                    },
                    label = "themeTransition"
                ) { dark ->
                    MyApplicationTheme(darkTheme = dark) {
                        MoodFitAppContent(
                            viewModel = viewModel,
                            isDarkMode = isDarkMode,
                            activeTabState = activeTabState,
                            profileImgState = profileImgState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoodFitAppContent(
    viewModel: MoodFitViewModel,
    isDarkMode: MutableState<Boolean>,
    activeTabState: MutableState<String>,
    profileImgState: MutableState<String>
) {
    val context = LocalContext.current
    val currentMood by viewModel.selectedMood.collectAsStateWithLifecycle()
    val recState by viewModel.recommendationState.collectAsStateWithLifecycle()
    val savedList by viewModel.savedActivities.collectAsStateWithLifecycle()
    val historyLogs by viewModel.moodHistory.collectAsStateWithLifecycle()
    
    val authState by viewModel.isUserAuthenticated.collectAsStateWithLifecycle()
    val emailState by viewModel.userEmail.collectAsStateWithLifecycle()
    val langState by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val deviceState by viewModel.connectedHardware.collectAsStateWithLifecycle()
    
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCropDialog by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // Force recomposition of the app layout when language changes
    val currentLanguage = langState

    val sharedPrefs = remember { context.getSharedPreferences("moodfit_prefs", Context.MODE_PRIVATE) }
    var usernameState by remember { mutableStateOf(sharedPrefs.getString("username", "") ?: "") }
    
    var showOnboarding by remember { mutableStateOf(usernameState.isEmpty()) }
    var isFirstLaunch by remember { mutableStateOf(!sharedPrefs.getBoolean("first_launch_done", false)) }

    var activeTab by activeTabState
    var showExitDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = showOnboarding,
        transitionSpec = {
            fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
        },
        label = "screenTransition"
    ) { onboarding ->
        if (onboarding) {
            OnboardingScreenContent(
                viewModel = viewModel,
                isDark = isDarkMode.value,
                onNameEntered = { name ->
                    sharedPrefs.edit()
                        .putString("username", name)
                        .putBoolean("first_launch_done", true)
                        .apply()
                    usernameState = name
                    showOnboarding = false
                }
            )
        } else {
            BackHandler(enabled = true) {
                showExitDialog = true
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text(viewModel.t("MoodFit"), fontWeight = FontWeight.Bold) },
                    text = { Text(viewModel.t("Do you want to exit MoodFit?")) },
                    confirmButton = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    (context as? ComponentActivity)?.finish()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exit_dialog_ok"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4)
                                )
                            ) {
                                Text("Ok")
                            }
                            OutlinedButton(
                                onClick = { showExitDialog = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exit_dialog_cancel")
                            ) {
                                Text(viewModel.t("Cancel"))
                            }
                        }
                    }
                )
            }

            // Atmosphere Color Setup according to current mood selection
            val backgroundBrush = getMoodGradientBrush(currentMood, isDarkMode.value)
            val moodColorAccent = getMoodAccentColor(currentMood)

            // Trigger lock covers if biometrics are enabled and locked
            if (viewModel.isBiometricLocked.value) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(viewModel.t("Biometric Security"), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Security Screen", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp)) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                viewModel.t("Secure Privacy lock active. Please touch scan screen or authentic check with device sensor."),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.unlockBiometrics() },
                            modifier = Modifier.testTag("biometric_scan_button")
                        ) {
                            Text("Scan Fingerprint")
                        }
                    }
                )
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    val navbarBgColor = if (isDarkMode.value) Color(0xFF0F0C20) else Color(0xFF2E3B84)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(navbarBgColor)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .height(72.dp)
                            .testTag("navigation_menu")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            NavBarItem(
                                icon = Icons.Rounded.DateRange,
                                label = viewModel.t("Active Checks"),
                                selected = activeTab == "analytics",
                                onClick = { activeTab = "analytics" },
                                testTag = "nav_analytics"
                            )
                            NavBarItem(
                                icon = Icons.Rounded.Favorite,
                                label = viewModel.t("Favorites"),
                                selected = activeTab == "favorites",
                                onClick = { activeTab = "favorites" },
                                testTag = "nav_favorites"
                            )
                            
                            // Spacer for center FAB
                            Spacer(modifier = Modifier.width(64.dp))
                            
                            NavBarItem(
                                icon = Icons.Rounded.Newspaper,
                                label = viewModel.t("Articles"),
                                selected = activeTab == "news",
                                onClick = { activeTab = "news" },
                                testTag = "nav_news"
                            )
                            NavBarItem(
                                icon = Icons.Rounded.Settings,
                                label = viewModel.t("Settings"),
                                selected = activeTab == "settings",
                                onClick = { activeTab = "settings" },
                                testTag = "nav_settings"
                            )
                        }
                        
                        // Large Center Home FAB
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-20).dp)
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isDarkMode.value) {
                                            listOf(Color(0xFFD0BCFF), Color(0xFF8B5CF6))
                                        } else {
                                            listOf(Color(0xFF6750A4), Color(0xFF3B82F6))
                                        }
                                    )
                                )
                                .clickable { activeTab = "explore" }
                                .testTag("nav_explore"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = viewModel.t("Home"),
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                        .padding(innerPadding)
                ) {
                    // High level App Status Banner matching Vibrant Palette Layout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Top text: WELCOME TO MOODFIT (small size)
                            Text(
                                text = viewModel.t("Welcome to MoodFit").uppercase(),
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDarkMode.value) Color(0xFFCCC2DC) else Color(0xFF49454F)
                            )
                            Spacer(modifier = Modifier.height(2.dp))

                            // Bottom text: GOOD EVENING USERNAME (large size)
                            val welcomeBackText = buildAnnotatedString {
                                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                                val greetingKey = when (hour) {
                                    in 5..10 -> "Good Morning"
                                    in 11..14 -> "Good Afternoon"
                                    in 15..17 -> "Good Evening"
                                    else -> "Good Night"
                                }
                                val prefix = "${viewModel.t(greetingKey)} ".uppercase()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                    append(prefix)
                                }
                                val formattedName = if (usernameState.length > 15) {
                                    usernameState.take(15) + "..."
                                } else {
                                    usernameState
                                }.uppercase()
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                                    append(formattedName)
                                }
                            }
                            Text(
                                text = welcomeBackText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDarkMode.value) Color.White else Color(0xFF1D1B20),
                                modifier = Modifier.testTag("app_title")
                            )
                        }

                        // Header Badges Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (deviceState != "None") {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Sensor connected",
                                    tint = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .clickable { showProfileDialog = true }
                                    .testTag("status_connection_icon"),
                                contentAlignment = Alignment.Center
                            ) {
                                val imgPath = profileImgState.value
                                if (!imgPath.isNullOrEmpty()) {
                                    coil.compose.AsyncImage(
                                        model = if (imgPath.startsWith("http")) imgPath else java.io.File(imgPath),
                                        contentDescription = "Profile Picture",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Profile",
                                        tint = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(if (authState) Color(0xFF10B981) else Color.Gray, CircleShape)
                                        .align(Alignment.BottomEnd)
                                        .border(1.5.dp, if (isDarkMode.value) Color(0xFF1D1B30) else Color(0xFFF3EDF7), CircleShape)
                                )
                            }
                        }
                    }

                    // Wearable Vitals Telemetry bar matching Health Sync layout
                    if (deviceState != "None") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .testTag("wearable_vitals_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode.value) Color(0xFF2C2A4A).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val pulse by viewModel.hardwareHeartRate.collectAsStateWithLifecycle()
                                val stressIndex by viewModel.hardwareStressIndex.collectAsStateWithLifecycle()
                                
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (isDarkMode.value) Color(0xFF381E72) else Color(0xFFE8DEF8),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Pulse",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = viewModel.t("Health Sync") + ": $deviceState",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode.value) Color.LightGray else Color.Black
                                    )
                                    Text(
                                        text = "Heart Rate: $pulse BPM  |  Stress Level: $stressIndex/100",
                                        fontSize = 11.sp,
                                        color = if (isDarkMode.value) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // Tabs Content Area
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                            },
                            label = "tabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                "explore" -> ExploreTabContent(viewModel, recState, isDarkMode.value, moodColorAccent)
                                "favorites" -> FavoritesTabContent(viewModel, savedList, isDarkMode.value)
                                "analytics" -> AnalyticsTabContent(viewModel, historyLogs, savedList, isDarkMode.value, moodColorAccent)
                                "news" -> NewsTabContent(viewModel, isDarkMode.value)
                                "settings" -> SettingsTabContent(
                                    viewModel = viewModel,
                                    isDarkMode = isDarkMode,
                                    authState = authState,
                                    emailState = emailState,
                                    langState = langState,
                                    deviceState = deviceState,
                                    username = usernameState,
                                    onUsernameChange = { newName ->
                                        sharedPrefs.edit().putString("username", newName).apply()
                                        usernameState = newName
                                    }
                                )
                            }
                        }
                    }
                    
                    // Profile Change Dialog
                    if (showProfileDialog) {
                        AlertDialog(
                            onDismissRequest = { showProfileDialog = false },
                            title = { Text(viewModel.t("Change Profile Picture"), fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(Color.Gray.copy(alpha = 0.2f))
                                    ) {
                                        val imgPath = profileImgState.value
                                        if (!imgPath.isNullOrEmpty()) {
                                            coil.compose.AsyncImage(
                                                model = if (imgPath.startsWith("http")) imgPath else java.io.File(imgPath),
                                                contentDescription = "Preview",
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Default Profile",
                                                tint = Color.Gray,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    
                                    Text(viewModel.t("Select Avatar Template"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    val templates = listOf(
                                        "https://api.dicebear.com/7.x/fun-emoji/png?seed=Happy",
                                        "https://api.dicebear.com/7.x/fun-emoji/png?seed=Funny",
                                        "https://api.dicebear.com/7.x/adventurer/png?seed=Adventure",
                                        "https://api.dicebear.com/7.x/pixel-art/png?seed=Pixel",
                                        "https://api.dicebear.com/7.x/bottts/png?seed=Robot",
                                        "https://api.dicebear.com/7.x/lorelei/png?seed=Cute"
                                    )
                                    
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        templates.forEachIndexed { index, url ->
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .border(
                                                        width = if (profileImgState.value == url) 2.dp else 0.dp,
                                                        color = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                                                        shape = CircleShape
                                                    )
                                                    .background(Color.White)
                                                    .clickable {
                                                        profileImgState.value = url
                                                        sharedPrefs.edit().putString("profile_image_path", url).apply()
                                                        Toast.makeText(context, "Avatar selected!", Toast.LENGTH_SHORT).show()
                                                    }
                                            ) {
                                                coil.compose.AsyncImage(
                                                    model = url,
                                                    contentDescription = "Template $index",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                    
                                    val galleryLauncher = rememberLauncherForActivityResult(
                                        contract = ActivityResultContracts.GetContent()
                                    ) { uri ->
                                        if (uri != null) {
                                            showCropDialog = uri
                                            showProfileDialog = false
                                        }
                                    }
                                    
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                                            contentColor = if (isDarkMode.value) Color(0xFF1D1B20) else Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text(viewModel.t("Upload Photo"))
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showProfileDialog = false }) {
                                    Text(viewModel.t("Close"))
                                }
                            }
                        )
                    }

                    // Circular Crop Dialog
                    if (showCropDialog != null) {
                        val imageUri = showCropDialog!!
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        
                        AlertDialog(
                            onDismissRequest = { showCropDialog = null },
                            title = { Text(viewModel.t("Crop Image"), fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(200.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4), CircleShape)
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = imageUri,
                                            contentDescription = "Crop target",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = scale,
                                                    scaleY = scale,
                                                    translationX = offset.x,
                                                    translationY = offset.y
                                                )
                                                .pointerInput(Unit) {
                                                    detectTransformGestures { _, pan, zoom, _ ->
                                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                                        offset += pan
                                                    }
                                                }
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Zoom", fontSize = 12.sp, modifier = Modifier.width(48.dp))
                                        Slider(
                                            value = scale,
                                            onValueChange = { scale = it },
                                            valueRange = 1f..5f,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    
                                    Text("Drag image to pan, pinch or use slider to zoom.", fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val croppedUri = saveCroppedImage(context, imageUri, scale, offset, 200f)
                                        if (croppedUri != null) {
                                            val path = croppedUri.path ?: ""
                                            profileImgState.value = path
                                            sharedPrefs.edit().putString("profile_image_path", path).apply()
                                            Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to crop image.", Toast.LENGTH_SHORT).show()
                                        }
                                        showCropDialog = null
                                    }
                                ) {
                                    Text(viewModel.t("Confirm"))
                                }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showCropDialog = null }) {
                                    Text(viewModel.t("Cancel"))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ==================== TABS LAYOUT COMPONENTS ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreTabContent(
    viewModel: MoodFitViewModel,
    recState: RecommendationState,
    isDark: Boolean,
    moodColorAccent: Color
) {
    val context = LocalContext.current
    val currentMood by viewModel.selectedMood.collectAsStateWithLifecycle()

    val sharedPrefs = remember { context.getSharedPreferences("moodfit_prefs", Context.MODE_PRIVATE) }
    var quoteIndex by rememberSaveable { mutableStateOf(-1) }

    LaunchedEffect(Unit) {
        if (quoteIndex == -1) {
            val lastIndex = sharedPrefs.getInt("last_quote_index", -1)
            var newIndex = lastIndex
            if (com.example.model.MotivationalQuotes.quotes.size > 1) {
                do {
                    newIndex = kotlin.random.Random.nextInt(com.example.model.MotivationalQuotes.quotes.size)
                } while (newIndex == lastIndex)
            } else {
                newIndex = 0
            }
            sharedPrefs.edit().putInt("last_quote_index", newIndex).apply()
            quoteIndex = newIndex
        }
    }

    var currentGreetingKey by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val key = when (hour) {
                in 5..10 -> "Good Morning"
                in 11..14 -> "Good Afternoon"
                in 15..17 -> "Good Evening"
                else -> "Good Night"
            }
            if (currentGreetingKey.isNotEmpty() && currentGreetingKey != key) {
                if (com.example.model.MotivationalQuotes.quotes.size > 1) {
                    var newIndex = quoteIndex
                    do {
                        newIndex = kotlin.random.Random.nextInt(com.example.model.MotivationalQuotes.quotes.size)
                    } while (newIndex == quoteIndex)
                    sharedPrefs.edit().putInt("last_quote_index", newIndex).apply()
                    quoteIndex = newIndex
                }
            }
            currentGreetingKey = key
            kotlinx.coroutines.delay(10000)
        }
    }

    val rawQuote = if (quoteIndex in com.example.model.MotivationalQuotes.quotes.indices) {
        com.example.model.MotivationalQuotes.quotes[quoteIndex]
    } else {
        "Believe you can and you're halfway there."
    }
    val translatedQuote = com.example.model.MotivationalQuotes.t(rawQuote, viewModel.selectedLanguageState.value)
    
    val infiniteTransition = rememberInfiniteTransition(label = "smokeWaves")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    val waveColors = when (currentMood.lowercase()) {
        "happy" -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFD97706))
        "stressed" -> listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF059669))
        "tired" -> listOf(Color(0xFF6366F1), Color(0xFF818CF8), Color(0xFF4F46E5))
        "productive" -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFF2563EB))
        "bored" -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA), Color(0xFF7C3AED))
        else -> listOf(Color(0xFF6B7280), Color(0xFF9CA3AF), Color(0xFF4B5563))
    }

    val moodChoices = listOf("Happy", "Stressed", "Tired", "Productive", "Bored")
    val moodIcons = mapOf(
        "Happy" to Icons.Rounded.SentimentSatisfiedAlt,
        "Stressed" to Icons.Rounded.HeartBroken,
        "Tired" to Icons.Rounded.Snooze,
        "Productive" to Icons.Rounded.Bolt,
        "Bored" to Icons.Rounded.SentimentNeutral
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_lazy_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E1B30).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("How are you feeling?"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFE6E1E5) else Color(0xFF49454F),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Translucent grid of visual mood chips matching Vibrant Palette mockup exactly
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodChoices.forEach { mood ->
                            val isSelected = currentMood == mood
                            val bgChipColor = if (isSelected) {
                                getMoodAccentColor(mood)
                            } else {
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.6f)
                            }
                            val textChipColor = if (isSelected) {
                                Color.White
                            } else {
                                if (isDark) Color.White else Color(0xFF1D1B20)
                            }
                            
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(bgChipColor)
                                    .clickable { viewModel.selectMood(mood) }
                                    .testTag("visual_mood_chip_$mood")
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .padding(bottom = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = moodIcons[mood] ?: Icons.Rounded.Spa
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = mood,
                                        tint = textChipColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = viewModel.t(mood).uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textChipColor,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        // Faux add button perfectly mirroring "+" element form mockup
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.3f))
                                .clickable {
                                    Toast.makeText(context, viewModel.t("Custom wellness moods coming!"), Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Custom Mood",
                                tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF6750A4).copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp).padding(bottom = 2.dp)
                            )
                            Text(
                                text = viewModel.t("Custom").uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF6750A4).copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Divider and Custom Mood Text Input
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        Text(
                            text = viewModel.t("-------- or ---------"),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val customText by viewModel.customMoodText.collectAsStateWithLifecycle()
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { viewModel.setCustomMoodText(it) },
                        placeholder = {
                            Text(
                                text = viewModel.t("Tell us about your current mood..."),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_mood_text_input"),
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = moodColorAccent,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recommendations Button with Vibrant Shape rounded-3xl (24dp)
                    Button(
                        onClick = { viewModel.getRecommendations() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .drawBehind {
                                val w = size.width
                                val h = size.height

                                // Draw solid background base
                                drawRect(color = waveColors[0])

                                // Draw Wave 1
                                val path1 = Path()
                                path1.moveTo(0f, h)
                                for (x in 0..w.toInt() step 5) {
                                    val fx = x.toFloat()
                                    val y = h * 0.5f + kotlin.math.sin(fx * 0.015f + phase1).toFloat() * (h * 0.25f)
                                    path1.lineTo(fx, y)
                                }
                                path1.lineTo(w, h)
                                path1.close()
                                drawPath(
                                    path = path1,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(waveColors[1].copy(alpha = 0.4f), Color.Transparent),
                                        startY = h * 0.2f,
                                        endY = h
                                    )
                                )

                                // Draw Wave 2
                                val path2 = Path()
                                path2.moveTo(0f, h)
                                for (x in 0..w.toInt() step 5) {
                                    val fx = x.toFloat()
                                    val y = h * 0.6f + kotlin.math.cos(fx * 0.02f - phase2).toFloat() * (h * 0.2f)
                                    path2.lineTo(fx, y)
                                }
                                path2.lineTo(w, h)
                                path2.close()
                                drawPath(
                                    path = path2,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(waveColors[2].copy(alpha = 0.35f), Color.Transparent),
                                        startY = h * 0.3f,
                                        endY = h
                                    )
                                )

                                // Draw Wave 3
                                val path3 = Path()
                                path3.moveTo(0f, h)
                                for (x in 0..w.toInt() step 5) {
                                    val fx = x.toFloat()
                                    val y = h * 0.4f + kotlin.math.sin(fx * 0.01f + phase3).toFloat() * (h * 0.15f)
                                    path3.lineTo(fx, y)
                                }
                                path3.lineTo(w, h)
                                path3.close()
                                drawPath(
                                    path = path3,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.White.copy(alpha = 0.25f), Color.Transparent),
                                        startY = h * 0.1f,
                                        endY = h
                                    )
                                )
                            }
                            .testTag("get_recommendations_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = viewModel.t("Get Recommendations"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        if (recState !is RecommendationState.Success) {
            item {
                MotivationalQuoteCard(
                    quote = translatedQuote,
                    isDark = isDark,
                    modifier = Modifier.testTag("motivational_quote_card")
                )
            }
        }

        // Recommendations list layout header matching Suggested for You mockup
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.t("Suggested for You"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1D1B20)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.getRecommendations() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("refresh_recommendations_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.DarkGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        // Pure CSS/visual design AI-Gen badge capsule mimicking design HTML exactly
                        Box(
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF381E72) else Color(0xFFE8DEF8), RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AI GEN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFD0BCFF) else Color(0xFF1D192B)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (viewModel.selectedLanguage.value.lowercase() == "indonesian" || viewModel.selectedLanguage.value.lowercase() == "indonesia") 
                        "Ketuk kartu untuk melihat detail selengkapnya" 
                    else 
                        "Tap card to view full details",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        when (recState) {
            is RecommendationState.Idle -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = "Idle Suggestions",
                                tint = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                                modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = viewModel.t("Get suggestions powered by Gemini AI"),
                                fontSize = 13.sp,
                                color = if (isDark) Color.LightGray else Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is RecommendationState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ModernLoadingIndicator(
                            color = moodColorAccent,
                            modifier = Modifier.testTag("progress_indicator")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.t("Generating recommendations..."),
                            fontSize = 13.sp,
                            color = if (isDark) Color.LightGray else Color.DarkGray
                        )
                    }
                }
            }

            is RecommendationState.Success -> {
                if (recState.activities.isEmpty()) {
                    item {
                        Text("No activities found. Try generating again.", color = Color.Gray)
                    }
                } else {
                    items(recState.activities) { activity ->
                        ActivitySuggestionCard(
                            activity = activity,
                            isDark = isDark,
                            accentColor = moodColorAccent,
                            closeButtonText = viewModel.t("Close"),
                            language = viewModel.selectedLanguageState.value,
                            onSaveToggle = { viewModel.toggleSaveActivity(activity) },
                            onCalendarClick = {
                                scheduleGoogleCalendarEvent(context, activity.title, activity.description, activity.duration)
                            },
                            onShareClick = {
                                shareActivityIntent(context, activity.title, activity.description, activity.duration)
                            }
                        )
                    }
                }
            }

            is RecommendationState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8E8))
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = recState.message, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (recState is RecommendationState.Success) {
            item {
                MotivationalQuoteCard(
                    quote = translatedQuote,
                    isDark = isDark,
                    modifier = Modifier.testTag("motivational_quote_card")
                )
            }
        }
    }
}

@Composable
fun ActivitySuggestionCard(
    activity: ActivitySuggestion,
    isDark: Boolean,
    accentColor: Color,
    closeButtonText: String,
    language: String,
    onSaveToggle: () -> Unit,
    onCalendarClick: () -> Unit,
    onShareClick: () -> Unit
) {
    var showDetailDialog by remember { mutableStateOf(false) }

    // Dynamic Icon detection based on category or title or keywords
    val icon = when {
        activity.title.lowercase().contains("breath") || activity.title.lowercase().contains("medit") || activity.title.lowercase().contains("mind") || activity.description.lowercase().contains("breath") -> Icons.Rounded.SelfImprovement
        activity.title.lowercase().contains("music") || activity.title.lowercase().contains("sound") || activity.title.lowercase().contains("beat") || activity.title.lowercase().contains("lo-fi") || activity.title.lowercase().contains("audio") || activity.description.lowercase().contains("audio") -> Icons.Rounded.Headphones
        activity.title.lowercase().contains("journal") || activity.title.lowercase().contains("write") || activity.title.lowercase().contains("note") || activity.title.lowercase().contains("gratitude") || activity.description.lowercase().contains("write") -> Icons.Rounded.EditNote
        activity.title.lowercase().contains("walk") || activity.title.lowercase().contains("stretch") || activity.title.lowercase().contains("run") || activity.title.lowercase().contains("mov") -> Icons.Rounded.DirectionsWalk
        else -> Icons.Rounded.Spa
    }

    // Dynamic background for icon block matching mockup exactly
    val pastelBg = when (icon) {
        Icons.Rounded.SelfImprovement -> if (isDark) Color(0xFF623B4C) else Color(0xFFFFD8E4) // Pastel Pink
        Icons.Rounded.Headphones -> if (isDark) Color(0xFF2E243D) else Color(0xFFE8DEF8) // Soft Lilac
        Icons.Rounded.EditNote -> if (isDark) Color(0xFF1E3A54) else Color(0xFFD0BCFF) // Pastel Purple
        Icons.Rounded.DirectionsWalk -> if (isDark) Color(0xFF1C321C) else Color(0xFFE2F9EE) // Soft Mint
        else -> if (isDark) Color(0xFF2E243D) else Color(0xFFE8DEF8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetailDialog = true }
            .testTag("activity_card_${activity.title.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E1B30).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.82f)
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFCAC4D0).copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon illustration block
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(pastelBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = activity.category,
                        tint = if (isDark) Color.White else Color(0xFF6750A4),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Middle: Text Area & Badges + Add Calendar
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1D1B20),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = activity.description,
                        fontSize = 11.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF49454F),
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = activity.type,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFE8DEF8) else Color(0xFF6750A4),
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF2C1E4D) else Color(0xFFE8DEF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                        Text(
                            text = activity.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF0F3147) else Color(0xFFE0F2FE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Button(
                            onClick = onCalendarClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF34D399).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f),
                                contentColor = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("action_calendar_${activity.title.replace(" ", "_")}")
                        ) {
                            Text("Add Calendar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Column: Stack of Duration (Top), Heart (Middle), Share (Bottom)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Level 1: Duration Badge
                    Box(
                        modifier = Modifier
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = formatDuration(activity.duration, language),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Level 2: Heart/Save Button
                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("action_save_${activity.title.replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = if (activity.isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save activity",
                            tint = if (activity.isSaved) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Level 3: Share Button
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("action_share_${activity.title.replace(" ", "_")}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share item",
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF6750A4),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDetailDialog) {
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = activity.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color.Black
                    )
                }
            },
            text = {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = formatDuration(activity.duration, language), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                        Text(
                            text = activity.type,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFE8DEF8) else Color(0xFF6750A4),
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF2C1E4D) else Color(0xFFE8DEF8), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = activity.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier
                                .background(if (isDark) Color(0xFF0F3147) else Color(0xFFE0F2FE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = activity.description,
                        fontSize = 14.sp,
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDetailDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(text = closeButtonText)
                }
            }
        )
    }
}

// ==================== FAVORITES ROUTINES ====================

@Composable
fun FavoritesTabContent(
    viewModel: MoodFitViewModel,
    savedList: List<SavedActivity>,
    isDark: Boolean
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_lazy_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = viewModel.t("Saved Activities"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E1B4B)
            )
            Text(
                text = viewModel.t("Stored locally inside Room offline DB."),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (savedList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "No favorites",
                            tint = if (isDark) Color.LightGray else Color.Gray,
                            modifier = Modifier.size(48.dp).padding(bottom = 12.dp)
                        )
                        Text(
                            text = viewModel.t("Empty Favorites"),
                            color = if (isDark) Color.LightGray else Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(savedList) { saved ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("favorite_card_${saved.title.replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = saved.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDark) Color.White else Color(0xFF1F2937),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = { viewModel.deleteFavorite(saved.id) },
                                modifier = Modifier.testTag("delete_favorite_${saved.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = if (isDark) Color(0xFFFF8A80) else Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val accent = getMoodAccentColor(saved.mood)
                            Text(
                                text = saved.mood,
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(accent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Duration",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = saved.duration,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        Text(
                            text = saved.description,
                            fontSize = 13.sp,
                            color = if (isDark) Color.LightGray else Color.DarkGray,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scheduleGoogleCalendarEvent(context, saved.title, saved.description, saved.duration)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Book Calendar", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Schedule", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = {
                                    shareActivityIntent(context, saved.title, saved.description, saved.duration)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== LONG TERM ANALYTICS ====================

@Composable
fun AnalyticsTabContent(
    viewModel: MoodFitViewModel,
    historyLogs: List<MoodLog>,
    savedList: List<SavedActivity>,
    isDark: Boolean,
    accentColor: Color
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_lazy_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = viewModel.t("Long-Term Analytics"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E1B4B)
            )
            Text(
                text = viewModel.t("Tracking your wellness progress dynamically."),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // High Level Stats cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(viewModel.t("Total Journal Entries"), fontSize = 11.sp, color = Color.Gray)
                        Text("${historyLogs.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(viewModel.t("Saved Routines"), fontSize = 11.sp, color = Color.Gray)
                        Text("${savedList.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                }
            }
        }

        // Custom drawn Canvas graphical chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("Mood Frequency") + " / Energy Level Wave",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MoodHistoryChart(
                        logs = historyLogs,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("analytics_mood_chart"),
                        color = accentColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.t("Wave plotting energy fluctuations (1=Tired, 10=Productive) over the last 10 entries."),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Historical Journal log entries
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.t("Check In History"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color.Black
                )

                Text(
                    text = "Clear All",
                    fontSize = 12.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.clearLogHistory() }
                        .testTag("clear_history_logs_button")
                )
            }
        }

        if (historyLogs.isEmpty()) {
            item {
                Text("No check-in history yet. Generate recommendations above to create wellness logs.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            items(historyLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val logIcon = when (log.mood.lowercase()) {
                                "happy" -> Icons.Rounded.SentimentSatisfiedAlt
                                "stressed" -> Icons.Rounded.HeartBroken
                                "tired" -> Icons.Rounded.Snooze
                                "productive" -> Icons.Rounded.Bolt
                                "bored" -> Icons.Rounded.SentimentNeutral
                                else -> Icons.Rounded.Spa
                            }
                            Icon(
                                imageVector = logIcon,
                                contentDescription = log.mood,
                                tint = getMoodAccentColor(log.mood),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = viewModel.t(log.mood),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else Color.Black
                                )
                                Text(
                                    text = viewModel.t("Logged on state checkout"),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(getMoodAccentColor(log.mood).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${viewModel.t("Energy")}: ${log.energyLevel}/10",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = getMoodAccentColor(log.mood)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SETTINGS & ACCESSIBILITY SCREEN ====================

@Composable
fun SettingsTabContent(
    viewModel: MoodFitViewModel,
    isDarkMode: MutableState<Boolean>,
    authState: Boolean,
    emailState: String,
    langState: String,
    deviceState: String,
    username: String,
    onUsernameChange: (String) -> Unit
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Toast.makeText(context, viewModel.t("Notification permission granted!"), Toast.LENGTH_SHORT).show()
                viewModel.setRemindersEnabled(true)
            } else {
                Toast.makeText(context, viewModel.t("Notification permission denied!"), Toast.LENGTH_SHORT).show()
                viewModel.setRemindersEnabled(false)
            }
        }
    )
    var inputEmail by remember { mutableStateOf("") }
    var langExpanded by remember { mutableStateOf(false) }
    var hardwareExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_lazy_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = viewModel.t("Settings & Privacy"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode.value) Color.White else Color(0xFF1E1B4B)
            )
            Text(
                text = "Manage multi-language translation, biometrics, hardware, and OAuth sync.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Light/Dark mode accessibility toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.t("Dark Mode Toggle"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isDarkMode.value) Color.White else Color.Black
                        )
                        Text(
                            text = viewModel.t("Toggle theme for nighttime wellness breaks."),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isDarkMode.value,
                        onCheckedChange = {
                            isDarkMode.value = it
                            val sharedPrefs = context.getSharedPreferences("moodfit_prefs", Context.MODE_PRIVATE)
                            sharedPrefs.edit().putBoolean("dark_mode", it).apply()
                        },
                        modifier = Modifier.testTag("dark_mode_accessibility_switch")
                    )
                }
            }
        }

        // Multi-Language Dropdown selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("Language Setting"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode.value) Color.White else Color.Black
                    )
                    Text(
                        text = viewModel.t("Translate MoodFit dynamically."),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { langExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("language_dropdown")
                        ) {
                            Text(text = "${viewModel.t("Current Language: ")}$langState", color = if (isDarkMode.value) Color.White else Color.Black)
                        }

                        DropdownMenu(
                            expanded = langExpanded,
                            onDismissRequest = { langExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            val languages = listOf("English", "Indonesian", "Spanish", "French", "German")
                            languages.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        viewModel.setLanguage(language)
                                        langExpanded = false
                                    },
                                    modifier = Modifier.testTag("lang_item_$language")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Biometric Privacy Lock Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.t("Biometric Security"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode.value) Color.White else Color.Black
                            )
                            Text(
                                text = viewModel.t("Enforce lock screen privacy using fingerprints."),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = viewModel.isBiometricEnabled.value,
                            onCheckedChange = {
                                viewModel.setBiometricEnabled(it)
                            },
                            modifier = Modifier.testTag("biometric_security_switch")
                        )
                    }

                    if (viewModel.isBiometricEnabled.value) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.lockBiometrics() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("test_lock_biometrics_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563))
                        ) {
                            Text(viewModel.t("Test Lock Security Lock"), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Hardware Device integration controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("Connect Wearable") + " Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode.value) Color.White else Color.Black
                    )
                    Text(
                        text = viewModel.t("Simulate active vitals from physical tracking hardware."),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { hardwareExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hardware_sync_dropdown")
                        ) {
                             Text(text = if (deviceState == "None") viewModel.t("Connected tracker: None") else "${viewModel.t("Connected tracker: ")}$deviceState", color = if (isDarkMode.value) Color.White else Color.Black)
                        }

                        DropdownMenu(
                            expanded = hardwareExpanded,
                            onDismissRequest = { hardwareExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            val hardwares = listOf("None", "Garmin Forerunner", "Fitbit Sense", "Apple Watch Series X")
                            hardwares.forEach { device ->
                                DropdownMenuItem(
                                    text = { Text(device) },
                                    onClick = {
                                        viewModel.connectHardware(device)
                                        hardwareExpanded = false
                                        Toast.makeText(context, "$device sync active!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("hardware_item_${device.replace(" ", "_")}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Notification Break reminders Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.t("Custom Notifications"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDarkMode.value) Color.White else Color.Black
                            )
                            Text(
                                text = viewModel.t("Receive break timers to avoid physical strain."),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = viewModel.remindersEnabled.value,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (!hasPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setRemindersEnabled(true)
                                    }
                                } else {
                                    viewModel.setRemindersEnabled(checked)
                                }
                            },
                            modifier = Modifier.testTag("wellness_break_notif_switch")
                        )
                    }

                    if (viewModel.remindersEnabled.value) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = viewModel.t("Reminder frequency: ") + viewModel.t(viewModel.reminderFrequency.value),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode.value) Color.LightGray else Color.DarkGray
                        )
                        
                        var sliderPosition by remember {
                            mutableStateOf(
                                when (viewModel.reminderFrequency.value) {
                                    "Every 2 Hours" -> 1f
                                    "Every 4 Hours" -> 2f
                                    "Daily Break Reminders" -> 3f
                                    else -> 4f
                                }
                            )
                        }
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                                val freq = when (it.toInt()) {
                                    1 -> "Every 2 Hours"
                                    2 -> "Every 4 Hours"
                                    3 -> "Daily Break Reminders"
                                    else -> "Progress Summaries"
                                }
                                viewModel.setReminderFrequency(freq)
                            },
                            valueRange = 1f..4f,
                            steps = 2,
                            modifier = Modifier.testTag("notif_frequency_slider")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (!hasPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Button
                                    }
                                }
                                sendLocalNotification(
                                    context = context,
                                    title = "🧘 MoodFit Break Reminder",
                                    message = "Take 5 minutes to release your shoulders and do deep breathing!"
                                )
                                Toast.makeText(context, "System Alarm Fired!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("trigger_test_notification_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Trigger Test Wellness Push Alert", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Change Username Card (Placed above Sync, below Custom Notifications)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("Change Username"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode.value) Color.White else Color.Black
                    )
                    Text(
                        text = viewModel.t("Customize your display name inside the app."),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var newNameInput by rememberSaveable(inputs = arrayOf(username)) { mutableStateOf(username) }
                    
                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        label = { Text(viewModel.t("Your Name")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_username_input"),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = {
                            if (newNameInput.isNotBlank()) {
                                onUsernameChange(newNameInput.trim())
                                Toast.makeText(context, viewModel.t("Username updated successfully!"), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, viewModel.t("Name cannot be empty"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = newNameInput.isNotBlank() && newNameInput.trim() != username,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("change_username_submit"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkMode.value) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                            contentColor = if (isDarkMode.value) Color(0xFF1D1B20) else Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(viewModel.t("Save"))
                    }
                }
            }
        }

        // Simulated Google Account Sign-In / OAuth Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode.value) Color(0xFF1E293B) else Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = viewModel.t("Sign In for Sync") + " (OAuth)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDarkMode.value) Color.White else Color.Black
                    )
                    Text(
                        text = viewModel.t("Synchronize your moods and favorites securely to cloud repositories."),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (!authState) {
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Google Account Email") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("oauth_email_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (inputEmail.isNotBlank() && inputEmail.contains("@")) {
                                    viewModel.authenticateWithOAuth(inputEmail)
                                    Toast.makeText(context, "Google OAuth sync authorized!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please write a valid email address.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim_oauth_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text(viewModel.t("Simulate OAuth"))
                        }
                    } else {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active Sync", tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Secure Profile: $emailState",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDarkMode.value) Color.LightGray else Color.DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "OAuth status: Authorized Sync Active", fontSize = 11.sp, color = Color(0xFF10B981))

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.signOut() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("oauth_signout_button")
                            ) {
                                Text("Disconnect Google Session", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SYSTEM HELPERS ====================

fun getMoodGradientBrush(mood: String, isDark: Boolean): Brush {
    return when (mood.lowercase()) {
        "happy" -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF2C1E08), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFFFF4D4), Color(0xFFEADDFF)))
            }
        }
        "stressed" -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF092518), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFE2F9EE), Color(0xFFEADDFF)))
            }
        }
        "tired" -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF14122A), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFE9E5FF), Color(0xFFEADDFF)))
            }
        }
        "productive" -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF0C1D3A), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFEADDFF)))
            }
        }
        "bored" -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF1E2024), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFEADDFF)))
            }
        }
        else -> {
            if (isDark) {
                Brush.verticalGradient(listOf(Color(0xFF1D1B30), Color(0xFF13101E)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF3EDF7), Color(0xFFEADDFF)))
            }
        }
    }
}

fun getMoodAccentColor(mood: String): Color {
    return when (mood.lowercase()) {
        "happy" -> Color(0xFFF59E0B)       // Warm Amber Gold
        "stressed" -> Color(0xFF10B981)    // Soothing Emerald Teal
        "tired" -> Color(0xFF6366F1)       // Twilight Indigo
        "productive" -> Color(0xFF3B82F6)  // Neon Cobalt Blue
        "bored" -> Color(0xFF8B5CF6)       // Playful Deep Purple
        else -> Color(0xFF6B7280)          // Charcoal Gray
    }
}

fun scheduleGoogleCalendarEvent(context: Context, title: String, description: String, duration: String) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "MoodFit Routine: $title")
            putExtra(CalendarContract.Events.DESCRIPTION, "$description\n\nDuration recommendation: $duration\nUplifted with MoodFit.")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Home Wellness Sanctuary")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis() + 10 * 60 * 1000) // starts in 10 mins
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis() + 40 * 60 * 1000)   // 30 min event
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open system Calendar organizer.", Toast.LENGTH_SHORT).show()
    }
}

fun shareActivityIntent(context: Context, title: String, description: String, duration: String) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Hey! I am check-in on MoodFit and going to practice this activity to build state:\n\n✨ *$title* ✨\n$description\n⏱ *Vibe duration:* $duration\n\nInvite you to secure wellness breaks. Empowered by MoodFit! ♥"
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Routine with Contacts")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Sharing unavailable.", Toast.LENGTH_SHORT).show()
    }
}

fun sendLocalNotification(context: Context, title: String, message: String) {
    val channelId = "moodfit_reminders_channel"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "MoodFit Wellness Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Fires timely break notifications to prevent strain"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    notificationManager.notify(404, builder.build())
}

@Composable
fun MoodHistoryChart(logs: List<MoodLog>, modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        if (logs.isEmpty()) {
            drawLine(
                color = color.copy(alpha = 0.2f),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 4f
            )
            return@Canvas
        }

        val drawnLogs = logs.take(10).reversed()
        val maxVal = 10f
        val xSpacing = if (drawnLogs.size > 1) width / (drawnLogs.size - 1) else width

        val path = Path()
        drawnLogs.forEachIndexed { index, item ->
            // Map energy 1..10 to vertical bounds
            val yVal = height - (item.energyLevel / maxVal) * (height - 40f) - 20f
            val xVal = index * xSpacing

            if (index == 0) {
                path.moveTo(xVal, yVal)
            } else {
                path.lineTo(xVal, yVal)
            }

            drawCircle(
                color = color,
                radius = 7f,
                center = Offset(xVal, yVal)
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ModernLoadingIndicator(modifier: Modifier = Modifier, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Canvas(modifier = modifier.size(60.dp)) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * scale
        
        rotate(angle, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(color.copy(alpha = 0.1f), color),
                    center = center
                ),
                radius = radius,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SplashScreenContent(isDark: Boolean, onSplashComplete: () -> Unit) {
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF14122A), Color(0xFF13101E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE9E5FF), Color(0xFFEADDFF)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                val splashProgress = remember { androidx.compose.animation.core.Animatable(0f) }
                LaunchedEffect(Unit) {
                    splashProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                    )
                    onSplashComplete()
                }

                val infiniteTransition = rememberInfiniteTransition(label = "wave")
                val phase by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = (2 * Math.PI).toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "phase"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val padding = 40f
                    val boxW = w - 2 * padding
                    val boxH = h - 2 * padding
                    
                    val path = Path()
                    val drawProgress = splashProgress.value
                    
                    if (drawProgress > 0f) {
                        val pointsCount = (120 * drawProgress).toInt().coerceAtLeast(2)
                        val amplitude = 12f
                        val frequency = 4f
                        
                        for (i in 0..pointsCount) {
                            val localT = (i.toFloat() / pointsCount) * drawProgress
                            val tWrapped = localT % 1.0f
                            
                            var x0 = 0f
                            var y0 = 0f
                            
                            if (tWrapped <= 0.25f) {
                                val segmentT = tWrapped / 0.25f
                                x0 = padding + segmentT * boxW
                                y0 = padding
                            } else if (tWrapped <= 0.5f) {
                                val segmentT = (tWrapped - 0.25f) / 0.25f
                                x0 = padding + boxW
                                y0 = padding + segmentT * boxH
                            } else if (tWrapped <= 0.75f) {
                                val segmentT = (tWrapped - 0.5f) / 0.25f
                                x0 = padding + boxW - segmentT * boxW
                                y0 = padding + boxH
                            } else {
                                val segmentT = (tWrapped - 0.75f) / 0.25f
                                x0 = padding
                                y0 = padding + boxH - segmentT * boxH
                            }
                            
                            val cx = w / 2f
                            val cy = h / 2f
                            val vx = x0 - cx
                            val vy = y0 - cy
                            val len = kotlin.math.sqrt(vx * vx + vy * vy)
                            val nx = if (len > 0f) vx / len else 0f
                            val ny = if (len > 0f) vy / len else 0f
                            
                            val displacement = amplitude * kotlin.math.sin(localT * 2 * Math.PI * frequency - phase).toFloat()
                            
                            val x = x0 + nx * displacement
                            val y = y0 + ny * displacement
                            
                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        if (drawProgress >= 1f) {
                            path.close()
                        }
                        
                        drawPath(
                            path = path,
                            brush = Brush.sweepGradient(
                                colors = listOf(Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFFF59E0B)),
                                center = Offset(w / 2, h / 2)
                            ),
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A182E))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MoodFit",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF1D1B20)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "WELLNESS & MINDFULNESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = if (isDark) Color(0xFFCCC2DC) else Color(0xFF49454F)
            )
        }
    }
}

@Composable
fun OnboardingScreenContent(
    viewModel: MoodFitViewModel,
    isDark: Boolean,
    onNameEntered: (String) -> Unit
) {
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF14122A), Color(0xFF13101E)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE9E5FF), Color(0xFFEADDFF)))
    }

    var inputName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E1B30).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A182E))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.t("Welcome to MoodFit"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1D1B20),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = viewModel.t("Enter your name to personalize your experience:"),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text(viewModel.t("Your Name")) },
                    placeholder = { Text(viewModel.t("e.g. Ferdinand")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onNameEntered(inputName.trim())
                        }
                    },
                    enabled = inputName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_submit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                        contentColor = if (isDark) Color(0xFF1D1B20) else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = viewModel.t("Enter"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        onNameEntered("MoodFit")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_guest"),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
                    )
                ) {
                    Text(
                        text = viewModel.t("Enter as Guest"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

fun formatDuration(duration: String, lang: String): String {
    val digits = duration.filter { it.isDigit() }
    if (digits.isEmpty()) return duration
    return when (lang.lowercase()) {
        "indonesian", "indonesia" -> {
            if (duration.lowercase().contains("hour")) "$digits jam" else "$digits mnt"
        }
        "spanish" -> {
            if (duration.lowercase().contains("hour")) "$digits hr" else "$digits min"
        }
        "french" -> {
            if (duration.lowercase().contains("hour")) "$digits h" else "$digits min"
        }
        "german" -> {
            if (duration.lowercase().contains("hour")) "$digits Std." else "$digits Min."
        }
        else -> {
            if (duration.lowercase().contains("hour")) {
                if (digits == "1") "$digits hour" else "$digits hours"
            } else "$digits mins"
        }
    }
}

@Composable
fun MotivationalQuoteCard(
    quote: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFF1F5F9).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "“",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = quote,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

fun saveCroppedImage(
    context: Context,
    uri: android.net.Uri,
    scale: Float,
    offset: Offset,
    viewportSizeDp: Float
): android.net.Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val srcBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (srcBitmap == null) return null

        val density = context.resources.displayMetrics.density
        val viewportSizePx = viewportSizeDp * density

        val srcW = srcBitmap.width.toFloat()
        val srcH = srcBitmap.height.toFloat()

        val targetSize = 400
        val destBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(destBitmap)

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

        val viewToTargetScale = targetSize / viewportSizePx

        val fitScale = Math.max(viewportSizePx / srcW, viewportSizePx / srcH)
        val baseWidth = srcW * fitScale
        val baseHeight = srcH * fitScale

        val baseX = (viewportSizePx - baseWidth) / 2f
        val baseY = (viewportSizePx - baseHeight) / 2f

        val centerX = viewportSizePx / 2f
        val centerY = viewportSizePx / 2f

        val matrix = android.graphics.Matrix()
        matrix.postTranslate(baseX, baseY)
        matrix.postScale(scale, scale, centerX, centerY)
        matrix.postTranslate(offset.x, offset.y)
        matrix.postScale(viewToTargetScale, viewToTargetScale)

        canvas.drawBitmap(srcBitmap, matrix, paint)

        val file = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.png")
        val out = FileOutputStream(file)
        destBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()

        android.net.Uri.fromFile(file)
    } catch (e: Exception) {
        android.util.Log.e("CropImage", "Error cropping image", e)
        null
    }
}

@Composable
fun RowScope.NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.5f)
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

suspend fun fetchOnlineNews(): List<com.example.model.NewsArticle> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val client = okhttp3.OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url("https://saurav.tech/NewsAPI/top-headlines/category/health/us.json")
        .build()
    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            val jsonObject = org.json.JSONObject(bodyString)
            val jsonArray = jsonObject.getJSONArray("articles")
            val list = mutableListOf<com.example.model.NewsArticle>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    com.example.model.NewsArticle(
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        publishedAt = obj.optString("publishedAt", ""),
                        url = obj.optString("url", ""),
                        urlToImage = obj.optString("urlToImage", "")
                    )
                )
            }
            list.sortByDescending { it.publishedAt }
            list
        }
    } catch (e: Exception) {
        android.util.Log.e("NewsFetch", "Failed to fetch online news: ${e.message}")
        emptyList()
    }
}

@Composable
fun NewsTabContent(viewModel: MoodFitViewModel, isDark: Boolean) {
    val context = LocalContext.current
    val language = viewModel.selectedLanguageState.value
    var articlesState by remember { mutableStateOf<List<com.example.model.NewsArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }

    LaunchedEffect(language) {
        isLoading = true
        val fetched = fetchOnlineNews()
        if (fetched.isNotEmpty()) {
            isOffline = false
            val translated = if (language.lowercase() != "english") {
                viewModel.translateNews(fetched, language)
            } else {
                fetched
            }
            articlesState = translated
        } else {
            isOffline = true
            articlesState = com.example.model.MockArticles.getFallbackArticles(language)
        }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news_lazy_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = viewModel.t("Health News"),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E1B4B)
            )
            if (isOffline) {
                Text(
                    text = viewModel.t("No internet connection. Showing offline articles."),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = viewModel.t("Real-time health and wellness headlines."),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
                    )
                }
            }
        } else {
            items(articlesState) { article ->
                NewsArticleCard(article, isDark)
            }
        }
    }
}

@Composable
fun NewsArticleCard(article: com.example.model.NewsArticle, isDark: Boolean) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E1B30).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.82f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFCAC4D0).copy(alpha = 0.5f)
        )
    ) {
        Column {
            if (article.urlToImage.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = article.urlToImage,
                    contentDescription = article.title,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = article.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White else Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = article.description,
                    fontSize = 11.sp,
                    color = if (isDark) Color.LightGray else Color(0xFF49454F),
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                val formattedDate = article.publishedAt.take(10)
                Text(
                    text = formattedDate,
                    fontSize = 9.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
